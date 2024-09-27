package fi.vm.sade.organisaatio.export;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.DownloadFileRequest;
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Service
public class ExportService {
    private static final String V2_PREFIX = "fulldump/organisaatio/v2";
    private static final String V3_PREFIX = "fulldump/organisaatio/v3";
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT);

    @Value("${organisaatio.tasks.export.bucket-name}")
    private String bucketName;
    @Value("${organisaatio.tasks.export.lampi-bucket-name}")
    private String lampiBucketName;
    @Value("${organisaatio.tasks.export.upload-to-s3:true}")
    private boolean uploadToS3;

    private final S3AsyncClient opintopolkuS3Client;
    private final S3AsyncClient lampiS3Client;
    private final JdbcTemplate jdbcTemplate;
    private final String CREATE_EXPORT_ORGANISAATIO_SQL = """
            CREATE TABLE exportnew.organisaatio AS
            SELECT
              o.oid as organisaatio_oid,
              o.oppilaitostyyppi,
              o.oppilaitoskoodi as oppilaitosnumero,
              o.kotipaikka,
              o.yritysmuoto,
              o.ytunnus as y_tunnus,
              o.alkupvm,
              o.lakkautuspvm,
              o.tuontipvm,
              o.paivityspvm,
              (SELECT v.value
               FROM monikielinenteksti_values v
               WHERE v.id = o.nimi_mkt
               AND v.key = 'fi') as nimi_fi,
              (SELECT v.value
               FROM monikielinenteksti_values v
               WHERE v.id = o.nimi_mkt
               AND v.key = 'sv') as nimi_sv,
              (SELECT string_agg(tyypit, ',')
               FROM organisaatio_tyypit
               WHERE organisaatio_id = o.id) as organisaatiotyypit,
              (SELECT string_agg(kielet, ',')
               FROM organisaatio_kielet
               WHERE organisaatio_id = o.id) as opetuskielet,
              (SELECT parent_oid
               FROM organisaatio_parent_oids
               WHERE organisaatio_id = o.id
               AND parent_position = 1) AS grandparent_oid,
              (SELECT parent_oid
               FROM organisaatio_parent_oids
               WHERE organisaatio_id = o.id
               AND parent_position = 0) AS parent_oid,
              CASE
                WHEN o.organisaatiopoistettu = true THEN 'POISTETTU'
                WHEN current_date < o.alkupvm THEN 'SUUNNITELTU'
                WHEN current_date BETWEEN o.alkupvm AND COALESCE(o.lakkautuspvm, current_date) THEN 'AKTIIVINEN'
                ELSE 'LAKKAUTETTU'
              END AS tila
              FROM organisaatio AS o
              WHERE NOT EXISTS(
                SELECT 1
                FROM organisaatio_tyypit
                WHERE organisaatio_id = o.id
                AND tyypit = 'Ryhma'
              )
            """;

    private final String CREATE_EXPORT_OSOITE_SQL = """
              CREATE TABLE exportnew.osoite AS
              SELECT o.oid AS organisaatio_oid,
                     y.osoitetyyppi,
                     y.osoite,
                     y.postinumero,
                     y.postitoimipaikka,
                     y.kieli
              FROM yhteystieto y
              JOIN organisaatio o ON o.id = y.organisaatio_id
              WHERE y.osoitetyyppi in ('posti', 'kaynti')
            """;

    private final String CREATE_EXPORT_ORGANISAATIOSUHDE_SQL = """
            CREATE TABLE exportnew.organisaatiosuhde AS
            SELECT organisaatiosuhde.suhdetyyppi, parent.oid AS parent_oid, child.oid AS child_oid,
                   organisaatiosuhde.alkupvm, organisaatiosuhde.loppupvm
            FROM organisaatiosuhde
            JOIN organisaatio parent ON parent.id = parent_id
            JOIN organisaatio child ON child.id = child_id
            WHERE NOT EXISTS (SELECT * FROM organisaatio_tyypit WHERE organisaatio_id = parent.id AND tyypit = 'Ryhma')
            AND NOT EXISTS (SELECT * FROM organisaatio_tyypit WHERE organisaatio_id = child.id AND tyypit = 'Ryhma')
            """;

    private final String CREATE_EXPORT_RYHMA_SQL = """
            CREATE TABLE exportnew.ryhma AS
            SELECT oid AS ryhma_oid,
            (SELECT v.value FROM monikielinenteksti_values v WHERE v.id = r.nimi_mkt AND v.key = 'fi') nimi_fi,
            (SELECT v.value FROM monikielinenteksti_values v WHERE v.id = r.nimi_mkt AND v.key = 'sv') nimi_sv,
            (SELECT v.value FROM monikielinenteksti_values v WHERE v.id = r.nimi_mkt AND v.key = 'en') nimi_en
            FROM organisaatio r
            WHERE EXISTS (SELECT * FROM organisaatio_tyypit WHERE organisaatio_id = r.id AND tyypit = 'Ryhma')
            AND NOT r.organisaatiopoistettu
            AND (r.lakkautuspvm IS NULL OR r.lakkautuspvm > current_date)
            """;

    @Transactional
    public void createSchema() {
        jdbcTemplate.execute("DROP SCHEMA IF EXISTS exportnew CASCADE");
        jdbcTemplate.execute("CREATE SCHEMA exportnew");
        jdbcTemplate.execute(CREATE_EXPORT_ORGANISAATIO_SQL);
        jdbcTemplate.execute(CREATE_EXPORT_ORGANISAATIOSUHDE_SQL);
        jdbcTemplate.execute(CREATE_EXPORT_OSOITE_SQL);
        jdbcTemplate.execute("ALTER TABLE exportnew.organisaatio ADD CONSTRAINT organisaatio_pk PRIMARY KEY (organisaatio_oid)");
        jdbcTemplate.execute("ALTER TABLE exportnew.osoite ADD CONSTRAINT organisaatio_fk FOREIGN KEY (organisaatio_oid) REFERENCES exportnew.organisaatio (organisaatio_oid)");
        jdbcTemplate.execute("ALTER TABLE exportnew.organisaatiosuhde ADD CONSTRAINT parent_fk FOREIGN KEY (parent_oid) REFERENCES exportnew.organisaatio (organisaatio_oid)");
        jdbcTemplate.execute("ALTER TABLE exportnew.organisaatiosuhde ADD CONSTRAINT child_fk FOREIGN KEY (child_oid) REFERENCES exportnew.organisaatio (organisaatio_oid)");
        jdbcTemplate.execute(CREATE_EXPORT_RYHMA_SQL);
        jdbcTemplate.execute("ALTER TABLE exportnew.ryhma ADD CONSTRAINT ryhma_pk PRIMARY KEY (ryhma_oid)");
        jdbcTemplate.execute("ALTER TABLE exportnew.ryhma ALTER COLUMN nimi_fi SET NOT NULL");
        jdbcTemplate.execute("DROP SCHEMA IF EXISTS export CASCADE");
        jdbcTemplate.execute("ALTER SCHEMA exportnew RENAME TO export");
    }

    public void generateExportFiles() throws IOException {
        generateCsvExportsV2();
        generateJsonExportsV2();
        generateCsvExportsV3();
        generateJsonExportsV3();
    }

    private static final String ORGANISAATIO_QUERY = "SELECT organisaatio_oid, organisaatiotyypit, oppilaitosnumero, kotipaikka, yritysmuoto, y_tunnus, alkupvm, lakkautuspvm, tuontipvm, paivityspvm, nimi_fi, nimi_sv, oppilaitostyyppi, opetuskielet, grandparent_oid, parent_oid, tila FROM export.organisaatio";
    private static final String OSOITE_QUERY = "SELECT organisaatio_oid, osoitetyyppi, osoite, postinumero, postitoimipaikka, kieli FROM export.osoite";
    private static final String ORGANISAATIOSUHDE_QUERY = "SELECT suhdetyyppi, parent_oid, child_oid, alkupvm, loppupvm FROM export.organisaatiosuhde";
    private static final String RYHMA_QUERY = "SELECT ryhma_oid, nimi_fi, nimi_sv, nimi_en FROM export.ryhma";

    public void generateCsvExportsV2() {
        exportQueryToS3(V2_PREFIX + "/csv/organisaatio.csv", ORGANISAATIO_QUERY);
        exportQueryToS3(V2_PREFIX + "/csv/osoite.csv", OSOITE_QUERY);
        exportQueryToS3(V2_PREFIX + "/csv/organisaatiosuhde.csv", ORGANISAATIOSUHDE_QUERY);
    }

    public void generateCsvExportsV3() {
        exportQueryToS3(V3_PREFIX + "/csv/organisaatio.csv", ORGANISAATIO_QUERY);
        exportQueryToS3(V3_PREFIX + "/csv/osoite.csv", OSOITE_QUERY);
        exportQueryToS3(V3_PREFIX + "/csv/organisaatiosuhde.csv", ORGANISAATIOSUHDE_QUERY);
        exportQueryToS3(V3_PREFIX + "/csv/ryhma.csv", RYHMA_QUERY);
    }

    private void exportQueryToS3(String objectKey, String query) {
        log.info("Exporting table to S3: {}/{}", bucketName, objectKey);
        var sql = "SELECT rows_uploaded FROM aws_s3.query_export_to_s3(?, aws_commons.create_s3_uri(?, ?, ?), options := 'FORMAT CSV, HEADER TRUE')";
        var rowsUploaded = jdbcTemplate.queryForObject(sql, Long.class, query, bucketName, objectKey, OpintopolkuAwsClients.REGION.id());
        log.info("Exported {} rows to S3 object {}", rowsUploaded, objectKey);
    }

    List<File> generateJsonExportsV2() throws IOException {
        var organisaatioFile = exportQueryToS3AsJson(ORGANISAATIO_QUERY, V2_PREFIX + "/json/organisaatio.json", unchecked(rs ->
                new ExportedOrganisaatio(
                        rs.getString("organisaatio_oid"),
                        rs.getString("organisaatiotyypit"),
                        rs.getString("oppilaitosnumero"),
                        rs.getString("kotipaikka"),
                        rs.getString("yritysmuoto"),
                        rs.getString("y_tunnus"),
                        rs.getString("alkupvm"),
                        rs.getString("lakkautuspvm"),
                        rs.getString("tuontipvm"),
                        rs.getString("paivityspvm"),
                        rs.getString("nimi_fi"),
                        rs.getString("nimi_sv"),
                        rs.getString("oppilaitostyyppi"),
                        rs.getString("opetuskielet"),
                        rs.getString("grandparent_oid"),
                        rs.getString("parent_oid"),
                        rs.getString("tila")
                )
        ));
        var osoiteFile = exportQueryToS3AsJson(OSOITE_QUERY, V2_PREFIX + "/json/osoite.json", unchecked(rs ->
                new ExportedOsoite(
                        rs.getString("organisaatio_oid"),
                        rs.getString("osoitetyyppi"),
                        rs.getString("osoite"),
                        rs.getString("postinumero"),
                        rs.getString("postitoimipaikka"),
                        rs.getString("kieli")
                )
        ));
        var organisaatioSuhdeFile = exportQueryToS3AsJson(ORGANISAATIOSUHDE_QUERY, V2_PREFIX + "/json/organisaatiosuhde.json", unchecked(rs ->
                new ExportedOrganisaatioSuhde(
                        rs.getString("suhdetyyppi"),
                        rs.getString("parent_oid"),
                        rs.getString("child_oid"),
                        rs.getString("alkupvm"),
                        rs.getString("loppupvm")
                )
        ));
        return List.of(organisaatioFile, osoiteFile, organisaatioSuhdeFile);
    }

    List<File> generateJsonExportsV3() throws IOException {
        var organisaatioFile = exportQueryToS3AsJson(ORGANISAATIO_QUERY, V3_PREFIX + "/json/organisaatio.json", unchecked(rs ->
                new ExportedOrganisaatio(
                        rs.getString("organisaatio_oid"),
                        rs.getString("organisaatiotyypit"),
                        rs.getString("oppilaitosnumero"),
                        rs.getString("kotipaikka"),
                        rs.getString("yritysmuoto"),
                        rs.getString("y_tunnus"),
                        rs.getString("alkupvm"),
                        rs.getString("lakkautuspvm"),
                        rs.getString("tuontipvm"),
                        rs.getString("paivityspvm"),
                        rs.getString("nimi_fi"),
                        rs.getString("nimi_sv"),
                        rs.getString("oppilaitostyyppi"),
                        rs.getString("opetuskielet"),
                        rs.getString("grandparent_oid"),
                        rs.getString("parent_oid"),
                        rs.getString("tila")
                )
        ));
        var osoiteFile = exportQueryToS3AsJson(OSOITE_QUERY, V3_PREFIX + "/json/osoite.json", unchecked(rs ->
                new ExportedOsoite(
                        rs.getString("organisaatio_oid"),
                        rs.getString("osoitetyyppi"),
                        rs.getString("osoite"),
                        rs.getString("postinumero"),
                        rs.getString("postitoimipaikka"),
                        rs.getString("kieli")
                )
        ));
        var organisaatioSuhdeFile = exportQueryToS3AsJson(ORGANISAATIOSUHDE_QUERY, V3_PREFIX + "/json/organisaatiosuhde.json", unchecked(rs ->
                new ExportedOrganisaatioSuhde(
                        rs.getString("suhdetyyppi"),
                        rs.getString("parent_oid"),
                        rs.getString("child_oid"),
                        rs.getString("alkupvm"),
                        rs.getString("loppupvm")
                )
        ));
        var ryhmaFile = exportQueryToS3AsJson(RYHMA_QUERY, V3_PREFIX + "/json/ryhma.json", unchecked(rs ->
                new ExportedRyhma(
                        rs.getString("ryhma_oid"),
                        rs.getString("nimi_fi"),
                        Optional.ofNullable(rs.getString("nimi_sv")),
                        Optional.ofNullable(rs.getString("nimi_en"))
                )
        ));
        return List.of(organisaatioFile, osoiteFile, organisaatioSuhdeFile, ryhmaFile);
    }

    private <T> File exportQueryToS3AsJson(String query, String objectKey, Function<ResultSet, T> mapper) throws IOException {
        @SuppressWarnings("java:S5443")
        var tempFile = File.createTempFile("export", ".json");
        try {
            exportToFile(query, mapper, tempFile);
            uploadFile(opintopolkuS3Client, bucketName, objectKey, tempFile);
        } finally {
            if (uploadToS3) {
                Files.deleteIfExists(tempFile.toPath());
            } else {
                log.info("Not uploading file to S3, keeping it at {}", tempFile.getAbsolutePath());
            }
        }
        return tempFile;
    }

    private <T> void exportToFile(String query, Function<ResultSet, T> mapper, File file) throws IOException {
        log.info("Writing JSON export to {}", file.getAbsolutePath());
        try (var writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write("[\n");
            var firstElement = true;
            try (Stream<T> stream = jdbcTemplate.queryForStream(query, (rs, n) -> mapper.apply(rs))) {
                Iterable<T> iterable = stream::iterator;
                for (T jsonObject : iterable) {
                    if (firstElement) {
                        firstElement = false;
                    } else {
                        writer.write(",\n");
                    }
                    writer.write(objectMapper.writeValueAsString(jsonObject));
                }
            }
            writer.write("\n");
            writer.write("]\n");
        }
    }

    private <T, R, E extends Throwable> Function<T, R> unchecked(ThrowingFunction<T, R, E> f) {
        return t -> {
            try {
                return f.apply(t);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

    private interface ThrowingFunction<T, R, E extends Throwable> {
        R apply(T rs) throws E;
    }

    public void copyExportFilesToLampi() throws IOException {
        copyExportFilesV2();
        copyExportFilesV3();
    }

    private void copyExportFilesV2() throws IOException {
        log.info("Copying v2 export files to Lampi");
        var csvManifest = new ArrayList<ExportManifest.ExportFileDetails>();
        csvManifest.add(copyFileToLampi(V2_PREFIX + "/csv/organisaatio.csv"));
        csvManifest.add(copyFileToLampi(V2_PREFIX + "/csv/osoite.csv"));
        csvManifest.add(copyFileToLampi(V2_PREFIX + "/csv/organisaatiosuhde.csv"));
        writeManifest(V2_PREFIX + "/csv/manifest.json", new ExportManifest(csvManifest));

        var jsonManifest = new ArrayList<ExportManifest.ExportFileDetails>();
        jsonManifest.add(copyFileToLampi(V2_PREFIX + "/json/organisaatio.json"));
        jsonManifest.add(copyFileToLampi(V2_PREFIX + "/json/osoite.json"));
        jsonManifest.add(copyFileToLampi(V2_PREFIX + "/json/organisaatiosuhde.json"));
        writeManifest(V2_PREFIX + "/json/manifest.json", new ExportManifest(jsonManifest));
    }

    private void copyExportFilesV3() throws IOException {
        log.info("Copying v3 export files to Lampi");
        var csvManifest = new ArrayList<ExportManifest.ExportFileDetails>();
        csvManifest.add(copyFileToLampi(V3_PREFIX + "/csv/organisaatio.csv"));
        csvManifest.add(copyFileToLampi(V3_PREFIX + "/csv/osoite.csv"));
        csvManifest.add(copyFileToLampi(V3_PREFIX + "/csv/organisaatiosuhde.csv"));
        csvManifest.add(copyFileToLampi(V3_PREFIX + "/csv/ryhma.csv"));
        writeManifest(V3_PREFIX + "/csv/manifest.json", new ExportManifest(csvManifest));

        var jsonManifest = new ArrayList<ExportManifest.ExportFileDetails>();
        jsonManifest.add(copyFileToLampi(V3_PREFIX + "/json/organisaatio.json"));
        jsonManifest.add(copyFileToLampi(V3_PREFIX + "/json/osoite.json"));
        jsonManifest.add(copyFileToLampi(V3_PREFIX + "/json/organisaatiosuhde.json"));
        jsonManifest.add(copyFileToLampi(V3_PREFIX + "/json/ryhma.json"));
        writeManifest(V3_PREFIX + "/json/manifest.json", new ExportManifest(jsonManifest));
    }

    private ExportManifest.ExportFileDetails copyFileToLampi(String objectKey) throws IOException {
        @SuppressWarnings("java:S5443")
        var temporaryFile = File.createTempFile("export", ".csv");
        try {
            log.info("Downloading file from S3: {}/{}", bucketName, objectKey);
            try (var downloader = S3TransferManager.builder().s3Client(opintopolkuS3Client).build()) {
                var fileDownload = downloader.downloadFile(DownloadFileRequest.builder()
                        .getObjectRequest(b -> b.bucket(bucketName).key(objectKey))
                        .destination(temporaryFile)
                        .build());
                fileDownload.completionFuture().join();
            }

            var response = uploadFile(lampiS3Client, lampiBucketName, objectKey, temporaryFile);
            return new ExportManifest.ExportFileDetails(objectKey, response.versionId());
        } finally {
            Files.deleteIfExists(temporaryFile.toPath());
        }
    }

    private PutObjectResponse uploadFile(S3AsyncClient s3Client, String bucketName, String objectKey, File file) {
        if (!uploadToS3) {
            log.info("Skipping upload to S3");
            return null;
        }
        log.info("Uploading file to S3: {}/{}", bucketName, objectKey);
        try (var uploader = S3TransferManager.builder().s3Client(s3Client).build()) {
            var fileUpload = uploader.uploadFile(UploadFileRequest.builder()
                    .putObjectRequest(b -> b.bucket(bucketName).key(objectKey))
                    .source(file)
                    .build());
            var result = fileUpload.completionFuture().join();
            return result.response();
        }
    }

    private void writeManifest(String objectKey, ExportManifest manifest) throws JsonProcessingException {
        log.info("Writing manifest file {}/{}: {}", lampiBucketName, objectKey, manifest);
        var manifestJson = objectMapper.writeValueAsString(manifest);
        var response = lampiS3Client.putObject(
                b -> b.bucket(lampiBucketName).key(objectKey),
                AsyncRequestBody.fromString(manifestJson)
        ).join();
        log.info("Wrote manifest to S3: {}", response);
    }
}

record ExportedOrganisaatio(String organisaatio_oid,
                            String organisaatiotyypit,
                            String oppilaitosnumero,
                            String kotipaikka,
                            String yritysmuoto,
                            String y_tunnus,
                            String alkupvm,
                            String lakkautuspvm,
                            String tuontipvm,
                            String paivityspvm,
                            String nimi_fi,
                            String nimi_sv,
                            String oppilaitostyyppi,
                            String opetuskielet,
                            String grandparent_oid,
                            String parent_oid,
                            String tila) {
}

record ExportedOsoite(String organisaatio_oid,
                      String osoitetyyppi,
                      String osoite,
                      String postinumero,
                      String postitoimipaikka,
                      String kieli) {
}

record ExportedOrganisaatioSuhde(String suhdetyyppi, String parent_oid, String child_oid, String alkupvm, String loppupvm) {
}

record ExportedRyhma(String oid, String nimiFi, Optional<String> nimiSv, Optional<String> nimiEn) {
}

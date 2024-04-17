package fi.vm.sade.organisaatio.export;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.function.Function;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Service
public class ExportService {
    private static final String S3_PREFIX = "fulldump/organisaatio/v2";
    private final ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @Value("${organisaatio.tasks.export.bucket-name}")
    private String bucketName;
    @Value("${organisaatio.tasks.export.lampi-bucket-name}")
    private String lampiBucketName;
    @Value("${organisaatio.tasks.export.upload-to-s3:true}")
    private boolean uploadToS3;

    private final S3AsyncClient opintopolkuS3Client;
    private final S3AsyncClient lampiS3Client;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void createSchema() {
        jdbcTemplate.execute("DROP SCHEMA IF EXISTS exportnew CASCADE");
        jdbcTemplate.execute("CREATE SCHEMA exportnew");
        jdbcTemplate.execute("""
                CREATE TABLE exportnew.organisaatio AS
                SELECT
                     o.oid as organisaatio_oid,
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
                     o.oppilaitostyyppi,
                     o.oppilaitoskoodi as oppilaitosnumero,
                     o.kotipaikka,
                     o.ytunnus as y_tunnus,
                     o.tuontipvm,
                     o.paivityspvm
                FROM organisaatio AS o
                WHERE organisaatio_is_active(o)
                """);
        jdbcTemplate.execute("""
                CREATE TABLE exportnew.organisaatiosuhde AS
                SELECT suhdetyyppi,
                       (SELECT o.oid
                          FROM organisaatio o
                          WHERE o.id = parent_id) AS parent_oid,
                       (SELECT o.oid
                          FROM organisaatio o
                          WHERE id = child_id) AS child_oid
                       FROM organisaatiosuhde;
                """);
        jdbcTemplate.execute("DROP SCHEMA IF EXISTS export CASCADE");
        jdbcTemplate.execute("ALTER SCHEMA exportnew RENAME TO export");
    }

    public void generateExportFiles() throws IOException {
        generateCsvExports();
        generateJsonExports();
    }

    private static final String ORGANISAATIO_QUERY = "SELECT organisaatio_oid, nimi_fi, nimi_sv, organisaatiotyypit, oppilaitostyyppi, oppilaitosnumero, kotipaikka, y_tunnus, tuontipvm, paivityspvm FROM export.organisaatio";
    private static final String ORGANISAATIOSUHDE_QUERY = "SELECT suhdetyyppi, parent_oid, child_oid FROM export.organisaatiosuhde";

    public void generateCsvExports() {
        exportQueryToS3(S3_PREFIX + "/csv/organisaatio.csv", ORGANISAATIO_QUERY);
        exportQueryToS3(S3_PREFIX + "/csv/organisaatiosuhde.csv", ORGANISAATIOSUHDE_QUERY);
    }

    private void exportQueryToS3(String objectKey, String query) {
        log.info("Exporting table to S3: {}/{}", bucketName, objectKey);
        var sql = "SELECT rows_uploaded FROM aws_s3.query_export_to_s3(?, aws_commons.create_s3_uri(?, ?, ?), options := 'FORMAT CSV, HEADER TRUE')";
        var rowsUploaded = jdbcTemplate.queryForObject(sql, Long.class, query, bucketName, objectKey, OpintopolkuAwsClients.REGION.id());
        log.info("Exported {} rows to S3 object {}", rowsUploaded, objectKey);
    }

    List<File> generateJsonExports() throws IOException {
        var organisaatioFile = exportQueryToS3AsJson(ORGANISAATIO_QUERY, S3_PREFIX + "/json/organisaatio.json", unchecked(rs ->
                new ExportedOrganisaatio(
                        rs.getString("organisaatio_oid"),
                        rs.getString("nimi_fi"),
                        rs.getString("nimi_sv"),
                        rs.getString("organisaatiotyypit"),
                        rs.getString("oppilaitostyyppi"),
                        rs.getString("oppilaitosnumero"),
                        rs.getString("kotipaikka"),
                        rs.getString("y_tunnus"),
                        rs.getString("tuontipvm"),
                        rs.getString("paivityspvm")
                )
        ));
        var organisaatioSuhdeFile = exportQueryToS3AsJson(ORGANISAATIOSUHDE_QUERY, S3_PREFIX + "/json/organisaatiosuhteet.json", unchecked(rs ->
                new ExportedOrganisaatioSuhde(
                        rs.getString("suhdetyyppi"),
                        rs.getString("parent_oid"),
                        rs.getString("child_oid")
                )
        ));
        return List.of(organisaatioFile, organisaatioSuhdeFile);
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
        var csvManifest = new ArrayList<ExportManifest.ExportFileDetails>();
        csvManifest.add(copyFileToLampi(S3_PREFIX + "/csv/organisaatio.csv"));
        csvManifest.add(copyFileToLampi(S3_PREFIX + "/csv/organisaatiosuhde.csv"));
        writeManifest(S3_PREFIX + "/csv/manifest.json", new ExportManifest(csvManifest));
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
        var manifestJson = objectMapper.writeValueAsString(manifest);
        var response = lampiS3Client.putObject(
                b -> b.bucket(lampiBucketName).key(objectKey),
                AsyncRequestBody.fromString(manifestJson)
        ).join();
        log.info("Wrote manifest to S3: {}", response);
    }
}

record ExportedOrganisaatio(String organisaatio_oid, String nimi_fi, String nimi_sv, String organisaatiotyypit,
                            String oppilaitostyyppi, String oppilaitosnumero, String kotipaikka, String y_tunnus,
                            String tuontipvm, String paivityspvm) {
}

record ExportedOrganisaatioSuhde(String suhdetyyppi, String parent_oid, String child_oid) {
}
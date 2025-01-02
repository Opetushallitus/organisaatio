package fi.vm.sade.organisaatio.datantuonti;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import fi.vm.sade.organisaatio.export.ExportManifest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;

import java.util.Date;
import java.util.Map;

@Slf4j
@Service(value = "DatantuontiExportService")
public class ExportService {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private S3AsyncClient opintopolkuS3Client;
    @Value("${organisaatio.tasks.datantuonti.export.bucket-name}")
    private String bucketName;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
    private final String V1_PREFIX = "datantuonti/organisaatio/v1";
    private final String CREATE_ORGANISAATIO_SQL = """
        CREATE TABLE datantuonti_export_new.organisaatio AS
          SELECT
            o.oid,
            (SELECT parent_oid
             FROM organisaatio_parent_oids
             WHERE organisaatio_id = o.id
             AND parent_position = 0) AS parent_oid,
            o.oppilaitostyyppi,
            o.ytunnus,
            o.piilotettu,
            (SELECT v.value
             FROM monikielinenteksti_values v
             WHERE v.id = o.nimi_mkt
             AND v.key = 'fi') as nimi_fi,
            (SELECT v.value
             FROM monikielinenteksti_values v
             WHERE v.id = o.nimi_mkt
             AND v.key = 'sv') as nimi_sv,
            (SELECT v.value
             FROM monikielinenteksti_values v
             WHERE v.id = o.nimi_mkt
             AND v.key = 'en') as nimi_en,
            o.alkupvm,
            o.lakkautuspvm,
            o.yritysmuoto,
            o.kotipaikka,
            o.maa,
            (SELECT string_agg(kielet, ',')
             FROM organisaatio_kielet
             WHERE organisaatio_id = o.id) as kielet
          FROM organisaatio o
          WHERE NOT EXISTS(
            SELECT 1
            FROM organisaatio_tyypit
            WHERE organisaatio_id = o.id
            AND (
              tyypit = 'organisaatiotyyppi_08'
              OR tyypit = 'organisaatiotyyppi_07'
            )
          );
    """;
    private final String ORGANISAATIO_QUERY = """
        SELECT
          oid,
          parent_oid,
          oppilaitostyyppi,
          ytunnus,
          piilotettu,
          nimi_fi,
          nimi_sv,
          nimi_en,
          alkupvm,
          lakkautuspvm,
          yritysmuoto,
          kotipaikka,
          maa,
          kielet
        FROM datantuonti_export.organisaatio
    """;
    private final String OSOITE_QUERY = """
        SELECT
          oid,
          osoitetyyppi,
          osoite,
          postinumero,
          postitoimipaikka,
          kieli
        FROM datantuonti_export.osoite
    """;
    private final String CREATE_EXPORT_OSOITE_SQL = """
        CREATE TABLE datantuonti_export_new.osoite AS
          SELECT o.oid,
                 y.osoitetyyppi,
                 y.osoite,
                 y.postinumero,
                 y.postitoimipaikka,
                 y.kieli
          FROM yhteystieto y
          JOIN organisaatio o ON o.id = y.organisaatio_id
          WHERE y.osoitetyyppi in ('posti', 'kaynti')
          AND NOT EXISTS(
            SELECT 1
            FROM organisaatio_tyypit
            WHERE organisaatio_id = o.id
            AND (
              tyypit = 'organisaatiotyyppi_08'
              OR tyypit = 'organisaatiotyyppi_07'
            )
          )
    """;

    @Transactional
    public void createSchema() {
        jdbcTemplate.execute("DROP SCHEMA IF EXISTS datantuonti_export_new CASCADE");
        jdbcTemplate.execute("CREATE SCHEMA datantuonti_export_new");
        jdbcTemplate.execute(CREATE_ORGANISAATIO_SQL);
        jdbcTemplate.execute(CREATE_EXPORT_OSOITE_SQL);
        jdbcTemplate.execute("DROP SCHEMA IF EXISTS datantuonti_export CASCADE");
        jdbcTemplate.execute("ALTER SCHEMA datantuonti_export_new RENAME TO datantuonti_export");
    }

    public void generateExportFiles() throws JsonProcessingException {
        var timestamp = new Date().getTime();
        Map<String, String> objectKeysAndQueries = Map.of(
                V1_PREFIX + "/csv/organisaatio-" + timestamp + ".csv", ORGANISAATIO_QUERY,
                V1_PREFIX + "/csv/osoite-" + timestamp + ".csv", OSOITE_QUERY
        );
        var keySet = objectKeysAndQueries.keySet();
        keySet.forEach(key -> exportQueryToS3(key, objectKeysAndQueries.get(key)));
        var manifestEntries = keySet.stream().parallel().map(this::toManifestEntry).toList();
        writeManifest(V1_PREFIX + "/csv/manifest.json", new ExportManifest(manifestEntries));
    }

    private ExportManifest.ExportFileDetails toManifestEntry(String key) {
        return new ExportManifest.ExportFileDetails(key, null);
    }

    private void exportQueryToS3(String objectKey, String query) {
        log.info("Exporting table to S3: {}/{}", bucketName, objectKey);
        var sql = "SELECT rows_uploaded FROM aws_s3.query_export_to_s3(?, aws_commons.create_s3_uri(?, ?, ?), options := 'FORMAT CSV, HEADER TRUE')";
        var rowsUploaded = jdbcTemplate.queryForObject(sql, Long.class, query, bucketName, objectKey, Region.EU_WEST_1.id());
        log.info("Exported {} rows to S3 object {}", rowsUploaded, objectKey);
    }

    private void writeManifest(String objectKey, ExportManifest manifest) throws JsonProcessingException {
        log.info("Writing manifest file {}/{}: {}", bucketName, objectKey, manifest);
        var manifestJson = objectMapper.writeValueAsString(manifest);
        var response = opintopolkuS3Client.putObject(
                b -> b.bucket(bucketName).key(objectKey),
                AsyncRequestBody.fromString(manifestJson)
        ).join();
        log.info("Wrote manifest to S3: {}", response);
    }
}

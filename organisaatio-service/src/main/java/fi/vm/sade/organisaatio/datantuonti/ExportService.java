package fi.vm.sade.organisaatio.datantuonti;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.regions.Region;
import java.util.Map;

@Slf4j
@Service(value = "DatantuontiExportService")
public class ExportService {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Value("${organisaatio.tasks.datantuonti.export.bucket-name}")
    private String bucketName;
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

    public void generateExportFiles() {
        Map<String, String> objectKeysAndQueries = Map.of(
                V1_PREFIX + "/csv/organisaatio.csv", ORGANISAATIO_QUERY,
                V1_PREFIX + "/csv/osoite.csv", OSOITE_QUERY
        );
        objectKeysAndQueries.keySet().forEach(key -> exportQueryToS3(key, objectKeysAndQueries.get(key)));
    }

    private void exportQueryToS3(String objectKey, String query) {
        log.info("Exporting table to S3: {}/{}", bucketName, objectKey);
        var sql = "SELECT rows_uploaded FROM aws_s3.query_export_to_s3(?, aws_commons.create_s3_uri(?, ?, ?), options := 'FORMAT CSV, HEADER TRUE')";
        var rowsUploaded = jdbcTemplate.queryForObject(sql, Long.class, query, bucketName, objectKey, Region.EU_WEST_1.id());
        log.info("Exported {} rows to S3 object {}", rowsUploaded, objectKey);
    }
}

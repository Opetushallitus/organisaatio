package fi.vm.sade.organisaatio.export;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ExportService {
    private static final String S3_PREFIX = "fulldump/organisaatio/v2";

    @Value("${organisaatio.tasks.export.bucket-name}")
    private String bucketName;

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
}

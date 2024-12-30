package fi.vm.sade.organisaatio.datantuonti;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service(value = "DatantuontiExportService")
public class ExportService {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private final String CREATE_ORGANISAATIO_SQL = """
        CREATE TABLE datantuonti_export_new.organisaatio AS
          SELECT o.oid as organisaatio_oid
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

    @Transactional
    public void createSchema() {
        jdbcTemplate.execute("DROP SCHEMA IF EXISTS datantuonti_export_new CASCADE");
        jdbcTemplate.execute("CREATE SCHEMA datantuonti_export_new");
        jdbcTemplate.execute(CREATE_ORGANISAATIO_SQL);
        jdbcTemplate.execute("DROP SCHEMA IF EXISTS datantuonti_export CASCADE");
        jdbcTemplate.execute("ALTER SCHEMA datantuonti_export_new RENAME TO datantuonti_export");
    }
}

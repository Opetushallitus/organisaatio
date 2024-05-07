package fi.vm.sade.organisaatio.export;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@Sql({"/data/truncate_tables.sql"})
@Sql({"/data/basic_organisaatio_data.sql"})
@SpringBootTest(properties = {
        "organisaatio.tasks.export.upload-to-s3=false",
})
class ExportServiceTest {
    @Autowired private ExportService exportService;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void exportSchemaIsCreated() {
        exportService.createSchema();

        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM export.organisaatio", Long.class)).isEqualTo(13L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM export.osoite", Long.class)).isEqualTo(24L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM export.organisaatiosuhde", Long.class)).isEqualTo(11L);
    }
}
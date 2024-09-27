package fi.vm.sade.organisaatio.export;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "organisaatio.tasks.export.upload-to-s3=false",
})
class ExportServiceTest {
    @Autowired private ExportService exportService;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    @Sql({"/data/truncate_tables.sql"})
    @Sql({"/data/basic_organisaatio_data.sql"})
    void organisaatioExport() {
        exportService.createSchema();

        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM export.organisaatio", Long.class)).isEqualTo(13L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM export.osoite", Long.class)).isEqualTo(21L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM export.organisaatiosuhde", Long.class)).isEqualTo(11L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM export.ryhma", Long.class)).isEqualTo(0L);

        assertThat(getParentOid("1.2.2004.2")).isEqualTo("1.2.2004.1");
        assertThat(getParentOid("1.2.2004.1")).isEqualTo("1.2.246.562.24.00000000001");
        assertThat(getParentOid("1.2.246.562.24.00000000001")).isNull();
        assertThat(getEmail("1.2.2004.1")).isEqualTo("testi@oph.fi");
        assertThat(getEmail("1.2.8001.2")).isEqualTo("testiorganisaatio13@example.com,testiorganisaatio13b@bexample.com");
    }

    @Test
    @Sql({"/data/truncate_tables.sql"})
    @Sql({"/data/ryhma_organisaatio_data.sql"})
    void ryhmaExport() {
        exportService.createSchema();

        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM export.organisaatio", Long.class)).isEqualTo(2L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM export.osoite", Long.class)).isEqualTo(4L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM export.organisaatiosuhde", Long.class)).isEqualTo(1L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM export.ryhma", Long.class)).isEqualTo(1L);

        var ryhma = getRyhma("SELECT * FROM export.ryhma WHERE ryhma_oid = '1.2.2004.2'");
        assertThat(ryhma.nimi_fi()).isEqualTo("ryhma");
        assertThat(ryhma.nimi_sv()).isNull();
        assertThat(ryhma.nimi_en()).isNull();
    }

    private String getEmail(String oid) {
        return jdbcTemplate.queryForObject("SELECT email FROM export.organisaatio WHERE organisaatio_oid = ?", String.class, oid);
    }

    private String getParentOid(String oid) {
        return jdbcTemplate.queryForObject("SELECT parent_oid FROM export.organisaatio WHERE organisaatio_oid = ?", String.class, oid);
    }

    private RyhmaRow getRyhma(String sql) {
        return jdbcTemplate.query(sql, (rs, rowNum) -> new RyhmaRow(
                rs.getString("ryhma_oid"),
                rs.getString("nimi_fi"),
                rs.getString("nimi_sv"),
                rs.getString("nimi_en")
        )).getFirst();
    }
}

record RyhmaRow(String ryhma_oid, String nimi_fi, String nimi_sv, String nimi_en) {}
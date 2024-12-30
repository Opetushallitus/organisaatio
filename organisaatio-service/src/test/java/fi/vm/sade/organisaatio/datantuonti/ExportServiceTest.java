package fi.vm.sade.organisaatio.datantuonti;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ExportServiceTest {
    @Autowired
    @Qualifier("DatantuontiExportService")
    private ExportService exportService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Sql({"/data/truncate_tables.sql"})
    @Sql({"/data/basic_organisaatio_data.sql"})
    void doesNotExportVarhaiskasvastusOrganisaatiot() {
        var allOrganisaatioOids = getAllOrgansaatioOids();
        var allOrganisaatioCount = allOrganisaatioOids.size();
        var varhaiskasvatusOrganisaatioOids = getVarhaiskasvatusOrganisaatioOids();
        var varhaiskasvatusOrganisaatioCount = varhaiskasvatusOrganisaatioOids.size();
        assertThat(allOrganisaatioCount).isEqualTo(13L);
        assertThat(varhaiskasvatusOrganisaatioCount).isEqualTo(3L);

        exportService.createSchema();

        var exportedOrganisatioOids = getExportedOrganisaatioCount();
        assertThat(exportedOrganisatioOids.size()).isEqualTo(10L);
        assertThat(exportedOrganisatioOids).doesNotContainSequence(varhaiskasvatusOrganisaatioOids);
    }

    private List<String> getExportedOrganisaatioCount() {
        return jdbcTemplate.queryForList("SELECT organisaatio_oid FROM datantuonti_export.organisaatio", String.class);
    }

    private List<String> getVarhaiskasvatusOrganisaatioOids() {
        var sql = """
            SELECT distinct(organisaatio_id)
            FROM organisaatio_tyypit
            WHERE tyypit = 'organisaatiotyyppi_08'
            OR tyypit = 'organisaatiotyyppi_07'
        """;
        return jdbcTemplate.queryForList(sql, String.class);
    }

    private List<String> getAllOrgansaatioOids() {
        return jdbcTemplate.queryForList("SELECT oid FROM organisaatio", String.class);
    }
}

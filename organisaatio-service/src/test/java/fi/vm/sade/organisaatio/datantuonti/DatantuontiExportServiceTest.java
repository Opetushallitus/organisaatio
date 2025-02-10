package fi.vm.sade.organisaatio.datantuonti;

import fi.vm.sade.organisaatio.RyhmaBuilder;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DatantuontiExportServiceTest {
    @Autowired
    OrganisaatioRepository organisaatioRepository;
    @Autowired
    private DatantuontiExportService datantuontiExportService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Sql({"/data/truncate_tables.sql"})
    @Sql({"/data/datantuonti_organisaatio_data.sql"})
    void doesNotExportVarhaiskasvastusOrganisaatiot() {
        var allOrganisaatioOids = getAllOrgansaatioOids();
        var allOrganisaatioCount = allOrganisaatioOids.size();
        var varhaiskasvatusOrganisaatioOids = getVarhaiskasvatusOrganisaatioOids();
        var varhaiskasvatusOrganisaatioCount = varhaiskasvatusOrganisaatioOids.size();
        assertThat(allOrganisaatioCount).isEqualTo(13L);
        assertThat(varhaiskasvatusOrganisaatioCount).isEqualTo(3L);

        datantuontiExportService.createSchemaAndReturnTransactionTimestampFromEpoch();

        var exportedOrganisatioOids = getExportedOrganisaatioCount();
        assertThat(exportedOrganisatioOids.size()).isEqualTo(10L);
        assertThat(exportedOrganisatioOids).doesNotContainSequence(varhaiskasvatusOrganisaatioOids);
    }

    @Test
    @Sql({"/data/truncate_tables.sql"})
    void exportsRyhmat() {
        long expectedRyhmaCount = 10;
        for (int i = 0; i < expectedRyhmaCount; ++i) {
            var ryhma = new RyhmaBuilder("1.2.246.562.28." + i)
                    .nimi("FI", "Ryhma_" + i)
                    .nimi("SV", "Ryhma_" + i)
                    .build();
            organisaatioRepository.saveAndFlush(ryhma);
        }

        var actualRyhmaCount = getRyhmaOids().size();
        assertThat(actualRyhmaCount).isEqualTo(expectedRyhmaCount);

        datantuontiExportService.createSchemaAndReturnTransactionTimestampFromEpoch();

        var actualExportedRyhmaCount = getExportedRyhmaOids().size();
        assertThat(actualExportedRyhmaCount).isEqualTo(expectedRyhmaCount);
    }

    @Test
    @Sql({"/data/truncate_tables.sql"})
    void exportsRyhmatSeparateFromOrganisaatiot() {
        long expectedRyhmaCount = 10;
        insertRyhmat(expectedRyhmaCount);

        var actualRyhmaCount = getRyhmaOids().size();
        assertThat(actualRyhmaCount).isEqualTo(expectedRyhmaCount);

        datantuontiExportService.createSchemaAndReturnTransactionTimestampFromEpoch();

        var actualExportedRyhmaCount = getSepratelyExportedRyhmaOids().size();
        assertThat(actualExportedRyhmaCount).isEqualTo(expectedRyhmaCount);
    }

    private void insertRyhmat(long expectedRyhmaCount) {
        for (int i = 0; i < expectedRyhmaCount; ++i) {
            var ryhmaBuilder = new RyhmaBuilder("1.2.246.562.28." + i)
                    .nimi("FI", "Ryhma_" + i)
                    .nimi("SV", "Ryhma_" + i)
                    .nimi("EN", "Ryhma_" + i)
                    .kuvaus2("FI", "Kuvaus_" + i)
                    .kuvaus2("SV", "Kuvaus_" + i)
                    .kuvaus2("EN", "Kuvaus_" + i)
                    .kayttoryhma("kayttoryhma_" + ((i % 2) + 1))
                    .ryhmatyyppi("ryhmatyyppi_" + ((i % 2) + 1));

            if (i % 4 == 0) {
                ryhmaBuilder.poistettu();
            }

            if (i % 5 == 0) {
                ryhmaBuilder.lakkautusPvm(LocalDate.now());
            }

            var ryhma = ryhmaBuilder.build();

            organisaatioRepository.saveAndFlush(ryhma);
        }
    }

    private List<String> getRyhmaOids() {
        return organisaatioRepository
                .findByOrganisaatiotyyppi(OrganisaatioTyyppi.RYHMA.koodiValue())
                .stream()
                .map(Organisaatio::getOid)
                .toList();
    }

    private List<String> getExportedRyhmaOids() {
        return jdbcTemplate.queryForList("SELECT oid FROM datantuonti_export.organisaatio WHERE organisaatiotyypit = 'Ryhma'", String.class);
    }

    private List<String> getSepratelyExportedRyhmaOids() {
        return jdbcTemplate.queryForList("SELECT oid FROM datantuonti_export.ryhma", String.class);
    }

    private List<String> getExportedOrganisaatioCount() {
        return jdbcTemplate.queryForList("SELECT oid FROM datantuonti_export.organisaatio", String.class);
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

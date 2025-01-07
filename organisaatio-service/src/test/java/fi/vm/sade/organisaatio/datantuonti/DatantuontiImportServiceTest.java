package fi.vm.sade.organisaatio.datantuonti;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;

@SpringBootTest
public class DatantuontiImportServiceTest {
    @Autowired
    private DatantuontiImportService importService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private OrganisaatioRepository organisaatioRepository;

    @BeforeEach
    public void insertDatantuonti() throws Exception {
        jdbcTemplate.execute(DatantuontiImportService.CREATE_DATANTUONTI_ORGANISAATIO);
        jdbcTemplate.execute("""
            INSERT INTO datantuonti_organisaatio_temp (
                oid,
                parent_oid,
                oppilaitostyyppi,
                organisaatiotyypit,
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
                kielet)
            VALUES
                ('1.2.2004.1', '1.2.2321.0', NULL, 'organisaatiotyyppi_06', '3763114-8', true, 'nimifi', 'nimisv', 'nimien', DATE '2025-01-01', DATE '2025-01-02', 'ry', 'Helsinki', 'maatjavaltiot1_fin', NULL),
                ('1.2.2321.0', NULL, NULL, 'organisaatiotyyppi_06', '4733601-1', false, 'eka', 'första', 'first', DATE '2024-01-01', NULL, 'ry', 'kunta_445', 'maatjavaltiot1_fin', 'oppilaitoksenopetuskieli_1#2'),
                ('1.2.2123.4', '1.2.2004.1', 'oppilaitostyyppi_19#1', 'organisaatiotyyppi_03,organisaatiotyyppi_04', '1621417-7', true, 'toka', 'andra', 'second', DATE '2023-01-01', DATE '2025-01-01', 'ry', 'kunta_445', NULL, 'oppilaitoksenopetuskieli_1#1,oppilaitoksenopetuskieli_2#2')
        """);
        jdbcTemplate.execute(DatantuontiImportService.CREATE_DATANTUONTI_OSOITE);
        jdbcTemplate.execute("""
            INSERT INTO datantuonti_osoite_temp (
                oid,
                osoitetyyppi,
                osoite,
                postinumero,
                postitoimipaikka,
                kieli)
            VALUES
                ('1.2.2004.1', 'posti', 'VIÄRÄTIÄ 3', 'posti_00100', 'Helsinki', 'kieli_sv#1'),
                ('1.2.2321.0', 'posti', 'KATU 1', 'posti_00100', 'Helsinki', 'kieli_fi#1'),
                ('1.2.2123.4', 'posti', 'TIE 1 B 3', 'posti_20100', 'Turku', 'kieli_fi#1'),
                ('1.2.2123.4', 'kaynti', 'VÄGEN 1 B 3', 'posti_20100', 'Åbo', 'kieli_sv#1')
        """);
    }

    @Test
    @Transactional
    @Sql({"/data/truncate_tables.sql"})
    @Sql({"/data/basic_organisaatio_data.sql"})
    public void createNewOrganisationsCreates() throws Exception {
        Long originalCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM organisaatio", Long.class);

        importService.createNewOrganisations();

        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM organisaatio", Long.class)).isEqualTo(originalCount + 2);

        Organisaatio eka = organisaatioRepository.findByOids(List.of("1.2.2321.0"), false, false).get(0);
        assertThat(eka)
            .returns("4733601-1", from(Organisaatio::getYtunnus))
            .returns(Optional.empty(), from(Organisaatio::getParentOid))
            .returns(null, from(Organisaatio::getOppilaitosTyyppi))
            .returns(Set.of("organisaatiotyyppi_06"), from(Organisaatio::getTyypit))
            .returns(Date.from(LocalDate.of(2024, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()), from(Organisaatio::getAlkuPvm))
            .returns(null, from(Organisaatio::getLakkautusPvm))
            .returns(Set.of("oppilaitoksenopetuskieli_1#2"), from(Organisaatio::getKielet))
            .returns(Map.of("fi", "eka", "sv", "första", "en", "first"), o -> o.getActualNimi().getValues())
            .returns("ry", from(Organisaatio::getYritysmuoto))
            .returns("maatjavaltiot1_fin", from(Organisaatio::getMaa))
            .returns("kunta_445", from(Organisaatio::getKotipaikka))
            .returns("KATU 1", o -> o.getPostiosoite().getOsoite())
            .returns(null, from(Organisaatio::getKayntiosoite));

        Organisaatio toka = organisaatioRepository.findByOids(List.of("1.2.2123.4"), false, false).get(0);
        assertThat(toka)
            .returns("1621417-7", from(Organisaatio::getYtunnus))
            .returns(Optional.of("1.2.2004.1"), from(Organisaatio::getParentOid))
            .returns(Date.from(LocalDate.of(2023, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()), from(Organisaatio::getAlkuPvm))
            .returns(Date.from(LocalDate.of(2025, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()), from(Organisaatio::getLakkautusPvm))
            .returns("oppilaitostyyppi_19#1", from(Organisaatio::getOppilaitosTyyppi))
            .returns(Set.of("organisaatiotyyppi_03", "organisaatiotyyppi_04"), from(Organisaatio::getTyypit))
            .returns(Set.of("oppilaitoksenopetuskieli_1#1", "oppilaitoksenopetuskieli_2#2"), from(Organisaatio::getKielet))
            .returns(Map.of("fi", "toka", "sv", "andra", "en", "second"), o -> o.getActualNimi().getValues())
            .returns("ry", from(Organisaatio::getYritysmuoto))
            .returns(null, from(Organisaatio::getMaa))
            .returns("kunta_445", from(Organisaatio::getKotipaikka))
            .returns("TIE 1 B 3", o -> o.getPostiosoite().getOsoite())
            .returns("VÄGEN 1 B 3", o -> o.getKayntiosoite().getOsoite());
    }

    @Test
    @Transactional
    @Sql({"/data/truncate_tables.sql"})
    @Sql({"/data/basic_organisaatio_data.sql"})
    public void createNewOrganisationsDoesNotUpdateExisting() throws Exception {
        importService.createNewOrganisations();
        Organisaatio existing = organisaatioRepository.findByOids(List.of("1.2.2004.1"), false, false).get(0);
        assertThat(existing)
            .returns("2255802-1", from(Organisaatio::getYtunnus))
            .returns(Optional.of("1.2.246.562.24.00000000001"), from(Organisaatio::getParentOid))
            .returns(null, from(Organisaatio::getOppilaitosTyyppi))
            .returns(Date.from(LocalDate.of(2004, 8, 8).atStartOfDay(ZoneId.systemDefault()).toInstant()), from(Organisaatio::getAlkuPvm))
            .returns(null, from(Organisaatio::getLakkautusPvm))
            .returns(Set.of(), from(Organisaatio::getKielet))
            .returns(Map.of("fi", "root test koulutustoimija", "sv", "root test utbildningsoperator"), o -> o.getActualNimi().getValues())
            .returns("oy", from(Organisaatio::getYritysmuoto))
            .returns(null, from(Organisaatio::getMaa))
            .returns("Helsinki", from(Organisaatio::getKotipaikka))
            .returns("Mannerheimintie 1", o -> o.getPostiosoite().getOsoite())
            .returns("Mannerheimintie 1", o -> o.getKayntiosoite().getOsoite());
    }
}

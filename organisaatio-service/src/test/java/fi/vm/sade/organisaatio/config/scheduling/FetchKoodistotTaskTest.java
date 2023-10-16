package fi.vm.sade.organisaatio.config.scheduling;

import fi.vm.sade.organisaatio.client.OrganisaatioKoodistoClient;
import fi.vm.sade.organisaatio.resource.BaseOrganisaatioApiTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Sql("/data/truncate_tables.sql")
class FetchKoodistotTaskTest extends BaseOrganisaatioApiTest {
    @Value("${host.virkailija}")
    String virkailijaHost;
    @Autowired
    FetchKoodistotTask fetchKoodistotTask;
    @MockBean
    OrganisaatioKoodistoClient koodistoClient;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void testDataImportFromKoodisto() throws IOException {
        for (String koodisto : List.of("oppilaitostyyppi", "kunta", "posti", "oppilaitoksenopetuskieli", "vuosiluokat")) {
            String url = "https://" + virkailijaHost + "/koodisto-service/rest/json/" + koodisto + "/koodi?onlyValidKoodis=true";
            when(koodistoClient.get(url)).thenReturn(loadKoodistoFromResources(koodisto));
        }

        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM koodisto_oppilaitostyyppi", Long.class)).isEqualTo(0L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM koodisto_oppilaitoksenopetuskieli", Long.class)).isEqualTo(0L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM koodisto_kunta", Long.class)).isEqualTo(0L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM koodisto_posti", Long.class)).isEqualTo(0L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM koodisto_vuosiluokat", Long.class)).isEqualTo(0L);

        fetchKoodistotTask.execute();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM koodisto_oppilaitostyyppi", Long.class)).isEqualTo(23L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM koodisto_oppilaitoksenopetuskieli", Long.class)).isEqualTo(6L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM koodisto_kunta", Long.class)).isEqualTo(313L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM koodisto_posti", Long.class)).isEqualTo(4207L);

        assertThat(jdbcTemplate.queryForObject("SELECT nimi_fi FROM koodisto_oppilaitostyyppi WHERE koodiuri = 'oppilaitostyyppi_41'", String.class)).isEqualTo("Ammattikorkeakoulut");
        assertThat(jdbcTemplate.queryForObject("SELECT nimi_fi FROM koodisto_posti WHERE koodiuri = 'posti_04400'", String.class)).isEqualTo("JÄRVENPÄÄ");
        assertThat(jdbcTemplate.queryForObject("SELECT versio FROM koodisto_posti WHERE koodiuri = 'posti_04400'", Long.class)).isEqualTo(2L);
        assertThat(jdbcTemplate.queryForObject("SELECT nimi_fi FROM koodisto_posti WHERE koodiarvo = '00960'", String.class)).isEqualTo("HELSINKI");
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM koodisto_vuosiluokat", Long.class)).isEqualTo(12L);
    }

    private String loadKoodistoFromResources(String koodisto) throws IOException {
        try (InputStream is = this.getClass().getResourceAsStream("/koodisto/" + koodisto + ".json")) {
            Objects.requireNonNull(is);
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        }
    }
}
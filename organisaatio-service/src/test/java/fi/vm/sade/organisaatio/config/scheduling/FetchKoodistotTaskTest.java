package fi.vm.sade.organisaatio.config.scheduling;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import fi.vm.sade.organisaatio.client.Oauth2BearerClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@WireMockTest(httpPort = 28080)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql("/data/truncate_tables.sql")
@Sql("/data/basic_organisaatio_data.sql")
class FetchKoodistotTaskTest {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    FetchKoodistotTask fetchKoodistotTask;
    @Autowired
    FetchKoulutusluvatTask fetchKoulutusluvatTask;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void beforeEach() throws IOException {
        mockOauth2TokenRequest();
    }

    @Test
    void testDataImportFromKoodisto() throws IOException, InterruptedException {
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM koodisto_oppilaitostyyppi", Long.class)).isEqualTo(0L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM koodisto_oppilaitoksenopetuskieli", Long.class)).isEqualTo(0L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM koodisto_kunta", Long.class)).isEqualTo(0L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM koodisto_posti", Long.class)).isEqualTo(0L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM koodisto_vuosiluokat", Long.class)).isEqualTo(0L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM koodisto_koulutus", Long.class)).isEqualTo(0L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM koodisto_maakunta", Long.class)).isEqualTo(0L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM maakuntakuntarelation", Long.class)).isEqualTo(0L);

        fetchKoodistotTask.execute();

        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM koodisto_oppilaitostyyppi", Long.class)).isEqualTo(23L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM koodisto_oppilaitoksenopetuskieli", Long.class)).isEqualTo(6L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM koodisto_kunta", Long.class)).isEqualTo(313L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM koodisto_posti", Long.class)).isEqualTo(4207L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM koodisto_maakunta", Long.class)).isEqualTo(21L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM maakuntakuntarelation", Long.class)).isEqualTo(311L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM koodisto_koulutus", Long.class)).isEqualTo(3172L);


        assertThat(jdbcTemplate.queryForObject("SELECT nimi_fi FROM koodisto_oppilaitostyyppi WHERE koodiuri = 'oppilaitostyyppi_41'", String.class)).isEqualTo("Ammattikorkeakoulut");
        assertThat(jdbcTemplate.queryForObject("SELECT nimi_fi FROM koodisto_posti WHERE koodiuri = 'posti_04400'", String.class)).isEqualTo("JÄRVENPÄÄ");
        assertThat(jdbcTemplate.queryForObject("SELECT versio FROM koodisto_posti WHERE koodiuri = 'posti_04400'", Long.class)).isEqualTo(2L);
        assertThat(jdbcTemplate.queryForObject("SELECT nimi_fi FROM koodisto_posti WHERE koodiarvo = '00960'", String.class)).isEqualTo("HELSINKI");
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM koodisto_vuosiluokat", Long.class)).isEqualTo(12L);
    }

    @Test
    void worksEvenIfThereAreReferencestoKoulutusKoodisto() {
        fetchKoodistotTask.execute();
        fetchKoulutusluvatTask.execute();
        assertDoesNotThrow(() -> fetchKoodistotTask.execute());
    }

    private void mockOauth2TokenRequest() throws IOException {
        var fakeTokenResponse = new Oauth2BearerClient.Token("", "Bearer", Integer.MAX_VALUE);
        stubFor(post(urlEqualTo("/kayttooikeus-service/oauth2/token"))
                .willReturn(aResponse().withStatus(200).withBody(objectMapper.writeValueAsString(fakeTokenResponse))));
        stubFor(any(anyUrl()).atPriority(Integer.MAX_VALUE).willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));
    }
}
package fi.vm.sade.organisaatio.config.scheduling;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import fi.vm.sade.organisaatio.client.Oauth2BearerClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

@WireMockTest(httpPort = 28080)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql({"/data/truncate_tables.sql", "/data/basic_organisaatio_data.sql"})
class FetchKoulutusluvatTaskTest {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private FetchKoulutusluvatTask fetchKoulutusluvatTask;
    @Autowired
    private FetchKoodistotTask fetchKoodistotTask;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void beforeEach() throws IOException {
        mockOauth2TokenRequest();
        fetchKoodistotTask.execute();
    }

    @Test
    void testDataImportFromOiva() {
        fetchKoulutusluvatTask.execute();
        assertKoulutuslupaCount("1492449-0", 3);
        assertKoulutuslupaCount("2255802-1", 0); // koulutusluvalla päättymispäivä menneisyydessä
    }

    private void assertKoulutuslupaCount(String ytunnus, int expectedCount) {
        var sql = "SELECT count(*) FROM organisaatio" +
                " JOIN organisaatio_koulutuslupa ON (id = organisaatio_id)" +
                " WHERE ytunnus = ?";
        var count = jdbcTemplate.queryForObject(sql, Long.class, ytunnus);
        assertThat(count).isEqualTo(expectedCount);
    }

    private void mockOauth2TokenRequest() throws IOException {
        var fakeTokenResponse = new Oauth2BearerClient.Token("", "Bearer", Integer.MAX_VALUE);
        stubFor(post(urlEqualTo("/kayttooikeus-service/oauth2/token"))
                .willReturn(aResponse().withStatus(200).withBody(objectMapper.writeValueAsString(fakeTokenResponse))));
    }
}
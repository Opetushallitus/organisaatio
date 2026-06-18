package fi.vm.sade.varda.rekisterointi.client;

import tools.jackson.databind.ObjectMapper;
import fi.vm.sade.varda.rekisterointi.model.Koodi;
import fi.vm.sade.varda.rekisterointi.model.KoodistoType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.net.http.HttpClient;
import java.util.Collection;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest
@ActiveProfiles("test")
@EnableWireMock(@ConfigureWireMock(
        baseUrlProperties = "varda-rekisterointi.url-virkailija",
        filesUnderDirectory = "src/test/resources"))
public class KoodistoClientTest {

    @Autowired
    private HttpClient httpClient;
    @Value("${varda-rekisterointi.url-virkailija}")
    private String virkailijaUrl;
    @Autowired
    private ObjectMapper objectMapper;

    private KoodistoClient client;

    @BeforeEach
    public void setup() {
        client = new KoodistoClient(httpClient, virkailijaUrl, objectMapper);
    }

    @Test
    public void listKoodit() {
        stubFor(get(urlEqualTo("/koodisto-service/rest/json/vardatoimintamuoto/koodi"))
                .willReturn(aResponse().withStatus(200).withBodyFile("koodisto/vardatoimintamuoto.json")));

        Collection<Koodi> koodit = client.listKoodit(KoodistoType.VARDA_TOIMINTAMUOTO);

        assertThat(koodit)
                .extracting(koodi -> koodi.uri, koodi -> koodi.nimi)
                .containsExactly(
                        tuple("vardatoimintamuoto_tm01", Map.of("fi", "Päiväkoti", "sv", "Daghem")),
                        tuple("vardatoimintamuoto_tm02", Map.of("fi", "Perhepäivähoito", "sv", "Familjedagvård")),
                        tuple("vardatoimintamuoto_tm03", Map.of("fi", "Ryhmäperhepäivähoito", "sv", "Gruppfamiljedagvård")));
    }

}

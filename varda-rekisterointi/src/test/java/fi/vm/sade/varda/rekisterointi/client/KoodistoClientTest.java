package fi.vm.sade.varda.rekisterointi.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import fi.vm.sade.javautils.httpclient.OphHttpClient;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.model.Koodi;
import fi.vm.sade.varda.rekisterointi.model.KoodistoType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class KoodistoClientTest {

    @Autowired
    private OphHttpClient httpClient;
    @Autowired
    private OphProperties properties;
    @Autowired
    private ObjectMapper objectMapper;

    private KoodistoClient client;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort().dynamicHttpsPort());

    @Before
    public void setup() {
        properties.addOverride("url-virkailija", "http://localhost:" + wireMockRule.port());
        client = new KoodistoClient(httpClient, properties, objectMapper);
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

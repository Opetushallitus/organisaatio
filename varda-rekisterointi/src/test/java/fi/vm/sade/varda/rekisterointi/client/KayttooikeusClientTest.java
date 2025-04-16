package fi.vm.sade.varda.rekisterointi.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.model.VirkailijaCriteria;
import fi.vm.sade.varda.rekisterointi.model.VirkailijaDto;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class KayttooikeusClientTest {
    @Autowired
    private OphProperties properties;
    @Autowired
    private ObjectMapper objectMapper;

    private KayttooikeusClient client;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort().dynamicHttpsPort());

    @Before
    public void setup() {
        properties.addOverride("url-virkailija", "http://localhost:" + wireMockRule.port());
        var bearer = new Oauth2BearerClient(objectMapper);
        bearer.setOauth2IssuerUri("http://localhost:" + wireMockRule.port());
        client = new KayttooikeusClient(new OtuvaOauth2Client(bearer), properties, objectMapper);
    }

    @Test
    public void listVirkailijaBy() {
        stubFor(post(urlEqualTo("/kayttooikeus-service/virkailija/haku"))
                .willReturn(aResponse().withStatus(200).withBody("[{\"oid\": \"oid1\"}, {\"oid\": \"oid2\"}]")));
        stubFor(post(urlEqualTo("/oauth2/token"))
                .willReturn(aResponse().withStatus(200).withBody("{ \"access_token\": \"token\", \"expires_in\": 12346, \"token_type\": \"Bear\" }")));
        VirkailijaCriteria criteria = new VirkailijaCriteria();

        Collection<VirkailijaDto> list = client.listVirkailijaBy(criteria);

        assertThat(list).extracting(virkailija -> virkailija.oid).containsExactly("oid1", "oid2");
    }

}

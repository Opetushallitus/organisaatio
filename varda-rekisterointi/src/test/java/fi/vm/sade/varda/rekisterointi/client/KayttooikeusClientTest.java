package fi.vm.sade.varda.rekisterointi.client;

import tools.jackson.databind.ObjectMapper;
import fi.vm.sade.varda.rekisterointi.model.VirkailijaCriteria;
import fi.vm.sade.varda.rekisterointi.model.VirkailijaDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.util.Collection;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@EnableWireMock(@ConfigureWireMock(
        baseUrlProperties = "varda-rekisterointi.url-virkailija",
        filesUnderDirectory = "src/test/resources"))
public class KayttooikeusClientTest {
    @Value("${varda-rekisterointi.url-virkailija}")
    private String virkailijaUrl;
    @Autowired
    private ObjectMapper objectMapper;

    private KayttooikeusClient client;

    @BeforeEach
    public void setup() {
        var bearer = new Oauth2BearerClient(objectMapper);
        bearer.setOauth2IssuerUri(virkailijaUrl);
        bearer.setClientId("dummy");
        bearer.setClientSecret("dummy");
        client = new KayttooikeusClient(new OtuvaOauth2Client(bearer), virkailijaUrl, objectMapper);
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

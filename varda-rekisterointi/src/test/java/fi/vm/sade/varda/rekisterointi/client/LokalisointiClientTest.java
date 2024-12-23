package fi.vm.sade.varda.rekisterointi.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import fi.vm.sade.properties.OphProperties;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class LokalisointiClientTest {

    @Autowired
    private LokalisointiClient client;
    @Autowired
    private OphProperties properties;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort().dynamicHttpsPort());

    @Before
    public void setup() {
        properties.addOverride("url-virkailija", "http://localhost:" + wireMockRule.port());
    }

    @Test
    public void getByCategory() {
        stubFor(get(urlEqualTo("/lokalisointi/cxf/rest/v1/localisation?category=getByCategory"))
                .willReturn(aResponse().withStatus(200).withBodyFile("lokalisointi/lokalisointi.json")));

        Map<String, Map<String, String>> lokalisointi = client.getByCategory("getByCategory");

        assertThat(lokalisointi).isEqualTo(Map.of(
                "fi", Map.of("key1", "value1-fi", "key2", "value2-fi"),
                "sv", Map.of("key1", "value1-sv"),
                "en", Map.of()));
    }

}

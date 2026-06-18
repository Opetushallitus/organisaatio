package fi.vm.sade.varda.rekisterointi.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@EnableWireMock(@ConfigureWireMock(
        baseUrlProperties = "varda-rekisterointi.url-virkailija",
        filesUnderDirectory = "src/test/resources"))
public class LokalisointiClientTest {

    @Autowired
    private LokalisointiClient client;

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

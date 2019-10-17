package fi.vm.sade.varda.rekisterointi.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.model.EmailDto;
import fi.vm.sade.varda.rekisterointi.model.EmailMessageDto;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class ViestintaClientTest {

    @Autowired
    private ViestintaClient client;
    @Autowired
    private OphProperties properties;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort().dynamicHttpsPort());

    @Before
    public void setup() {
        properties.addOverride("url-virkailija", "http://localhost:" + wireMockRule.port());
    }

    @Test
    public void saveEmail() {
        stubFor(post(urlEqualTo("/ryhmasahkoposti-service/email?sanitize=false"))
                .willReturn(aResponse().withStatus(200).withBody("{\"id\": \"id123\", \"tuntematon\": \"arvo\"}")));
        EmailDto email = EmailDto.builder()
                .email("example@example.com")
                .message(EmailMessageDto.builder()
                        .subject("subject")
                        .body("body")
                        .build())
                .build();

        String id = client.save(email, false);

        assertThat(id).isEqualTo("id123");
        verify(postRequestedFor(urlEqualTo("/ryhmasahkoposti-service/email?sanitize=false")));
    }

}

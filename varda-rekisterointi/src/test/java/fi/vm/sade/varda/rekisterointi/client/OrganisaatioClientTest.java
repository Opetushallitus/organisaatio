package fi.vm.sade.varda.rekisterointi.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioV4Dto;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class OrganisaatioClientTest {

    @Autowired
    private OrganisaatioClient client;
    @Autowired
    private OphProperties properties;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort().dynamicHttpsPort());

    @Before
    public void setup() {
        properties.addOverride("url-virkailija", "http://localhost:" + wireMockRule.port());
    }

    @Test
    public void getByYtunnus() {
        stubFor(get(urlEqualTo("/organisaatio-service/rest/organisaatio/v4/ytunnus123"))
                .willReturn(aResponse().withStatus(200).withBody("{\"ytunnus\": \"ytunnus123\", \"tuntematon\": \"arvo\"}")));

        Optional<OrganisaatioV4Dto> organisaatio = client.getV4ByYtunnus("ytunnus123");

        assertThat(organisaatio).isNotEmpty();
    }

    @Test
    public void getByYtunnusNotFound() {
        stubFor(get(urlEqualTo("/organisaatio-service/rest/organisaatio/v4/ytunnus123"))
                .willReturn(aResponse().withStatus(404)));

        Optional<OrganisaatioV4Dto> organisaatio = client.getV4ByYtunnus("ytunnus123");

        assertThat(organisaatio).isEmpty();
    }

    @Test
    public void getByYtunnusFromYtj() {
        stubFor(get(urlEqualTo("/organisaatio-service/rest/ytj/ytunnus123/v4"))
                .willReturn(aResponse().withStatus(200).withBody("{\"ytunnus\": \"ytunnus123\", \"tuntematon\": \"arvo\"}")));

        Optional<OrganisaatioV4Dto> organisaatio = client.getV4ByYtunnusFromYtj("ytunnus123");

        assertThat(organisaatio).isNotEmpty();
    }

    @Test
    public void create() {
        stubFor(post(urlEqualTo("/organisaatio-service/rest/organisaatio/v4"))
                .willReturn(aResponse().withStatus(200).withBody("{\"ytunnus\": \"ytunnus123\", \"tuntematon\": \"arvo\"}")));
        OrganisaatioV4Dto organisaatio = new OrganisaatioV4Dto();

        organisaatio = client.save(organisaatio);

        assertThat(organisaatio).extracting(t -> t.ytunnus).isEqualTo("ytunnus123");
    }

    @Test
    public void update() {
        stubFor(put(urlEqualTo("/organisaatio-service/rest/organisaatio/v4/oid123"))
                .willReturn(aResponse().withStatus(200).withBody("{\"ytunnus\": \"ytunnus123\", \"tuntematon\": \"arvo\"}")));
        OrganisaatioV4Dto organisaatio = new OrganisaatioV4Dto();
        organisaatio.oid = "oid123";

        organisaatio = client.save(organisaatio);

        assertThat(organisaatio).extracting(t -> t.ytunnus).isEqualTo("ytunnus123");
    }

}

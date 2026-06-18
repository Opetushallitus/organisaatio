package fi.vm.sade.varda.rekisterointi.client;

import tools.jackson.databind.ObjectMapper;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioCriteria;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest
@ActiveProfiles("test")
@EnableWireMock(@ConfigureWireMock(
        baseUrlProperties = "varda-rekisterointi.url-virkailija",
        filesUnderDirectory = "src/test/resources"))
public class OrganisaatioClientTest {
    @Value("${varda-rekisterointi.url-virkailija}")
    private String virkailijaUrl;
    @Autowired
    private ObjectMapper objectMapper;

    private OrganisaatioClient client;

    @BeforeEach
    public void setup() {
        var bearer = new Oauth2BearerClient(objectMapper);
        bearer.setOauth2IssuerUri(virkailijaUrl);
        bearer.setClientId("dummy");
        bearer.setClientSecret("dummy");
        client = new OrganisaatioClient(
                new OtuvaOauth2Client(bearer),
                virkailijaUrl,
                objectMapper);
    }

    @Test
    public void getByYtunnus() {
        stubFor(get(urlEqualTo("/organisaatio-service/api/ytunnus123"))
                .willReturn(aResponse().withStatus(200).withBody("{\"ytunnus\": \"ytunnus123\", \"tuntematon\": \"arvo\"}")));
        stubFor(post(urlEqualTo("/oauth2/token"))
                .willReturn(aResponse().withStatus(200).withBody("{ \"access_token\": \"token\", \"expires_in\": 12346, \"token_type\": \"Bear\" }")));

        Optional<OrganisaatioDto> organisaatio = client.getOrganisaatioByYtunnus("ytunnus123");

        assertThat(organisaatio).isNotEmpty();
    }

    @Test
    public void getByYtunnusNotFound() {
        stubFor(get(urlEqualTo("/organisaatio-service/api/ytunnus123"))
                .willReturn(aResponse().withStatus(404)));
        stubFor(post(urlEqualTo("/oauth2/token"))
                .willReturn(aResponse().withStatus(200).withBody("{ \"access_token\": \"token\", \"expires_in\": 12346, \"token_type\": \"Bear\" }")));

        Optional<OrganisaatioDto> organisaatio = client.getOrganisaatioByYtunnus("ytunnus123");

        assertThat(organisaatio).isEmpty();
    }

    @Test
    public void getByYtunnusFromYtj() {
        stubFor(get(urlEqualTo("/organisaatio-service/rest/ytj/ytunnus123/v4"))
                .willReturn(aResponse().withStatus(200).withBody("{\"ytunnus\": \"ytunnus123\", \"tuntematon\": \"arvo\"}")));
        stubFor(post(urlEqualTo("/oauth2/token"))
                .willReturn(aResponse().withStatus(200).withBody("{ \"access_token\": \"token\", \"expires_in\": 12346, \"token_type\": \"Bear\" }")));

        Optional<OrganisaatioDto> organisaatio = client.getOrganisaatioByYtunnusFromYtj("ytunnus123");

        assertThat(organisaatio).isNotEmpty();
    }

    @Test
    public void create() {
        stubFor(post(urlEqualTo("/organisaatio-service/api/"))
                .willReturn(aResponse().withStatus(200).withBody("{\"organisaatio\": {\"ytunnus\": \"ytunnus123\", \"tuntematon\": \"arvo\"}}")));
        stubFor(post(urlEqualTo("/oauth2/token"))
                .willReturn(aResponse().withStatus(200).withBody("{ \"access_token\": \"token\", \"expires_in\": 12346, \"token_type\": \"Bear\" }")));
        OrganisaatioDto organisaatio = new OrganisaatioDto();

        organisaatio = client.save(organisaatio);

        assertThat(organisaatio).extracting(t -> t.ytunnus).isEqualTo("ytunnus123");
    }

    @Test
    public void update() {
        stubFor(put(urlEqualTo("/organisaatio-service/api/oid123"))
                .willReturn(aResponse().withStatus(200).withBody("{\"organisaatio\": {\"ytunnus\": \"ytunnus123\", \"tuntematon\": \"arvo\"}}")));
        stubFor(post(urlEqualTo("/oauth2/token"))
                .willReturn(aResponse().withStatus(200).withBody("{ \"access_token\": \"token\", \"expires_in\": 12346, \"token_type\": \"Bear\" }")));
        OrganisaatioDto organisaatio = new OrganisaatioDto();
        organisaatio.oid = "oid123";

        organisaatio = client.save(organisaatio);

        assertThat(organisaatio).extracting(t -> t.ytunnus).isEqualTo("ytunnus123");
    }

    @Test
    public void listBy() {
        stubFor(get(urlEqualTo("/organisaatio-service/api/hae?aktiiviset=true&suunnitellut=false&lakkautetut=false&yritysmuoto=Kunta&kunta=kunta_020&kunta=kunta_030"))
                .willReturn(aResponse().withStatus(200).withBody("{\"numHits\": 3, \"organisaatiot\": [" +
                        "{\"oid\": \"oid1\", \"alkuPvm\": \"1992-01-01\"}," +
                        "{\"oid\": \"oid2\", \"alkuPvm\": 258760800000}," +
                        "{\"oid\": \"oid3\", \"alkuPvm\": 1093467600000}]}")));
        stubFor(post(urlEqualTo("/oauth2/token"))
                .willReturn(aResponse().withStatus(200).withBody("{ \"access_token\": \"token\", \"expires_in\": 12346, \"token_type\": \"Bear\" }")));
        OrganisaatioCriteria criteria = new OrganisaatioCriteria();
        criteria.aktiiviset = true;
        criteria.yritysmuoto = List.of("Kunta");
        criteria.kunta = List.of("kunta_020", "kunta_030");

        Collection<OrganisaatioDto> list = client.listBy(criteria);

        assertThat(list).extracting(organisaatio -> organisaatio.oid, organisaatio -> organisaatio.alkuPvm).containsExactly(
                tuple("oid1", LocalDate.parse("1992-01-01")),
                tuple("oid2", LocalDate.parse("1978-03-15")),
                tuple("oid3", LocalDate.parse("2004-08-26")));
    }

}

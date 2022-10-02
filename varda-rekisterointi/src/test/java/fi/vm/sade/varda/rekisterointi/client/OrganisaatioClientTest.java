package fi.vm.sade.varda.rekisterointi.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioCriteria;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioDto;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

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
        stubFor(get(urlEqualTo("/organisaatio-service/rest/organisaatio/api/ytunnus123"))
                .willReturn(aResponse().withStatus(200).withBody("{\"ytunnus\": \"ytunnus123\", \"tuntematon\": \"arvo\"}")));

        Optional<OrganisaatioDto> organisaatio = client.getOrganisaatioByYtunnus("ytunnus123");

        assertThat(organisaatio).isNotEmpty();
    }

    @Test
    public void getByYtunnusNotFound() {
        stubFor(get(urlEqualTo("/organisaatio-service/rest/organisaatio/api/ytunnus123"))
                .willReturn(aResponse().withStatus(404)));

        Optional<OrganisaatioDto> organisaatio = client.getOrganisaatioByYtunnus("ytunnus123");

        assertThat(organisaatio).isEmpty();
    }

    @Test
    public void getByYtunnusFromYtj() {
        stubFor(get(urlEqualTo("/organisaatio-service/rest/ytj/ytunnus123/v4"))
                .willReturn(aResponse().withStatus(200).withBody("{\"ytunnus\": \"ytunnus123\", \"tuntematon\": \"arvo\"}")));

        Optional<OrganisaatioDto> organisaatio = client.getOrganisaatioByYtunnusFromYtj("ytunnus123");

        assertThat(organisaatio).isNotEmpty();
    }

    @Test
    public void create() {
        stubFor(post(urlEqualTo("/organisaatio-service/rest/organisaatio/api"))
                .willReturn(aResponse().withStatus(200).withBody("{\"organisaatio\": {\"ytunnus\": \"ytunnus123\", \"tuntematon\": \"arvo\"}}")));
        OrganisaatioDto organisaatio = new OrganisaatioDto();

        organisaatio = client.save(organisaatio);

        assertThat(organisaatio).extracting(t -> t.ytunnus).isEqualTo("ytunnus123");
    }

    @Test
    public void update() {
        stubFor(put(urlEqualTo("/organisaatio-service/rest/organisaatio/api/oid123"))
                .willReturn(aResponse().withStatus(200).withBody("{\"organisaatio\": {\"ytunnus\": \"ytunnus123\", \"tuntematon\": \"arvo\"}}")));
        OrganisaatioDto organisaatio = new OrganisaatioDto();
        organisaatio.oid = "oid123";

        organisaatio = client.save(organisaatio);

        assertThat(organisaatio).extracting(t -> t.ytunnus).isEqualTo("ytunnus123");
    }

    @Test
    public void listBy() {
        stubFor(get(urlEqualTo("/organisaatio-service/rest/organisaatio/api/hae?aktiiviset=true&suunnitellut=false&lakkautetut=false&yritysmuoto=Kunta&kunta=kunta_020&kunta=kunta_030"))
                .willReturn(aResponse().withStatus(200).withBody("{\"numHits\": 3, \"organisaatiot\": [" +
                        "{\"oid\": \"oid1\", \"alkuPvm\": \"1992-01-01\"}," +
                        "{\"oid\": \"oid2\", \"alkuPvm\": 258760800000}," +
                        "{\"oid\": \"oid3\", \"alkuPvm\": 1093467600000}]}")));
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

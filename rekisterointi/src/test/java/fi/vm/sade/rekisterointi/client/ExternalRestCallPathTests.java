package fi.vm.sade.rekisterointi.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.BasicCredentials;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.rekisterointi.configuration.HttpClientConfiguration;
import fi.vm.sade.rekisterointi.model.Kayttaja;
import fi.vm.sade.rekisterointi.model.KielistettyNimi;
import fi.vm.sade.rekisterointi.model.KoodistoType;
import fi.vm.sade.rekisterointi.model.Organisaatio;
import fi.vm.sade.rekisterointi.model.OrganisaatioCriteria;
import fi.vm.sade.rekisterointi.model.OrganisaatioV4Dto;
import fi.vm.sade.rekisterointi.model.RekisterointiDto;
import fi.vm.sade.rekisterointi.model.Yhteystiedot;
import fi.vm.sade.rekisterointi.properties.ValtuudetPropertiesImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

@SpringBootTest(
    classes = ExternalRestCallPathTests.TestApplication.class,
    properties = {
        "varda-rekisterointi.username=varda-rekisterointi",
        "varda-rekisterointi.password=varda-rekisterointi",
        "rekisterointi.valtuudet.host=http://localhost",
        "rekisterointi.valtuudet.client-id=test-client-id",
        "rekisterointi.valtuudet.api-key=test-api-key",
        "rekisterointi.valtuudet.oauth-password=test-oauth-password"
    })
@ActiveProfiles("test")
@EnableWireMock({
    @ConfigureWireMock(
        name = "virkailija",
        baseUrlProperties = {"url-virkailija", "otuva.jwt.issuer-uri"}),
    @ConfigureWireMock(name = "oppija", baseUrlProperties = "url-oppija"),
    @ConfigureWireMock(name = "rekisterointi", baseUrlProperties = "url-rekisterointi")
})
class ExternalRestCallPathTests {

  @SpringBootConfiguration
  @EnableAutoConfiguration
  @EnableConfigurationProperties(ValtuudetPropertiesImpl.class)
  @Import({
      HttpClientConfiguration.class,
      KoodistoClient.class,
      LokalisointiClient.class,
      Oauth2BearerClient.class,
      OrganisaatioClient.class,
      OtuvaOauth2Client.class,
      RekisterointiClient.class
  })
  static class TestApplication {

    @Bean("properties")
    OphProperties properties(Environment environment) {
      var properties = new OphProperties("/rekisterointi_oph.properties");
      var urlVirkailija = environment.getRequiredProperty("url-virkailija");
      properties.addDefault("url-oppija", environment.getRequiredProperty("url-oppija"));
      properties.addDefault("url-virkailija", urlVirkailija);
      properties.addDefault("url-rekisterointi", environment.getRequiredProperty("url-rekisterointi"));
      properties.addDefault("varda-rekisterointi.url", urlVirkailija + "/varda-rekisterointi/api/rekisterointi");
      properties.addDefault("varda-rekisterointi.username",
          environment.getRequiredProperty("varda-rekisterointi.username"));
      properties.addDefault("varda-rekisterointi.password",
          environment.getRequiredProperty("varda-rekisterointi.password"));
      return properties;
    }
  }

  @Autowired
  KoodistoClient koodistoClient;
  @Autowired
  LokalisointiClient lokalisointiClient;
  @Autowired
  OrganisaatioClient organisaatioClient;
  @Autowired
  Oauth2BearerClient oauth2BearerClient;
  @Autowired
  RekisterointiClient rekisterointiClient;

  @InjectWireMock("virkailija")
  WireMockServer virkailija;

  @Test
  void koodistoClientUsesOriginalKoodiPathAndQueryParameters() {
    virkailija.stubFor(get(urlPathEqualTo("/koodisto-service/rest/json/kunta/koodi"))
        .withQueryParam("koodistoVersio", equalTo("1"))
        .withQueryParam("onlyValidKoodis", equalTo("true"))
        .willReturn(okJson("[]")));

    koodistoClient.listKoodit(KoodistoType.KUNTA, Optional.of(1), Optional.of(true));

    virkailija.verify(getRequestedFor(urlPathEqualTo("/koodisto-service/rest/json/kunta/koodi"))
        .withQueryParam("koodistoVersio", equalTo("1"))
        .withQueryParam("onlyValidKoodis", equalTo("true")));
  }

  @Test
  void lokalisointiClientUsesOriginalListByCategoryPathAndQueryParameter() {
    virkailija.stubFor(get(urlPathEqualTo("/lokalisointi/cxf/rest/v1/localisation"))
        .withQueryParam("category", equalTo("varda-rekisterointi"))
        .willReturn(okJson("[]")));

    lokalisointiClient.getByCategory("varda-rekisterointi");

    virkailija.verify(getRequestedFor(urlPathEqualTo("/lokalisointi/cxf/rest/v1/localisation"))
        .withQueryParam("category", equalTo("varda-rekisterointi")));
  }

  @Test
  void rekisterointiClientUsesOriginalVardaRekisterointiPath() {
    virkailija.stubFor(post(urlEqualTo("/varda-rekisterointi/api/rekisterointi"))
        .withBasicAuth("varda-rekisterointi", "varda-rekisterointi")
        .willReturn(aResponse().withStatus(200)));

    rekisterointiClient.create(rekisterointiDto());

    virkailija.verify(postRequestedFor(urlEqualTo("/varda-rekisterointi/api/rekisterointi"))
        .withBasicAuth(new BasicCredentials("varda-rekisterointi", "varda-rekisterointi"))
        .withHeader("Content-Type", containing("application/json")));
  }

  @Test
  void organisaatioClientUsesOriginalPathsForReadsWritesAndSearch() {
    oauth2BearerClient.evictOauth2Bearer();
    stubOauthToken();
    virkailija.stubFor(get(urlEqualTo("/organisaatio-service/rest/organisaatio/v4/1234567-8"))
        .willReturn(okJson("{\"ytunnus\":\"1234567-8\"}")));
    virkailija.stubFor(get(urlEqualTo("/organisaatio-service/rest/ytj/1234567-8/v4"))
        .willReturn(okJson("{\"ytunnus\":\"1234567-8\"}")));
    virkailija.stubFor(get(urlEqualTo("/organisaatio-service/rest/organisaatio/v4/1.2.246.562.10.1"))
        .willReturn(okJson("{\"oid\":\"1.2.246.562.10.1\"}")));
    virkailija.stubFor(post(urlEqualTo("/organisaatio-service/rest/organisaatio/v4"))
        .willReturn(okJson("{\"organisaatio\":{\"oid\":\"1.2.246.562.10.2\"}}")));
    virkailija.stubFor(post(urlEqualTo("/organisaatio-service/rest/organisaatio/v4/1.2.246.562.10.1"))
        .willReturn(okJson("{\"organisaatio\":{\"oid\":\"1.2.246.562.10.1\"}}")));
    virkailija.stubFor(get(urlPathEqualTo("/organisaatio-service/rest/organisaatio/v4/hae"))
        .withQueryParam("aktiiviset", equalTo("true"))
        .withQueryParam("suunnitellut", equalTo("false"))
        .withQueryParam("lakkautetut", equalTo("false"))
        .withQueryParam("yritysmuoto", equalTo("yritysmuoto_01"))
        .withQueryParam("kunta", equalTo("kunta_091"))
        .willReturn(okJson("{\"organisaatiot\":[]}")));

    organisaatioClient.getV4ByYtunnus("1234567-8");
    organisaatioClient.getV4ByYtunnusFromYtj("1234567-8");
    organisaatioClient.getV4ByOid("1.2.246.562.10.1");
    organisaatioClient.create(OrganisaatioV4Dto.of("1234567-8", "Uusi organisaatio"));
    OrganisaatioV4Dto existing = OrganisaatioV4Dto.of("1234567-8", "Olemassa oleva organisaatio");
    existing.oid = "1.2.246.562.10.1";
    organisaatioClient.update(existing);
    organisaatioClient.listBy(criteria());

    virkailija.verify(getRequestedFor(urlEqualTo("/organisaatio-service/rest/organisaatio/v4/1234567-8")));
    virkailija.verify(getRequestedFor(urlEqualTo("/organisaatio-service/rest/ytj/1234567-8/v4")));
    virkailija.verify(getRequestedFor(urlEqualTo("/organisaatio-service/rest/organisaatio/v4/1.2.246.562.10.1")));
    virkailija.verify(postRequestedFor(urlEqualTo("/organisaatio-service/rest/organisaatio/v4")));
    virkailija.verify(postRequestedFor(urlEqualTo("/organisaatio-service/rest/organisaatio/v4/1.2.246.562.10.1")));
    virkailija.verify(getRequestedFor(urlPathEqualTo("/organisaatio-service/rest/organisaatio/v4/hae"))
        .withQueryParam("aktiiviset", equalTo("true"))
        .withQueryParam("suunnitellut", equalTo("false"))
        .withQueryParam("lakkautetut", equalTo("false"))
        .withQueryParam("yritysmuoto", equalTo("yritysmuoto_01"))
        .withQueryParam("kunta", equalTo("kunta_091")));
  }

  private void stubOauthToken() {
    virkailija.stubFor(post(urlEqualTo("/oauth2/token"))
        .willReturn(okJson("{\"access_token\":\"test-token\",\"token_type\":\"Bearer\",\"expires_in\":3600}")));
  }

  private static OrganisaatioCriteria criteria() {
    OrganisaatioCriteria criteria = new OrganisaatioCriteria();
    criteria.aktiiviset = true;
    criteria.yritysmuoto = List.of("yritysmuoto_01");
    criteria.kunta = List.of("kunta_091");
    return criteria;
  }

  private static RekisterointiDto rekisterointiDto() {
    Organisaatio organisaatio = Organisaatio.of(
        "1234567-8",
        null,
        LocalDate.of(2026, 1, 1),
        KielistettyNimi.of("Testiorganisaatio", "fi", LocalDate.of(2026, 1, 1)),
        "yritysmuoto_01",
        Set.of("organisaatiotyyppi_01"),
        "kunta_091",
        "maatjavaltiot1_fin",
        Set.of("oppilaitoksenopetuskieli_1#1"),
        Yhteystiedot.of("0401234567", "testi@example.com", null, null),
        false);
    Kayttaja kayttaja = Kayttaja.of("Testi", "Kayttaja", "testi@example.com", "fi", null);
    return RekisterointiDto.of(organisaatio, "organisaatiotyyppi_01", Set.of("testi@example.com"), kayttaja);
  }
}

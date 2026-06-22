package fi.vm.sade.rekisterointi.client;

import fi.vm.sade.rekisterointi.model.OrganisaatioCriteria;
import fi.vm.sade.rekisterointi.model.OrganisaatioV4Dto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.Collection;
import java.util.Optional;

/**
 * Client organisaatiopalvelun käyttämiseen.
 */
@Component
public class OrganisaatioClient {

  private static final String KUNTA_YRITYSMUOTO = "Kunta";
  private final OtuvaOauth2Client httpClient;
  private final JsonMapper jsonMapper;
  private final String urlVirkailija;

  public OrganisaatioClient(OtuvaOauth2Client httpClient,
      JsonMapper jsonMapper,
      @Value("${url-virkailija}") String urlVirkailija) {
    this.httpClient = httpClient;
    this.jsonMapper = jsonMapper;
    this.urlVirkailija = urlVirkailija;
  }

  private String toJson(Object object) {
    return jsonMapper.writeValueAsString(object);
  }

  private <T> T fromJson(String json, Class<T> type) {
    return jsonMapper.readValue(json, type);
  }

  /**
   * Hakee organisaation y-tunnuksella organisaatiopalvelusta.
   *
   * @param ytunnus y-tunnus
   *
   * @return organisaatio, tai <code>empty</code> mikäli ei löydy.
   */
  public Optional<OrganisaatioV4Dto> getV4ByYtunnus(String ytunnus) {
    String url = UriComponentsBuilder.fromUriString(
        urlVirkailija + "/organisaatio-service/rest/organisaatio/v4/{ytunnus}")
        .encode()
        .buildAndExpand(ytunnus)
        .toUriString();
    return getV4FromUrl(url);
  }

  /**
   * Hakee organisaation y-tunnuksella YTJ:stä.
   *
   * @param ytunnus y-tunnus
   *
   * @return organisaatio, tai <code>empty</code> mikäli ei löydy.
   */
  public Optional<OrganisaatioV4Dto> getV4ByYtunnusFromYtj(String ytunnus) {
    String url = UriComponentsBuilder.fromUriString(
        urlVirkailija + "/organisaatio-service/rest/ytj/{ytunnus}/v4")
        .encode()
        .buildAndExpand(ytunnus)
        .toUriString();
    return getV4FromUrl(url);
  }

  /**
   * Hakee organisaation OID:illa organisaatiopalvelusta.
   *
   * @param oid organisaation OID
   *
   * @return organisaatio, tai <code>empty</code> mikäli ei löydy.
   */
  public Optional<OrganisaatioV4Dto> getV4ByOid(String oid) {
    String url = UriComponentsBuilder.fromUriString(
        urlVirkailija + "/organisaatio-service/rest/organisaatio/v4/{oid}")
        .encode()
        .buildAndExpand(oid)
        .toUriString();
    return getV4FromUrl(url);
  }

  private  Optional<OrganisaatioV4Dto> getV4FromUrl(String url) {
    try {
      var request = HttpRequest.newBuilder().uri(new URI(url)).GET();
      var response = httpClient.executeRequest(request);
      if (response.statusCode() == 200) {
        return Optional.of(fromJson(response.body(), OrganisaatioV4Dto.class));
      } else {
        return Optional.empty();
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Tallentaa organisaation organisaatiopalveluun. Olemassa olevan organisaation
   * kohdalla tallentaa muutokset, muutoin luo uuden.
   *
   * @param organisaatio tallennettava organisaatio tai muutos
   *
   * @return tallennettu/luotu organisaatio.
   *
   * @see #create(OrganisaatioV4Dto)
   * @see #update(OrganisaatioV4Dto)
   */
  public OrganisaatioV4Dto save(OrganisaatioV4Dto organisaatio) {
    if (organisaatio.oid == null) {
      return create(organisaatio);
    } else {
      return update(organisaatio);
    }
  }

  /**
   * Luo uuden organisaation organisaatiopalveluun.
   *
   * @param organisaatio luotava organisaatio
   *
   * @return luotu organisaatio
   */
  public OrganisaatioV4Dto create(OrganisaatioV4Dto organisaatio) {
    assert organisaatio.oid == null;
    return postOrganisaatio(urlVirkailija + "/organisaatio-service/rest/organisaatio/v4", organisaatio);
  }

  /**
   * Päivittää organisaation organisaatiopalveluun.
   *
   * @param organisaatio tallennettava organisaatio
   *
   * @return tallennettu organisaatio.
   */
  public OrganisaatioV4Dto update(OrganisaatioV4Dto organisaatio) {
    assert organisaatio.oid != null;
    String url = UriComponentsBuilder.fromUriString(
        urlVirkailija + "/organisaatio-service/rest/organisaatio/v4/{oid}")
        .encode()
        .buildAndExpand(organisaatio.oid)
        .toUriString();
    return postOrganisaatio(url, organisaatio);
  }

  public OrganisaatioV4Dto postOrganisaatio(String url, OrganisaatioV4Dto organisaatio) {
    try {
      var request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Content-Type", "application/json")
          .POST(BodyPublishers.ofString(toJson(organisaatio)));
      var response = httpClient.executeRequest(request);
      if (response.statusCode() == 200) {
        return fromJson(response.body(), OrganisaatioResultDto.class).organisaatio;
      } else {
        throw new RuntimeException(String.format("Url %s returned 204 or 404", url));
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Listaa organisaatiot hakuehdoilla organisaatiopalvelusta.
   *
   * @param criteria hakuehdot
   *
   * @return lista ehtoihin täsmäävistä organisaatioista.
   */
  public Collection<OrganisaatioV4Dto> listBy(OrganisaatioCriteria criteria) {
    var builder = UriComponentsBuilder.fromUriString(
        urlVirkailija + "/organisaatio-service/rest/organisaatio/v4/hae");
    criteria.asMap().forEach((name, value) -> {
      if (value instanceof Collection<?> values) {
        values.forEach(queryValue -> builder.queryParam(name, queryValue));
      } else {
        builder.queryParam(name, value);
      }
    });
    String url = builder.build().encode().toUriString();
    try {
      var request = HttpRequest.newBuilder().uri(new URI(url)).GET();
      var response = httpClient.executeRequest(request);
      if (response.statusCode() == 200) {
        return fromJson(response.body(), OrganisaatioListDto.class).organisaatiot;
      } else {
        throw new RuntimeException(String.format("Url %s returned 204 or 404", url));
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Lataan kunnan OID:illa organisaatiopalvelusta.
   *
   * @param oid kunnan OID
   *
   * @return kunnan tiedot, tai <code>empty</code> mikäli ei löydy.
   */
  public Optional<OrganisaatioV4Dto> getKuntaByOid(String oid) {
    return getV4ByOid(oid).filter(organisaatioV4Dto -> KUNTA_YRITYSMUOTO.equals(organisaatioV4Dto.yritysmuoto));
  }

  private static class OrganisaatioListDto {
    public Collection<OrganisaatioV4Dto> organisaatiot;
  }

  private static class OrganisaatioResultDto {
    public OrganisaatioV4Dto organisaatio;
  }
}

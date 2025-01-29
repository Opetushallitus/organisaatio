package fi.vm.sade.rekisterointi.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpEntity;
import fi.vm.sade.javautils.http.OphHttpRequest;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.rekisterointi.model.OrganisaatioCriteria;
import fi.vm.sade.rekisterointi.model.OrganisaatioV4Dto;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

/**
 * Client organisaatiopalvelun käyttämiseen.
 */
@Component
public class OrganisaatioClient {

  private static final String KUNTA_YRITYSMUOTO = "Kunta";
  private final OphHttpClient httpClient;
  private final OphProperties properties;
  private final ObjectMapper objectMapper;

  /**
   * Alustaa clientin annetulla HTTP-clientillä, konfiguraatiolla ja
   * <code>ObjectMapper</code>illä.
   *
   * @param httpClient   HTTP-client
   * @param properties   konfiguraatio
   * @param objectMapper Jackson object mapper
   */
  public OrganisaatioClient(@Qualifier("httpClientOrganisaatio") OphHttpClient httpClient,
      OphProperties properties,
      ObjectMapper objectMapper) {
    this.httpClient = httpClient;
    this.properties = properties;
    this.objectMapper = objectMapper;
  }

  private String toJson(Object object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException ex) {
      throw new RuntimeException(ex);
    }
  }

  private <T> T fromJson(String json, Class<T> type) {
    try {
      return objectMapper.readValue(json, type);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Hakee organisaation y-tunnuksella organisaatiopalvelusta.
   *
   * @param ytunnus y-tunnus
   *
   * @return organisaatio, tai <code>empty</code> mikäli ei löydy.
   */
  public Optional<OrganisaatioV4Dto> getV4ByYtunnus(String ytunnus) {
    String url = properties.url("organisaatio-service.organisaatio.v4.byYtunnus", ytunnus);
    OphHttpRequest request = OphHttpRequest.Builder.get(url).build();
    return httpClient.<OrganisaatioV4Dto>execute(request)
        .expectedStatus(200)
        .mapWith(json -> fromJson(json, OrganisaatioV4Dto.class));
  }

  /**
   * Hakee organisaation y-tunnuksella YTJ:stä.
   *
   * @param ytunnus y-tunnus
   *
   * @return organisaatio, tai <code>empty</code> mikäli ei löydy.
   */
  public Optional<OrganisaatioV4Dto> getV4ByYtunnusFromYtj(String ytunnus) {
    String url = properties.url("organisaatio-service.vtj.ytunnus", ytunnus);
    OphHttpRequest request = OphHttpRequest.Builder.get(url).build();
    return httpClient.<OrganisaatioV4Dto>execute(request)
        .expectedStatus(200)
        .mapWith(json -> fromJson(json, OrganisaatioV4Dto.class));
  }

  /**
   * Hakee organisaation OID:illa organisaatiopalvelusta.
   *
   * @param oid organisaation OID
   *
   * @return organisaatio, tai <code>empty</code> mikäli ei löydy.
   */
  public Optional<OrganisaatioV4Dto> getV4ByOid(String oid) {
    String url = properties.url("organisaatio-service.organisaatio.v4.byOid", oid);
    OphHttpRequest request = OphHttpRequest.Builder.get(url).build();
    return httpClient.<OrganisaatioV4Dto>execute(request)
        .expectedStatus(200)
        .mapWith(json -> fromJson(json, OrganisaatioV4Dto.class));
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
    String url = properties.url("organisaatio-service.organisaatio.v4", organisaatio.oid);
    OphHttpEntity entity = new OphHttpEntity.Builder()
        .content(toJson(organisaatio))
        .contentType(ContentType.APPLICATION_JSON)
        .build();
    OphHttpRequest request = OphHttpRequest.Builder
        .post(url)
        .setEntity(entity)
        .build();
    return httpClient.<OrganisaatioV4Dto>execute(request)
        .expectedStatus(200)
        .mapWith(json -> fromJson(json, OrganisaatioResultDto.class).organisaatio)
        .orElseThrow(() -> new RuntimeException(String.format("Url %s returned 204 or 404", url)));
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
    String url = properties.url("organisaatio-service.organisaatio.v4.byOid", organisaatio.oid);
    OphHttpEntity entity = new OphHttpEntity.Builder()
        .content(toJson(organisaatio))
        .contentType(ContentType.APPLICATION_JSON)
        .build();
    OphHttpRequest request = OphHttpRequest.Builder
        .put(url)
        .setEntity(entity)
        .build();
    return httpClient.<OrganisaatioV4Dto>execute(request)
        .expectedStatus(200)
        .mapWith(json -> fromJson(json, OrganisaatioResultDto.class).organisaatio)
        .orElseThrow(() -> new RuntimeException(String.format("Url %s returned 204 or 404", url)));
  }

  /**
   * Listaa organisaatiot hakuehdoilla organisaatiopalvelusta.
   *
   * @param criteria hakuehdot
   *
   * @return lista ehtoihin täsmäävistä organisaatioista.
   */
  public Collection<OrganisaatioV4Dto> listBy(OrganisaatioCriteria criteria) {
    String url = properties.url("organisaatio-service.organisaatio.v4.hae", criteria.asMap());
    OphHttpRequest request = OphHttpRequest.Builder.get(url).build();
    return httpClient.<Collection<OrganisaatioV4Dto>>execute(request)
        .expectedStatus(200)
        .mapWith(json -> fromJson(json, OrganisaatioListDto.class).organisaatiot)
        .orElseThrow(() -> new RuntimeException(String.format("Url %s returned 204 or 404", url)));
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

package fi.vm.sade.rekisterointi.client;

import fi.vm.sade.rekisterointi.model.BaseDto;
import fi.vm.sade.rekisterointi.model.Koodi;
import fi.vm.sade.rekisterointi.model.KoodistoType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.function.Function;

import static fi.vm.sade.rekisterointi.util.Constants.CALLER_ID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Client koodistopalvelun käyttämiseen.
 */
@Component
public class KoodistoClient {

  private final HttpClient httpClient;
  private final String urlVirkailija;
  private final JsonMapper jsonMapper;

  /**
   * Alustaa clientin annetulla HTTP-clientillä, konfiguraatiolla ja
   * <code>JsonMapper</code>illa.
   *
   * @param httpClient   HTTP-client
   * @param urlVirkailija virkailijan palveluiden base URL
   * @param jsonMapper Jackson JSON mapper
   */
  public KoodistoClient(HttpClient httpClient,
      @Value("${url-virkailija}") String urlVirkailija,
      JsonMapper jsonMapper) {
    this.httpClient = httpClient;
    this.urlVirkailija = urlVirkailija;
    this.jsonMapper = jsonMapper;
  }

  /**
   * Listaa annetun koodiston koodit.
   *
   * @param koodisto haluttu koodisto
   *
   * @return koodiston koodit.
   */
  public Collection<Koodi> listKoodit(KoodistoType koodisto) {
    return listKoodit(koodisto, Optional.empty(), Optional.empty());
  }

  /**
   * Lista annetun koodiston koodit.
   *
   * @param koodisto  haluttu koodisto
   * @param versio    mikäli annettu, haetaan vain halutun version koodit
   * @param onlyValid sisällytetäänkö vain voimassaolevat koodit?
   *
   * @return koodiston koodit.
   */
  public Collection<Koodi> listKoodit(KoodistoType koodisto, Optional<Integer> versio, Optional<Boolean> onlyValid) {
    Map<String, Object> parameters = new LinkedHashMap<>();
    versio.ifPresent(value -> parameters.put("koodistoVersio", value));
    onlyValid.ifPresent(value -> parameters.put("onlyValidKoodis", value));
    var builder = UriComponentsBuilder.fromUriString(
        urlVirkailija + "/koodisto-service/rest/json/{koodisto}/koodi");
    parameters.forEach((name, value) -> builder.queryParam(name, value));
    String url = builder.encode().buildAndExpand(koodisto.uri).toUriString();
    return listKoodit(url);
  }

  private Collection<Koodi> listKoodit(String url) {
    KoodiDto[] koodit = get(url);
    return Arrays.stream(koodit).map(KoodistoClient::dtoToKoodi).collect(toList());
  }

  private KoodiDto[] get(String url) {
    try {
      var request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Caller-Id", CALLER_ID)
          .GET()
          .build();
      var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 200) {
        throw new RuntimeException(
            "Url " + url + " returned status code " + response.statusCode() + ": " + response.body());
      }
      return jsonMapper.readValue(response.body(), KoodiDto[].class);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(ex);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  private static Koodi dtoToKoodi(KoodiDto dto) {
    Koodi koodi = new Koodi();
    koodi.uri = dto.koodiUri;
    koodi.arvo = dto.koodiArvo;
    koodi.nimi = metadataTo(dto.metadata, metadata -> metadata.nimi);
    return koodi;
  }

  private static Map<String, String> metadataTo(List<KoodiMetadataDto> metadataList,
      Function<KoodiMetadataDto, String> valueProvider) {
    return metadataList.stream()
        .filter(metadata -> metadata != null && isNotEmpty(metadata.kieli) && isNotEmpty(valueProvider.apply(metadata)))
        .collect(toMap(metadata -> metadata.kieli.toLowerCase(), valueProvider));
  }

  private static boolean isNotEmpty(String str) {
    return str != null && !str.isEmpty();
  }

  private static class KoodiDto extends BaseDto {
    public String koodiUri;
    public String koodiArvo;
    public List<KoodiMetadataDto> metadata;
  }

  private static class KoodiMetadataDto extends BaseDto {
    public String kieli;
    public String nimi;
  }

}

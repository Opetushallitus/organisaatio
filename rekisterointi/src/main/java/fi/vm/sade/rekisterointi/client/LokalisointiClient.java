package fi.vm.sade.rekisterointi.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static fi.vm.sade.rekisterointi.util.Constants.CALLER_ID;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

/**
 * Client lokalisointipalvelun käyttämiseen.
 */
@Component
public class LokalisointiClient {

  private final HttpClient httpClient;
  private final String urlVirkailija;
  private final JsonMapper jsonMapper;

  @Value("${virkailija.override:null}")
  private String virkailijaOverride;

  /**
   * Alusta clientin annetulla HTTP-clientilla, konfiguraatiolla ja
   * <code>JsonMapper</code>illä.
   *
   * @param httpClient   HTTP-client
   * @param urlVirkailija virkailijan palveluiden base URL
   * @param jsonMapper Jackson JSON mapper
   */
  public LokalisointiClient(HttpClient httpClient,
      @Value("${url-virkailija}") String urlVirkailija,
      JsonMapper jsonMapper) {
    this.httpClient = httpClient;
    this.urlVirkailija = urlVirkailija;
    this.jsonMapper = jsonMapper;
  }

  /**
   * Hakee annetun kategorian lokalisaatiot.
   *
   * @param category haluttu kategoria.
   *
   * @return lokalisaatiot sisäkkäisinä <code>Map</code>peinä: lokaali -&gt; avain
   *         -&gt; arvo
   */
  public Map<String, Map<String, String>> getByCategory(String category) {
    var host = virkailijaOverride != null && virkailijaOverride.startsWith("http")
      ? virkailijaOverride
      : urlVirkailija;
    var url = UriComponentsBuilder.fromUriString(host)
          .path("/lokalisointi/cxf/rest/v1/localisation")
          .queryParam("category", "{category}")
          .encode()
          .buildAndExpand(category)
          .toUriString();
    return getByUrl(url);
  }

  private Map<String, Map<String, String>> getByUrl(String url) {
    return Arrays.stream(getAsArray(url))
        .collect(
            groupingBy(dto -> dto.locale, mapFactory(), mapping(identity(), toMap(dto -> dto.key, dto -> dto.value))));
  }

  private static Supplier<Map<String, Map<String, String>>> mapFactory() {
    return () -> {
      Map<String, Map<String, String>> map = new LinkedHashMap<>();
      map.put("fi", new LinkedHashMap<>());
      map.put("sv", new LinkedHashMap<>());
      map.put("en", new LinkedHashMap<>());
      return map;
    };
  }

  private Dto[] getAsArray(String url) {
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
      return jsonMapper.readValue(response.body(), Dto[].class);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(ex);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  private static class Dto {

    public String locale;
    public String key;
    public String value;

  }

}

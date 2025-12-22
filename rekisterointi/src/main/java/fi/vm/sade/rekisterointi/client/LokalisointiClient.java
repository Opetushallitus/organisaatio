package fi.vm.sade.rekisterointi.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.javautils.httpclient.OphHttpClient;
import fi.vm.sade.properties.OphProperties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

/**
 * Client lokalisointipalvelun käyttämiseen.
 */
@Component
public class LokalisointiClient {

  private final OphHttpClient httpClient;
  private final OphProperties properties;
  private final ObjectMapper objectMapper;

  @Value("${lokalisointi.override:null}")
  private String urlOverride;

  /**
   * Alusta clientin annetulla HTTP-clientilla, konfiguraatiolla ja
   * <code>ObjectMapper</code>illä.
   *
   * @param httpClient   HTTP-client
   * @param properties   konfiguraatio
   * @param objectMapper Jackson object mapper
   */
  public LokalisointiClient(OphHttpClient httpClient, OphProperties properties, ObjectMapper objectMapper) {
    this.httpClient = httpClient;
    this.properties = properties;
    this.objectMapper = objectMapper;
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
    var url = urlOverride != null && urlOverride.startsWith("http")
      ? urlOverride
      : properties.url("lokalisointi.v1.listByCategory", category);
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
    return httpClient.get(url).execute(response -> objectMapper.readValue(response.asInputStream(), Dto[].class));
  }

  private static class Dto {

    public String locale;
    public String key;
    public String value;

  }

}

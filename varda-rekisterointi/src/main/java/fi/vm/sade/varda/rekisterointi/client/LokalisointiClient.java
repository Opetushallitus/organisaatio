package fi.vm.sade.varda.rekisterointi.client;

import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static fi.vm.sade.varda.rekisterointi.util.Constants.CALLER_ID;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

/**
 * Client lokalisointipalvelun käyttämiseen.
 */
@Component
public class LokalisointiClient {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(35);

    private final HttpClient httpClient;
    private final String virkailijaUrl;
    private final ObjectMapper objectMapper;

    /**
     * Alusta clientin annetulla HTTP-clientilla, konfiguraatiolla ja <code>ObjectMapper</code>illä.
     *
     * @param httpClient    HTTP-client
     * @param virkailijaUrl virkailijan palveluosoite
     * @param objectMapper  Jackson object mapper
     */
    public LokalisointiClient(HttpClient httpClient,
                              @Value("${varda-rekisterointi.url-virkailija}") String virkailijaUrl,
                              ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.virkailijaUrl = virkailijaUrl;
        this.objectMapper = objectMapper;
    }

    public String getKutsujaForKutsuEmail(String kutsujaKey, String locale) {
        String url = virkailijaUrl + "/lokalisointi/cxf/rest/v1/localisation"
                + "?category=varda-rekisterointi&key=" + kutsujaKey + "&locale=" + locale;
        return objectMapper.readTree(get(url)).get(0).get("value").asString();

    }

    /**
     * Hakee annetun kategorian lokalisaatiot.
     *
     * @param category  haluttu kategoria.
     *
     * @return lokalisaatiot sisäkkäisinä <code>Map</code>peinä: lokaali -&gt; avain -&gt; arvo
     */
    public Map<String, Map<String, String>> getByCategory(String category) {
        return getByUrl(virkailijaUrl + "/lokalisointi/cxf/rest/v1/localisation?category=" + category);
    }

    private Map<String, Map<String, String>> getByUrl(String url) {
        return Arrays.stream(getAsArray(url))
                .collect(groupingBy(dto -> dto.locale, mapFactory(), mapping(identity(), toMap(dto -> dto.key, dto -> dto.value))));
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
        return objectMapper.readValue(get(url), Dto[].class);
    }

    private String get(String url) {
        try {
            var request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Caller-Id", CALLER_ID)
                    .GET()
                    .build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException(String.format("Url %s returned status %d", url, response.statusCode()));
            }
            return response.body();
        } catch (IOException e) {
            throw new RuntimeException(String.format("Error while executing GET %s", url), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(String.format("Interrupted while executing GET %s", url), e);
        }
    }

    private static class Dto {

        public String locale;
        public String key;
        public String value;

    }

}

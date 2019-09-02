package fi.vm.sade.varda.rekisterointi.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.javautils.httpclient.OphHttpClient;
import fi.vm.sade.properties.OphProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

@Component
public class LokalisointiClient {

    private final OphHttpClient httpClient;
    private final OphProperties properties;
    private final ObjectMapper objectMapper;

    public LokalisointiClient(OphHttpClient httpClient, OphProperties properties, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    // locale -> key -> value
    public Map<String, Map<String, String>> getByCategory(String category) {
        return getByUrl(properties.url("lokalisointi.v1.listByCategory", category));
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
        return httpClient.get(url).execute(response -> objectMapper.readValue(response.asInputStream(), Dto[].class));
    }

    private static class Dto {

        public String locale;
        public String key;
        public String value;

    }

}

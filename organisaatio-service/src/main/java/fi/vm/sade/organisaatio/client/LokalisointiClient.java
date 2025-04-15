package fi.vm.sade.organisaatio.client;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.vm.sade.properties.OphProperties;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

import java.net.URI;
import java.net.http.HttpRequest;

@Component
@RequiredArgsConstructor
public class LokalisointiClient {
    private final OtuvaOauth2Client httpClient;
    private final OphProperties properties;
    private final ObjectMapper objectMapper;

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
        try {
            var request = HttpRequest.newBuilder().uri(new URI(url)).GET();
            var response = httpClient.executeRequest(request);
            return objectMapper.readValue(response.body(), Dto[].class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class Dto {

        public String locale;
        public String key;
        public String value;

    }

}

package fi.vm.sade.organisaatio.client.viestinvalitys;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.List;

@Slf4j
public class ViestinvalitysClient extends CasAuthenticatedServiceClient {
    private final ObjectMapper mapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    public ViestinvalitysClient(HttpClient httpClient, CasClient casClient, String baseUrl) {
        super(httpClient, casClient, baseUrl);
        log.info("Creating OppijanumerorekisteriClient");
    }

    public ApiResponse luoLahetys(LuoLahetysRequest requestBody) throws IOException, InterruptedException {
        return post("/lahetys/v1/lahetykset", mapper.writeValueAsString(requestBody));
    }

    public ApiResponse post(String path, String requestBody) throws IOException, InterruptedException {
        log.info("Doing request to {} with body {}", path, requestBody);
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serviceUrl + path))
                .method("POST", HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", "application/json");
        var response = sendRequest(request);
        return new ApiResponse(response.statusCode(), response.body());
    }

    @Builder
    @Data
    public static class LuoLahetysRequest {
        public String otsikko;
        public List<String> kayttooikeusRajoitukset;
    }

    @Data
    public static class LuoLahetysSuccessResponse {
        private String lahetysTunniste;
    }

    @Data
    public static class LuoLahetysFailureResponse {
        private List<String> validointiVirheet;
    }

    @Data
    public static class ApiResponse {
        private final Integer status;
        private final String body;
    }
}

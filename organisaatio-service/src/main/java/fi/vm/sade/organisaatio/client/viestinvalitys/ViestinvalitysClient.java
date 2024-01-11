package fi.vm.sade.organisaatio.client.viestinvalitys;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

@Slf4j
public class ViestinvalitysClient extends CasAuthenticatedServiceClient {
    private final ObjectMapper mapper;

    public ViestinvalitysClient(HttpClient httpClient, CasClient casClient, String baseUrl, ObjectMapper objectMapper) {
        super(httpClient, casClient, baseUrl);
        this.mapper = objectMapper;
    }

    public LuoViestiSuccessResponse luoViesti(Viesti viesti) {
        try {
            var response = post("/lahetys/v1/viestit", mapper.writeValueAsString(viesti));
            if (response.getStatus() == 200) {
                return mapper.readValue(response.getBody(), LuoViestiSuccessResponse.class);
            } else {
                throw new UnexpectedResponseException(response);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public ApiResponse post(String path, String requestBody) throws IOException, InterruptedException {
        log.info("Doing request to {} with body {}", path, requestBody);
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serviceUrl + path))
                .method("POST", HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
        var response = sendRequest(request);
        return new ApiResponse(response.statusCode(), response.body());
    }
}

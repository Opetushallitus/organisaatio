package fi.vm.sade.varda.rekisterointi.client.viestinvalitys;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

@Slf4j
public class ViestinvalitysClient extends CasAuthenticatedServiceClient {
    private final ObjectMapper mapper;
    private final String baseUrl;

    public ViestinvalitysClient(HttpClient httpClient, CasClient casClient, String baseUrl, ObjectMapper objectMapper) {
        super(httpClient, casClient, baseUrl + "/login");
        this.mapper = objectMapper;
        this.baseUrl = baseUrl;
    }

    public LuoLahetysSuccessResponse luoLahetys(Lahetys lahetys) {
        try {
            var response = post("/v1/lahetykset", mapper.writeValueAsString(lahetys));
            return switch (response.getStatus()) {
                case 200 -> mapper.readValue(response.getBody(), LuoLahetysSuccessResponse.class);
                case 400 -> throw new BadRequestException(response);
                default -> throw new UnexpectedResponseException(response);
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public LuoViestiSuccessResponse luoViesti(Viesti viesti) {
        try {
            var response = post("/v1/viestit", mapper.writeValueAsString(viesti));
            return switch (response.getStatus()) {
                case 200 -> mapper.readValue(response.getBody(), LuoViestiSuccessResponse.class);
                case 400 -> throw new BadRequestException(response);
                default -> throw new UnexpectedResponseException(response);
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public ApiResponse post(String path, String requestBody) throws IOException, InterruptedException {
        log.info("Doing request to {} with body {}", path, requestBody);
        var request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .method("POST", HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
        var response = sendRequest(request);
        return new ApiResponse(response.statusCode(), response.body());
    }
}
package fi.vm.sade.organisaatio.client.viestinvalitys;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;

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

    public PostAttachmentResponse postAttachment(MultipartFile file) {
        try {
            var request = getAttachmentRequestBuilder(file);
            var response = sendRequest(request);
            return switch (response.statusCode()) {
                case 200 -> mapper.readValue(response.body(), PostAttachmentResponse.class);
                case 400 -> throw new BadRequestException(new ApiResponse(response.statusCode(), response.body()));
                default -> throw new UnexpectedResponseException(new ApiResponse(response.statusCode(), response.body()));
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private HttpRequest.Builder getAttachmentRequestBuilder(MultipartFile file) throws IOException {
        HttpEntity httpEntity = MultipartEntityBuilder.create()
                .addBinaryBody("liite", file.getInputStream(), ContentType.create(file.getContentType()), removeUnwantedCharactersFromFilename(file.getOriginalFilename()))
                .build();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        httpEntity.writeTo(os);
        os.flush();
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/liitteet"))
                .header("Content-Type", httpEntity.getContentType().getValue())
                .header("Accept", "application/json")
                .POST(BodyPublishers.ofByteArray(os.toByteArray()));
    }

    private String removeUnwantedCharactersFromFilename(String filename) {
        // Tämä regex mätsää Viestinvälityspalvelun LiiteValidatoriin:
        // https://github.com/Opetushallitus/viestinvalityspalvelu/blob/9ae813853b09756be6eefcad9efb2e4afddb9d98/lambdat/vastaanotto/src/main/scala/fi/oph/viestinvalitys/vastaanotto/validation/LiiteValidator.scala#L28
        var regex = "[^0-9A-Za-z\\s._\\-+]";
        // MultipartFile#getOriginalFilename() voi palauttaa esim. "ä" kirjaimen muodossa "a?",
        // jolloin tämän operaation jälkeen "ääää.pdf" on kätevästi, mutta yllättävästi "aaaa.pdf" :)
        return filename.replaceAll(regex, "");
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

    @Data
    public static class PostAttachmentResponse {
        private String liiteTunniste;
    }
}
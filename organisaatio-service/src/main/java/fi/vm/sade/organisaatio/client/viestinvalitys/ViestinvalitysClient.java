package fi.vm.sade.organisaatio.client.viestinvalitys;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
public class ViestinvalitysClient extends CasAuthenticatedServiceClient {
    private final ObjectMapper mapper;

    public ViestinvalitysClient(HttpClient httpClient, CasClient casClient, String baseUrl, ObjectMapper objectMapper) {
        super(httpClient, casClient, baseUrl);
        this.mapper = objectMapper;
    }

    public LuoViestiSuccessResponse luoViesti(Viesti viesti) {
        try {
            var response = post("/v1/viestit", mapper.writeValueAsString(viesti));
            if (response.getStatus() == 200) {
                return mapper.readValue(response.getBody(), LuoViestiSuccessResponse.class);
            } else {
                throw new UnexpectedResponseException(response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public PostAttachmentResponse postAttachment(MultipartFile file) {
        try {
            var request = getMultipartFileRequestBuilder(file);
            var response = sendRequest(request);
            if (response.statusCode() == 200) {
                return mapper.readValue(response.body(), PostAttachmentResponse.class);
            } else {
                throw new UnexpectedResponseException(new ApiResponse(response.statusCode(), response.body()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private HttpRequest.Builder getMultipartFileRequestBuilder(MultipartFile file) throws IOException {
        HttpEntity httpEntity = MultipartEntityBuilder.create()
            .addBinaryBody("liite", file.getInputStream(), ContentType.create(file.getContentType()), file.getOriginalFilename())
            .build();
        Pipe pipe = Pipe.open();
        new Thread(() -> {
            try (OutputStream outputStream = Channels.newOutputStream(pipe.sink())) {
                httpEntity.writeTo(outputStream);
            } catch (IOException e) {
                log.error("Error while writing attachment to stream", e);
            }
        }).start();
        return HttpRequest.newBuilder()
                .uri(URI.create(serviceUrl + "/v1/liitteet"))
                .header("Content-Type", httpEntity.getContentType().getValue())
                .header("Accept", "application/json")
                .POST(BodyPublishers.ofInputStream(() -> Channels.newInputStream(pipe.source())));
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

    @Data
    public static class PostAttachmentResponse {
        private String liiteTunniste;
    }
}
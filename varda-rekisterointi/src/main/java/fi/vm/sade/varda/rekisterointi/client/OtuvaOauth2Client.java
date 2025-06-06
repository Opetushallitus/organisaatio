package fi.vm.sade.varda.rekisterointi.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import fi.vm.sade.varda.rekisterointi.util.Constants;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtuvaOauth2Client {
    private final Oauth2BearerClient oauth2BearerClient;
    private final HttpClient http = HttpClient.newBuilder().build();

    private HttpResponse<String> execute(HttpRequest.Builder requestBuilder) {
        try {
            var request = requestBuilder
                    .timeout(Duration.ofSeconds(35))
                    .setHeader("Authorization", "Bearer " + oauth2BearerClient.getOauth2Bearer())
                    .setHeader("Caller-Id", Constants.CALLER_ID)
                    .setHeader("CSRF", "CSRF")
                    .setHeader("Cookie", "CSRF=CSRF")
                    .build();
            var response = http.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("{} {} {} {}", response.statusCode(), response.request().method(), response.request().uri(), response.body());
            return response;
        } catch (IOException|InterruptedException e) {
            throw new RestClientException("error while executing request", e);
        }
    }

    public HttpResponse<String> executeRequest(HttpRequest.Builder requestBuilder) throws RestClientException {
        HttpResponse<String> res = execute(requestBuilder);
        if (res.statusCode() == 401) {
            log.info("received WWW-authenticate header: " + res.headers().firstValue("WWW-Authenticate"));
            var authHeader = res.headers().firstValue("WWW-Authenticate");
            if (authHeader.orElse("").contains("invalid_token")) {
                oauth2BearerClient.evictOauth2Bearer();
                return execute(requestBuilder);
            }
        }
        return res;
    }
}

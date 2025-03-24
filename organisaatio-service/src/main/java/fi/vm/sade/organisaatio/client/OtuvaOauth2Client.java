package fi.vm.sade.organisaatio.client;

import fi.vm.sade.organisaatio.config.HttpClientConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

import static java.util.function.Function.identity;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(Oauth2BearerClient.class)
public class OtuvaOauth2Client {
    private final Oauth2BearerClient oauth2BearerClient;
    private final HttpClient http = HttpClient.newBuilder().build();

    private HttpResponse<String> execute(HttpRequest.Builder requestBuilder) {
        try {
            var request = requestBuilder
                    .timeout(Duration.ofSeconds(35))
                    .setHeader("Authorization", "Bearer " + oauth2BearerClient.getOauth2Bearer())
                    .setHeader("Caller-Id", HttpClientConfiguration.CALLER_ID)
                    .setHeader("CSRF", "CSRF")
                    .setHeader("Cookie", "CSRF=CSRF")
                    .build();
            return http.send(request, HttpResponse.BodyHandlers.ofString());
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

package fi.vm.sade.organisaatio.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty("organisaatio.palvelukayttaja.client_id")
public class Oauth2BearerClient {
    private static final String CACHE_NAME_OAUTH2_BEARER = "oauth2Bearer";
    private final ObjectMapper objectMapper;

    @Value("${organisaatio.palvelukayttaja.client_id}")
    private String clientId;
    @Value("${organisaatio.palvelukayttaja.client_secret}")
    private String clientSecret;
    @Value("${otuva.jwt.issuer-uri}")
    private String oauth2IssuerUri;

    private String cachedToken = null;

    @Cacheable(value = CACHE_NAME_OAUTH2_BEARER, sync = true)
    public String getOauth2Bearer() throws IOException, InterruptedException {
        if (cachedToken != null) return cachedToken;
        String tokenUrl = oauth2IssuerUri + "/oauth2/token";
        log.info("refetching oauth2 bearer from " + tokenUrl);
        String body = "grant_type=client_credentials&client_id="
                      + clientId
                      + "&client_secret="
                      + clientSecret;
        var request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        var client = HttpClient.newHttpClient();
        HttpResponse<String> res = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new RuntimeException("Oauth2 bearer returned status code " + res.statusCode() + ": " + res.body());
        }
        log.debug("oauth2 bearer body: " + res.body());
        var newToken = objectMapper.readValue(res.body(), Token.class).access_token();
        cachedToken = newToken;
        return newToken;
    }

    @CacheEvict(value = CACHE_NAME_OAUTH2_BEARER, allEntries = true)
    public void evictOauth2Bearer() {
        log.info("evicting oauth2 bearer cache");
        cachedToken = null;
    }

    public record Token(String access_token, String token_type, Integer expires_in) {}
}

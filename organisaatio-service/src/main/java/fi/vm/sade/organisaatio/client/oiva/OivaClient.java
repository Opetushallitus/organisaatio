package fi.vm.sade.organisaatio.client.oiva;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OivaClient {
    @Value("${oiva.baseurl}")
    private String oivaBaseurl;
    @Value("${oiva.username}")
    private String oivaUsername;
    @Value("${oiva.password}")
    private String oivaPassword;

    private final ObjectMapper objectMapper;

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .authenticator(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(oivaUsername, oivaPassword.toCharArray());
                }
            })
            .build();

    @SneakyThrows
    public List<Koulutuslupa> getKoulutusluvat() {
        HttpRequest request = HttpRequest.newBuilder(URI.create(oivaBaseurl + "/api/export/koulutusluvat"))
                .GET()
                .timeout(Duration.ofSeconds(60))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("{} {} {}", request.method(), request.uri(), response.statusCode());

        if (response.statusCode() != 200) {
            log.info("Unexpected status code from Oiva: {} {}", response.statusCode(), response.body());
            throw new RuntimeException(String.format("Unexpected status code from Oiva: %d", response.statusCode()));
        }

        return objectMapper.readValue(response.body(), new TypeReference<>() {
        });
    }
}
package fi.vm.sade.organisaatio.client.viestinvalitys;

import tools.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.CookieManager;
import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
@Slf4j
public class ViestinvalitysConfig {
    @Value("${viestinvalitys.baseurl}")
    private String viestinvalitysUrl;
    @Value("${organisaatio.service.username}")
    private String username;
    @Value("${organisaatio.service.password}")
    private String password;
    @Value("${cas.base}")
    private String casBase;

    @Bean
    public ViestinvalitysClient viestinvalitysClient(ObjectMapper objectMapper) {
        var httpClient = HttpClient.newBuilder()
                .cookieHandler(new CookieManager())
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        var casClient = new CasClient(httpClient, casBase, username, password);

        return new ViestinvalitysClient(httpClient, casClient, viestinvalitysUrl, objectMapper);
    }
}


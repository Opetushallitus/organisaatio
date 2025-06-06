package fi.vm.sade.varda.rekisterointi.client.viestinvalitys;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.properties.OphProperties;
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
    @Value("${varda-rekisterointi.service.username}")
    private String username;
    @Value("${varda-rekisterointi.service.password}")
    private String password;

    @Bean
    public ViestinvalitysClient viestinvalitysClient(OphProperties properties, ObjectMapper objectMapper) {
        var casBase = properties.require("cas.base");

        var httpClient = HttpClient.newBuilder()
                .cookieHandler(new CookieManager())
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        var casClient = new CasClient(httpClient, casBase, username, password);

        return new ViestinvalitysClient(httpClient, casClient, viestinvalitysUrl, objectMapper);
    }
}


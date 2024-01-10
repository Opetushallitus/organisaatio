package fi.vm.sade.organisaatio.client.viestinvalitys;

import fi.vm.sade.properties.OphProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.CookieManager;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

@Configuration
@Slf4j
public class ViestinvalitysConfig {
    @Bean
    public ViestinvalitysClient viestinvalitysClient(OphProperties properties) {
        var username = properties.require("organisaatio.service.username");
        var password = properties.require("organisaatio.service.password");
        var casBase = properties.require("cas.base");
        var viestinvalitysUrl = properties.require("viestinvalitys.baseurl");

        var httpClient = HttpClient.newBuilder()
                .cookieHandler(new CookieManager())
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        var casClient = new CasClient(httpClient, casBase, username, password);

        return new ViestinvalitysClient(httpClient, casClient, viestinvalitysUrl);
    }
}


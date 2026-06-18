package fi.vm.sade.varda.rekisterointi.configuration;

import tools.jackson.databind.ObjectMapper;
import fi.vm.sade.javautils.httpclient.apache.ApacheOphHttpClient;
import fi.vm.sade.suomifi.valtuudet.ValtuudetClient;
import fi.vm.sade.suomifi.valtuudet.ValtuudetClientImpl;
import fi.vm.sade.suomifi.valtuudet.ValtuudetPropertiesImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;

import static fi.vm.sade.varda.rekisterointi.util.Constants.CALLER_ID;

@Configuration
public class HttpClientConfiguration {

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Bean
    public ValtuudetClient valtuudetClient(ObjectMapper objectMapper,
                                           @Value("${varda-rekisterointi.valtuudet.host}") String host,
                                           @Value("${varda-rekisterointi.valtuudet.client-id}") String clientId,
                                           @Value("${varda-rekisterointi.valtuudet.api-key}") String apiKey,
                                           @Value("${varda-rekisterointi.valtuudet.oauth-password}") String oauthPassword) {
        var httpClient = ApacheOphHttpClient.createDefaultOphClient(CALLER_ID, null);
        var properties = ValtuudetPropertiesImpl.builder()
                .host(host)
                .clientId(clientId)
                .apiKey(apiKey)
                .oauthPassword(oauthPassword)
                .build();
        return new ValtuudetClientImpl(httpClient, objectMapper::readValue, properties);
    }
}

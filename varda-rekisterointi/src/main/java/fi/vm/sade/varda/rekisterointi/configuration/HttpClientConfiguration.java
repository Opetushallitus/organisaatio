package fi.vm.sade.varda.rekisterointi.configuration;

import tools.jackson.databind.ObjectMapper;
import fi.vm.sade.javautils.httpclient.OphHttpClient;
import fi.vm.sade.javautils.httpclient.apache.ApacheOphHttpClient;
import fi.vm.sade.suomifi.valtuudet.ValtuudetClient;
import fi.vm.sade.suomifi.valtuudet.ValtuudetClientImpl;
import fi.vm.sade.suomifi.valtuudet.ValtuudetPropertiesImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static fi.vm.sade.varda.rekisterointi.util.Constants.CALLER_ID;

@Configuration
public class HttpClientConfiguration {

    @Bean
    public OphHttpClient httpClient() {
        return ApacheOphHttpClient.createDefaultOphClient(CALLER_ID, null);
    }

    @Bean
    public ValtuudetClient valtuudetClient(OphHttpClient httpClient,
                                           ObjectMapper objectMapper,
                                           @Value("${varda-rekisterointi.valtuudet.host}") String host,
                                           @Value("${varda-rekisterointi.valtuudet.client-id}") String clientId,
                                           @Value("${varda-rekisterointi.valtuudet.api-key}") String apiKey,
                                           @Value("${varda-rekisterointi.valtuudet.oauth-password}") String oauthPassword) {
        var properties = ValtuudetPropertiesImpl.builder()
                .host(host)
                .clientId(clientId)
                .apiKey(apiKey)
                .oauthPassword(oauthPassword)
                .build();
        return new ValtuudetClientImpl(httpClient, objectMapper::readValue, properties);
    }
}

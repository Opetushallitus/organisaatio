package fi.vm.sade.varda.rekisterointi.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.javautils.httpclient.OphHttpClient;
import fi.vm.sade.javautils.httpclient.apache.ApacheOphHttpClient;
import fi.vm.sade.suomifi.valtuudet.ValtuudetClient;
import fi.vm.sade.suomifi.valtuudet.ValtuudetClientImpl;
import fi.vm.sade.suomifi.valtuudet.ValtuudetProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfiguration {

    private static final String CALLER_ID = "varda-rekisterointi";

    @Bean
    public OphHttpClient httpClient() {
        return ApacheOphHttpClient.createDefaultOphClient(CALLER_ID, null);
    }

    @Bean
    public ValtuudetClient valtuudetClient(OphHttpClient httpClient, ObjectMapper objectMapper, ValtuudetProperties properties) {
        return new ValtuudetClientImpl(httpClient, objectMapper::readValue, properties);
    }

}

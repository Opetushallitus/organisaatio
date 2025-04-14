package fi.vm.sade.organisaatio.config;

import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.httpclient.apache.ApacheOphHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class HttpClientConfiguration {

    public static final String CALLER_ID = "1.2.246.562.10.00000000001.organisaatio-service";

    @Bean
    @Primary
    public OphHttpClient httpClient() {
        return new OphHttpClient.Builder(CALLER_ID).build();
    }

    @Bean // new default TODO check for better implementation.
    public fi.vm.sade.javautils.httpclient.OphHttpClient defaultHttpClient() {
        return ApacheOphHttpClient.createDefaultOphClient(CALLER_ID, null);
    }
}

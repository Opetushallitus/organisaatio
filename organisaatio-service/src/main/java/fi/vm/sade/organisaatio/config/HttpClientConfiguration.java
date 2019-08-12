package fi.vm.sade.organisaatio.config;

import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.auth.CasAuthenticator;
import fi.vm.sade.properties.OphProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class HttpClientConfiguration {

    private static final String CALLER_ID = "1.2.246.562.10.00000000001.organisaatio-service";
    public static final String HTTP_CLIENT_KAYTTOOIKEUS = "kayttooikeusHttpClient";
    public static final String HTTP_CLIENT_VIESTINTA = "viestintaHttpClient";
    public static final String HTTP_CLIENT_KOODISTO = "koodistoHttpClient";

    @Bean
    @Primary
    public OphHttpClient httpClient() {
        return new OphHttpClient.Builder(CALLER_ID).build();
    }

    @Bean(name = HTTP_CLIENT_KAYTTOOIKEUS)
    public OphHttpClient kayttooikeusHttpClient(OphProperties properties) {
        CasAuthenticator authenticator = new CasAuthenticator.Builder()
                .username(properties.require("organisaatio.service.username"))
                .password(properties.require("organisaatio.service.password"))
                .webCasUrl(properties.url("cas.base"))
                .casServiceUrl(properties.url("kayttooikeus-service.login"))
                .build();
        return new OphHttpClient.Builder(CALLER_ID).authenticator(authenticator).build();
    }

    @Bean(name = HTTP_CLIENT_VIESTINTA)
    public OphHttpClient viestintaHttpClient(OphProperties properties) {
        CasAuthenticator authenticator = new CasAuthenticator.Builder()
                .username(properties.require("organisaatio.service.username.to.viestinta"))
                .password(properties.require("organisaatio.service.password.to.viestinta"))
                .webCasUrl(properties.url("cas.base"))
                .casServiceUrl(properties.url("organisaatio-service.ryhmasahkoposti-service.login"))
                .build();
        return new OphHttpClient.Builder(CALLER_ID).authenticator(authenticator).build();
    }

    @Bean(name = HTTP_CLIENT_KOODISTO)
    public OphHttpClient koodistoHttpClient(OphProperties properties) {
        CasAuthenticator authenticator = new CasAuthenticator.Builder()
                .username(properties.require("organisaatio.service.username.to.koodisto"))
                .password(properties.require("organisaatio.service.password.to.koodisto"))
                .webCasUrl(properties.url("cas.base"))
                .casServiceUrl(properties.url("organisaatio-service.koodisto-service.login"))
                .build();
        return new OphHttpClient.Builder(CALLER_ID).authenticator(authenticator).build();
    }

}

package fi.vm.sade.organisaatio.config;

import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.auth.CasAuthenticator;
import fi.vm.sade.javautils.httpclient.apache.ApacheOphHttpClient;
import fi.vm.sade.properties.OphProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class HttpClientConfiguration {

    public static final String CALLER_ID = "1.2.246.562.10.00000000001.organisaatio-service";
    public static final String HTTP_CLIENT_KAYTTOOIKEUS = "kayttooikeusHttpClient";
    public static final String HTTP_CLIENT_KOODISTO = "koodistoHttpClient";
    public static final String HTTP_CLIENT_LOKALISOINTI = "lokalisointiHttpClient";
    public static final String HTTP_CLIENT_OPPIJANUMERO = "oppijanumeroHttpClient";

    @Bean
    @Primary
    public OphHttpClient httpClient() {
        return new OphHttpClient.Builder(CALLER_ID).build();
    }

    @Bean // new default TODO check for better implementation.
    public fi.vm.sade.javautils.httpclient.OphHttpClient defaultHttpClient() {
        return ApacheOphHttpClient.createDefaultOphClient(CALLER_ID, null);
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

    @Bean(name = HTTP_CLIENT_OPPIJANUMERO)
    public OphHttpClient oppijanumeroHttpClient(OphProperties properties) {
        CasAuthenticator authenticator = new CasAuthenticator.Builder()
                .username(properties.require("organisaatio.service.username"))
                .password(properties.require("organisaatio.service.password"))
                .webCasUrl(properties.url("cas.base"))
                .casServiceUrl(properties.url("oppijanumero-service.login"))
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

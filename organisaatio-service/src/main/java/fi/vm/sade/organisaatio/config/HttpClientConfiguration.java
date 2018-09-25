package fi.vm.sade.organisaatio.config;

import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.auth.CasAuthenticator;
import fi.vm.sade.properties.OphProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfiguration {

    private static final String CLIENT_SUBSYSTEM_CODE = "organisaatio";
    public static final String HTTP_CLIENT_KAYTTOOIKEUS = "kayttooikeusHttpClient";

    @Bean(name = HTTP_CLIENT_KAYTTOOIKEUS)
    public OphHttpClient kayttooikeusHttpClient(OphProperties properties) {
        CasAuthenticator authenticator = new CasAuthenticator.Builder()
                .username(properties.getProperty("organisaatio.service.username"))
                .password(properties.getProperty("organisaatio.service.password"))
                .webCasUrl(properties.url("cas.base"))
                .casServiceUrl(properties.url("kayttooikeus-service.login"))
                .build();
        return new OphHttpClient.Builder(CLIENT_SUBSYSTEM_CODE).authenticator(authenticator).build();
    }

}

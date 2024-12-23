package fi.vm.sade.varda.rekisterointi.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.javautils.http.auth.Authenticator;
import fi.vm.sade.javautils.http.auth.CasAuthenticator;
import fi.vm.sade.javautils.httpclient.OphHttpClient;
import fi.vm.sade.javautils.httpclient.apache.ApacheOphHttpClient;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.suomifi.valtuudet.ValtuudetClient;
import fi.vm.sade.suomifi.valtuudet.ValtuudetClientImpl;
import fi.vm.sade.suomifi.valtuudet.ValtuudetProperties;
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
    public ValtuudetClient valtuudetClient(OphHttpClient httpClient, ObjectMapper objectMapper, ValtuudetProperties properties) {
        return new ValtuudetClientImpl(httpClient, objectMapper::readValue, properties);
    }

    @Bean
    public fi.vm.sade.javautils.http.OphHttpClient httpClientKayttooikeus(OphProperties properties) {
        Authenticator authenticator = new CasAuthenticator.Builder()
                .username(properties.require("varda-rekisterointi.service.username"))
                .password(properties.require("varda-rekisterointi.service.password"))
                .webCasUrl(properties.url("cas.base"))
                .casServiceUrl(properties.url("kayttooikeus-service.login"))
                .build();
        return new fi.vm.sade.javautils.http.OphHttpClient.Builder(CALLER_ID)
                .authenticator(authenticator)
                .build();
    }

    @Bean
    public fi.vm.sade.javautils.http.OphHttpClient httpClientOrganisaatio(OphProperties properties) {
        Authenticator authenticator = new CasAuthenticator.Builder()
                .username(properties.require("varda-rekisterointi.service.username"))
                .password(properties.require("varda-rekisterointi.service.password"))
                .webCasUrl(properties.url("cas.base"))
                .casServiceUrl(properties.url("organisaatio-service.login"))
                .build();
        return new fi.vm.sade.javautils.http.OphHttpClient.Builder(CALLER_ID)
                .authenticator(authenticator)
                .build();
    }

    @Bean
    public fi.vm.sade.javautils.http.OphHttpClient httpClientViestinta(OphProperties properties) {
        Authenticator authenticator = new CasAuthenticator.Builder()
                .username(properties.require("varda-rekisterointi.service.username"))
                .password(properties.require("varda-rekisterointi.service.password"))
                .webCasUrl(properties.url("cas.base"))
                .casServiceUrl(properties.url("ryhmasahkoposti-service.login"))
                .build();
        return new fi.vm.sade.javautils.http.OphHttpClient.Builder(CALLER_ID)
                .authenticator(authenticator)
                .build();
    }

}

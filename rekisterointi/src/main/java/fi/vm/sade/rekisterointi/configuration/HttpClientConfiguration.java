package fi.vm.sade.rekisterointi.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.vm.sade.javautils.httpclient.apache.ApacheOphHttpClient;
import fi.vm.sade.suomifi.valtuudet.ValtuudetClient;
import fi.vm.sade.suomifi.valtuudet.ValtuudetClientImpl;
import fi.vm.sade.suomifi.valtuudet.ValtuudetPropertiesImpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

import static fi.vm.sade.rekisterointi.util.Constants.CALLER_ID;

@Configuration
public class HttpClientConfiguration {

  @Bean
  public HttpClient httpClient() {
    return HttpClient.newHttpClient();
  }

  @Bean
  public ValtuudetClient valtuudetClient(ObjectMapper objectMapper,
      @Value("${rekisterointi.valtuudet.host}") String host,
      @Value("${rekisterointi.valtuudet.client-id}") String clientId,
      @Value("${rekisterointi.valtuudet.api-key}") String apiKey,
      @Value("${rekisterointi.valtuudet.oauth-password}") String oauthPassword) {
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

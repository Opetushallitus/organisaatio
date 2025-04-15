package fi.vm.sade.rekisterointi.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.javautils.httpclient.OphHttpClient;
import fi.vm.sade.javautils.httpclient.apache.ApacheOphHttpClient;
import fi.vm.sade.suomifi.valtuudet.ValtuudetClient;
import fi.vm.sade.suomifi.valtuudet.ValtuudetClientImpl;
import fi.vm.sade.suomifi.valtuudet.ValtuudetProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static fi.vm.sade.rekisterointi.util.Constants.CALLER_ID;

@Configuration
public class HttpClientConfiguration {

  @Bean
  public OphHttpClient httpClient() {
    return ApacheOphHttpClient.createDefaultOphClient(CALLER_ID, null);
  }

  @Bean
  public ValtuudetClient valtuudetClient(OphHttpClient httpClient, ObjectMapper objectMapper,
      ValtuudetProperties properties) {
    return new ValtuudetClientImpl(httpClient, objectMapper::readValue, properties);
  }
}

package fi.vm.sade.rekisterointi.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpEntity;
import fi.vm.sade.javautils.http.OphHttpRequest;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.rekisterointi.model.RekisterointiDto;

import java.util.Base64;

import org.apache.http.entity.ContentType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RekisterointiClient {
  private final OphProperties properties;
  private final ObjectMapper objectMapper;

  public RekisterointiClient(OphProperties properties, ObjectMapper objectMapper) {
    this.properties = properties;
    this.objectMapper = objectMapper;
  }

  private String toJson(Object object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException ex) {
      throw new RuntimeException(ex);
    }
  }

  public void create(RekisterointiDto rekisterointiDto) {
    var uri = properties.getProperty("varda-rekisterointi.url");
    var username = properties.getProperty("varda-rekisterointi.username");
    var password = properties.getProperty("varda-rekisterointi.password");
    var basicAuth = Base64.getEncoder().encodeToString(new String(username + ":" + password).getBytes());
    System.out.println(basicAuth);
    var restTemplate = new RestTemplate();
    var headers = new HttpHeaders();
    headers.add("Authorization", "Basic " + basicAuth);
    headers.add("Content-Type", "application/json");
    var entity = new HttpEntity<String>(toJson(rekisterointiDto), headers);
    restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
  }
}

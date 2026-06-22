package fi.vm.sade.rekisterointi.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.rekisterointi.model.RekisterointiDto;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RekisterointiClient {
  private final String urlVirkailija;
  private final String username;
  private final String password;
  private final ObjectMapper objectMapper;

  public RekisterointiClient(@Value("${url-virkailija}") String urlVirkailija,
      @Value("${varda-rekisterointi.username}") String username,
      @Value("${varda-rekisterointi.password}") String password,
      ObjectMapper objectMapper) {
    this.urlVirkailija = urlVirkailija;
    this.username = username;
    this.password = password;
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
    var uri = urlVirkailija + "/varda-rekisterointi/api/rekisterointi";
    var basicAuth = Base64.getEncoder().encodeToString(new String(username + ":" + password).getBytes());
    var restTemplate = new RestTemplate();
    var headers = new HttpHeaders();
    headers.add("Authorization", "Basic " + basicAuth);
    headers.add("Content-Type", "application/json");
    var entity = new HttpEntity<String>(toJson(rekisterointiDto), headers);
    restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
  }
}

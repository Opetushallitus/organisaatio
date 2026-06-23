package fi.vm.sade.rekisterointi.client;

import fi.vm.sade.rekisterointi.model.RekisterointiDto;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.json.JsonMapper;

@Component
public class RekisterointiClient {
  private final String urlVirkailija;
  private final String username;
  private final String password;
  private final JsonMapper jsonMapper;

  @Value("${virkailija.override:null}")
  private String virkailijaOverride;

  public RekisterointiClient(@Value("${url-virkailija}") String urlVirkailija,
      @Value("${varda-rekisterointi.username}") String username,
      @Value("${varda-rekisterointi.password}") String password,
      JsonMapper jsonMapper) {
    this.urlVirkailija = urlVirkailija;
    this.username = username;
    this.password = password;
    this.jsonMapper = jsonMapper;
  }

  private String toJson(Object object) {
    return jsonMapper.writeValueAsString(object);
  }

  public void create(RekisterointiDto rekisterointiDto) {
    var host = virkailijaOverride != null && virkailijaOverride.startsWith("http")
      ? virkailijaOverride
      : urlVirkailija;
    var uri = host + "/varda-rekisterointi/api/rekisterointi";
    var basicAuth = Base64.getEncoder().encodeToString(new String(username + ":" + password).getBytes());
    var restTemplate = new RestTemplate();
    var headers = new HttpHeaders();
    headers.add("Authorization", "Basic " + basicAuth);
    headers.add("Content-Type", "application/json");
    var entity = new HttpEntity<String>(toJson(rekisterointiDto), headers);
    restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
  }
}

package fi.vm.sade.rekisterointi.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpEntity;
import fi.vm.sade.javautils.http.OphHttpRequest;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.rekisterointi.model.RekisterointiDto;

import org.apache.http.entity.ContentType;
import org.springframework.stereotype.Component;

@Component
public class RekisterointiClient {
  private final OphHttpClient httpClient;
  private final OphProperties properties;
  private final ObjectMapper objectMapper;

  public RekisterointiClient(OphHttpClient httpClient,
      OphProperties properties,
      ObjectMapper objectMapper) {
    this.httpClient = httpClient;
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
    String url = properties.url("varda-rekisterointi.url");
    OphHttpEntity entity = new OphHttpEntity.Builder()
        .content(toJson(rekisterointiDto))
        .contentType(ContentType.APPLICATION_JSON)
        .build();
    OphHttpRequest request = OphHttpRequest.Builder
        .put(url)
        .setEntity(entity)
        .build();
    httpClient.execute(request)
        .expectedStatus(200);
  }
}

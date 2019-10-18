package fi.vm.sade.varda.rekisterointi.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpEntity;
import fi.vm.sade.javautils.http.OphHttpRequest;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.model.VirkailijaCriteria;
import fi.vm.sade.varda.rekisterointi.model.VirkailijaDto;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

@Component
public class KayttooikeusClient {

    private final OphHttpClient httpClient;
    private final OphProperties properties;
    private final ObjectMapper objectMapper;

    public KayttooikeusClient(@Qualifier("httpClientKayttooikeus") OphHttpClient httpClient,
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

    private <T> T fromJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    Collection<VirkailijaDto> listVirkailijaBy(VirkailijaCriteria criteria) {
        String url = properties.url("kayttooikeus-service.virkailija.haku");
        OphHttpEntity entity = new OphHttpEntity.Builder()
                .content(toJson(criteria))
                .contentType(ContentType.APPLICATION_JSON)
                .build();
        OphHttpRequest request = OphHttpRequest.Builder.post(url).setEntity(entity).build();
        return httpClient.<Collection<VirkailijaDto>>execute(request)
                .expectedStatus(200)
                .mapWith(json -> Arrays.asList(fromJson(json, VirkailijaDto[].class)))
                .orElseThrow(() -> new RuntimeException(String.format("Url %s returned 204 or 404", url)));
    }
}

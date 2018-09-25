package fi.vm.sade.organisaatio.business.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpEntity;
import fi.vm.sade.javautils.http.OphHttpRequest;
import static fi.vm.sade.organisaatio.config.HttpClientConfiguration.HTTP_CLIENT_KAYTTOOIKEUS;
import fi.vm.sade.organisaatio.dto.HenkiloOrganisaatioCriteria;
import fi.vm.sade.organisaatio.dto.VirkailijaCriteria;
import fi.vm.sade.organisaatio.dto.VirkailijaDto;
import fi.vm.sade.properties.OphProperties;
import java.io.IOException;
import java.util.Collection;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class KayttooikeusClient {

    private final OphHttpClient httpClient;
    private final OphProperties properties;
    private final ObjectReader objectReader;
    private final ObjectWriter objectWriter;

    public KayttooikeusClient(@Qualifier(HTTP_CLIENT_KAYTTOOIKEUS) OphHttpClient httpClient, OphProperties properties) {
        this.httpClient = httpClient;
        this.properties = properties;
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectReader = objectMapper.reader();
        this.objectWriter = objectMapper.writer();
    }

    private String toJson(Object object) {
        try {
            return objectWriter.writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

    private <T> T fromJson(String json, TypeReference<T> javaType) {
        try {
            return objectReader.forType(javaType).readValue(json);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Collection<String> listOrganisaatioOid(HenkiloOrganisaatioCriteria criteria) {
        String url = properties.url("kayttooikeus-service.organisaatiohenkilo.organisaatiooid", criteria.asMap());
        OphHttpRequest request = OphHttpRequest.Builder.get(url).build();
        return httpClient.<Collection<String>>execute(request)
                .expectedStatus(200)
                .mapWith(json -> fromJson(json, new TypeReference<Collection<String>>() {}))
                .orElseThrow(() -> new RuntimeException(String.format("Osoite %s palautti 204 tai 404", url)));
    }

    public Collection<VirkailijaDto> listVirkailija(VirkailijaCriteria criteria) {
        String url = properties.url("kayttooikeus-service.virkailija.haku");
        OphHttpEntity entity = new OphHttpEntity.Builder()
                .contentType(ContentType.APPLICATION_JSON)
                .content(toJson(criteria))
                .build();
        OphHttpRequest request = OphHttpRequest.Builder.post(url).setEntity(entity).build();
        return httpClient.<Collection<VirkailijaDto>>execute(request)
                .expectedStatus(200)
                .mapWith(json -> fromJson(json, new TypeReference<Collection<VirkailijaDto>>() {}))
                .orElseThrow(() -> new RuntimeException(String.format("Osoite %s palautti 204 tai 404", url)));
    }

}

package fi.vm.sade.varda.rekisterointi.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpEntity;
import fi.vm.sade.javautils.http.OphHttpRequest;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioCriteria;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioV4Dto;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

@Component
public class OrganisaatioClient {

    private final OphHttpClient httpClient;
    private final OphProperties properties;
    private final ObjectMapper objectMapper;

    public OrganisaatioClient(@Qualifier("httpClientOrganisaatio") OphHttpClient httpClient,
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

    public Optional<OrganisaatioV4Dto> getV4ByYtunnus(String ytunnus) {
        String url = properties.url("organisaatio-service.organisaatio.v4.byYtunnus", ytunnus);
        OphHttpRequest request = OphHttpRequest.Builder.get(url).build();
        return httpClient.<OrganisaatioV4Dto>execute(request)
                .expectedStatus(200)
                .mapWith(json -> fromJson(json, OrganisaatioV4Dto.class));
    }

    public Optional<OrganisaatioV4Dto> getV4ByYtunnusFromYtj(String ytunnus) {
        String url = properties.url("organisaatio-service.vtj.ytunnus", ytunnus);
        OphHttpRequest request = OphHttpRequest.Builder.get(url).build();
        return httpClient.<OrganisaatioV4Dto>execute(request)
                .expectedStatus(200)
                .mapWith(json -> fromJson(json, OrganisaatioV4Dto.class));
    }

    public OrganisaatioV4Dto save(OrganisaatioV4Dto organisaatio) {
        if (organisaatio.oid == null) {
            return create(organisaatio);
        } else {
            return update(organisaatio);
        }
    }

    public OrganisaatioV4Dto create(OrganisaatioV4Dto organisaatio) {
        assert organisaatio.oid == null;
        String url = properties.url("organisaatio-service.organisaatio.v4", organisaatio.oid);
        OphHttpEntity entity = new OphHttpEntity.Builder()
                .content(toJson(organisaatio))
                .contentType(ContentType.APPLICATION_JSON)
                .build();
        OphHttpRequest request = OphHttpRequest.Builder
                .post(url)
                .setEntity(entity)
                .build();
        return httpClient.<OrganisaatioV4Dto>execute(request)
                .expectedStatus(200)
                .mapWith(json -> fromJson(json, OrganisaatioV4Dto.class))
                .orElseThrow(() -> new RuntimeException(String.format("Url %s returned 204 or 404", url)));
    }

    public OrganisaatioV4Dto update(OrganisaatioV4Dto organisaatio) {
        assert organisaatio.oid != null;
        String url = properties.url("organisaatio-service.organisaatio.v4.byOid", organisaatio.oid);
        OphHttpEntity entity = new OphHttpEntity.Builder()
                .content(toJson(organisaatio))
                .contentType(ContentType.APPLICATION_JSON)
                .build();
        OphHttpRequest request = OphHttpRequest.Builder
                .put(url)
                .setEntity(entity)
                .build();
        return httpClient.<OrganisaatioV4Dto>execute(request)
                .expectedStatus(200)
                .mapWith(json -> fromJson(json, OrganisaatioV4Dto.class))
                .orElseThrow(() -> new RuntimeException(String.format("Url %s returned 204 or 404", url)));
    }

    public Collection<OrganisaatioV4Dto> listBy(OrganisaatioCriteria criteria) {
        String url = properties.url("organisaatio-service.organisaatio.v4.hae", criteria.asMap());
        OphHttpRequest request = OphHttpRequest.Builder.get(url).build();
        return httpClient.<Collection<OrganisaatioV4Dto>>execute(request)
                .expectedStatus(200)
                .mapWith(json -> fromJson(json, OrganisaatioListDto.class).organisaatiot)
                .orElseThrow(() -> new RuntimeException(String.format("Url %s returned 204 or 404", url)));
    }

    private static class OrganisaatioListDto {
        public long numHits;
        public Collection<OrganisaatioV4Dto> organisaatiot;
    }

}

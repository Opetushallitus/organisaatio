package fi.vm.sade.organisaatio.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpRequest;
import fi.vm.sade.properties.OphProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

import static fi.vm.sade.organisaatio.config.HttpClientConfiguration.HTTP_CLIENT_OPPIJANUMERO;
@Component
public class OppijanumeroClient {
    private final OphHttpClient httpClient;
    private final OphProperties properties;
    private final ObjectReader objectReader;
    private final ObjectWriter objectWriter;
    public OppijanumeroClient(@Qualifier(HTTP_CLIENT_OPPIJANUMERO) OphHttpClient httpClient, OphProperties properties) {
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


    public OppijanumeroDto henkilo(String oid) {
        String url = properties.url("oppijanumero-service.henkilo", oid);
        OphHttpRequest request = OphHttpRequest.Builder.get(url).build();
        return httpClient.<OppijanumeroDto>execute(request)
                .expectedStatus(200)
                .mapWith(json -> fromJson(json, new TypeReference<>() {}))
                .orElseThrow(() -> new RuntimeException(String.format("Osoite %s palautti 204 tai 404", url)));
    }

    public static class OppijanumeroDto {

        private String oidHenkilo;
        private String etunimet;
        private String sukunimi;

        public String getOidHenkilo() {
            return oidHenkilo;
        }

        public void setOidHenkilo(String oidHenkilo) {
            this.oidHenkilo = oidHenkilo;
        }

        public String getEtunimet() {
            return etunimet;
        }

        public void setEtunimet(String etunimet) {
            this.etunimet = etunimet;
        }

        public String getSukunimi() {
            return sukunimi;
        }

        public void setSukunimi(String sukunimi) {
            this.sukunimi = sukunimi;
        }
    }
}

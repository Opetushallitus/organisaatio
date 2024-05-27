package fi.vm.sade.organisaatio.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.properties.OphProperties;

import java.io.IOException;

public abstract class CustomClient {
    final OphHttpClient httpClient;
    final OphProperties properties;
    private final ObjectReader objectReader;
    private final ObjectWriter objectWriter;

    CustomClient(OphHttpClient httpClient, OphProperties properties) {
        this.httpClient = httpClient;
        this.properties = properties;

        ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule());
        this.objectReader = objectMapper.reader();
        this.objectWriter = objectMapper.writer();
    }

    String toJson(Object object) {
        try {
            return objectWriter.writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            throw new ClientException(ex.getMessage());
        }
    }

    <T> T fromJson(String json, TypeReference<T> javaType) {
        try {
            return objectReader.forType(javaType).readValue(json);
        } catch (IOException ex) {
            throw new ClientException(ex.getMessage());
        }
    }


}

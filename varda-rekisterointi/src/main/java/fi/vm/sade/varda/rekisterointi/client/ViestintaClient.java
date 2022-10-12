package fi.vm.sade.varda.rekisterointi.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpEntity;
import fi.vm.sade.javautils.http.OphHttpRequest;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.model.EmailDto;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Client viestintäpalvelun käyttämiseen.
 */
@Component
public class ViestintaClient {

    private final OphHttpClient httpClient;
    private final OphProperties properties;
    private final ObjectMapper objectMapper;

    /**
     * Alustaa clientin HTTP-clientillä, konfiguraatiolla ja <code>ObjectMapper</code>illä.
     *
     * @param httpClient    HTTP-client
     * @param properties    konfiguraatio
     * @param objectMapper  Jackson object mapper
     */
    public ViestintaClient(@Qualifier("httpClientViestinta") OphHttpClient httpClient,
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

    /**
     * Tallentaa sähköpostiviestin lähetettäväksi. Sanitoi viestin.
     *
     * @param email lähetettävä sähköposti
     *
     * @return  tallennetun viestin tunniste.
     * @see #save(EmailDto, boolean)
     */
    public String save(EmailDto email) {
        return save(email, true);
    }

    /**
     * Tallentaa sähköpostiviestin lähetettäväksi.
     *
     * @param email     lähettettävä sähköposti
     * @param sanitize  sanitoidaanko viestin sisältö
     *
     * @return  tallennetun viestin tunniste.
     */
    public String save(EmailDto email, boolean sanitize) {
        String url = properties.url("ryhmasahkoposti-service.email", sanitize);
        OphHttpEntity entity = new OphHttpEntity.Builder()
                .content(toJson(email))
                .contentType(ContentType.APPLICATION_JSON)
                .build();
        System.out.println(toJson(email));
        OphHttpRequest request = OphHttpRequest.Builder
                .post(url)
                .setEntity(entity)
                .build();
        SaveEmailResponseDto response = httpClient.<SaveEmailResponseDto>execute(request)
                .expectedStatus(200)
                .mapWith(json -> fromJson(json, SaveEmailResponseDto.class))
                .orElseThrow(() -> new RuntimeException(String.format("Url %s returned 204 or 404", url)));
        return response.id;
    }

    private static class SaveEmailResponseDto {
        public String id;
    }

}

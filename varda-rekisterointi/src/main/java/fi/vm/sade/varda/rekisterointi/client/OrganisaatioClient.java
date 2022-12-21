package fi.vm.sade.varda.rekisterointi.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpEntity;
import fi.vm.sade.javautils.http.OphHttpRequest;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioCriteria;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioDto;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

/**
 * Client organisaatiopalvelun käyttämiseen.
 */
@Component
public class OrganisaatioClient {

    private static final String KUNTA_YRITYSMUOTO = "Kunta";
    private final OphHttpClient httpClient;
    private final OphProperties properties;
    private final ObjectMapper objectMapper;

    /**
     * Alustaa clientin annetulla HTTP-clientillä, konfiguraatiolla ja <code>ObjectMapper</code>illä.
     *
     * @param httpClient    HTTP-client
     * @param properties    konfiguraatio
     * @param objectMapper  Jackson object mapper
     */
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

    /**
     * Hakee organisaation y-tunnuksella organisaatiopalvelusta.
     *
     * @param ytunnus   y-tunnus
     *
     * @return organisaatio, tai <code>empty</code> mikäli ei löydy.
     */
    public Optional<OrganisaatioDto> getOrganisaatioByYtunnus(String ytunnus) {
        String url = properties.url("organisaatio-service.organisaatio.api.byYtunnus", ytunnus);
        OphHttpRequest request = OphHttpRequest.Builder.get(url).build();
        return httpClient.<OrganisaatioDto>execute(request)
                .expectedStatus(200)
                .mapWith(json -> fromJson(json, OrganisaatioDto.class));
    }

    /**
     * Hakee organisaation y-tunnuksella YTJ:stä.
     *
     * @param ytunnus   y-tunnus
     *
     * @return organisaatio, tai <code>empty</code> mikäli ei löydy.
     */
    public Optional<OrganisaatioDto> getOrganisaatioByYtunnusFromYtj(String ytunnus) {
        String url = properties.url("organisaatio-service.vtj.ytunnus", ytunnus);
        OphHttpRequest request = OphHttpRequest.Builder.get(url).build();
        return httpClient.<OrganisaatioDto>execute(request)
                .expectedStatus(200)
                .mapWith(json -> fromJson(json, OrganisaatioDto.class));
    }

    /**
     * Hakee organisaation OID:illa organisaatiopalvelusta.
     *
     * @param oid   organisaation OID
     *
     * @return organisaatio, tai <code>empty</code> mikäli ei löydy.
     */
    public Optional<OrganisaatioDto> getOrganisaatioByOid(String oid) {
        String url = properties.url("organisaatio-service.organisaatio.api.byOid", oid);
        OphHttpRequest request = OphHttpRequest.Builder.get(url).build();
        return httpClient.<OrganisaatioDto>execute(request)
                .expectedStatus(200)
                .mapWith(json -> fromJson(json, OrganisaatioDto.class));
    }

    public Collection<OrganisaatioDto> getOrganisaatioJalkelaisetByOid(String oid) {
        String url = properties.url("organisaatio-service.organisaatio.api.jalkelaisetByOid", oid);
        OphHttpRequest request = OphHttpRequest.Builder.get(url).build();
        return httpClient.<Collection<OrganisaatioDto>>execute(request)
                .expectedStatus(200)
                .mapWith(json -> fromJson(json, OrganisaatioListDto.class).organisaatiot)
                .orElseThrow(() -> new RuntimeException(String.format("Url %s returned 204 or 404", url)));
    }

    /**
     * Tallentaa organisaation organisaatiopalveluun. Olemassa olevan organisaation
     * kohdalla tallentaa muutokset, muutoin luo uuden.
     *
     * @param organisaatio  tallennettava organisaatio tai muutos
     *
     * @return tallennettu/luotu organisaatio.
     *
     * @see #create(OrganisaatioDto)
     * @see #update(OrganisaatioDto)
     */
    public OrganisaatioDto save(OrganisaatioDto organisaatio) {
        if (organisaatio.oid == null) {
            return create(organisaatio);
        } else {
            return update(organisaatio);
        }
    }

    /**
     * Luo uuden organisaation organisaatiopalveluun.
     *
     * @param organisaatio  luotava organisaatio
     *
     * @return  luotu organisaatio
     */
    public OrganisaatioDto create(OrganisaatioDto organisaatio) {
        assert organisaatio.oid == null;
        String url = properties.url("organisaatio-service.organisaatio.api", organisaatio.oid);
        OphHttpEntity entity = new OphHttpEntity.Builder()
                .content(toJson(organisaatio))
                .contentType(ContentType.APPLICATION_JSON)
                .build();
        OphHttpRequest request = OphHttpRequest.Builder
                .post(url)
                .setEntity(entity)
                .build();
        return httpClient.<OrganisaatioDto>execute(request)
                .expectedStatus(200)
                .mapWith(json -> fromJson(json, OrganisaatioResultDto.class).organisaatio)
                .orElseThrow(() -> new RuntimeException(String.format("Url %s returned 204 or 404", url)));
    }

    /**
     * Päivittää organisaation organisaatiopalveluun.
     *
     * @param organisaatio  tallennettava organisaatio
     *
     * @return tallennettu organisaatio.
     */
    public OrganisaatioDto update(OrganisaatioDto organisaatio) {
        assert organisaatio.oid != null;
        String url = properties.url("organisaatio-service.organisaatio.api.byOid", organisaatio.oid);
        OphHttpEntity entity = new OphHttpEntity.Builder()
                .content(toJson(organisaatio))
                .contentType(ContentType.APPLICATION_JSON)
                .build();
        OphHttpRequest request = OphHttpRequest.Builder
                .put(url)
                .setEntity(entity)
                .build();
        return httpClient.<OrganisaatioDto>execute(request)
                .expectedStatus(200)
                .mapWith(json -> fromJson(json, OrganisaatioResultDto.class).organisaatio)
                .orElseThrow(() -> new RuntimeException(String.format("Url %s returned 204 or 404", url)));
    }

    /**
     * Listaa organisaatiot hakuehdoilla organisaatiopalvelusta.
     *
     * @param criteria  hakuehdot
     *
     * @return lista ehtoihin täsmäävistä organisaatioista.
     */
    public Collection<OrganisaatioDto> listBy(OrganisaatioCriteria criteria) {
        String url = properties.url("organisaatio-service.organisaatio.api.hae", criteria.asMap());
        OphHttpRequest request = OphHttpRequest.Builder.get(url).build();
        return httpClient.<Collection<OrganisaatioDto>>execute(request)
                .expectedStatus(200)
                .mapWith(json -> fromJson(json, OrganisaatioListDto.class).organisaatiot)
                .orElseThrow(() -> new RuntimeException(String.format("Url %s returned 204 or 404", url)));
    }

    /**
     * Lataan kunnan OID:illa organisaatiopalvelusta.
     *
     * @param oid   kunnan OID
     *
     * @return kunnan tiedot, tai <code>empty</code> mikäli ei löydy.
     */
    public Optional<OrganisaatioDto> getKuntaByOid(String oid) {
        return getOrganisaatioByOid(oid).filter(organisaatioV4Dto -> KUNTA_YRITYSMUOTO.equals(organisaatioV4Dto.yritysmuoto));
    }

    private static class OrganisaatioListDto {
        public long numHits;
        public Collection<OrganisaatioDto> organisaatiot;
    }

    private static class OrganisaatioResultDto {
        public OrganisaatioDto organisaatio;
    }
}

package fi.vm.sade.varda.rekisterointi.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioCriteria;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioDto;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * Client organisaatiopalvelun käyttämiseen.
 */
@Component
@RequiredArgsConstructor
public class OrganisaatioClient {

    private static final String KUNTA_YRITYSMUOTO = "Kunta";
    private final OtuvaOauth2Client httpClient;
    private final OphProperties properties;
    private final ObjectMapper objectMapper;

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
        return getOrganisaatioFromUrl(url);
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
        return getOrganisaatioFromUrl(url);
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
        return getOrganisaatioFromUrl(url);
    }

    private  Optional<OrganisaatioDto> getOrganisaatioFromUrl(String url) {
        try {
            var request = HttpRequest.newBuilder().uri(new URI(url)).GET();
            var response = httpClient.executeRequest(request);
            if (response.statusCode() == 200) {
                return Optional.of(fromJson(response.body(), OrganisaatioDto.class));
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Collection<OrganisaatioDto> getOrganisaatioJalkelaisetByOid(String oid) {
        String url = properties.url("organisaatio-service.organisaatio.api.jalkelaisetByOid", oid);
        try {
            var request = HttpRequest.newBuilder().uri(new URI(url)).GET();
            var response = httpClient.executeRequest(request);
            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), OrganisaatioListDto.class).organisaatiot;
            } else {
                throw new RuntimeException(String.format("Url %s returned 204 or 404", url));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        try {
            var request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(toJson(organisaatio)));
            var response = httpClient.executeRequest(request);
            if (response.statusCode() == 200) {
                return fromJson(response.body(), OrganisaatioResultDto.class).organisaatio;
            } else {
                throw new RuntimeException(String.format("Url %s returned 204 or 404", url));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        try {
            var request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .PUT(BodyPublishers.ofString(toJson(organisaatio)));
            var response = httpClient.executeRequest(request);
            if (response.statusCode() == 200) {
                return fromJson(response.body(), OrganisaatioResultDto.class).organisaatio;
            } else {
                throw new RuntimeException(String.format("Url %s returned 204 or 404", url));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        try {
            var request = HttpRequest.newBuilder().uri(new URI(url)).GET();
            var response = httpClient.executeRequest(request);
            if (response.statusCode() == 200) {
                return fromJson(response.body(), OrganisaatioListDto.class).organisaatiot;
            } else {
                throw new RuntimeException(String.format("Url %s returned 204 or 404", url));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

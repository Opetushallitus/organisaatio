package fi.vm.sade.varda.rekisterointi.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpEntity;
import fi.vm.sade.javautils.http.OphHttpRequest;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.dto.KayttooikeusKutsuDto;
import fi.vm.sade.varda.rekisterointi.model.Kayttaja;
import fi.vm.sade.varda.rekisterointi.model.VirkailijaCriteria;
import fi.vm.sade.varda.rekisterointi.model.VirkailijaDto;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

/**
 * Client käyttöoikeuspalvelun käyttämiseen.
 */
@Component
public class KayttooikeusClient {

    private final OphHttpClient httpClient;
    private final OphProperties properties;
    private final ObjectMapper objectMapper;

    /**
     * Alustaa clientin annetulla HTTP-clientilla, konfiguraatiolla ja <code>ObjectMapper</code>illä.
     *
     * @param httpClient    HTTP-client
     * @param properties    konfiguraatio
     * @param objectMapper  Jackson object mapper
     */
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

    /**
     * Listaa virkailijat annetuilla ehdoilla.
     *
     * @param criteria  hakuehdot
     *
     * @return  lista hakuehtoihin täsmäävistä virkailijoista
     */
    public Collection<VirkailijaDto> listVirkailijaBy(VirkailijaCriteria criteria) {
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

    /**
     * Lähettää kutsun organisaation pääkäyttäjälle.
     *
     * @param kutsujaOid        kutsun lähettäjän OID
     * @param kayttaja          kutsuttava käyttäjä
     * @param organisaatioOid   käytäjän organisaation OID
     * @param oikeusRyhmaId     käyttäjälle myönnettävän oikeusryhmän OID
     */
    public void kutsuKayttaja(String kutsujaOid, Kayttaja kayttaja, String organisaatioOid, Long oikeusRyhmaId, String kutsujaForEmail) {
        KayttooikeusKutsuDto dto = KayttooikeusKutsuDto.builder()
                .kutsujaOid(kutsujaOid)
                .etunimi(kayttaja.etunimi)
                .sukunimi(kayttaja.sukunimi)
                .asiointikieli(kayttaja.asiointikieli)
                .saate(kayttaja.saateteksti)
                .sahkoposti(kayttaja.sahkoposti)
                .kutsujaForEmail(kutsujaForEmail)
                .organisaatiot(Set.of(
                        KayttooikeusKutsuDto.KutsuOrganisaatioDto.of(
                                organisaatioOid,
                                Set.of(KayttooikeusKutsuDto.KutsuKayttooikeusRyhmaDto.of(oikeusRyhmaId)),
                                LocalDate.now().plusYears(1))
                        ))
                .build();
        String url = properties.url("kayttooikeus-service.kutsu");
        OphHttpEntity entity = new OphHttpEntity.Builder()
                .content(toJson(dto))
                .contentType(ContentType.APPLICATION_JSON)
                .build();
        OphHttpRequest request = OphHttpRequest.Builder.post(url).setEntity(entity).build();
        httpClient.<Long>execute(request)
                .expectedStatus(201)
                .ignoreResponse();
    }
}

package fi.vm.sade.varda.rekisterointi.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.dto.KayttooikeusKutsuDto;
import fi.vm.sade.varda.rekisterointi.model.Kayttaja;
import fi.vm.sade.varda.rekisterointi.model.VirkailijaCriteria;
import fi.vm.sade.varda.rekisterointi.model.VirkailijaDto;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

/**
 * Client käyttöoikeuspalvelun käyttämiseen.
 */
@Component
@RequiredArgsConstructor
public class KayttooikeusClient {
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
     * Listaa virkailijat annetuilla ehdoilla.
     *
     * @param criteria  hakuehdot
     *
     * @return  lista hakuehtoihin täsmäävistä virkailijoista
     */
    public Collection<VirkailijaDto> listVirkailijaBy(VirkailijaCriteria criteria) {
        String url = properties.url("kayttooikeus-service.virkailija.haku");
        try {
            var request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(toJson(criteria)));
            var response = httpClient.executeRequest(request);
            if (response.statusCode() == 200) {
                return Arrays.asList(fromJson(response.body(), VirkailijaDto[].class));
            } else {
                throw new RuntimeException(String.format("Url %s returned 204 or 404", url));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        try {
            var request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(toJson(dto)));
            var response = httpClient.executeRequest(request);
            if (response.statusCode() != 201) {
                throw new RuntimeException(String.format("Url %s returned 204 or 404", url));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

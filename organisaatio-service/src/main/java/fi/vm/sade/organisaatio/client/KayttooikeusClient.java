package fi.vm.sade.organisaatio.client;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import fi.vm.sade.organisaatio.business.exception.KayttooikeusInternalServerErrorException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioKayttooikeusException;
import fi.vm.sade.organisaatio.dto.VirkailijaCriteria;
import fi.vm.sade.organisaatio.dto.VirkailijaDto;
import fi.vm.sade.organisaatio.model.Kayttaja;
import fi.vm.sade.organisaatio.model.KayttajaKutsu;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class KayttooikeusClient {
    private final ObjectMapper objectMapper;
    private final OtuvaOauth2Client httpClient;

    @Value("${url-virkailija}")
    private String urlVirkailija;

    public Collection<VirkailijaDto> listVirkailija(VirkailijaCriteria criteria) {
        String url = urlVirkailija + "/kayttooikeus-service/virkailija/haku";
        try {
            var request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(toJson(criteria)));
            var response = httpClient.executeRequest(request);
            if (response.statusCode() == 200) {
                return fromJson(response.body(), new TypeReference<>() {});
            } else {
                throw new ClientException(String.format("Osoite %s palautti 204", response.request().uri()));
            }
        } catch  (Exception e) {
            OrganisaatioKayttooikeusException ex = new OrganisaatioKayttooikeusException(e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    public Long kutsuKayttaja(Kayttaja kayttaja, String organisaatioOid, Long oikeusRyhmaId, String kutsujaForEmail) {
        KayttajaKutsu dto = KayttajaKutsu.builder()
                .etunimi(kayttaja.etunimi)
                .sukunimi(kayttaja.sukunimi)
                .asiointikieli(kayttaja.asiointikieli)
                .sahkoposti(kayttaja.sahkoposti)
                .kutsujaForEmail(kutsujaForEmail)
                .kutsujaOid(SecurityContextHolder.getContext().getAuthentication().getName())
                .organisaatiot(Set.of(
                        KayttajaKutsu.KutsuOrganisaatio.of(
                                organisaatioOid,
                                Set.of(KayttajaKutsu.KutsuKayttooikeusRyhma.of(oikeusRyhmaId)),
                                LocalDate.now().plusYears(1))
                        ))
                .build();
        String url = urlVirkailija + "/kayttooikeus-service/kutsu";
        try {
            var request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(toJson(dto)));
            var response = httpClient.executeRequest(request);
            if (response.statusCode() == 201) {
                return fromJson(response.body(), new TypeReference<>() {});
            } else if (response.statusCode() == 500) {
                throw new KayttooikeusInternalServerErrorException("Käyttäjän kutsu epäonnistui");
            } else {
                throw new ClientException(String.format("Osoite %s palautti " + response.statusCode(), response.request().uri()));
            }
        } catch  (Exception e) {
            OrganisaatioKayttooikeusException ex = new OrganisaatioKayttooikeusException(e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception ex) {
            throw new ClientException(ex.getMessage());
        }
    }

    <T> T fromJson(String json, TypeReference<T> javaType) {
        try {
            return objectMapper.reader().forType(javaType).readValue(json);
        } catch (Exception ex) {
            throw new ClientException(ex.getMessage());
        }
    }
}

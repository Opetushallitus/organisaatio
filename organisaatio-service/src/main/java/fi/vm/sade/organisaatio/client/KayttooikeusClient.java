package fi.vm.sade.organisaatio.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import fi.vm.sade.organisaatio.business.exception.KayttooikeusInternalServerErrorException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioKayttooikeusException;
import fi.vm.sade.organisaatio.dto.HenkiloOrganisaatioCriteria;
import fi.vm.sade.organisaatio.dto.VirkailijaCriteria;
import fi.vm.sade.organisaatio.dto.VirkailijaDto;
import fi.vm.sade.organisaatio.model.Kayttaja;
import fi.vm.sade.organisaatio.model.KayttajaKutsu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

@Component
public class KayttooikeusClient {
    private final ObjectReader objectReader;
    private final ObjectWriter objectWriter;

    @Autowired
    private OtuvaOauth2Client httpClient;

    @Value("${url-virkailija}")
    private String urlVirkailija;

    public KayttooikeusClient() {
        ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule());
        this.objectReader = objectMapper.reader();
        this.objectWriter = objectMapper.writer();
    }

    public Collection<String> listOrganisaatioOid(HenkiloOrganisaatioCriteria criteria) {
        UriComponentsBuilder urlBuilder = UriComponentsBuilder
                .fromUriString(urlVirkailija + "/kayttooikeus-service/organisaatiohenkilo/organisaatioOid");
        criteria.asMap().forEach((key, value) -> {
            if (value instanceof Collection<?> values) {
                if (!values.isEmpty()) {
                    urlBuilder.queryParam(key, values.toArray());
                }
            } else if (value != null) {
                urlBuilder.queryParam(key, value);
            }
        });
        String url = urlBuilder.build().toUriString();
        try {
            var request = HttpRequest.newBuilder().uri(URI.create(url)).GET();
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

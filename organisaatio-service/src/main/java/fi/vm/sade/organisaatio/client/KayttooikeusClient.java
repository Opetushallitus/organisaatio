package fi.vm.sade.organisaatio.client;

import com.fasterxml.jackson.core.type.TypeReference;
import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpEntity;
import fi.vm.sade.javautils.http.OphHttpRequest;
import fi.vm.sade.organisaatio.business.exception.KayttooikeusInternalServerErrorException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioKayttooikeusException;
import fi.vm.sade.organisaatio.config.scheduling.AuthenticationUtil;
import fi.vm.sade.organisaatio.dto.HenkiloOrganisaatioCriteria;
import fi.vm.sade.organisaatio.dto.VirkailijaCriteria;
import fi.vm.sade.organisaatio.dto.VirkailijaDto;
import fi.vm.sade.organisaatio.model.Kayttaja;
import fi.vm.sade.organisaatio.model.KayttajaKutsu;
import fi.vm.sade.properties.OphProperties;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

import static fi.vm.sade.organisaatio.config.HttpClientConfiguration.HTTP_CLIENT_KAYTTOOIKEUS;

@Component
public class KayttooikeusClient extends CustomClient {
    @Autowired
    private AuthenticationUtil authenticationUtil;

    public KayttooikeusClient(@Qualifier(HTTP_CLIENT_KAYTTOOIKEUS) OphHttpClient httpClient, OphProperties properties) {
        super(httpClient, properties);
    }

    public Collection<String> listOrganisaatioOid(HenkiloOrganisaatioCriteria criteria) {
        String url = properties.url("kayttooikeus-service.organisaatiohenkilo.organisaatiooid", criteria.asMap());
        OphHttpRequest request = OphHttpRequest.Builder.get(url).build();
        return httpClient.<Collection<String>>execute(request)
                .expectedStatus(200)
                .mapWith(json -> fromJson(json, new TypeReference<>() {
                }))
                .orElseThrow(() -> new OrganisaatioKayttooikeusException(String.format("Osoite %s palautti 204 tai 404", url)));
    }

    public Collection<VirkailijaDto> listVirkailija(VirkailijaCriteria criteria) {
        String url = properties.url("kayttooikeus-service.virkailija.haku");
        OphHttpEntity entity = new OphHttpEntity.Builder()
                .contentType(ContentType.APPLICATION_JSON)
                .content(toJson(criteria))
                .build();
        OphHttpRequest request = OphHttpRequest.Builder.post(url).setEntity(entity).build();
        return httpClient.<Collection<VirkailijaDto>>execute(request)
                .expectedStatus(200)
                .mapWith(json -> fromJson(json, new TypeReference<>() {
                }))
                .orElseThrow(() -> new OrganisaatioKayttooikeusException(String.format("Osoite %s palautti 204 tai 404", url)));
    }

    public Long kutsuKayttaja(Kayttaja kayttaja, String organisaatioOid, Long oikeusRyhmaId, String kutsujaForEmail) {
        KayttajaKutsu dto = KayttajaKutsu.builder()
                .etunimi(kayttaja.etunimi)
                .sukunimi(kayttaja.sukunimi)
                .asiointikieli(kayttaja.asiointikieli)
                .sahkoposti(kayttaja.sahkoposti)
                .kutsujaForEmail(kutsujaForEmail)
                .kutsujaOid(authenticationUtil.getCurrentUserOid())
                .organisaatiot(Set.of(
                        KayttajaKutsu.KutsuOrganisaatio.of(
                                organisaatioOid,
                                Set.of(KayttajaKutsu.KutsuKayttooikeusRyhma.of(oikeusRyhmaId)),
                                LocalDate.now().plusYears(1))
                        ))
                .build();
        String url = properties.url("kayttooikeus-service.kutsu");
        OphHttpEntity entity = new OphHttpEntity.Builder()
                .content(toJson(dto))
                .contentType(ContentType.APPLICATION_JSON)
                .build();
        OphHttpRequest request = OphHttpRequest.Builder.post(url).setEntity(entity).build();
        return httpClient.<Long>execute(request)
                .handleErrorStatus(500).with(json -> { throw new KayttooikeusInternalServerErrorException("Käyttäjän kutsu epäonnistui"); })
                .expectedStatus(201)
                .mapWith(json -> fromJson(json, new TypeReference<>() {
                }))
                .orElseThrow(() -> new OrganisaatioKayttooikeusException("Käyttäjän kutsua ei hyväksytty"));
    }
}

package fi.vm.sade.organisaatio.client;

import com.fasterxml.jackson.core.type.TypeReference;
import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpEntity;
import fi.vm.sade.javautils.http.OphHttpRequest;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioKayttooikeusException;
import fi.vm.sade.organisaatio.dto.HenkiloOrganisaatioCriteria;
import fi.vm.sade.organisaatio.dto.VirkailijaCriteria;
import fi.vm.sade.organisaatio.dto.VirkailijaDto;
import fi.vm.sade.properties.OphProperties;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collection;

import static fi.vm.sade.organisaatio.config.HttpClientConfiguration.HTTP_CLIENT_KAYTTOOIKEUS;

@Component
public class KayttooikeusClient extends CustomClient {

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

}

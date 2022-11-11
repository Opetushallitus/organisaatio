package fi.vm.sade.organisaatio.client;

import com.fasterxml.jackson.core.type.TypeReference;
import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpRequest;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioOppijanumeroException;
import fi.vm.sade.properties.OphProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static fi.vm.sade.organisaatio.config.HttpClientConfiguration.HTTP_CLIENT_OPPIJANUMERO;

@Component
public class OppijanumeroClient extends CustomClient {

    public OppijanumeroClient(@Qualifier(HTTP_CLIENT_OPPIJANUMERO) OphHttpClient httpClient, OphProperties properties) {
        super(httpClient, properties);
    }

    public OppijanumeroDto henkilo(String oid) {
        String url = properties.url("oppijanumero-service.henkilo", oid);
        OphHttpRequest request = OphHttpRequest.Builder.get(url).build();
        return httpClient.<OppijanumeroDto>execute(request)
                .expectedStatus(200)
                .mapWith(json -> fromJson(json, new TypeReference<>() {
                }))
                .orElseThrow(() -> new OrganisaatioOppijanumeroException(String.format("Osoite %s palautti 204 tai 404", url)));
    }

    @Getter
    @Setter
    public static class OppijanumeroDto {
        private String oidHenkilo;
        private String etunimet;
        private String sukunimi;
    }
}

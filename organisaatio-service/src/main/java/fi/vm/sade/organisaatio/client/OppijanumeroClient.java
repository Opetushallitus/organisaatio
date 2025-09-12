package fi.vm.sade.organisaatio.client;

import com.google.gson.Gson;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioOppijanumeroException;
import fi.vm.sade.properties.OphProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpRequest;

@Component
@RequiredArgsConstructor
public class OppijanumeroClient {
    private final OtuvaOauth2Client httpClient;
    private final OphProperties properties;

    private Gson gson = new Gson();

    @Value("${oppijanumerorekisteri.baseurl}")
    private String oppijanumerorekisteriBaseurl;

    public OppijanumeroDto henkilo(String oid) {
        var url = path("/henkilo/").pathSegment(oid).toUriString();
        try {
            var request = HttpRequest.newBuilder().uri(new URI(url)).GET();
            var response = httpClient.executeRequest(request);
            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), OppijanumeroDto.class);
            } else {
                throw new ClientException(String.format("Osoite %s palautti 204", response.request().uri()));
            }
        } catch  (Exception e) {
            OrganisaatioOppijanumeroException ex = new OrganisaatioOppijanumeroException(e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    private UriComponentsBuilder path(String path) {
        return UriComponentsBuilder.fromUriString(oppijanumerorekisteriBaseurl).path(path);
    }

    public record OppijanumeroDto(String oidHenkilo, String etunimet, String sukunimi) {}
}

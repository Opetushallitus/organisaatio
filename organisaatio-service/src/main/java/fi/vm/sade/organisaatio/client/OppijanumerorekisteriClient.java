package fi.vm.sade.organisaatio.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.organisaatio.cas.CasAuthenticatedServiceClient;
import fi.vm.sade.organisaatio.cas.CasClient;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
public class OppijanumerorekisteriClient extends CasAuthenticatedServiceClient {
    private final ObjectMapper mapper;
    private final String baseUrl;
    public OppijanumerorekisteriClient(HttpClient httpClient, CasClient casClient, String baseUrl, ObjectMapper mapper) {
        super(httpClient, casClient, baseUrl);
        this.mapper = mapper;
        this.baseUrl = baseUrl;
    }

    public Henkilo getHenkilo(String oid) throws IOException, InterruptedException {
        var response = get("/henkilo/" + oid);
        if (response.statusCode() == 200) {
            return mapper.readValue(response.body(), Henkilo.class);
        } else {
            throw new RuntimeException("Unexpected status code " + response.statusCode() + " when fetching henkilo " + oid);
        }
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        var uri = URI.create(baseUrl + path);
        log.info("Executing request GET {}", uri);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .header("Accept", "application/json");
        var response = sendRequest(request);
        log.info("Received response {} {}", response.statusCode(), response.body());
        return response;
    }

    public record Henkilo(String oidHenkilo, String etunimet, String sukunimi) {}
}

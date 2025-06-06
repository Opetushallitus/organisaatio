package fi.vm.sade.varda.rekisterointi.client.viestinvalitys;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import fi.vm.sade.varda.rekisterointi.util.Constants;

@Slf4j
public abstract class CasAuthenticatedServiceClient {
    protected final HttpClient httpClient;
    private final CasClient casClient;
    protected final String serviceUrl;

    protected CasAuthenticatedServiceClient(HttpClient httpClient, CasClient casClient, String serviceUrl) {
        log.info("Initializing CasAuthenticatedServiceClient for service {}", serviceUrl);
        this.httpClient = httpClient;
        this.casClient = casClient;
        this.serviceUrl = serviceUrl;
    }

    protected HttpResponse<String> sendRequest(HttpRequest.Builder requestBuilder) throws IOException, InterruptedException {
        log.info("Sending CAS authenticated request");
        requestBuilder.timeout(Duration.ofSeconds(35))
                .header("Caller-Id", Constants.CALLER_ID)
                .header("CSRF", "CSRF")
                .header("Cookie", "CSRF=CSRF");
        var response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

        if (isLoginToCas(response)) {
            log.info("Was redirected to CAS login");
            authenticateWithJSpringCasSecurityCheckEndpoint();
            return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        }

        if (response.statusCode() == 401) {
            log.info("Received HTTP 401 response");
            authenticateWithJSpringCasSecurityCheckEndpoint();
            return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        } else {
            return response;
        }
    }

    private void authenticateWithJSpringCasSecurityCheckEndpoint() throws IOException, InterruptedException {
        var uri = URI.create(serviceUrl + "/j_spring_cas_security_check" + "?ticket=" + fetchCasServiceTicket());
        var authRequest = HttpRequest.newBuilder(uri)
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        var authResponse = httpClient.send(authRequest, HttpResponse.BodyHandlers.ofString());
        log.info("Auth reset response: {}", authResponse);
    }

    private String fetchCasServiceTicket() throws IOException, InterruptedException {
        log.info("Refreshing CAS ticket");
        return casClient.getTicket(serviceUrl);
    }

    private boolean isLoginToCas(HttpResponse<?> response) {
        if (response.statusCode() == 302) {
            var header = response.headers().firstValue("Location");
            return header.map(location -> location.contains("/cas/login")).orElse(false);
        }
        return false;
    }
}

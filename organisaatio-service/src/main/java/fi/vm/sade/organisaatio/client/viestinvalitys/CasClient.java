package fi.vm.sade.organisaatio.client.viestinvalitys;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

public class CasClient {
    private final Logger log = LogManager.getLogger(this.getClass());
    public static final String CAS_SECURITY_TICKET = "CasSecurityTicket";
    private final HttpClient httpClient;
    private final String baseUrl;
    private final String username;
    private final String password;

    public CasClient(HttpClient httpClient, String baseUrl, String username, String password) {
        log.info("Initializing CasClient for CAS server at {}", baseUrl);
        this.baseUrl = baseUrl;
        this.httpClient = httpClient;
        this.username = username;
        this.password = password;
    }

    public String getTicket(String service) {
        try {
            return getServiceTicket(service);
        } catch (IOException | InterruptedException e) {
            log.error("Failed to get service ticket", e);
            throw new RuntimeException(e);
        }
    }

    private String getServiceTicket(String service) throws IOException, InterruptedException {
        log.info("Fetching service ticket for service {}...", service);
        var ticketGrantingTicket = getTicketGrantingTicket(username, password);
        var request = HttpRequest.newBuilder(URI.create(baseUrl + "/v1/tickets/" + ticketGrantingTicket))
                .POST(formBody(Map.of("service", service + "/j_spring_cas_security_check")))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(Duration.ofSeconds(10))
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            var serviceTicket = response.body();
            log.info("Successfully got service ticket: {}", serviceTicket);
            return serviceTicket;
        } else {
            var msg = "Failed to get service ticket: " + response.statusCode() + " " + response.body();
            log.error(msg);
            throw new RuntimeException(msg);
        }
    }

    private String getTicketGrantingTicket(String username, String password) throws IOException, InterruptedException {
        log.info("Fetching TGT (Ticket Granting Ticket) from CAS server...", baseUrl);
        var request = HttpRequest.newBuilder(URI.create(baseUrl + "/v1/tickets"))
                .POST(formBody(Map.of("username", username, "password", password)))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 201) {
            var location = response.headers().firstValue("Location").get();
            var tgt = location.substring(location.lastIndexOf("/") + 1);
            log.info("Successfully fetched TGT (Ticket Granting Ticket): {}", tgt);
            return tgt;
        } else {
            var msg = "Failed to get ticket granting ticket: " + response.statusCode() + " " + response.body();
            log.error(msg);
            throw new RuntimeException(msg);
        }
    }

    private HttpRequest.BodyPublisher formBody(Map<String, String> params) {
        var body = new StringBuilder();
        for (var entry : params.entrySet()) {
            if (body.length() > 0) body.append("&");
            body.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            body.append("=");
            body.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(body.toString());
    }
}
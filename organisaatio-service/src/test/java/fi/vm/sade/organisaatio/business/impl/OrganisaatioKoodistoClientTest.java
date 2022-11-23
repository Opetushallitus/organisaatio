package fi.vm.sade.organisaatio.business.impl;

import com.github.tomakehurst.wiremock.WireMockServer;
import fi.vm.sade.organisaatio.client.OrganisaatioKoodistoClient;
import fi.vm.sade.organisaatio.config.HttpClientConfiguration;
import fi.vm.sade.properties.OphProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        HttpClientConfiguration.class,
        OrganisaatioKoodistoClient.class
})
public class OrganisaatioKoodistoClientTest {

    @Autowired
    private OrganisaatioKoodistoClient client;

    @MockBean
    private OphProperties ophProperties;

    private WireMockServer server;

    @BeforeEach
    public void startServer() {
        server = new WireMockServer(options().dynamicPort());
        server.start();
    }

    @AfterEach
    public void stopServer() {
        server.resetAll();
        server.stop();
    }

    @Test
    public void getWithStatus200ShouldReturnBody() {
        server.stubFor(get(urlEqualTo("/test")).willReturn(aResponse().withStatus(200).withBody("test body")));
        String response = client.get("http://localhost:" + server.port() + "/test");
        assertThat(response).isEqualTo("test body");
    }

    @Test
    public void getWithStatus404ShouldReturnNull() {
        server.stubFor(get(urlEqualTo("/test")).willReturn(aResponse().withStatus(404).withBody("test body")));
        String response = client.get("http://localhost:" + server.port() + "/test");
        assertThat(response).isNull();
    }

    @Test
    public void getWithStatus500ShouldReturnNull() {
        server.stubFor(get(urlEqualTo("/test")).willReturn(aResponse().withStatus(500).withBody("test body")));
        String response = client.get("http://localhost:" + server.port() + "/test");
        assertThat(response).isNull();
    }

}

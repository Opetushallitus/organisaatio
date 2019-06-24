package fi.vm.sade.organisaatio.business.impl;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(locations = {"classpath:spring/test-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class OrganisaatioKoodistoClientTest {

    @Autowired
    private OrganisaatioKoodistoClient client;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort().dynamicHttpsPort());

    @Test
    public void getWithStatus200ShouldReturnBody() {
        stubFor(get(urlEqualTo("/test")).willReturn(aResponse().withStatus(200).withBody("test body")));

        String response = client.get("http://localhost:" + wireMockRule.port() + "/test");

        assertThat(response).isEqualTo("test body");
    }

    @Test
    public void getWithStatus404ShouldReturnNull() {
        stubFor(get(urlEqualTo("/test")).willReturn(aResponse().withStatus(404).withBody("test body")));

        String response = client.get("http://localhost:" + wireMockRule.port() + "/test");

        assertThat(response).isNull();
    }

    @Test
    public void getWithStatus500ShouldReturnNull() {
        stubFor(get(urlEqualTo("/test")).willReturn(aResponse().withStatus(500).withBody("test body")));

        String response = client.get("http://localhost:" + wireMockRule.port() + "/test");

        assertThat(response).isNull();
    }

}

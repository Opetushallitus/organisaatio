package fi.vm.sade.organisaatio.resource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HealthCheckImplSpringBootTest {
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void greetingShouldReturnDefaultMessage() throws Exception {
        String url = "http://localhost:" + port + contextPath + "/";
        Long res = Long.parseLong(this.restTemplate.getForObject(url,
                String.class));
        System.out.println(String.format("%s %s", url, res));
        assertThat(res).isLessThan(System.currentTimeMillis());
    }
}
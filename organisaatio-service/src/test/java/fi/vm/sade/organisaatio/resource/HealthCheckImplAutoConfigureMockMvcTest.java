package fi.vm.sade.organisaatio.resource.provider;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@AutoConfigureMockMvc
class HealthCheckImplAutoConfigureMockMvcTest {
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Value("${server.service.context-path}")
    private String path;


    @Autowired
    private MockMvc mockMvc;

    @Test
    void greetingShouldReturnDefaultMessage() throws Exception {
        String url = "http://localhost:" + port + contextPath + path + "/";
        this.mockMvc.perform(get(url)).andDo(print()).andExpect(status().isOk())
                .andExpect(isLessThan(System.currentTimeMillis());
    }
}
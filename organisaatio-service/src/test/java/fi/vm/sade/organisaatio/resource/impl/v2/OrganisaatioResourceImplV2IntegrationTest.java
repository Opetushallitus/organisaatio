package fi.vm.sade.organisaatio.resource.impl.v2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql({"/data//truncate_tables.sql"})
class OrganisaatioResourceImplV2IntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Case Riveria Niskala")
    @Sql({"/data/riveria_organisaatio_data.sql"})
    @WithMockUser(value = "1.2.3.4.5", authorities = {"FOO"})
    void testRiveriaNiskala() throws Exception {
        this.mockMvc.perform(get("/rest/organisaatio/v2/1.2.246.562.10.87575903965/nimet"))
                .andExpect(status().isOk())
                .andExpect(content().json(readFile("/fixtures/resource/v2/riveria.json")));
    }

    @Test
    @DisplayName("Case Sataedu Kokemäki")
    @Sql({"/data/sataedu_organisaatio_data.sql"})
    @WithMockUser(value = "1.2.3.4.5", authorities = {"FOO"})
    void testSataeduKokemaki() throws Exception {
        this.mockMvc.perform(get("/rest/organisaatio/v2/1.2.246.562.10.58061828313/nimet"))
                .andExpect(status().isOk())
                .andExpect(content().json(readFile("/fixtures/resource/v2/sataedu.json")));
    }

    private String readFile(String fileName) throws Exception {
        return Files.readString(Paths.get(Objects.requireNonNull(getClass().getResource(fileName)).toURI()), StandardCharsets.UTF_8);
    }
}
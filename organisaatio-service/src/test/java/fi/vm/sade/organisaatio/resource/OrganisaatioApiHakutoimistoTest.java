package fi.vm.sade.organisaatio.resource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrganisaatioApiHakutoimistoTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Organisaatio not found")
    @Sql({"/data/truncate_tables.sql"})
    @WithMockUser(value = "1.2.3.4.5", authorities = {"FOO"})
    void test1() throws Exception {
          this.mockMvc.perform(get("/api/no-oid/hakutoimisto"))
                .andExpect(status().isNotFound())
                .andExpect(content().json("{" +
                        "\"errorMessage\":\"Hakutoimistoa ei löytynyt, ylin organisaatio no-oid\"," +
                        "\"errorKey\":\"organisaatio.exception.organisaatio.hakutoimisto.not.found\"}"));
    }

    @Test
    @DisplayName("Hakutoimisto not found")
    @Sql({"/data/truncate_tables.sql"})
    @Sql({"/data/basic_organisaatio_data.sql"})
    @WithMockUser(value = "1.2.3.4.5", authorities = {"FOO"})//1.2.2004.2
    void test2() throws Exception {
        this.mockMvc.perform(get("/api/1.2.2004.2/hakutoimisto"))
                .andExpect(status().isNotFound())
                .andExpect(content().json("{" +
                        "\"errorMessage\":\"Hakutoimistoa ei löytynyt, ylin organisaatio 1.2.2004.2\"," +
                        "\"errorKey\":\"organisaatio.exception.organisaatio.hakutoimisto.not.found\"}"));
    }

    @Test
    @DisplayName("Hakutoimisto found")
    @Sql({"/data/truncate_tables.sql"})
    @Sql({"/data/basic_organisaatio_data.sql"})
    @WithMockUser(value = "1.2.3.4.5", authorities = {"FOO"})
    void test3() throws Exception {
        this.mockMvc.perform(get("/api/1.2.2004.4/hakutoimisto"))
                .andExpect(status().isNotFound())
                .andExpect(content().json("{" +
                        "\"errorMessage\":\"Hakutoimistoa ei löytynyt, ylin organisaatio 1.2.2004.4\"," +
                        "\"errorKey\":\"organisaatio.exception.organisaatio.hakutoimisto.not.found\"}"));
    }

}
package fi.vm.sade.organisaatio.resource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
class OrganisaatioApiSpringTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Test empty database")
    @Sql("/data/truncate_tables.sql")
    void testOids1() throws Exception {
        mockMvc.perform(get("/api/oids"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("Test content in database")
    @Sql("/data/truncate_tables.sql")
    @Sql("/data/basic_organisaatio_data.sql")
    void testOids2() throws Exception {
        mockMvc.perform(get("/api/oids"))
                .andExpect(status().isOk())
                .andExpect(content().json("[\"1.2.246.562.24.00000000001\",\"1.2.2004.1\",\"1.2.2004.2\",\"1.2.2004.3\",\"1.2.2004.4\",\"1.2.2005.4\",\"1.2.2004.5\",\"1.2.2004.6\",\"1.2.2005.5\",\"1.2.8000.1\"]"));

        mockMvc.perform(get("/api/oids").param("type", "KOULUTUSTOIMIJA"))
                .andExpect(status().isOk())
                .andExpect(content().json("[\"1.2.246.562.24.00000000001\",\"1.2.2004.1\",\"1.2.2004.5\"]"));

        mockMvc.perform(get("/api/oids").param("count", "2"))
                .andExpect(status().isOk())
                .andExpect(content().json("[\"1.2.246.562.24.00000000001\",\"1.2.2004.1\"]"));

    }
}

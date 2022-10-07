package fi.vm.sade.organisaatio.resource;

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
class OrganisaatioApiRyhmatTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("No ryhmat in DB")
    @Sql({"/data/truncate_tables.sql"})
    void testEmptyRyhmat() throws Exception {
        this.mockMvc.perform(get("/api/ryhmat"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("Some ryhmat in DB")
    @Sql({"/data/truncate_tables.sql"})
    @Sql({"/data/ryhma_organisaatio_data.sql"})
    void testSomeRyhmat() throws Exception {
        this.mockMvc.perform(get("/api/ryhmat"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"oid\":\"1.2.2004.2\"}]",false));
    }

    private String readFile(String fileName) throws Exception {
        return Files.readString(Paths.get(Objects.requireNonNull(getClass().getResource(fileName)).toURI()), StandardCharsets.UTF_8);
    }
}
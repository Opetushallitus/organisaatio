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
class OrganisaatioApiJalkelaisetTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Basic data, get jalkelaiset of root")
    @Sql({"/data/truncate_tables.sql"})
    @Sql({"/data/basic_organisaatio_data.sql"})
    @OrganisaatioNimiMaskingTest.OPHUser
    void testBasicAllJalkelaiset() throws Exception {
        this.mockMvc.perform(get("/api/{rootOid}/jalkelaiset", "1.2.246.562.24.00000000001"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        readFile("/fixtures/resource/api/jalkelaiset.json")
                        , true));
    }

    @Test
    @DisplayName("Basic data, get jalkelaiset of root without OPH user permissions")
    @Sql({"/data/truncate_tables.sql"})
    @Sql({"/data/basic_organisaatio_data.sql"})
    @OrganisaatioNimiMaskingTest.AnonymousUser
    void testBasicAllJalkelaisetAsAnonymousUser() throws Exception {
        this.mockMvc.perform(get("/api/{rootOid}/jalkelaiset", "1.2.246.562.24.00000000001"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        readFile("/fixtures/resource/api/jalkelaiset-masked.json")
                        , true));
    }

    private String readFile(String fileName) throws Exception {
        return Files.readString(Paths.get(Objects.requireNonNull(getClass().getResource(fileName)).toURI()), StandardCharsets.UTF_8);
    }
}
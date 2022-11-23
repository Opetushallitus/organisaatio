package fi.vm.sade.organisaatio.resource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrganisaatioApiMuutetutTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Hae muutetut organisaatiot")
    @Sql({"/data/truncate_tables.sql"})
    @Sql({"/data/basic_organisaatio_data.sql"})
    void haeMuutetut() throws Exception {
        this.mockMvc.perform(get("/api/muutetut?lastModifiedSince=2012-12-12"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"ytunnus\":\"1234567-2\",\"oid\":\"1.2.2004.2\"}]",false));

    }

    @Test
    @DisplayName("Hae muutetut organisaatioiden oidit")
    @Sql({"/data/truncate_tables.sql"})
    @Sql({"/data/basic_organisaatio_data.sql"})
    void haeMuutettujenOid() throws Exception {
        this.mockMvc.perform(get("/api/muutetut/oid?lastModifiedSince=2009-11-06"))
                .andExpect(status().isOk())
                .andExpect(content().json("[\"1.2.2005.4\",\"1.2.2004.2\"]"));
    }

    @Test
    @DisplayName("Hae muutetut organisaatioiden oidit, virheellinen syöte")
    @Sql({"/data/truncate_tables.sql"})
    @Sql({"/data/basic_organisaatio_data.sql"})
    void haeMuutettujenOidErrors() throws Exception {
        this.mockMvc.perform(get("/api/muutetut/oid?lastModifiedSince=2009-11-66"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{" +
                        "\"parameters\":[\"lastModifiedSince\"]," +
                        "\"errorMessage\":\"unable to parse (2009-11-66) supported formats are yyyy-MM-dd, yyyy-MM-dd HH:mm, yyyy-MM-dd'T'HH:mm, yyyy-MM-dd'T'HH:mm:ss\"," +
                        "\"errorKey\": \"method.argument.type.mismatch\"}"));

        this.mockMvc.perform(get("/api/muutetut/oid?lastModifiedSince=2009-11-26&organisaatioTyypit=FOO"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{" +
                        "\"parameters\":[\"organisaatioTyypit\"]," +
                        "\"errorMessage\":\"No enum constant fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi.FOO\"," +
                        "\"errorKey\": \"method.argument.type.mismatch\"}"));

        this.mockMvc.perform(get("/api/muutetut/oid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{" +
                        "\"parameters\":[\"lastModifiedSince\"]," +
                        "\"errorMessage\":\"Missing parameter\"," +
                        "\"errorKey\": \"mising.servlet.request.parameter\"}"));
    }
}
package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.business.OrganisaatioFindBusinessService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class OrganisaatioApiSpringTest {
    @Autowired
    private MockMvc mockMvc;

    @SpyBean
    OrganisaatioFindBusinessService organisaatioFindBusinessService;

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

    @Test
    @DisplayName("Test parent oids")
    @Sql("/data/truncate_tables.sql")
    @Sql("/data/basic_organisaatio_data.sql")
    void testParentOids1() throws Exception {
        mockMvc.perform(get("/api/1.2.2004.1/parentoids"))
                .andExpect(status().isOk())
                .andExpect(content().json("[\"1.2.246.562.24.00000000001\",\"1.2.2004.1\"]"));
    }

    @Test
    @DisplayName("Test child oids")
    @Sql("/data/truncate_tables.sql")
    @Sql("/data/basic_organisaatio_data.sql")
    void testChildOids1() throws Exception {
        doReturn(List.of("foo", "bar")).when(organisaatioFindBusinessService).findChildOidsRecursive(any());
        mockMvc.perform(get("/api/1.2.2004.1/childoids").param("rekursiivisesti", "true"))
                .andExpect(status().isOk())
                .andExpect(content().json("[\"foo\",\"bar\"]"));

        mockMvc.perform(get("/api/1.2.2004.1/childoids"))
                .andExpect(status().isOk())
                .andExpect(content().json("[\"1.2.2004.2\",\"1.2.2004.3\"]"));
    }

    @Test
    @DisplayName("Test child oids permission aspect")
    @Sql("/data/truncate_tables.sql")
    @Sql("/data/basic_organisaatio_data.sql")
    void testChildOids2() throws Exception {
        mockMvc.perform(get("/api/1.2.2020.1/childoids").param("rekursiivisesti", "true"))
                .andExpect(status().isForbidden())
                .andExpect(content().json("{\"errorMessage\":\"no.permission\",\"errorKey\":\"no.permission\"}"));
    }
}

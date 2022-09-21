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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

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
    @DisplayName("Test read permission aspect for protected oid")
    @Sql("/data/truncate_tables.sql")
    @Sql("/data/basic_organisaatio_data.sql")
    @WithMockUser(value = "1.2.3.4.5", roles={"APP_ORGANISAATIOHALLINTA"})
    void testReadProtection() throws Exception {
        String oid = "1.2.2020.1";
        String errorResponse = "{\"errorMessage\":\"Not authorized to read organisation: " + oid + "\",\"errorKey\":\"no.permission\"}";

        expectForbiddenAtPath(get("/api/{oid}/childoids", oid).param("rekursiivisesti", "true"), errorResponse);
        expectForbiddenAtPath(get("/api/{oid}/parentoids", oid), errorResponse);
        expectForbiddenAtPath(get("/api/{oid}/children", oid), errorResponse);
        expectForbiddenAtPath(get("/api/{oid}", oid), errorResponse);
        expectForbiddenAtPath(get("/api/{oid}/historia", oid), errorResponse);
        expectForbiddenAtPath(get("/api/{oid}/paivittaja", oid), errorResponse);
    }

    private void expectForbiddenAtPath(MockHttpServletRequestBuilder request, String errorResponse) throws Exception {
        mockMvc.perform(request)
                .andExpect(status().isForbidden())
                .andExpect(content().json(errorResponse));
    }
}

package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.business.OrganisaatioFindBusinessService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class OrganisaatioApiSpringTest {
    @Autowired
    private MockMvc mockMvc;

    @SpyBean
    OrganisaatioFindBusinessService organisaatioFindBusinessService;


    @Test
    @DisplayName("Test get oid")
    @Sql("/data/truncate_tables.sql")
    @Sql("/data/basic_organisaatio_data.sql")
    void testGetOid1() throws Exception {
        mockMvc.perform(get("/api/1.2.246.562.24.00000000001"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"oid\": \"1.2.246.562.24.00000000001\"}"));
    }

    @Test
    @DisplayName("Test empty database get oid")
    @Sql("/data/truncate_tables.sql")
    void testGetOid2() throws Exception {
        mockMvc.perform(get("/api/1.2.246.562.24.00000000001"))
                .andExpect(status().isNotFound())
                .andExpect(content().json("{" +
                        "\"errorMessage\":\"organisaatio.exception.organisaatio.not.found\"," +
                        "\"errorKey\":\"organisaatio.exception.organisaatio.not.found\"}", true));
    }

    @Test
    @DisplayName("Test get oid")
    @Sql("/data/truncate_tables.sql")
    @Sql("/data/basic_organisaatio_data.sql")
    @WithMockUser(value = "1.2.3.4.5", roles = {"APP_ORGANISAATIOHALLINTA", "APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.24.00000000001"})
    void testDeleteOid1() throws Exception {
        mockMvc.perform(delete("/api/1.2.2004.1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{" +
                        "\"errorMessage\":\"organisaatio.exception.delete.parent\"," +
                        "\"errorKey\":\"organisaatio.exception.delete.parent\"}", true));
    }

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
                .andExpect(content().json("[\"1.2.246.562.24.00000000001\",\"1.2.2004.1\",\"1.2.2004.2\",\"1.2.2004.3\",\"1.2.2004.4\",\"1.2.2005.4\",\"1.2.2004.5\",\"1.2.2004.6\",\"1.2.2005.5\",\"1.2.8000.1\",\"1.2.8001.1\",\"1.2.8001.2\"]"));

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
    @WithMockUser(value = "1.2.3.4.5", roles = {"APP_ORGANISAATIOHALLINTA"})
    void testReadProtection() throws Exception {
        String oid = "1.2.2020.1";
        String errorResponse = "{\"errorMessage\":\"Not authorized to read organisation: " + oid + "\",\"errorKey\":\"no.permission\"}";

        expectForbiddenAtPath(get("/api/{oid}/childoids", oid).param("rekursiivisesti", "true"), errorResponse);
        expectForbiddenAtPath(get("/api/{oid}/parentoids", oid), errorResponse);
        expectForbiddenAtPath(get("/api/{oid}/children", oid), errorResponse);
        expectForbiddenAtPath(get("/api/{oid}", oid), errorResponse);
        expectForbiddenAtPath(get("/api/{oid}/historia", oid), errorResponse);
        expectForbiddenAtPath(get("/api/{oid}/paivittaja", oid), errorResponse);
        expectForbiddenAtPath(get("/api/{oid}/jalkelaiset", oid), errorResponse);
    }

    @Test
    @DisplayName("Test CUD permission aspect")
    @Sql("/data/truncate_tables.sql")
    @Sql("/data/basic_organisaatio_data.sql")
    @WithMockUser(value = "1.2.3.4.5", roles = {"APP_ORGANISAATIOHALLINTA"})
    void testCUDProtection() throws Exception {
        String oid = "1.2.2020.1";
        expectForbiddenAtPath(put("/api/{oid}", oid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oid\": \"" + oid + "\"}"),
                "{\"errorMessage\":\"Not authorized to update organisation: " + oid + "\",\"errorKey\":\"no.permission\"}");

        expectForbiddenAtPath(post("/api/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oid\": \"" + oid + "\",\"parentOid\": \"9.9.9.9.9\"}"),
                "{\"errorMessage\":\"Not authorized to create child organisation for 9.9.9.9.9\",\"errorKey\":\"no.permission\"}");
        expectForbiddenAtPath(delete("/api/{oid}", oid),
                "{\"errorMessage\":\"Not authorized to delete organisation: " + oid + "\",\"errorKey\":\"no.permission\"}");

    }

    @Test
    @DisplayName("Test tarkasta permission aspect")
    @Sql("/data/truncate_tables.sql")
    @Sql("/data/basic_organisaatio_data.sql")
    @WithMockUser(value = "1.2.3.4.5", roles = {"APP_ORGANISAATIOHALLINTA"})
    void testTarkastaProtection() throws Exception {
        String oid = "1.2.2020.1";
        expectForbiddenAtPath(put("/api/{oid}/tarkasta", oid)
                        .contentType(MediaType.APPLICATION_JSON),
                "{\"errorMessage\":\"Not authorized to update tarkastus for organisation: " + oid + "\",\"errorKey\":\"no.permission\"}");
    }

    @Test
    @DisplayName("Test name CUD permission aspect ")
    @Sql("/data/truncate_tables.sql")
    @Sql("/data/basic_organisaatio_data.sql")
    @WithMockUser(value = "1.2.3.4.5", roles = {"APP_ORGANISAATIOHALLINTA"})
    void testNameCUDProtection() throws Exception {

        String nimiOid = "1.2.2004.1";
        expectForbiddenAtPath(post("/api/{oid}/nimet", nimiOid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oid\": \"" + nimiOid + "\", " +
                                "\"nimi\":{\"fi\": \"foo\"} }"),
                "{\"errorMessage\":\"Not authorized to update name for organisation: " + nimiOid + "\",\"errorKey\":\"no.permission\"}");

        expectForbiddenAtPath(put("/api/{oid}/nimet", nimiOid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oid\": \"" + nimiOid + "\", " +
                                "\"nimi\":{\"fi\": \"foo\"} }"),
                "{\"errorMessage\":\"Not authorized to update name for organisation: " + nimiOid + "\",\"errorKey\":\"no.permission\"}");

        expectForbiddenAtPath(delete("/api/{oid}/nimet", nimiOid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oid\": \"" + nimiOid + "\", " +
                                "\"nimi\":{\"fi\": \"foo\"} }"),
                "{\"errorMessage\":\"Not authorized to update name for organisation: " + nimiOid + "\",\"errorKey\":\"no.permission\"}");

    }

    private void expectForbiddenAtPath(MockHttpServletRequestBuilder request, String errorResponse) throws Exception {
        mockMvc.perform(request)
                .andExpect(status().isForbidden())
                .andExpect(content().json(errorResponse));
    }

    @Test
    @DisplayName("Test /api/oids happy path ")
    @Sql("/data/truncate_tables.sql")
    @Sql("/data/basic_organisaatio_data.sql")
    void testOids() throws Exception {
        mockMvc.perform(get("/api/oids"))
                .andExpect(status().isOk())
                .andExpect(content().json("[\"1.2.246.562.24.00000000001\",\"1.2.2004.1\",\"1.2.2004.2\",\"1.2.2004.3\",\"1.2.2004.4\",\"1.2.2005.4\",\"1.2.2004.5\",\"1.2.2004.6\",\"1.2.2005.5\",\"1.2.8000.1\",\"1.2.8001.1\",\"1.2.8001.2\"]"));
        ;

        mockMvc.perform(get("/api/oids")
                        .param("type", "KOULUTUSTOIMIJA"))
                .andExpect(status().isOk())
                .andExpect(content().json("[\"1.2.246.562.24.00000000001\",\"1.2.2004.1\",\"1.2.2004.5\"]"));
        ;


        mockMvc.perform(get("/api/oids")
                        .param("count", "5"))
                .andExpect(status().isOk())
                .andExpect(content().json("[\"1.2.246.562.24.00000000001\",\"1.2.2004.1\",\"1.2.2004.2\",\"1.2.2004.3\",\"1.2.2004.4\"]"));
        ;

        mockMvc.perform(get("/api/oids")
                        .param("count", "5")
                        .param("startIndex", "5"))
                .andExpect(status().isOk())
                .andExpect(content().json("[\"1.2.2005.4\",\"1.2.2004.5\",\"1.2.2004.6\",\"1.2.2005.5\",\"1.2.8000.1\"]"));
        ;
    }

    @Test
    @DisplayName("Test /api/oids validation ")
    @Sql("/data/truncate_tables.sql")
    @Sql("/data/basic_organisaatio_data.sql")
    void testOidsValidation() throws Exception {
        mockMvc.perform(get("/api/oids")
                        .param("count", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{" +
                        "\"parameters\":[\"oids.count\"]," +
                        "\"errorMessage\":\"oids.count: must be greater than or equal to 0\"," +
                        "\"errorKey\":\"constraint.violation\"}"));

        mockMvc.perform(get("/api/oids")
                        .param("startIndex", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{" +
                        "\"parameters\":[\"oids.startIndex\"]," +
                        "\"errorMessage\":\"oids.startIndex: must be greater than or equal to 0\"," +
                        "\"errorKey\":\"constraint.violation\"}"));

        mockMvc.perform(get("/api/oids")
                        .param("type", "FOO"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("" +
                        "{\"parameters\":[\"type\"]," +
                        "\"errorMessage\":\"No enum constant fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi.FOO\"," +
                        "\"errorKey\":\"method.argument.type.mismatch\"}"));
    }
}

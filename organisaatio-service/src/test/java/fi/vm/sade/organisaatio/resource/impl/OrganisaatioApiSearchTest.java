package fi.vm.sade.organisaatio.resource.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrganisaatioApiSearchTest {
    @Autowired
    private MockMvc mockMvc;


    @Test
    @DisplayName("Test /api/hae")
    @Sql({"/data/truncate_tables.sql"})
    @Sql({"/data/basic_organisaatio_data.sql"})
    void testApiHae() throws Exception {
        mockMvc.perform(get("/api/hae")
                        .param("aktiiviset", "true")
                        .param("suunnitellut", "false")
                        .param("lakkautetut", "false")
                        .param("oidRestrictionList", "1.2.2004.2")
                        .param("oidRestrictionList", "1.2.2005.4"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"numHits\":2," +
                                "\"organisaatiot\":[" +
                                "{\"oid\":\"1.2.2004.2\",\"alkuPvm\":1372453200000,\"parentOid\":\"1.2.2004.1\",\"parentOidPath\":\"1.2.2004.2/1.2.2004.1/1.2.246.562.24.00000000001\",\"ytunnus\":\"1234567-2\",\"oppilaitostyyppi\":\"oppilaitostyyppi_41#1\",\"toimipistekoodi\":\"123451\",\"match\":true,\"nimi\":{\"fi\":\"root test koulutustoimija, node1 asd\"},\"lyhytNimi\":{\"fi\":\"node1 asd\"},\"kieletUris\":[],\"kotipaikkaUri\":\"Helsinki\",\"children\":[],\"subRows\":[],\"status\":\"AKTIIVINEN\",\"organisaatiotyypit\":[\"organisaatiotyyppi_02\",\"organisaatiotyyppi_04\",\"organisaatiotyyppi_03\"],\"aliOrganisaatioMaara\":0}," +
                                "{\"oid\":\"1.2.2005.4\",\"alkuPvm\":1283029200000,\"parentOid\":\"1.2.2004.3\",\"parentOidPath\":\"1.2.2005.4/1.2.2004.3/1.2.2004.1/1.2.246.562.24.00000000001\",\"ytunnus\":\"1234568-4\",\"oppilaitostyyppi\":\"oppilaitostyyppi_42#1\",\"match\":true,\"nimi\":{\"fi\":\"root test koulutustoimija, node2 foo, node23 foo bar\"},\"lyhytNimi\":{\"fi\":\"node23 foo bar\"},\"kieletUris\":[],\"kotipaikkaUri\":\"Helsinki\",\"children\":[],\"subRows\":[],\"status\":\"AKTIIVINEN\",\"organisaatiotyypit\":[\"organisaatiotyyppi_02\",\"organisaatiotyyppi_04\",\"organisaatiotyyppi_03\"],\"aliOrganisaatioMaara\":0}" +
                                "]}",
                        false));
    }

    @Test
    @DisplayName("Test /api/hae/nimi")
    @Sql({"/data/truncate_tables.sql"})
    @Sql({"/data/basic_organisaatio_data.sql"})
    void testApiHaeNimi() throws Exception {
        mockMvc.perform(get("/api/hae/nimi")
                        .param("aktiiviset", "true")
                        .param("suunnitellut", "false")
                        .param("lakkautetut", "false")
                        .param("oidRestrictionList", "1.2.2004.2")
                        .param("oidRestrictionList", "1.2.2005.4"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"numHits\":2," +
                                "\"items\":[" +
                                "{\"oid\":\"1.2.2005.4\",\"nimi\":{\"fi\":\"root test koulutustoimija, node2 foo, node23 foo bar\"}}," +
                                "{\"oid\":\"1.2.2004.2\",\"nimi\":{\"fi\":\"root test koulutustoimija, node1 asd\"}}" +
                                "]}",
                        false));
    }

    @Test
    @DisplayName("Test /api/hae/tyyppi")
    @Sql({"/data/truncate_tables.sql"})
    @Sql({"/data/basic_organisaatio_data.sql"})
    void testApiHaeTyyppi() throws Exception {

        //mockMvc.perform(get("/rest/organisaatio/v2/hae/tyyppi")
        mockMvc.perform(get("/api/hae/tyyppi")
                        .param("aktiiviset", "true")
                        .param("suunnitellut", "false")
                        .param("lakkautetut", "false")
                        .param("oidRestrictionList", "1.2.2004.2")
                        .param("oidRestrictionList", "1.2.2005.4"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"numHits\":2," +
                                "\"items\":[" +
                                "{\"oid\":\"1.2.2004.2\",\"nimi\":{\"fi\":\"root test koulutustoimija, node1 asd\"}," +
                                "\"oppilaitostyyppi\":\"oppilaitostyyppi_41#1\"," +
                                "\"tyypit\":[\"organisaatiotyyppi_02\",\"organisaatiotyyppi_04\",\"organisaatiotyyppi_03\"]}," +
                                "{\"oid\":\"1.2.2005.4\",\"nimi\":{\"fi\":\"root test koulutustoimija, node2 foo, node23 foo bar\"}," +
                                "\"oppilaitostyyppi\":\"oppilaitostyyppi_42#1\"," +
                                "\"tyypit\":[\"organisaatiotyyppi_02\",\"organisaatiotyyppi_04\",\"organisaatiotyyppi_03\"]}" +
                                "]}",
                        true));
    }
}
package fi.vm.sade.organisaatio.model.listeners;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProtectedDataListenerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Liitokset with OPH role")
    @OPHUser
    void testLiitoksetWithRole() throws Exception {
        this.mockMvc.perform(get("/api/liitokset"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{" +
                        "\"organisaatio\":{" +
                        "\"oid\":\"1.2.8001.1\"," +
                        "\"nimi\":{\"fi\":\"Liitostesti\"}," +
                        "\"status\":\"AKTIIVINEN\"," +
                        "\"tyypit\":[]" +
                        "}," +
                        "\"kohde\":{" +
                        "\"oid\":\"1.2.2020.1\"," +
                        "\"nimi\":{\"fi\":\"Varhaiskasvatuksen toimipiste\"}," +
                        "\"status\":\"AKTIIVINEN\"," +
                        "\"tyypit\":[\"Varhaiskasvatuksen toimipaikka\"]" +
                        "}," +
                        "\"alkuPvm\":\"2014-12-02\"" +
                        "}]", false));
    }

    @Test
    @DisplayName("Liitokset without role")
    @AnonymousUser
    void testLiitoksetWithoutRole() throws Exception {
        this.mockMvc.perform(get("/api/liitokset"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "[{" +
                                "\"organisaatio\":" +
                                "{\"oid\":\"1.2.8001.1\"," +
                                "\"nimi\":{\"fi\":\"Liitostesti\"}" +
                                ",\"status\":\"AKTIIVINEN\"," +
                                "\"tyypit\":[]}" +
                                ",\"kohde\":" +
                                "{\"oid\":\"1.2.2020.1\"," +
                                "\"nimi\":{\"sv\":\"Dold (1.2.2020.1)\",\"fi\":\"Piilotettu (1.2.2020.1)\",\"en\":\"Hidden (1.2.2020.1)\"}," +
                                "\"status\":\"AKTIIVINEN\"," +
                                "\"tyypit\":[\"Varhaiskasvatuksen toimipaikka\"]}" +
                                ",\"alkuPvm\":\"2014-12-02\"" +
                                "}]", false));
    }

    @Test
    @DisplayName("Liitokset with limited role")
    @LimitedUser
    void testLiitoksetWithLimitedRole() throws Exception {
        this.mockMvc.perform(get("/api/liitokset"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "[{" +
                                "\"organisaatio\":" +
                                "{\"oid\":\"1.2.8001.1\"," +
                                "\"nimi\":{\"fi\":\"Liitostesti\"}" +
                                ",\"status\":\"AKTIIVINEN\"," +
                                "\"tyypit\":[]}" +
                                ",\"kohde\":" +
                                "{\"oid\":\"1.2.2020.1\"," +
                                "\"nimi\":{\"sv\":\"Dold (1.2.2020.1)\",\"fi\":\"Piilotettu (1.2.2020.1)\",\"en\":\"Hidden (1.2.2020.1)\"}," +
                                "\"status\":\"AKTIIVINEN\"," +
                                "\"tyypit\":[\"Varhaiskasvatuksen toimipaikka\"]}" +
                                ",\"alkuPvm\":\"2014-12-02\"" +
                                "}]", false));
    }

    @Test
    @DisplayName("Names with OPH role")
    @OPHUser
    void testNimetWithOPHRole() throws Exception {
        this.mockMvc.perform(get("/api/1.2.8001.2/nimet"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "[{\"nimi\":" +
                                "{\"fi\":\"Piilotustesti\"},\"alkuPvm\":\"1970-01-01\",\"version\":0}]",
                        false));
    }

    @Test
    @DisplayName("Names without role")
    @AnonymousUser
    void testNimetWithoutRole() throws Exception {
        this.mockMvc.perform(get("/api/1.2.8001.2/nimet"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "[" +
                                "{\"nimi\":{" +
                                "\"fi\":\"Yksityinen elinkeinonharjoittaja (6165189-7)\"," +
                                "\"sv\":\"Enskild näringsidkare (6165189-7)\"," +
                                "\"en\":\"Private trader (6165189-7)\"}," +
                                "\"alkuPvm\":\"1970-01-01\",\"version\":0}" +
                                "]", false));
    }

    @Test
    @DisplayName("Names with limited role")
    @LimitedUser
    void testNimetLimitedRole() throws Exception {
        this.mockMvc.perform(get("/api/1.2.8001.2/nimet"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "[" +
                                "{\"nimi\":{" +
                                "\"fi\":\"Yksityinen elinkeinonharjoittaja (6165189-7)\"," +
                                "\"sv\":\"Enskild näringsidkare (6165189-7)\"," +
                                "\"en\":\"Private trader (6165189-7)\"}," +
                                "\"alkuPvm\":\"1970-01-01\",\"version\":0}" +
                                "]", false));
    }

    @Test
    @DisplayName("Findbyoids with OPH role")
    @OPHUser
    void testFindByOidsWithRole() throws Exception {
        this.mockMvc.perform(post("/api/findbyoids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"1.2.8001.1\",\"1.2.8001.2\"]"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "[" +
                                "{" +
                                "\"oid\":\"1.2.8001.1\"," +
                                "\"yritysmuoto\":\"oy\"," +
                                "\"nimi\":{\"fi\":\"Liitostesti\"}" +
                                "}," +
                                "{" +
                                "\"yritysmuoto\":\"Yksityinen elinkeinonharjoittaja\"," +
                                "\"kotipaikkaUri\":\"Helsinki\"," +
                                "\"parentOidPath\":\"\"," +
                                "\"kayntiosoite\":{}," +
                                "\"postiosoite\":{}," +
                                "\"lisatiedot\":[]," +
                                "\"yhteystietoArvos\":[]," +
                                "\"piilotettu\":false," +
                                "\"lyhytNimi\":{\"fi\":\"Piilotustesti\"}," +
                                "\"oid\":\"1.2.8001.2\"," +
                                "\"nimet\":[{\"nimi\":{\"fi\":\"Piilotustesti\"},\"alkuPvm\":\"1970-01-01\",\"version\":0}]," +
                                "\"nimi\":{\"fi\":\"Piilotustesti\"}," +
                                "\"kuvaus2\":{}," +
                                "\"tyypit\":[]," +
                                "\"ytunnus\":\"6165189-7\"," +
                                "\"status\":\"AKTIIVINEN\"" +
                                "}]", false));
    }

    @Test
    @DisplayName("Findbyoids without role")
    @AnonymousUser
    void testFindByOidsWithOutRole() throws Exception {
        this.mockMvc.perform(post("/api/findbyoids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"1.2.8001.1\",\"1.2.8001.2\"]"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "[" +
                                "{" +
                                "\"oid\":\"1.2.8001.1\"," +
                                "\"yritysmuoto\":\"oy\"," +
                                "\"nimi\":{\"fi\":\"Liitostesti\"}" +
                                "}," +
                                "{" +
                                "\"yritysmuoto\":\"Yksityinen elinkeinonharjoittaja\"," +
                                "\"kotipaikkaUri\":\"Helsinki\"," +
                                "\"parentOidPath\":\"\"," +
                                "\"kayntiosoite\":{}," +
                                "\"postiosoite\":{}," +
                                "\"lisatiedot\":[]," +
                                "\"yhteystietoArvos\":[]," +
                                "\"piilotettu\":false," +
                                "\"lyhytNimi\":{\"fi\":\"Yksityinen elinkeinonharjoittaja (6165189-7)\"}," +
                                "\"oid\":\"1.2.8001.2\"," +
                                "\"nimet\":[{\"nimi\":{\"fi\":\"Yksityinen elinkeinonharjoittaja (6165189-7)\"},\"alkuPvm\":\"1970-01-01\",\"version\":0}]," +
                                "\"nimi\":{\"fi\":\"Yksityinen elinkeinonharjoittaja (6165189-7)\"}," +
                                "\"kuvaus2\":{}," +
                                "\"tyypit\":[]," +
                                "\"ytunnus\":\"6165189-7\"," +
                                "\"status\":\"AKTIIVINEN\"" +
                                "}]", false));
    }

    @Test
    @DisplayName("Findbyoids with limited role")
    @LimitedUser
    void testFindByOidsWithLimitedRole() throws Exception {
        this.mockMvc.perform(post("/api/findbyoids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"1.2.8001.1\",\"1.2.8001.2\"]"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "[" +
                                "{" +
                                "\"oid\":\"1.2.8001.1\"," +
                                "\"yritysmuoto\":\"oy\"," +
                                "\"nimi\":{\"fi\":\"Liitostesti\"}" +
                                "}," +
                                "{" +
                                "\"yritysmuoto\":\"Yksityinen elinkeinonharjoittaja\"," +
                                "\"kotipaikkaUri\":\"Helsinki\"," +
                                "\"parentOidPath\":\"\"," +
                                "\"kayntiosoite\":{}," +
                                "\"postiosoite\":{}," +
                                "\"lisatiedot\":[]," +
                                "\"yhteystietoArvos\":[]," +
                                "\"piilotettu\":false," +
                                "\"lyhytNimi\":{\"fi\":\"Yksityinen elinkeinonharjoittaja (6165189-7)\"," +
                                "\"sv\":\"Enskild näringsidkare (6165189-7)\"," +
                                "\"en\":\"Private trader (6165189-7)\"}," +
                                "\"oid\":\"1.2.8001.2\"," +
                                "\"nimet\":[{\"nimi\":{\"fi\":\"Yksityinen elinkeinonharjoittaja (6165189-7)\"," +
                                "\"sv\":\"Enskild näringsidkare (6165189-7)\"," +
                                "\"en\":\"Private trader (6165189-7)\"}" +
                                ",\"alkuPvm\":\"1970-01-01\",\"version\":0}]," +
                                "\"nimi\":{\"fi\":\"Yksityinen elinkeinonharjoittaja (6165189-7)\"," +
                                "\"sv\":\"Enskild näringsidkare (6165189-7)\"," +
                                "\"en\":\"Private trader (6165189-7)\"}," +
                                "\"kuvaus2\":{}," +
                                "\"tyypit\":[]," +
                                "\"ytunnus\":\"6165189-7\"," +
                                "\"status\":\"AKTIIVINEN\"" +
                                "}]", false));
    }
    @Test
    @DisplayName("Get with limited role, check masked")
    @LimitedUser
    void testGetWithLimitedRole() throws Exception {
        this.mockMvc.perform(get("/api/{oid}","1.2.8001.2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"katketty\": true}",false));
    }
    @Test
    @DisplayName("Get with OPH role, check not masked")
    @OPHUser
    void testGetWithOPHRole() throws Exception {
        this.mockMvc.perform(get("/api/{oid}","1.2.8001.2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"katketty\": false}",false));
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Sql({"/data/truncate_tables.sql"})
    @Sql({"/data/basic_organisaatio_data.sql"})
    @WithMockUser(value = "1.2.3.4.5", roles = {"APP_ORGANISAATIOHALLINTA", "APP_ORGANISAATIOHALLINTA_READ", "APP_ORGANISAATIOHALLINTA_READ_1.2.246.562.10.90008375488"})
    @interface LimitedUser {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Sql({"/data/truncate_tables.sql"})
    @Sql({"/data/basic_organisaatio_data.sql"})
    @WithAnonymousUser
    @interface AnonymousUser {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Sql({"/data/truncate_tables.sql"})
    @Sql({"/data/basic_organisaatio_data.sql"})
    @WithMockUser(roles = {"APP_ORGANISAATIOHALLINTA", "APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.24.00000000001"})
    @interface OPHUser {
    }

}
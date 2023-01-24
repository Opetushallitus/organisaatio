package fi.vm.sade.organisaatio.resource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
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
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@Sql("/data/truncate_tables.sql")
@Sql("/data/basic_organisaatio_data.sql")
class OrganisaatioNimiMaskingTest {
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
                                "\"parentOidPath\":\"|1.2.246.562.24.00000000001|\"," +
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
                                "\"tyypit\":[\"organisaatiotyyppi_07\"]," +
                                "\"ytunnus\":\"6165189-7\"," +
                                "\"status\":\"AKTIIVINEN\"," +
                                "\"yhteystiedot\":[" +
                                "{\"kieli\":\"kieli_fi#1\",\"yhteystietoOid\":\"1674212904.18573\",\"id\":\"51\",\"email\":\"testiorganisaatio13@example.com\"}," +
                                "{\"osoiteTyyppi\":\"posti\",\"kieli\":\"kieli_fi#1\",\"postinumeroUri\":\"posti_00960\",\"yhteystietoOid\":\"1674212910.814504\",\"id\":\"52\",\"postitoimipaikka\":\"Helsinki\",\"osoite\":\"Haapasaarentie 7\"}," +
                                "{\"kieli\":\"kieli_fi#1\",\"numero\":\"0400123456\",\"tyyppi\":\"puhelin\",\"yhteystietoOid\":\"1674212916.068001\",\"id\":\"53\"}," +
                                "{\"osoiteTyyppi\":\"kaynti\",\"kieli\":\"kieli_fi#1\",\"postinumeroUri\":\"posti_00960\",\"yhteystietoOid\":\"1674212896.872914\",\"id\":\"50\",\"postitoimipaikka\":\"Helsinki\",\"osoite\":\"Haapasaarentie 7\"}" +
                                "]" +
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
                                "\"parentOidPath\":\"|1.2.246.562.24.00000000001|\"," +
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
                                "\"tyypit\":[\"organisaatiotyyppi_07\"]," +
                                "\"ytunnus\":\"6165189-7\"," +
                                "\"status\":\"AKTIIVINEN\"," +
                                "\"yhteystiedot\":[]" +
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
                                "\"parentOidPath\":\"|1.2.246.562.24.00000000001|\"," +
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
                                "\"tyypit\":[\"organisaatiotyyppi_07\"]," +
                                "\"ytunnus\":\"6165189-7\"," +
                                "\"status\":\"AKTIIVINEN\"," +
                                "\"yhteystiedot\":[]" +
                                "}]", false));
    }
    @Test
    @DisplayName("Hierarkia haku with OPH role")
    @OPHUser
    void testHierarkiaHaeWithRole() throws Exception {
        this.mockMvc.perform(get("/api/hierarkia/hae?searchStr=1.2.8001.2&lakkautetut=false&aktiiviset=true&suunnitellut=true"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{" +
                                "\"numHits\": 1," +
                                "\"organisaatiot\": [" +
                                "{\"aliOrganisaatioMaara\":0," +
                                "\"children\":[]," +
                                "\"kieletUris\":[]," +
                                "\"kotipaikkaUri\":\"Helsinki\"," +
                                "\"lyhytNimi\":{\"fi\":\"Piilotustesti\"}," +
                                "\"match\":true," +
                                "\"nimi\":{\"fi\":\"Piilotustesti\"}," +
                                "\"oid\":\"1.2.8001.2\"," +
                                "\"organisaatiotyypit\":[\"organisaatiotyyppi_07\"]," +
                                "\"parentOidPath\":\"1.2.8001.2/1.2.246.562.24.00000000001\"," +
                                "\"status\":\"AKTIIVINEN\"," +
                                "\"subRows\":[]," +
                                "\"tyypit\":[\"organisaatiotyyppi_07\"]," +
                                "\"ytunnus\":\"6165189-7\"}" +
                                "]" +
                                "}", false));
    }

    @Test
    @DisplayName("Hierarkia haku with limited role")
    @LimitedUser
    void testHierarkiaHaeWithLimitedRole() throws Exception {
        this.mockMvc.perform(get("/api/hierarkia/hae?searchStr=1.2.8001.2&lakkautetut=false&aktiiviset=true&suunnitellut=true"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{" +
                                "\"numHits\": 1," +
                                "\"organisaatiot\": [" +
                                "{\"aliOrganisaatioMaara\":0," +
                                "\"children\":[]," +
                                "\"kieletUris\":[]," +
                                "\"kotipaikkaUri\":\"Helsinki\"," +
                                "\"lyhytNimi\":{" +
                                "\"fi\":\"Yksityinen elinkeinonharjoittaja (6165189-7)\"," +
                                "\"sv\":\"Enskild näringsidkare (6165189-7)\"," +
                                "\"en\":\"Private trader (6165189-7)\"}," +
                                "\"match\":true," +
                                "\"nimi\":{" +
                                "\"fi\":\"Yksityinen elinkeinonharjoittaja (6165189-7)\"," +
                                "\"sv\":\"Enskild näringsidkare (6165189-7)\"," +
                                "\"en\":\"Private trader (6165189-7)\"}," +
                                "\"oid\":\"1.2.8001.2\"," +
                                "\"organisaatiotyypit\":[\"organisaatiotyyppi_07\"]," +
                                "\"parentOidPath\":\"1.2.8001.2/1.2.246.562.24.00000000001\"," +
                                "\"status\":\"AKTIIVINEN\"," +
                                "\"subRows\":[]," +
                                "\"tyypit\":[\"organisaatiotyyppi_07\"]," +
                                "\"ytunnus\":\"6165189-7\"}" +
                                "]" +
                                "}", false));
    }

    @Test
    @DisplayName("Hierarkia haku anonymously")
    @AnonymousUser
    void testHierarkiaHaeWithoutRole() throws Exception {
        this.mockMvc.perform(get("/api/hierarkia/hae?searchStr=1.2.8001.2&lakkautetut=false&aktiiviset=true&suunnitellut=true"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{" +
                                "\"numHits\": 1," +
                                "\"organisaatiot\": [" +
                                "{\"aliOrganisaatioMaara\":0," +
                                "\"children\":[]," +
                                "\"kieletUris\":[]," +
                                "\"kotipaikkaUri\":\"Helsinki\"," +
                                "\"lyhytNimi\":{" +
                                "\"fi\":\"Yksityinen elinkeinonharjoittaja (6165189-7)\"," +
                                "\"sv\":\"Enskild näringsidkare (6165189-7)\"," +
                                "\"en\":\"Private trader (6165189-7)\"}," +
                                "\"match\":true," +
                                "\"nimi\":{" +
                                "\"fi\":\"Yksityinen elinkeinonharjoittaja (6165189-7)\"," +
                                "\"sv\":\"Enskild näringsidkare (6165189-7)\"," +
                                "\"en\":\"Private trader (6165189-7)\"}," +
                                "\"oid\":\"1.2.8001.2\"," +
                                "\"organisaatiotyypit\":[\"organisaatiotyyppi_07\"]," +
                                "\"parentOidPath\":\"1.2.8001.2/1.2.246.562.24.00000000001\"," +
                                "\"status\":\"AKTIIVINEN\"," +
                                "\"subRows\":[]," +
                                "\"tyypit\":[\"organisaatiotyyppi_07\"]," +
                                "\"ytunnus\":\"6165189-7\"}" +
                                "]" +
                                "}", false));
    }

    @Test
    @DisplayName("Get with limited role, check masked")
    @LimitedUser
    void testGetWithLimitedRole() throws Exception {
        this.mockMvc.perform(get("/api/{oid}","1.2.8001.2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"maskingActive\": true}",false));
    }
    @Test
    @DisplayName("Get with OPH role, check not masked")
    @OPHUser
    void testGetWithOPHRole() throws Exception {
        this.mockMvc.perform(get("/api/{oid}","1.2.8001.2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"maskingActive\": false}",false));
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @WithMockUser(value = "1.2.3.4.5", roles = {"APP_ORGANISAATIOHALLINTA", "APP_ORGANISAATIOHALLINTA_READ", "APP_ORGANISAATIOHALLINTA_READ_1.2.246.562.10.90008375488"})
    @interface LimitedUser {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @WithAnonymousUser
    @interface AnonymousUser {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @WithMockUser(roles = {"APP_ORGANISAATIOHALLINTA", "APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.24.00000000001"})
    @interface OPHUser {
    }


}

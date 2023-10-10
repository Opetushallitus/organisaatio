package fi.vm.sade.organisaatio.resource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrganisaatioNimiMaskingTest extends BaseOrganisaatioApiTest {
    @Test
    @Sql("/data/truncate_tables.sql")
    @Sql("/data/basic_organisaatio_data.sql")
    @OPHUser
    void testGetByOidWithOPHUser() throws Exception {
        mvc.perform(get("/api/1.2.8001.2")).andExpect(status().isOk())
                .andExpect(jsonPath("$.nimi.fi").value("Piilotustesti"))
                .andExpect(jsonPath("$.yhteystiedot").isNotEmpty())
                .andExpect(jsonPath("$.postiosoite").isNotEmpty())
                .andExpect(jsonPath("$.kayntiosoite").isNotEmpty());
    }

    @Test
    @Sql("/data/truncate_tables.sql")
    @Sql("/data/basic_organisaatio_data.sql")
    @AnonymousUser
    void testGetByOidAnonymously() throws Exception {
        mvc.perform(get("/api/1.2.8001.2")).andExpect(status().isOk())
                .andExpect(jsonPath("$.nimi.fi").value("Yksityinen elinkeinonharjoittaja (6165189-7)"))
                .andExpect(jsonPath("$.yhteystiedot").isEmpty())
                .andExpect(jsonPath("$.postiosoite").isEmpty())
                .andExpect(jsonPath("$.kayntiosoite").isEmpty());
    }

    @Test
    @DisplayName("Liitokset with OPH role")
    @OPHUser
    void testLiitoksetWithRole() throws Exception {
        mvc.perform(get("/api/liitokset"))
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
        mvc.perform(get("/api/liitokset"))
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
        mvc.perform(get("/api/liitokset"))
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
        mvc.perform(get("/api/1.2.8001.2/nimet"))
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
        mvc.perform(get("/api/1.2.8001.2/nimet"))
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
        mvc.perform(get("/api/1.2.8001.2/nimet"))
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
        mvc.perform(post("/api/findbyoids")
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
        mvc.perform(post("/api/findbyoids")
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
        mvc.perform(post("/api/findbyoids")
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
        mvc.perform(get("/api/hierarkia/hae?searchStr=1.2.8001.2&lakkautetut=false&aktiiviset=true&suunnitellut=true"))
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
        mvc.perform(get("/api/hierarkia/hae?searchStr=1.2.8001.2&lakkautetut=false&aktiiviset=true&suunnitellut=true"))
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
        mvc.perform(get("/api/hierarkia/hae?searchStr=1.2.8001.2&lakkautetut=false&aktiiviset=true&suunnitellut=true"))
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
        mvc.perform(get("/api/{oid}", "1.2.8001.2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"maskingActive\": true}", false));
    }

    @Test
    @DisplayName("Get with OPH role, check not masked")
    @OPHUser
    void testGetWithOPHRole() throws Exception {
        mvc.perform(get("/api/{oid}", "1.2.8001.2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"maskingActive\": false}", false));
    }


}

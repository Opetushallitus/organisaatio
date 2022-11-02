package fi.vm.sade.organisaatio.resource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
class OrganisaatioApiLiitoksetTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("No liitokset in DB")
    @Sql({"/data/truncate_tables.sql"})
    void testLiitoksetApiEmpty() throws Exception {
        this.mockMvc.perform(get("/api/liitokset"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]",false));
    }

    @Test
    @DisplayName("Some liitokset in DB")
    @Sql({"/data/truncate_tables.sql"})
    @Sql({"/data/basic_organisaatio_data.sql"})
    void testLiitoksetApiExisting() throws Exception {
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
                        "}]",false));
    }
}
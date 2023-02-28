package fi.vm.sade.organisaatio.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.organisaatio.dto.OrganisaatioNimiDTO;
import fi.vm.sade.organisaatio.dto.OrganisaatioNimiUpdateDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrganisaatioApiNimetTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Case Riveria Niskala")
    @Sql({"/data/truncate_tables.sql"})
    @Sql({"/data/riveria_organisaatio_data.sql"})
    @WithMockUser(value = "1.2.3.4.5", authorities = {"FOO"})
    void testRiveriaNiskala() throws Exception {
        this.mockMvc.perform(get("/rest/organisaatio/v2/1.2.246.562.10.87575903965/nimet"))
                .andExpect(status().isOk())
                .andExpect(content().json(readFile("/fixtures/resource/api/riveria.json")));
        this.mockMvc.perform(get("/api/1.2.246.562.10.87575903965/nimet"))
                .andExpect(status().isOk())
                .andExpect(content().json(readFile("/fixtures/resource/api/riveria.json")));

    }

    @Test
    @DisplayName("Case Sataedu Kokemäki")
    @Sql({"/data/truncate_tables.sql"})
    @Sql({"/data/sataedu_organisaatio_data.sql"})
    @WithMockUser(value = "1.2.3.4.5", authorities = {"FOO"})
    void testSataeduKokemaki() throws Exception {
        this.mockMvc.perform(get("/rest/organisaatio/v2/1.2.246.562.10.58061828313/nimet"))
                .andExpect(status().isOk())
                .andExpect(content().json(readFile("/fixtures/resource/api/sataedu.json")));
        this.mockMvc.perform(get("/api/1.2.246.562.10.58061828313/nimet"))
                .andExpect(status().isOk())
                .andExpect(content().json(readFile("/fixtures/resource/api/sataedu.json")));
    }

    @Test
    @Sql({"/data/truncate_tables.sql"})
    @Sql({"/data/basic_organisaatio_data.sql"})
    @OrganisaatioNimiMaskingTest.OPHUser
    void testAddingNameUpdatesNimihaku() throws Exception {
        String oid = "1.2.2004.3";
        String alkuperainenNimi = "node2 foo";

        // Organisaatio löytyy alkuperäisellä nimellä
        this.mockMvc.perform(get("/api/hae?aktiiviset=true&suunnitellut=false&lakkautetut=false&searchStr=" + alkuperainenNimi))
                .andExpect(jsonPath("$.numHits").value(1))
                .andExpect(jsonPath("$.organisaatiot[0].oid").value(oid));
        this.mockMvc.perform(get("/api/" + oid))
                .andExpect(jsonPath("$.nimi.fi").value("root test koulutustoimija, " + alkuperainenNimi))
                .andExpect(jsonPath("$.lyhytNimi.fi").value(alkuperainenNimi));

        // Uuden ajantasaisen nimen lisäämisen jälkeen...
        OrganisaatioNimiDTO uusiNimi = new OrganisaatioNimiDTO();
        uusiNimi.setNimi(Map.of("fi", "Uusnimi"));
        uusiNimi.setAlkuPvm(new Date());
        this.mockMvc.perform(post("/api/" + oid + "/nimet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(uusiNimi)))
                .andExpect(status().isOk());
        this.mockMvc.perform(get("/api/" + oid))
                .andExpect(jsonPath("$.nimi.fi").value("root test koulutustoimija, Uusnimi"))
                .andExpect(jsonPath("$.lyhytNimi.fi").value("Uusnimi"));
        // ...organisaatio löytyy hakemalla uudella nimellä
        this.mockMvc.perform(get("/api/hae?aktiiviset=true&suunnitellut=false&lakkautetut=false&searchStr=Uusnimi"))
                .andExpect(jsonPath("$.numHits").value(1))
                .andExpect(jsonPath("$.organisaatiot[0].oid").value(oid));

        // Ajantasaisen nimen päivittämisen jälkeen...
        OrganisaatioNimiUpdateDTO nimenkorjaus = new OrganisaatioNimiUpdateDTO();
        nimenkorjaus.setCurrentNimi(uusiNimi);
        OrganisaatioNimiDTO korjattuNimi = new OrganisaatioNimiDTO();
        korjattuNimi.setNimi(Map.of("fi", "Uusi nimi"));
        korjattuNimi.setAlkuPvm(new Date());
        nimenkorjaus.setUpdatedNimi(korjattuNimi);
        this.mockMvc.perform(put("/api/" + oid + "/nimet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nimenkorjaus)))
                .andExpect(status().isOk());
        this.mockMvc.perform(get("/api/" + oid))
                .andExpect(jsonPath("$.nimi.fi").value("root test koulutustoimija, Uusi nimi"))
                .andExpect(jsonPath("$.lyhytNimi.fi").value("Uusi nimi"));
        // ...organisaatio löytyy hakemalla päivitetyllä nimellä
        this.mockMvc.perform(get("/api/hae?aktiiviset=true&suunnitellut=false&lakkautetut=false&searchStr=Uusi nimi"))
                .andExpect(jsonPath("$.numHits").value(1))
                .andExpect(jsonPath("$.organisaatiot[0].oid").value(oid));
    }


    private String readFile(String fileName) throws Exception {
        return Files.readString(Paths.get(Objects.requireNonNull(getClass().getResource(fileName)).toURI()), StandardCharsets.UTF_8);
    }
}
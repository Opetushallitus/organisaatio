package fi.vm.sade.varda.rekisterointi.controller.hakija;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.varda.rekisterointi.model.Kayttaja;
import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import fi.vm.sade.varda.rekisterointi.service.RekisterointiService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class RekisterointiControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private RekisterointiService service;

    private Rekisterointi newValidRekisterointi() {
        Rekisterointi rekisterointi = new Rekisterointi();
        rekisterointi.organisaatio = objectMapper.createObjectNode();
        rekisterointi.kayttaja = new Kayttaja();
        rekisterointi.kayttaja.etunimi = "John";
        rekisterointi.kayttaja.sukunimi = "Smith";
        rekisterointi.kayttaja.sahkoposti = "john.smith@example.com";
        rekisterointi.kayttaja.asiointikieli = "en";
        rekisterointi.toimintamuoto = "vardatoimintamuoto_tm01";
        return rekisterointi;
    }

    @Test
    @WithMockUser(roles = "APP_VARDAREKISTEROINTI_HAKIJA")
    public void ok() throws Exception {
        Rekisterointi rekisterointi = newValidRekisterointi();
        String rekisterointiAsJson = objectMapper.writeValueAsString(rekisterointi);

        mvc.perform(post("/hakija/api/rekisterointi")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(rekisterointiAsJson))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "APP_VARDAREKISTEROINTI_HAKIJA")
    public void notOk() throws Exception {
        Rekisterointi rekisterointi = newValidRekisterointi();
        rekisterointi.kayttaja.sahkoposti = "invalid";
        String rekisterointiAsJson = objectMapper.writeValueAsString(rekisterointi);

        mvc.perform(post("/hakija/api/rekisterointi")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(rekisterointiAsJson))
                .andExpect(status().isBadRequest());
    }

}

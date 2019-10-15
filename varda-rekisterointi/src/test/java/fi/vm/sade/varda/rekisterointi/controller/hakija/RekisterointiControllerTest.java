package fi.vm.sade.varda.rekisterointi.controller.hakija;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.varda.rekisterointi.model.Kayttaja;
import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import fi.vm.sade.varda.rekisterointi.model.TestiRekisterointi;
import fi.vm.sade.varda.rekisterointi.service.RekisterointiService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class RekisterointiControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private RekisterointiService service;

    @Test
    @WithMockUser(roles = "APP_VARDAREKISTEROINTI_HAKIJA")
    public void ok() throws Exception {
        Rekisterointi rekisterointi = TestiRekisterointi.validiRekisterointi();
        String rekisterointiAsJson = objectMapper.writeValueAsString(rekisterointi);

        mvc.perform(post(RekisterointiController.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(rekisterointiAsJson))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "APP_VARDAREKISTEROINTI_HAKIJA")
    public void notOk() throws Exception {
        Rekisterointi rekisterointi = TestiRekisterointi.rekisterointi(Kayttaja.builder()
                .etunimi("Testi")
                .sukunimi("Henkilö")
                .asiointikieli("fi")
                .sahkoposti("invalid")
                .build()
        );
        String rekisterointiAsJson = objectMapper.writeValueAsString(rekisterointi);

        mvc.perform(post(RekisterointiController.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(rekisterointiAsJson))
                .andExpect(status().isBadRequest());
    }

}

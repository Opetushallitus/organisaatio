package fi.vm.sade.varda.rekisterointi.controller.hakija;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.varda.rekisterointi.model.Kayttaja;
import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import fi.vm.sade.varda.rekisterointi.model.TestiRekisterointi;
import fi.vm.sade.varda.rekisterointi.service.RekisterointiService;
import fi.vm.sade.varda.rekisterointi.util.Constants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
    @MockitoBean
    private RekisterointiService service;

    @Test
    public void redirectionIlmanRoolia() throws Exception {
        Rekisterointi rekisterointi = TestiRekisterointi.validiVardaRekisterointi();
        String rekisterointiAsJson = objectMapper.writeValueAsString(rekisterointi);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.SESSION_ATTRIBUTE_NAME_BUSINESS_ID, rekisterointi.organisaatio.ytunnus);
        mvc.perform(post(RekisterointiController.BASE_PATH)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(rekisterointiAsJson))
                .andExpect(status().is3xxRedirection());
    }
/*
    @Test
    @WithMockUser(roles = "APP_VARDAREKISTEROINTI_HAKIJA")
    public void okValidillaRekisteroinnillaJaSessiolla() throws Exception {
        Rekisterointi rekisterointi = TestiRekisterointi.validiVardaRekisterointi();
        String rekisterointiAsJson = objectMapper.writeValueAsString(rekisterointi);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.SESSION_ATTRIBUTE_NAME_BUSINESS_ID, rekisterointi.organisaatio.ytunnus);
        mvc.perform(post(RekisterointiController.BASE_PATH)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(rekisterointiAsJson))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "APP_VARDAREKISTEROINTI_HAKIJA")
    public void badRequestEpavalidillaRekisteroinnilla() throws Exception {
        Rekisterointi rekisterointi = TestiRekisterointi.vardaRekisterointi(Kayttaja.builder()
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

    @Test
    @WithMockUser(roles = "APP_VARDAREKISTEROINTI_HAKIJA")
    public void badRequestVaarallaYtunnuksella() throws Exception {
        Rekisterointi rekisterointi = TestiRekisterointi.validiVardaRekisterointi();
        String rekisterointiAsJson = objectMapper.writeValueAsString(rekisterointi);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.SESSION_ATTRIBUTE_NAME_BUSINESS_ID, "0101010-1");
        mvc.perform(post(RekisterointiController.BASE_PATH)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(rekisterointiAsJson))
                .andExpect(status().isBadRequest());
    }
*/
}

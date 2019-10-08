package fi.vm.sade.varda.rekisterointi.controller.virkailija;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.varda.rekisterointi.exception.InvalidInputException;
import fi.vm.sade.varda.rekisterointi.model.Paatos;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class VirkailijaControllerTest {

    private static final Paatos TESTI_PAATOS = new Paatos(
            1L,
            true,
            LocalDateTime.now(),
            2L,
            "Ihan okei."
    );

    @Autowired
    private MockMvc mvc;

    @MockBean
    private RekisterointiService rekisterointiService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void listaaRekisteroinnitReturnsOk() throws Exception {
        when(rekisterointiService.listByTila(Rekisterointi.Tila.KASITTELYSSA)).thenReturn(Collections.singleton(TestiRekisterointi.validiRekisterointi()));
        mvc.perform(get(VirkailijaController.BASE_PATH + VirkailijaController.REKISTEROINNIT_PATH + "?tila={tila}", Rekisterointi.Tila.KASITTELYSSA.toString())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    public void luoPaatosReturnsOk() throws Exception {
        Rekisterointi resolved = TestiRekisterointi.validiRekisterointi().withTila(Rekisterointi.Tila.HYVAKSYTTY);
        when(rekisterointiService.resolve(TESTI_PAATOS)).thenReturn(resolved);
        mvc.perform(post(VirkailijaController.BASE_PATH + VirkailijaController.PAATOKSET_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(TESTI_PAATOS))
        ).andExpect(status().isOk());
    }

    @Test
    public void luoPaatosReturnsBadRequestOnInvalidRekisterointiId() throws Exception {
        when(rekisterointiService.resolve(TESTI_PAATOS)).thenThrow(new InvalidInputException("Ouch!"));
        mvc.perform(post(VirkailijaController.BASE_PATH + VirkailijaController.PAATOKSET_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(TESTI_PAATOS))
        ).andExpect(status().isBadRequest());
    }
}

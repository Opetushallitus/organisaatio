package fi.vm.sade.varda.rekisterointi.controller.virkailija;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.varda.rekisterointi.exception.InvalidInputException;
import fi.vm.sade.varda.rekisterointi.model.*;
import fi.vm.sade.varda.rekisterointi.service.RekisterointiService;
import fi.vm.sade.varda.rekisterointi.util.Constants;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static fi.vm.sade.varda.rekisterointi.controller.virkailija.WithMockVirkailijaUser.MOCK_VIRKAILIJA_OID;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class VirkailijaControllerTest {

    private static final PaatosDto TESTI_PAATOS_DTO = new PaatosDto(
            1L,
            true,
            "Ihan okei."
    );
    private static final PaatosBatch TESTI_PAATOS_BATCH = new PaatosBatch(
            true,
            "Tosi kiva!",
            List.of(1L)
    );

    @Autowired
    private MockMvc mvc;

    @MockBean
    private RekisterointiService rekisterointiService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithAnonymousUser
    public void unauthorizedRedirectsToLogin() throws Exception {
        mvc.perform(get(VirkailijaController.BASE_PATH + VirkailijaController.REKISTEROINNIT_PATH + "?tila={tila}", Rekisterointi.Tila.KASITTELYSSA.toString())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isFound());
    }

    @Test
    @WithMockVirkailijaUser
    public void listaaRekisteroinnitReturnsOk() throws Exception {
        when(rekisterointiService.listByTilaAndOrganisaatio(Rekisterointi.Tila.KASITTELYSSA, null))
                .thenReturn(Collections.singleton(TestiRekisterointi.validiVardaRekisterointi()));
        mvc.perform(get(VirkailijaController.BASE_PATH + VirkailijaController.REKISTEROINNIT_PATH + "?tila={tila}", Rekisterointi.Tila.KASITTELYSSA.toString())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    @WithMockVirkailijaUser
    public void luoPaatosReturnsCreated() throws Exception {
        Rekisterointi resolved = TestiRekisterointi.validiVardaRekisterointi().withPaatos(
                new Paatos(TESTI_PAATOS_DTO.hyvaksytty, LocalDateTime.now(), MOCK_VIRKAILIJA_OID, TESTI_PAATOS_DTO.perustelu)
        );
        when(rekisterointiService.resolve(eq(MOCK_VIRKAILIJA_OID), eq(TESTI_PAATOS_DTO), any())).thenReturn(resolved);
        mvc.perform(post(VirkailijaController.BASE_PATH + VirkailijaController.PAATOKSET_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(TESTI_PAATOS_DTO))
        ).andExpect(status().isCreated());
    }

    @Test
    @WithMockVirkailijaUser
    public void luoPaatosReturnsBadRequestOnInvalidRekisterointiId() throws Exception {
        when(rekisterointiService.resolve(eq(MOCK_VIRKAILIJA_OID), eq(TESTI_PAATOS_DTO), any())).thenThrow(new InvalidInputException("Ouch!"));
        mvc.perform(post(VirkailijaController.BASE_PATH + VirkailijaController.PAATOKSET_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(TESTI_PAATOS_DTO))
        ).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockVirkailijaUser
    public void luoPaatoksetReturnsCreated() throws Exception {
        doNothing().when(rekisterointiService).resolveBatch(anyString(), any(PaatosBatch.class), any());
        mvc.perform(post(VirkailijaController.BASE_PATH + VirkailijaController.PAATOKSET_BATCH_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(TESTI_PAATOS_BATCH))
        ).andExpect(status().isCreated());
    }

    @Test
    public void haeOrganisaatioOiditReturnsOidSuffix() {
        String oid = "1.23.456.7890";
        String authorityValue = String.format("ROLE_" + Constants.VARDA_ROLE + "_%s", oid);
        VirkailijaController controller = new VirkailijaController(null, null, null, null);
        GrantedAuthority authority = (GrantedAuthority) () -> authorityValue;
        List<String> returnedOids = controller.haeOrganisaatioOidit(List.of(authority));
        assertEquals(1, returnedOids.size());
        assertEquals(oid, returnedOids.get(0));
    }
}

package fi.vm.sade.varda.rekisterointi.controller.hakija;

import fi.vm.sade.varda.rekisterointi.client.OrganisaatioClient;
import fi.vm.sade.varda.rekisterointi.model.KielistettyNimi;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioNimi;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioV4Dto;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class OrganisaatioControllerTest {

    private final OrganisaatioClient organisaatioClient = Mockito.mock(OrganisaatioClient.class);
    private final OrganisaatioController controller = new OrganisaatioController(organisaatioClient);

    @Test(expected = IllegalStateException.class)
    public void kuranttiNimiThrowsWhenNoCurrentName() {
        OrganisaatioV4Dto dto = new OrganisaatioV4Dto();
        dto.nimet = Collections.singletonList(organisaatioNimi(LocalDate.MAX, null, null));
        controller.kuranttiNimi(dto);
    }

    @Test(expected = IllegalStateException.class)
    public void kuranttiNimiThrowsWhenNoNameMatchingLanguage() {
        OrganisaatioV4Dto dto = new OrganisaatioV4Dto();
        dto.nimet = Collections.singletonList(organisaatioNimi(LocalDate.MIN, "en", null));
        dto.ytjkieli = "sv";
        controller.kuranttiNimi(dto);
    }

    @Test
    public void kuranttiNimiPicksLatestName() {
        OrganisaatioV4Dto dto = new OrganisaatioV4Dto();
        dto.nimet = List.of(
                organisaatioNimi(LocalDate.MIN, "fi", "Vanha"),
                organisaatioNimi(LocalDate.now(), "fi", "Uusi")
        );
        dto.ytjkieli = "fi";
        KielistettyNimi nimi = controller.kuranttiNimi(dto);
        assertEquals(dto.ytjkieli, nimi.kieli);
        assertEquals("Uusi", nimi.nimi);
    }

    private OrganisaatioNimi organisaatioNimi(LocalDate alkuPvm, String kieli, String nimi) {
        OrganisaatioNimi organisaatioNimi = new OrganisaatioNimi();
        organisaatioNimi.alkuPvm = alkuPvm;
        organisaatioNimi.nimi = Collections.singletonMap(kieli, nimi);
        return organisaatioNimi;
    }
}

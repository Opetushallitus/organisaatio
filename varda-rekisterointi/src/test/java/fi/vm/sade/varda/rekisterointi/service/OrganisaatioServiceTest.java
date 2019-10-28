package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.varda.rekisterointi.model.KielistettyNimi;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioNimi;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioV4Dto;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class OrganisaatioServiceTest {

    private final OrganisaatioService service = new OrganisaatioService();

    @Test(expected = IllegalStateException.class)
    public void kuranttiNimiThrowsWhenNoCurrentName() {
        OrganisaatioV4Dto dto = new OrganisaatioV4Dto();
        dto.nimet = Collections.singletonList(organisaatioNimi(LocalDate.MAX, null, null));
        service.kuranttiNimi(dto);
    }

    @Test(expected = IllegalStateException.class)
    public void kuranttiNimiThrowsWhenNoNameMatchingLanguage() {
        OrganisaatioV4Dto dto = new OrganisaatioV4Dto();
        dto.nimet = Collections.singletonList(organisaatioNimi(LocalDate.MIN, "en", null));
        dto.ytjkieli = "sv";
        service.kuranttiNimi(dto);
    }

    @Test
    public void kuranttiNimiPicksLatestName() {
        OrganisaatioV4Dto dto = new OrganisaatioV4Dto();
        dto.nimet = List.of(
                organisaatioNimi(LocalDate.MIN, "fi", "Vanha"),
                organisaatioNimi(LocalDate.now(), "fi", "Uusi")
        );
        dto.ytjkieli = "fi";
        KielistettyNimi nimi = service.kuranttiNimi(dto);
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

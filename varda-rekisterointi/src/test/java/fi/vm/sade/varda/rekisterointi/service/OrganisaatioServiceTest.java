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
    public void kuranttiNimiThrowsWhenNoName() {
        OrganisaatioV4Dto dto = new OrganisaatioV4Dto();
        dto.nimet = Collections.emptyList();
        service.kuranttiNimi(dto);
    }

    @Test
    public void kuranttiNimiReturnsBlankWhenNoNameMatchingLanguage() {
        OrganisaatioV4Dto dto = new OrganisaatioV4Dto();
        dto.nimet = Collections.singletonList(organisaatioNimi(LocalDate.MIN, "en", "Finnish name missing!"));
        dto.ytjkieli = "fi";
        KielistettyNimi kuranttiNimi = service.kuranttiNimi(dto);
        assertEquals("", kuranttiNimi.nimi);
    }

    @Test
    public void kuranttiNimiPicksLatestName() {
        OrganisaatioV4Dto dto = new OrganisaatioV4Dto();
        dto.nimet = List.of(
                organisaatioNimi(LocalDate.MIN, "fi", "Vanha"),
                organisaatioNimi(LocalDate.now(), "fi", "Uusi"),
                organisaatioNimi(LocalDate.now().plusDays(1), "fi", "Tuleva")
        );
        dto.ytjkieli = "kieli_fi#1";
        KielistettyNimi nimi = service.kuranttiNimi(dto);
        assertEquals("fi", nimi.kieli);
        assertEquals("Uusi", nimi.nimi);
    }

    @Test
    public void kuranttiNimiPicksFirstNameForPlanned() {
        OrganisaatioV4Dto dto = new OrganisaatioV4Dto();
        dto.nimet = List.of(
                organisaatioNimi(LocalDate.now().plusDays(1), "fi", "Eka"),
                organisaatioNimi(LocalDate.now().plusWeeks(1), "fi", "Uusi"),
                organisaatioNimi(LocalDate.MAX, "fi", "Viimeinen")
        );
        dto.ytjkieli = "kieli_fi#1";
        KielistettyNimi nimi = service.kuranttiNimi(dto);
        assertEquals("fi", nimi.kieli);
        assertEquals("Eka", nimi.nimi);
    }

    @Test
    public void organisaationimetConvertsAllFields() {
        KielistettyNimi nimi = KielistettyNimi.of("Testi", "fi", LocalDate.now());
        List<OrganisaatioNimi> nimet = service.organisaatioNimet(nimi);
        assertEquals(1, nimet.size());
        OrganisaatioNimi muunnettu = nimet.get(0);
        assertEquals(nimi.nimi, muunnettu.nimi.get(nimi.kieli));
        assertEquals(nimi.alkuPvm, muunnettu.alkuPvm);
    }

    private OrganisaatioNimi organisaatioNimi(LocalDate alkuPvm, String kieli, String nimi) {
        OrganisaatioNimi organisaatioNimi = new OrganisaatioNimi();
        organisaatioNimi.alkuPvm = alkuPvm;
        organisaatioNimi.nimi = Collections.singletonMap(kieli, nimi);
        return organisaatioNimi;
    }
}

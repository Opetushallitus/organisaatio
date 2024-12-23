package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.varda.rekisterointi.client.KoodistoClient;
import fi.vm.sade.varda.rekisterointi.model.*;
import org.junit.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrganisaatioServiceTest {

    private final OrganisaatioService service = organisaatioService();

    @Test(expected = IllegalStateException.class)
    public void kuranttiNimiThrowsWhenNoName() {
        OrganisaatioDto dto = new OrganisaatioDto();
        dto.nimet = Collections.emptyList();
        service.kuranttiNimi(dto);
    }

    @Test
    public void kuranttiNimiReturnsBlankWhenNoNameMatchingLanguage() {
        OrganisaatioDto dto = new OrganisaatioDto();
        dto.nimet = Collections.singletonList(organisaatioNimi(LocalDate.MIN, "en", "Finnish and Swedish names missing!"));
        dto.ytjkieli = "fi";
        KielistettyNimi kuranttiNimi = service.kuranttiNimi(dto);
        assertEquals("", kuranttiNimi.nimi);
    }

    @Test
    public void kuranttiNimiPicksLatestName() {
        OrganisaatioDto dto = new OrganisaatioDto();
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
        OrganisaatioDto dto = new OrganisaatioDto();
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

    @Test
    public void yritysmuotoArvoIsConverted() {
        String yritysmuoto = "yritysmuoto_1";
        String converted = "Yritysmuoto";
        String result = service.yritysMuotoKoodiUriToNimi(yritysmuoto);
        assertEquals(converted, result);
    }

    @Test
    public void yritysmuotoNimiIsNotConverted() {
        String yritysmuoto = "Yritysmuoto";
        String result = service.yritysMuotoKoodiUriToNimi(yritysmuoto);
        assertEquals(yritysmuoto, result);
    }

    private OrganisaatioNimi organisaatioNimi(LocalDate alkuPvm, String kieli, String nimi) {
        OrganisaatioNimi organisaatioNimi = new OrganisaatioNimi();
        organisaatioNimi.alkuPvm = alkuPvm;
        organisaatioNimi.nimi = Collections.singletonMap(kieli, nimi);
        return organisaatioNimi;
    }

    private static OrganisaatioService organisaatioService() {
        Koodi koodi = new Koodi();
        koodi.uri = "yritysmuoto_1";
        koodi.nimi = Map.of("fi", "Yritysmuoto");
        KoodistoClient client = mock(KoodistoClient.class);
        when(client.listKoodit(any(KoodistoType.class))).thenReturn(Collections.singletonList(koodi));
        return new OrganisaatioService(client);
    }
}

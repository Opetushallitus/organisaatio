package fi.vm.sade.organisaatio.resource.impl.v2;

import fi.vm.sade.organisaatio.dto.OrganisaatioNimiDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class OrganisaatioResourceImplV2Test {

    @Autowired
    private OrganisaatioResourceImplV2 resource = new OrganisaatioResourceImplV2();

    @Test
    void testDecoreateToimipisteNimet1() {
        OrganisaatioNimiDTO toimipiste1 = createNimi(30, "one");
        OrganisaatioNimiDTO oppilaitos1 = createNimi(30, "first");
        List<OrganisaatioNimiDTO> res1 = resource.decoreateToimipisteNimet(List.of(toimipiste1), List.of(Map.entry(Map.entry(Date.valueOf(LocalDate.now().minusYears(1)), Optional.of(Date.valueOf(LocalDate.now()))),List.of(oppilaitos1))));
        assertThat(res1).isNotNull().hasSize(1);
        assertThat(res1.get(0).getNimi()).containsEntry("fi", "first, one");
        assertThat(res1.get(0).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(30)));

        toimipiste1 = createNimi(40, "one");
        oppilaitos1 = createNimi(30, "first");
        List<OrganisaatioNimiDTO> res2 = resource.decoreateToimipisteNimet(List.of(toimipiste1), List.of(Map.entry(Map.entry(Date.valueOf(LocalDate.now().minusYears(1)), Optional.of(Date.valueOf(LocalDate.now()))),List.of(oppilaitos1))));
        assertThat(res2).isNotNull().hasSize(1);
        assertThat(res2.get(0).getNimi()).containsEntry("fi", "first, one");
        assertThat(res2.get(0).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(40)));

        toimipiste1 = createNimi(30, "one");
        oppilaitos1 = createNimi(40, "first");
        List<OrganisaatioNimiDTO> res3 = resource.decoreateToimipisteNimet(List.of(toimipiste1), List.of(Map.entry(Map.entry(Date.valueOf(LocalDate.now().minusYears(1)), Optional.of(Date.valueOf(LocalDate.now()))),List.of(oppilaitos1))));
        assertThat(res3).isNotNull();
        assertThat(res2).hasSize(1);
        assertThat(res3.get(0).getNimi()).containsEntry("fi", "first, one");
        assertThat(res3.get(0).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(30)));
    }

    @Test
    void testDecoreateToimipisteNimet2() {
        OrganisaatioNimiDTO toimipiste1 = createNimi(30, "one");
        OrganisaatioNimiDTO oppilaitos1 = createNimi(30, "first");
        OrganisaatioNimiDTO oppilaitos2 = createNimi(15, "second");
        List<OrganisaatioNimiDTO> res = resource.decoreateToimipisteNimet(List.of(toimipiste1), List.of(Map.entry(Map.entry(Date.valueOf(LocalDate.now().minusYears(1)), Optional.of(Date.valueOf(LocalDate.now()))),List.of(oppilaitos1, oppilaitos2))));
        assertThat(res).isNotNull().hasSize(2);
        assertThat(res.get(0).getNimi()).containsEntry("fi", "first, one");
        assertThat(res.get(0).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(30)));

        assertThat(res.get(1).getNimi()).containsEntry("fi", "second, one");
        assertThat(res.get(1).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(15)));
    }

    @Test
    void testDecoreateToimipisteNimet3() {
        OrganisaatioNimiDTO toimipiste1 = createNimi(30, "one");
        OrganisaatioNimiDTO oppilaitos1 = createNimi(25, "first");
        OrganisaatioNimiDTO toimipiste2 = createNimi(20, "two");
        OrganisaatioNimiDTO oppilaitos2 = createNimi(15, "second");
        OrganisaatioNimiDTO toimipiste3 = createNimi(10, "three");

        List<OrganisaatioNimiDTO> res = resource.decoreateToimipisteNimet(Arrays.asList(toimipiste1, toimipiste2, toimipiste3), List.of(Map.entry(Map.entry(Date.valueOf(LocalDate.now().minusYears(1)), Optional.of(Date.valueOf(LocalDate.now()))),List.of(oppilaitos1, oppilaitos2))));
        assertThat(res).isNotNull().hasSize(4);
        assertThat(res.get(0).getNimi()).containsEntry("fi", "first, one");
        assertThat(res.get(0).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(30)));
        assertThat(res.get(1).getNimi()).containsEntry("fi", "first, two");
        assertThat(res.get(1).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(20)));
        assertThat(res.get(2).getNimi()).containsEntry("fi", "second, two");
        assertThat(res.get(2).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(15)));
        assertThat(res.get(3).getNimi()).containsEntry("fi", "second, three");
        assertThat(res.get(3).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(10)));
    }

    @Test
    void testDecoreateToimipisteNimet4() {
        OrganisaatioNimiDTO toimipiste1 = createNimi(30, "one");
        OrganisaatioNimiDTO toimipiste2 = createNimi(25, "two");
        OrganisaatioNimiDTO oppilaitos1 = createNimi(30, "first");

        List<OrganisaatioNimiDTO> res = resource.decoreateToimipisteNimet(List.of(toimipiste1, toimipiste2),List.of(Map.entry(Map.entry(Date.valueOf(LocalDate.now().minusYears(1)), Optional.of(Date.valueOf(LocalDate.now()))),List.of(oppilaitos1))));
        assertThat(res).isNotNull().hasSize(2);
        assertThat(res.get(0).getNimi()).containsEntry("fi", "first, one");
        assertThat(res.get(0).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(30)));
        assertThat(res.get(1).getNimi()).containsEntry("fi", "first, two");
        assertThat(res.get(1).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(25)));
    }

    @Test
    void testDecoreateToimipisteNimetLegacyNamesMayHaveConcatenation() {
        OrganisaatioNimiDTO toimipiste1 = createNimi(30, "foo, one");
        OrganisaatioNimiDTO toimipiste2 = createNimi(25, "two");
        OrganisaatioNimiDTO oppilaitos1 = createNimi(30, "first");

        List<OrganisaatioNimiDTO> res = resource.decoreateToimipisteNimet(List.of(toimipiste1, toimipiste2), List.of(Map.entry(Map.entry(Date.valueOf(LocalDate.now().minusYears(1)), Optional.of(Date.valueOf(LocalDate.now()))),List.of(oppilaitos1))));
        assertThat(res).isNotNull().hasSize(2);
        assertThat(res.get(0).getNimi()).containsEntry("fi", "first, one");
        assertThat(res.get(0).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(30)));
        assertThat(res.get(1).getNimi()).containsEntry("fi", "first, two");
        assertThat(res.get(1).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(25)));
    }


    OrganisaatioNimiDTO createNimi(int days, String name) {
        OrganisaatioNimiDTO toimipiste = new OrganisaatioNimiDTO();
        toimipiste.setAlkuPvm(Date.valueOf(LocalDate.now().minusDays(days)));
        toimipiste.setVersion(1);
        Map<String, String> nimi = new HashMap<>();
        nimi.put("fi", name);
        nimi.put("sv", name);
        nimi.put("en", name);
        toimipiste.setNimi(nimi);
        return toimipiste;
    }
}
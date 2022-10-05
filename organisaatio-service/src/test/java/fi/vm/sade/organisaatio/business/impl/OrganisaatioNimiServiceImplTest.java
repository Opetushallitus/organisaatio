package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.dto.OrganisaatioNimiDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrganisaatioNimiServiceImplTest {

    @Autowired
    private OrganisaatioNimiServiceImpl resource;

    @Test
    @DisplayName("Single toimipiste-name / single oppilaitos-name with possible date combintaions should reult in single name with date from toimipiste-name")
    void testDecoreateToimipisteNimet1() {
        OrganisaatioNimiDTO toimipiste1 = createNimi(30, "one");
        OrganisaatioNimiDTO oppilaitos1 = createNimi(30, "first");
        List<OrganisaatioNimiDTO> res1 = resource.decoreateToimipisteNimet(List.of(toimipiste1), List.of(Map.entry(Map.entry(Date.valueOf(LocalDate.now().minusYears(1)), Optional.of(Date.valueOf(LocalDate.now()))), List.of(oppilaitos1))));
        assertThat(res1).isNotNull().hasSize(1);
        assertThat(res1.get(0).getNimi()).containsEntry("fi", "first, one");
        assertThat(res1.get(0).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(30)));

        toimipiste1 = createNimi(40, "one");
        oppilaitos1 = createNimi(30, "first");
        List<OrganisaatioNimiDTO> res2 = resource.decoreateToimipisteNimet(List.of(toimipiste1), List.of(Map.entry(Map.entry(Date.valueOf(LocalDate.now().minusYears(1)), Optional.of(Date.valueOf(LocalDate.now()))), List.of(oppilaitos1))));
        assertThat(res2).isNotNull().hasSize(1);
        assertThat(res2.get(0).getNimi()).containsEntry("fi", "first, one");
        assertThat(res2.get(0).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(40)));

        toimipiste1 = createNimi(30, "one");
        oppilaitos1 = createNimi(40, "first");
        List<OrganisaatioNimiDTO> res3 = resource.decoreateToimipisteNimet(List.of(toimipiste1), List.of(Map.entry(Map.entry(Date.valueOf(LocalDate.now().minusYears(1)), Optional.of(Date.valueOf(LocalDate.now()))), List.of(oppilaitos1))));
        assertThat(res3).isNotNull();
        assertThat(res2).hasSize(1);
        assertThat(res3.get(0).getNimi()).containsEntry("fi", "first, one");
        assertThat(res3.get(0).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(30)));
    }

    @Test
    @DisplayName("One toimipiste-name and two oppilaitos-name should result in two names, one extra generated from change in oppilitos name")
    void testDecoreateToimipisteNimet2() {
        OrganisaatioNimiDTO toimipiste1 = createNimi(30, "one");
        OrganisaatioNimiDTO oppilaitos1 = createNimi(30, "first");
        OrganisaatioNimiDTO oppilaitos2 = createNimi(15, "second");
        List<OrganisaatioNimiDTO> res = resource.decoreateToimipisteNimet(List.of(toimipiste1), List.of(Map.entry(Map.entry(Date.valueOf(LocalDate.now().minusYears(1)), Optional.of(Date.valueOf(LocalDate.now()))), List.of(oppilaitos1, oppilaitos2))));
        assertThat(res).isNotNull().hasSize(2);
        assertThat(res.get(0).getNimi()).containsEntry("fi", "first, one");
        assertThat(res.get(0).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(30)));

        assertThat(res.get(1).getNimi()).containsEntry("fi", "second, one");
        assertThat(res.get(1).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(15)));
    }

    @Test
    @DisplayName("Changes in both toimipiste-names and oppilaitos-names should generate name-changes in the result")
    void testDecoreateToimipisteNimet3() {
        OrganisaatioNimiDTO toimipiste1 = createNimi(30, "one");
        OrganisaatioNimiDTO oppilaitos1 = createNimi(25, "first");
        OrganisaatioNimiDTO toimipiste2 = createNimi(20, "two");
        OrganisaatioNimiDTO oppilaitos2 = createNimi(15, "second");
        OrganisaatioNimiDTO toimipiste3 = createNimi(10, "three");

        List<OrganisaatioNimiDTO> res = resource.decoreateToimipisteNimet(Arrays.asList(toimipiste1, toimipiste2, toimipiste3), List.of(Map.entry(Map.entry(Date.valueOf(LocalDate.now().minusYears(1)), Optional.of(Date.valueOf(LocalDate.now()))), List.of(oppilaitos1, oppilaitos2))));
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
    @DisplayName("If toimipiste and oppilaitos are valid from same date, there should be no extra records.")
    void testDecoreateToimipisteNimet4() {
        OrganisaatioNimiDTO toimipiste1 = createNimi(30, "one");
        OrganisaatioNimiDTO toimipiste2 = createNimi(25, "two");
        OrganisaatioNimiDTO oppilaitos1 = createNimi(30, "first");

        List<OrganisaatioNimiDTO> res = resource.decoreateToimipisteNimet(List.of(toimipiste1, toimipiste2), List.of(Map.entry(Map.entry(Date.valueOf(LocalDate.now().minusYears(1)), Optional.of(Date.valueOf(LocalDate.now()))), List.of(oppilaitos1))));
        assertThat(res).isNotNull().hasSize(2);
        assertThat(res.get(0).getNimi()).containsEntry("fi", "first, one");
        assertThat(res.get(0).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(30)));
        assertThat(res.get(1).getNimi()).containsEntry("fi", "first, two");
        assertThat(res.get(1).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(25)));
    }

    @Test
    @DisplayName("parentnamehistory should combine names from parent history, only names within the parent period should be included (names from first oppilaitos will be gathered from start of time)")
    void testEvaluateParentNameHistory1() {
        OrganisaatioNimiDTO oppilaitosA1 = createNimi(30, "A1");
        OrganisaatioNimiDTO oppilaitosA2 = createNimi(25, "A2");
        OrganisaatioNimiDTO oppilaitosA3 = createNimi(20, "A3");
        OrganisaatioNimiDTO oppilaitosB1 = createNimi(30, "B1");
        OrganisaatioNimiDTO oppilaitosB2 = createNimi(25, "B2");
        OrganisaatioNimiDTO oppilaitosB3 = createNimi(15, "B3");
        List<OrganisaatioNimiDTO> res = resource.evaluateParentNameHistory(List.of(
                Map.entry(Map.entry(Date.valueOf(LocalDate.now().minusDays(27)), Optional.of(Date.valueOf(LocalDate.now().minusDays(17)))), List.of(oppilaitosA1, oppilaitosA2, oppilaitosA3)),
                Map.entry(Map.entry(Date.valueOf(LocalDate.now().minusDays(17)), Optional.of(Date.valueOf(LocalDate.now()))), List.of(oppilaitosB1, oppilaitosB2, oppilaitosB3))));
        assertThat(res.get(0).getNimi()).containsEntry("fi", "A1");
        assertThat(res.get(0).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(27)));

        assertThat(res.get(1).getNimi()).containsEntry("fi", "A2");
        assertThat(res.get(1).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(25)));

        assertThat(res.get(2).getNimi()).containsEntry("fi", "A3");
        assertThat(res.get(2).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(20)));

        assertThat(res.get(3).getNimi()).containsEntry("fi", "B2");
        assertThat(res.get(3).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(17)));

        assertThat(res.get(4).getNimi()).containsEntry("fi", "B3");
        assertThat(res.get(4).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(15)));

        assertThat(res).isNotNull().hasSize(5);
    }

    @Test
    @DisplayName("parenthistory should work also when last date-range is open ended")
    void testEvaluateParentNameHistory2() {
        OrganisaatioNimiDTO oppilaitosA1 = createNimi(30, "A1");
        OrganisaatioNimiDTO oppilaitosA2 = createNimi(25, "A2");
        OrganisaatioNimiDTO oppilaitosA3 = createNimi(20, "A3");
        OrganisaatioNimiDTO oppilaitosB1 = createNimi(30, "B1");
        OrganisaatioNimiDTO oppilaitosB2 = createNimi(25, "B2");
        OrganisaatioNimiDTO oppilaitosB3 = createNimi(15, "B3");
        List<OrganisaatioNimiDTO> res = resource.evaluateParentNameHistory(List.of(
                Map.entry(Map.entry(Date.valueOf(LocalDate.now().minusDays(27)), Optional.of(Date.valueOf(LocalDate.now().minusDays(17)))), List.of(oppilaitosA1, oppilaitosA2, oppilaitosA3)),
                Map.entry(Map.entry(Date.valueOf(LocalDate.now().minusDays(17)), Optional.empty()), List.of(oppilaitosB1, oppilaitosB2, oppilaitosB3))));

        assertThat(res.get(0).getNimi()).containsEntry("fi", "A1");
        assertThat(res.get(0).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(27)));
        assertThat(res.get(1).getNimi()).containsEntry("fi", "A2");
        assertThat(res.get(1).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(25)));
        assertThat(res.get(2).getNimi()).containsEntry("fi", "A3");
        assertThat(res.get(2).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(20)));
        assertThat(res.get(3).getNimi()).containsEntry("fi", "B2");
        assertThat(res.get(3).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(17)));
        assertThat(res.get(4).getNimi()).containsEntry("fi", "B3");
        assertThat(res.get(4).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(15)));
        assertThat(res).isNotNull().hasSize(5);
    }

    @Test
    @DisplayName("parenthistory should work as expected when daterange match startdate of names")
    void testEvaluateParentNameHistory3() {
        OrganisaatioNimiDTO oppilaitosA1 = createNimi(30, "A1");
        OrganisaatioNimiDTO oppilaitosA2 = createNimi(25, "A2");
        OrganisaatioNimiDTO oppilaitosA3 = createNimi(20, "A3");
        OrganisaatioNimiDTO oppilaitosB1 = createNimi(30, "B1");
        OrganisaatioNimiDTO oppilaitosB2 = createNimi(20, "B2");
        OrganisaatioNimiDTO oppilaitosB3 = createNimi(15, "B3");
        List<OrganisaatioNimiDTO> res = resource.evaluateParentNameHistory(List.of(
                Map.entry(Map.entry(Date.valueOf(LocalDate.now().minusDays(25)), Optional.of(Date.valueOf(LocalDate.now().minusDays(20)))), List.of(oppilaitosA1, oppilaitosA2, oppilaitosA3)),
                Map.entry(Map.entry(Date.valueOf(LocalDate.now().minusDays(20)), Optional.empty()), List.of(oppilaitosB1, oppilaitosB2, oppilaitosB3))));

        assertThat(res.get(0).getNimi()).containsEntry("fi", "A1");
        assertThat(res.get(0).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(25)));
        assertThat(res.get(1).getNimi()).containsEntry("fi", "A2");
        assertThat(res.get(1).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(25)));
        assertThat(res.get(2).getNimi()).containsEntry("fi", "B2");
        assertThat(res.get(2).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(20)));
        assertThat(res.get(3).getNimi()).containsEntry("fi", "B3");
        assertThat(res.get(3).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(15)));
        assertThat(res).isNotNull().hasSize(4);
    }

    @Test
    @DisplayName("Names from first parent should be extrapolated to beginning of time")
    void testEvaluateParentNameHistory4() {
        OrganisaatioNimiDTO oppilaitosA1 = createNimi(30, "A1");
        OrganisaatioNimiDTO oppilaitosA2 = createNimi(25, "A2");
        OrganisaatioNimiDTO oppilaitosA3 = createNimi(20, "A3");
        OrganisaatioNimiDTO oppilaitosA4 = createNimi(15, "A4");
        List<OrganisaatioNimiDTO> res = resource.evaluateParentNameHistory(List.of(
                Map.entry(Map.entry(Date.valueOf(LocalDate.now().minusDays(27)), Optional.of(Date.valueOf(LocalDate.now().minusDays(17)))), List.of(oppilaitosA1, oppilaitosA2, oppilaitosA3, oppilaitosA4))
        ));
        assertThat(res).isNotNull().hasSize(3);
        assertThat(res.get(0).getNimi()).containsEntry("fi", "A1");
        assertThat(res.get(0).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(27)));
        assertThat(res.get(1).getNimi()).containsEntry("fi", "A2");
        assertThat(res.get(1).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(25)));
        assertThat(res.get(2).getNimi()).containsEntry("fi", "A3");
        assertThat(res.get(2).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(20)));
    }

    @Test
    @DisplayName("Last name from second parent should be included even if last parent starts after parent creation")
    void testEvaluateParentNameHistory5() {
        OrganisaatioNimiDTO oppilaitosA1 = createNimi(30, "A1");
        OrganisaatioNimiDTO oppilaitosA2 = createNimi(25, "A2");
        OrganisaatioNimiDTO oppilaitosB1 = createNimi(20, "B1");
        OrganisaatioNimiDTO oppilaitosB2 = createNimi(15, "B2");
        List<OrganisaatioNimiDTO> res = resource.evaluateParentNameHistory(List.of(
                Map.entry(Map.entry(Date.valueOf(LocalDate.now().minusDays(27)), Optional.of(Date.valueOf(LocalDate.now().minusDays(12)))), List.of(oppilaitosA1, oppilaitosA2)),
                Map.entry(Map.entry(Date.valueOf(LocalDate.now().minusDays(12)), Optional.empty()), List.of(oppilaitosB1, oppilaitosB2))
        ));
        assertThat(res.get(0).getNimi()).containsEntry("fi", "A1");
        assertThat(res.get(0).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(27)));
        assertThat(res.get(1).getNimi()).containsEntry("fi", "A2");
        assertThat(res.get(1).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(25)));
        assertThat(res.get(2).getNimi()).containsEntry("fi", "B2");
        assertThat(res.get(2).getAlkuPvm()).isEqualToIgnoringMinutes(Date.valueOf(LocalDate.now().minusDays(12)));
        assertThat(res).isNotNull().hasSize(3);
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
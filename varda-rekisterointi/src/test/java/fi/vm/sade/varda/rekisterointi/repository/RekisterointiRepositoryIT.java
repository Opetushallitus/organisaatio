package fi.vm.sade.varda.rekisterointi.repository;

import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import fi.vm.sade.varda.rekisterointi.model.TestiRekisterointi;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"integration-test", "dev"})
@Transactional
public class RekisterointiRepositoryIT {

    @Autowired
    private RekisterointiRepository rekisterointiRepository;

    @Test
    public void findByTilaReturnsMatch() {
        Iterable<Rekisterointi> iterable = rekisterointiRepository.findByTila(
                Rekisterointi.Tila.KASITTELYSSA.toString());
        List<Rekisterointi> results = new ArrayList<>();
        iterable.forEach(results::add);
        assertEquals(1, results.size());
    }

    @Test
    public void findByOrganisaatioReturnsMatch() {
        Iterable<Rekisterointi> iterable = rekisterointiRepository.findByOrganisaatioContaining("Testi");
        List<Rekisterointi> results = new ArrayList<>();
        iterable.forEach(results::add);
        assertEquals(1, results.size());
    }

    @Test
    public void findByTilaAndOrganisaatioContainingRulesOutTilaMismatch() {
        Iterable<Rekisterointi> iterable = rekisterointiRepository.findByTilaAndOrganisaatioContaining(
                Rekisterointi.Tila.HYVAKSYTTY.toString(), "Testi");
        List<Rekisterointi> results = new ArrayList<>();
        iterable.forEach(results::add);
        assertEquals(0, results.size());
    }

    @Test
    public void findByTilaAndOrganisaatioContainingRulesOutOrganisaatioMismatch() {
        Iterable<Rekisterointi> iterable = rekisterointiRepository.findByTilaAndOrganisaatioContaining(
                Rekisterointi.Tila.KASITTELYSSA.toString(), "Tämäeilöydy");
        List<Rekisterointi> results = new ArrayList<>();
        iterable.forEach(results::add);
        assertEquals(0, results.size());
    }

    @Test
    public void findByTilaAndKunnatRulesOutKuntaMismatch() {
        Iterable<Rekisterointi> iterable = rekisterointiRepository.findByTilaAndKunnat(
                Rekisterointi.Tila.KASITTELYSSA.toString(), new String[]{"Vääräkunta", "Toinenvääräkunta"});
        List<Rekisterointi> results = new ArrayList<>();
        iterable.forEach(results::add);
        assertEquals(0, results.size());
    }

    @Test
    public void findByTilaAndKunnatReturnsMatch() {
        Iterable<Rekisterointi> iterable = rekisterointiRepository.findByTilaAndKunnat(
                Rekisterointi.Tila.KASITTELYSSA.toString(), new String[]{"Helsinki", "Vääräkunta"});
        List<Rekisterointi> results = new ArrayList<>();
        iterable.forEach(results::add);
        assertEquals(1, results.size());
    }

    @Test
    public void findByYtunnusReturnsMatch() {
        Iterable<Rekisterointi> iterable = rekisterointiRepository.findByYtunnus("0000000-0");
        List<Rekisterointi> results = new ArrayList<>();
        iterable.forEach(results::add);
        assertEquals(1, results.size());
    }

    @Test
    public void findByTilaAndKunnatAndOrganisaatioRulesOutOrganisaatioMismatch() {
        Iterable<Rekisterointi> iterable = rekisterointiRepository.findByTilaAndKunnatAndOrganisaatioContaining(
                Rekisterointi.Tila.KASITTELYSSA.toString(), new String[]{"Helsinki"}, "vääräfirma");
        List<Rekisterointi> results = new ArrayList<>();
        iterable.forEach(results::add);
        assertEquals(0, results.size());
    }

    @Test
    public void findByTilaAndKunnatAndOrganisaatioReturnsMatch() {
        Iterable<Rekisterointi> iterable = rekisterointiRepository.findByTilaAndKunnatAndOrganisaatioContaining(
                Rekisterointi.Tila.KASITTELYSSA.toString(), new String[]{"Helsinki"}, "testi");
        List<Rekisterointi> results = new ArrayList<>();
        iterable.forEach(results::add);
        assertEquals(1, results.size());
    }

    @Test
    public void savesRekisterointi() {
        Rekisterointi rekisterointi = TestiRekisterointi.validiRekisterointi();
        rekisterointi = rekisterointiRepository.save(rekisterointi);
        assertNotNull(rekisterointi.id);
    }

    @Test
    public void savesUudelleenRekisterointi() {
        Rekisterointi rekisterointi = TestiRekisterointi.validiRekisterointi();
        rekisterointi = rekisterointiRepository.save(rekisterointi);
        Rekisterointi uudelleenRekisterointi = TestiRekisterointi.validiRekisterointi();
        uudelleenRekisterointi.organisaatio.setUudelleenRekisterointi(true);
        uudelleenRekisterointi = rekisterointiRepository.save(uudelleenRekisterointi);

        assertNotNull(rekisterointi.id);
        assertNotNull(uudelleenRekisterointi.id);
        assertNotEquals(rekisterointi.id, uudelleenRekisterointi.id);
        assertFalse(rekisterointi.organisaatio.uudelleenRekisterointi);
        assertTrue(uudelleenRekisterointi.organisaatio.uudelleenRekisterointi);
    }

    @Test(expected = DbActionExecutionException.class)
    public void oidMustBeUniqueUnlessUudelleenRekisterointi() {
        rekisterointiRepository.save(TestiRekisterointi.validiRekisterointi());
        rekisterointiRepository.save(TestiRekisterointi.validiRekisterointi());
    }
}

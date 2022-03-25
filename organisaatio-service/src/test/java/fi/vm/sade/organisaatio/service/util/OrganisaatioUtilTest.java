package fi.vm.sade.organisaatio.service.util;

import fi.vm.sade.organisaatio.model.Organisaatio;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrganisaatioUtilTest {

    @Test
    public void isAktiivinenSuunniteltuLakkautettuAlkuPvmLakkautusPvmNull() {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setAlkuPvm(null);
        organisaatio.setLakkautusPvm(null);

        assertTrue(OrganisaatioUtil.isAktiivinen(organisaatio));
        assertFalse(OrganisaatioUtil.isSuunniteltu(organisaatio));
        assertFalse(OrganisaatioUtil.isPassive(organisaatio));
    }

    @Test
    public void isAktiivinenSuunniteltuLakkautettuAlkuPvmEilen() {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setAlkuPvm(java.sql.Date.valueOf(LocalDate.now().minusDays(1L)));
        organisaatio.setLakkautusPvm(null);

        assertTrue(OrganisaatioUtil.isAktiivinen(organisaatio));
        assertFalse(OrganisaatioUtil.isSuunniteltu(organisaatio));
        assertFalse(OrganisaatioUtil.isPassive(organisaatio));
    }

    @Test
    public void isAktiivinenSuunniteltuLakkautettuLakkautusPvmEilen() {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setAlkuPvm(null);
        organisaatio.setLakkautusPvm(java.sql.Date.valueOf(LocalDate.now().minusDays(1L)));

        assertFalse(OrganisaatioUtil.isAktiivinen(organisaatio));
        assertFalse(OrganisaatioUtil.isSuunniteltu(organisaatio));
        assertTrue(OrganisaatioUtil.isPassive(organisaatio));
    }

    @Test
    public void isAktiivinenSuunniteltuLakkautettuAlkuPvmTanaan() {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setAlkuPvm(java.sql.Date.valueOf(LocalDate.now()));
        organisaatio.setLakkautusPvm(null);

        assertTrue(OrganisaatioUtil.isAktiivinen(organisaatio));
        assertFalse(OrganisaatioUtil.isSuunniteltu(organisaatio));
        assertFalse(OrganisaatioUtil.isPassive(organisaatio));
    }

    @Test
    public void isAktiivinenSuunniteltuLakkautettuLakkautusPvmTanaan() {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setAlkuPvm(null);
        organisaatio.setLakkautusPvm(java.sql.Date.valueOf(LocalDate.now()));

        assertFalse(OrganisaatioUtil.isAktiivinen(organisaatio));
        assertFalse(OrganisaatioUtil.isSuunniteltu(organisaatio));
        assertTrue(OrganisaatioUtil.isPassive(organisaatio));
    }

    @Test
    public void isAktiivinenSuunniteltuLakkautettuAlkuPvmHuomenna() {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setAlkuPvm(java.sql.Date.valueOf(LocalDate.now().plusDays(1L)));
        organisaatio.setLakkautusPvm(null);

        assertFalse(OrganisaatioUtil.isAktiivinen(organisaatio));
        assertTrue(OrganisaatioUtil.isSuunniteltu(organisaatio));
        assertFalse(OrganisaatioUtil.isPassive(organisaatio));
    }

    @Test
    public void isAktiivinenSuunniteltuLakkautettuLakkautusPvmHuomenna() {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setAlkuPvm(null);
        organisaatio.setLakkautusPvm(java.sql.Date.valueOf(LocalDate.now().plusDays(1L)));

        assertTrue(OrganisaatioUtil.isAktiivinen(organisaatio));
        assertFalse(OrganisaatioUtil.isSuunniteltu(organisaatio));
        assertFalse(OrganisaatioUtil.isPassive(organisaatio));
    }

    @Test
    public void isAktiivinenSuunniteltuLakkautettuAlkuPvmEilenLakkautusPvmHuomenna() {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setAlkuPvm(java.sql.Date.valueOf(LocalDate.now().minusDays(1L)));
        organisaatio.setLakkautusPvm(java.sql.Date.valueOf(LocalDate.now().plusDays(1L)));

        assertTrue(OrganisaatioUtil.isAktiivinen(organisaatio));
        assertFalse(OrganisaatioUtil.isSuunniteltu(organisaatio));
        assertFalse(OrganisaatioUtil.isPassive(organisaatio));
    }

    @Test
    public void parentOidPathPalauttaaTyhjanNullArgumentilla() {
        String parentOidPath = OrganisaatioUtil.parentOidPath(null);
        assertEquals("", parentOidPath);
    }

    @Test
    public void parentOidPathPalauttaaTyhjanTyhjallaArgumentilla() {
        String parentOidPath = OrganisaatioUtil.parentOidPath(Collections.emptyList());
        assertEquals("", parentOidPath);
    }

    @Test
    public void parentOidPathPalauttaaPathinKaanteisena() {
        String parentOidPath = OrganisaatioUtil.parentOidPath(Arrays.asList("1.2.3.1", "1.2.3.0"));
        String odotettuTulos = "|1.2.3.0|1.2.3.1|";
        assertEquals(odotettuTulos, parentOidPath);
    }

    @Test
    public void parentOidsPalauttaaTyhjanNullArgumentilla() {
        List<String> parentOids = OrganisaatioUtil.parentOids(null);
        assertTrue(parentOids.isEmpty());
    }

    @Test
    public void parentOidsPalauttaaTyhjanTyhjallaArgumentilla() {
        List<String> parentOids = OrganisaatioUtil.parentOids("");
        assertTrue(parentOids.isEmpty());
    }

    @Test
    public void parentOidsPalauttaaOiditKaanteisena() {
        List<String> parentOids = OrganisaatioUtil.parentOids("|1.2.3.0|1.2.3.1|");
        assertEquals(2, parentOids.size());
        assertEquals("1.2.3.1", parentOids.get(0));
        assertEquals("1.2.3.0", parentOids.get(1));
    }
}

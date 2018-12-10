package fi.vm.sade.organisaatio.service.util;

import fi.vm.sade.organisaatio.model.Organisaatio;
import org.junit.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    public void getParentOid() {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setParentOidPath("1|2|3|4");
        organisaatio.getParentOid();
        assertThat(organisaatio.getParentOid()).contains("3");
    }

    @Test
    public void getParentOidJuuriOrganisaatioon() {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setParentOidPath(null);
        organisaatio.getParentOid();
        assertThat(organisaatio.getParentOid()).isEmpty();
    }

}

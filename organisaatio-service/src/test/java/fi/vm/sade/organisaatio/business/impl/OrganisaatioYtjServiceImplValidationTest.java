/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 */

package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.oid.service.OIDService;
import fi.vm.sade.oid.service.simple.OIDServiceSimpleImpl;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.rajapinnat.ytj.api.YTJDTO;
import fi.vm.sade.rajapinnat.ytj.api.YTJOsoiteDTO;
import fi.ytj.YTunnusDTO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class OrganisaatioYtjServiceImplValidationTest {
    private OrganisaatioYtjServiceImpl organisaatioYtjService = new OrganisaatioYtjServiceImpl();
    private OrganisaatioBusinessChecker checker = new OrganisaatioBusinessChecker();
    private OrganisaatioBusinessService organisaatioBusinessService = new OrganisaatioBusinessServiceImpl();
    private OIDService oidService = new OIDServiceSimpleImpl();
    private YTJDTO ytjdto;
    private Organisaatio org = new Organisaatio();

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(organisaatioYtjService, "checker", checker);
        ReflectionTestUtils.setField(organisaatioBusinessService, "checker", checker);
        ReflectionTestUtils.setField(organisaatioYtjService, "businessService", organisaatioBusinessService);
        ReflectionTestUtils.setField(organisaatioYtjService, "oidService", oidService);
        ReflectionTestUtils.setField(organisaatioYtjService, "ytjPaivitysLoki", new YtjPaivitysLoki());
        ytjdto = generateValidYtjdto();
        initGeneralOrgData();
        generateOrganisaatioNimi(new GregorianCalendar(2010, Calendar.JANUARY, 1).getTime());
    }

    @Test
    public void noNameHistoryUpdateIfOnlyLetterCaseChanges() {
        ytjdto.setNimi("NIMI");
        // nimen alkupvm
        ytjdto.setAloitusPvm("01.02.2013");
        // original name
        Assert.assertEquals("nimi", org.getNimi().getString("fi"));
        Assert.assertTrue((Boolean) ReflectionTestUtils.invokeMethod(organisaatioYtjService, "updateOrg", ytjdto, org, false));
        // updated name from YTJ
        Assert.assertEquals("NIMI", org.getNimi().getString("fi"));
        Assert.assertEquals(1, org.getNimet().size());
        Assert.assertEquals(org.getNimet().get(0).getAlkuPvm(), new GregorianCalendar(2013, Calendar.FEBRUARY, 1).getTime());
        // same value in name history
        Assert.assertEquals("NIMI", org.getNimet().get(0).getNimi().getString("fi"));
    }

    @Test
    public void validationPassesIfYtjHasOlderStartDateAndOrgNoChildren() {
        ytjdto.getYritysTunnus().setAlkupvm("01.02.2000");
        // just to pass name validations, we are not testing that now
        ytjdto.setAloitusPvm("01.01.2010");
        org.setNimet(new ArrayList<OrganisaatioNimi>());
        generateOrganisaatioNimi(new GregorianCalendar(2010, Calendar.JANUARY, 1).getTime());
        org.setAlkuPvm(new GregorianCalendar(2010, Calendar.JANUARY, 1).getTime());
        Assert.assertTrue((Boolean) ReflectionTestUtils.invokeMethod(organisaatioYtjService, "updateOrg", ytjdto, org, false));
        try {
            // date updated from YTJ
            Assert.assertEquals(new SimpleDateFormat("dd.MM.yyyy").parse(ytjdto.getYritysTunnus().getAlkupvm()), org.getAlkuPvm());
        } catch (ParseException e) {
            Assert.fail();
        }
        YtjPaivitysLoki loki = (YtjPaivitysLoki) ReflectionTestUtils.getField(organisaatioYtjService, "ytjPaivitysLoki");
        Assert.assertTrue(loki.getYtjVirheet().isEmpty());
    }

    @Test
    public void ytjHasLaterStartThanOrgsChildren() {
        ytjdto.getYritysTunnus().setAlkupvm("01.02.2000");
        // just to pass name validations, we are not testing that now
        ytjdto.setAloitusPvm("01.01.2010");

        Organisaatio child = new Organisaatio();
        child.setAlkuPvm(new GregorianCalendar(1999, Calendar.JANUARY, 1).getTime());
        OrganisaatioSuhde os1 = new OrganisaatioSuhde();
        os1.setParent(org);
        os1.setChild(child);
        os1.setSuhdeTyyppi(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.HISTORIA);
        List<OrganisaatioSuhde> children = new ArrayList<>();
        children.add(os1);
        ReflectionTestUtils.setField(org, "childSuhteet", children);
        Assert.assertTrue((Boolean) ReflectionTestUtils.invokeMethod(organisaatioYtjService, "updateOrg", ytjdto, org, false));
        try {
            Assert.assertEquals(new SimpleDateFormat("dd.MM.yyyy").parse(ytjdto.getYritysTunnus().getAlkupvm()), org.getAlkuPvm());
        } catch (ParseException e) {
            Assert.fail();
        }
        YtjPaivitysLoki loki = (YtjPaivitysLoki) ReflectionTestUtils.getField(organisaatioYtjService, "ytjPaivitysLoki");
        Assert.assertTrue(loki.getYtjVirheet().isEmpty());
        //Assert.assertTrue(loki.getYtjVirheet().size()==1);
        //Assert.assertEquals(YtjVirhe.YTJVirheKohde.ALKUPVM, loki.getYtjVirheet().get(0).getVirhekohde());
        //Assert.assertEquals("ilmoitukset.log.virhe.alkupvm.tarkistukset", loki.getYtjVirheet().get(0).getVirheviesti());
    }

    @Test
    public void validationFailsIfYtjHasStartDateBeforeMinDate() {
        ytjdto.getYritysTunnus().setAlkupvm("01.02.1870");
        // just to pass name validations, we are not testing that now
        ytjdto.setAloitusPvm("01.01.2010");
        Assert.assertFalse((Boolean) ReflectionTestUtils.invokeMethod(organisaatioYtjService, "updateOrg", ytjdto, org, false));
        try {
            // date updated from YTJ
            Assert.assertNotEquals(new SimpleDateFormat("dd.MM.yyyy").parse(ytjdto.getYritysTunnus().getAlkupvm()), org.getAlkuPvm());
        } catch (ParseException e) {
            Assert.fail();
        }
        YtjPaivitysLoki loki = (YtjPaivitysLoki) ReflectionTestUtils.getField(organisaatioYtjService, "ytjPaivitysLoki");
        Assert.assertFalse(loki.getYtjVirheet().isEmpty());
        Assert.assertTrue(loki.getYtjVirheet().size()==1);
        Assert.assertEquals(YtjVirhe.YTJVirheKohde.ALKUPVM, loki.getYtjVirheet().get(0).getVirhekohde());
        Assert.assertEquals("ilmoitukset.log.virhe.alkupvm.tarkistukset", loki.getYtjVirheet().get(0).getVirheviesti());
    }

    private YTJDTO generateValidYtjdto() {
        YTJDTO ytjdto = new YTJDTO();
        ytjdto.setYrityksenKieli("Suomi");
        // nimen alkupvm
        ytjdto.setNimi("nimi");
        ytjdto.setAloitusPvm("01.02.2000");
        ytjdto.setYritysTunnus(new YTunnusDTO());
        // alkupvm
        ytjdto.getYritysTunnus().setAlkupvm("01.02.2000");
        YTJOsoiteDTO osoite = new YTJOsoiteDTO();
        osoite.setKatu("katu1");
        ytjdto.setPostiOsoite(osoite);
        return ytjdto;
    }

    private void initGeneralOrgData() {
        org.setOid("1234.5");
        String kieli = organisaatioYtjService.ORG_KIELI_KOODI_FI;
        List kielet = new ArrayList<>();
        kielet.add(kieli);
        org.setKielet(kielet);
        Osoite orgOsoite = new Osoite();
        orgOsoite.setOsoite("katu1");
        orgOsoite.setKieli(organisaatioYtjService.KIELI_KOODI_FI);
        orgOsoite.setOsoiteTyyppi(Osoite.TYYPPI_POSTIOSOITE);
        List yhteystiedot = new ArrayList<>();
        yhteystiedot.add(orgOsoite);
        org.setYhteystiedot(yhteystiedot);
        ReflectionTestUtils.setField(org, "parentSuhteet", new ArrayList());
    }

    private void generateOrganisaatioNimi(Date alkupvm) {
        OrganisaatioNimi organisaatioNimi = new OrganisaatioNimi();
        organisaatioNimi.setOrganisaatio(org);
        final MonikielinenTeksti nimi = new MonikielinenTeksti();
        nimi.setValues(new HashMap<String, String>() {{put("fi", "nimi");}});
        organisaatioNimi.setNimi(nimi);
        organisaatioNimi.setAlkuPvm(alkupvm);
        org.setNimi(nimi);
        org.addNimi(organisaatioNimi);
    }
}

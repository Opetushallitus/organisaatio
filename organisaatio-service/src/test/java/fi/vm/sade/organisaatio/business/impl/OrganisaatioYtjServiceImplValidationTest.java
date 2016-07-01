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

import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioNimi;
import fi.vm.sade.rajapinnat.ytj.api.YTJDTO;
import fi.ytj.YTunnusDTO;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class OrganisaatioYtjServiceImplValidationTest {
    private OrganisaatioYtjServiceImpl organisaatioYtjService = new OrganisaatioYtjServiceImpl();
    private  OrganisaatioBusinessChecker checker = new OrganisaatioBusinessChecker();

    public OrganisaatioYtjServiceImplValidationTest() {
        ReflectionTestUtils.setField(organisaatioYtjService, "checker", checker);
    }

    @Test
    public void noNameHistoryUpdateIfOnlyLetterCaseChanges() {
        YTJDTO ytjdto = new YTJDTO();
        ytjdto.setYrityksenKieli("Suomi");
        ytjdto.setYritysTunnus(new YTunnusDTO());
        ytjdto.setNimi("NIMI");
        ytjdto.setAloitusPvm("01.02.2013");
        Organisaatio org = new Organisaatio();
        OrganisaatioNimi organisaatioNimi = new OrganisaatioNimi();
        organisaatioNimi.setOrganisaatio(org);
        final MonikielinenTeksti nimi = new MonikielinenTeksti();
        nimi.setValues(new HashMap<String, String>() {{put("fi", "nimi");}});
        organisaatioNimi.setNimi(nimi);
        organisaatioNimi.setAlkuPvm(new GregorianCalendar(2013, Calendar.JANUARY, 1).getTime());
        org.setNimi(nimi);
        // add to name history
        org.addNimi(organisaatioNimi);
        Assert.assertTrue((Boolean) ReflectionTestUtils.invokeMethod(organisaatioYtjService, "updateOrg", ytjdto, org, false));
        //Assert.assertTrue(organisaatioYtjService.updateOrg(ytjdto, org, false));
        Assert.assertEquals("NIMI", org.getNimi().getString("fi"));
        Assert.assertEquals(1, org.getNimet().size());
        Assert.assertEquals(org.getNimet().get(0).getAlkuPvm(), new GregorianCalendar(2013, Calendar.FEBRUARY, 1).getTime());
        // same value in name history
        Assert.assertEquals("NIMI", org.getNimet().get(0).getNimi().getString("fi"));
    }
}

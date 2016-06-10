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

import fi.vm.sade.organisaatio.model.YtjPaivitysLoki;
import fi.vm.sade.organisaatio.model.YtjVirhe;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.GregorianCalendar;

@RunWith(MockitoJUnitRunner.class)
public class ViestintaTest {
    @Mock
    private OrganisaatioViestintaClient organisaatioViestintaClient;

    @InjectMocks
    private OrganisaatioViestintaImpl organisaatioViestinta;

    @Test
    public void messageFromLogTest() {
        final YtjVirhe virhe = new YtjVirhe();
        YtjPaivitysLoki loki = new YtjPaivitysLoki();
        // virhe
        virhe.setOid("12345.0");
        virhe.setVirhekentta("foo");
        virhe.setVirheviesti("bar");
        virhe.setOrgNimi("Organisaatio x");
        // loki
        loki.setPaivitetytLkm(1);
        loki.setPaivitysaika(new GregorianCalendar(2017, 0, 1).getTime());
        loki.setYtjVirheet(new ArrayList<YtjVirhe>() {{add(virhe);}});
        loki.setPaivitysTila(YtjPaivitysLoki.YTJPaivitysStatus.ONNISTUNUT_VIRHEITA);

        organisaatioViestinta.sendPaivitysLokiViestintaEmail(loki);
    }
}

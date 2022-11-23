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

import fi.vm.sade.organisaatio.client.OrganisaatioViestintaClient;
import fi.vm.sade.organisaatio.model.YtjPaivitysLoki;
import fi.vm.sade.organisaatio.model.YtjVirhe;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@Transactional
@SpringBootTest
@AutoConfigureTestDatabase
public class ViestintaTest {

    @TestConfiguration
    static class TestContextConfiguration {


        @Bean
        @Primary
        public OrganisaatioViestintaClient organisaatioViestintaClient() {
            return mock(OrganisaatioViestintaClient.class);
        }


        @Bean
        @Primary
        public OrganisaatioViestintaImpl organisaatioViestinta() {
            return mock(OrganisaatioViestintaImpl.class);
        }

    }

    @Autowired
    private OrganisaatioViestintaClient organisaatioViestintaClient;
    @Autowired
    private OrganisaatioViestintaImpl organisaatioViestinta;

    @Test
    public void messageFromLogTestWith1Error() {
        final YtjVirhe virhe = new YtjVirhe();
        YtjPaivitysLoki loki = new YtjPaivitysLoki();
        String validMessage = "YTJ-Tietojen haku 01.01.2017 klo 00.00 onnistui, 1 virheellistä<br><a href=\"https://localhost:8180/organisaatio-ui/html/organisaatiot/12345.0\">Organisaatio x</a> (Nimitieto on vanhempi YTJ:ssä)<br><br><a href=\"https://localhost:8180/organisaatio-ui/html/organisaatiot/ilmoitukset\">YTJ-päivitykset</a>";
        // virhe
        virhe.setOid("12345.0");
        virhe.setVirhekohde(YtjVirhe.YTJVirheKohde.NIMI);
        virhe.setVirheviesti("ilmoitukset.log.virhe.nimi.vanha");
        virhe.setOrgNimi("Organisaatio x");
        // loki
        loki.setPaivitetytLkm(1);
        loki.setPaivitysaika(new GregorianCalendar(2017, 0, 1).getTime());
        loki.setYtjVirheet(new ArrayList<YtjVirhe>() {{add(virhe);}});
        loki.setPaivitysTila(YtjPaivitysLoki.YTJPaivitysStatus.ONNISTUNUT_VIRHEITA);

        String outputMessage = ReflectionTestUtils.invokeMethod(organisaatioViestinta, "generateMessageFromPaivitysloki", loki);
        assertEquals(validMessage, outputMessage);
    }

    @Test
    public void messageFromLogTestWithNoErrors() {
        YtjPaivitysLoki loki = new YtjPaivitysLoki();
        String validMessage = "YTJ-Tietojen haku 01.01.2017 klo 00.00 onnistui<br><br><a href=\"https://localhost:8180/organisaatio-ui/html/organisaatiot/ilmoitukset\">YTJ-päivitykset</a>";
        // loki
        loki.setPaivitetytLkm(3);
        loki.setPaivitysaika(new GregorianCalendar(2017, 0, 1).getTime());
        loki.setYtjVirheet(new ArrayList<YtjVirhe>());
        loki.setPaivitysTila(YtjPaivitysLoki.YTJPaivitysStatus.ONNISTUNUT_VIRHEITA);

        String outputMessage = ReflectionTestUtils.invokeMethod(organisaatioViestinta, "generateMessageFromPaivitysloki", loki);
        assertEquals(validMessage, outputMessage);
    }

    @Test
    public void messageFromLogTestWith3Errors() {
        final YtjVirhe virhe = new YtjVirhe();
        final YtjVirhe virhe2 = new YtjVirhe();
        final YtjVirhe virhe3 = new YtjVirhe();
        YtjPaivitysLoki loki = new YtjPaivitysLoki();
        String validMessagePart1 = "YTJ-Tietojen haku 01.01.2014 klo 00.00 onnistui, 2 virheellistä<br>";
        String validMessagePart2 = "<a href=\"https://localhost:8180/organisaatio-ui/html/organisaatiot/12345.2\">Organisaatio y</a> (YTJ alkupäivämäärä ei läpäise tarkistuksia, Nimitieto on vanhempi YTJ:ssä)<br>";
        String validMessagePart3 = "<a href=\"https://localhost:8180/organisaatio-ui/html/organisaatiot/12345.0\">Organisaatio x</a> (Nimitieto on vanhempi YTJ:ssä)<br>";
        String validMessagePart4 = "<br><a href=\"https://localhost:8180/organisaatio-ui/html/organisaatiot/ilmoitukset\">YTJ-päivitykset</a>";

        // virhe 1
        virhe.setOid("12345.0");
        virhe.setVirhekohde(YtjVirhe.YTJVirheKohde.NIMI);
        virhe.setVirheviesti("ilmoitukset.log.virhe.nimi.vanha");
        virhe.setOrgNimi("Organisaatio x");
        // virhe 2
        virhe2.setOid("12345.2");
        virhe2.setVirhekohde(YtjVirhe.YTJVirheKohde.TALLENNUS);
        virhe2.setVirheviesti("ilmoitukset.log.virhe.alkupvm.tarkistukset");
        virhe2.setOrgNimi("Organisaatio y");
        // virhe 3
        virhe3.setOid("12345.2");
        virhe3.setVirhekohde(YtjVirhe.YTJVirheKohde.TUNTEMATON);
        virhe3.setVirheviesti("ilmoitukset.log.virhe.nimi.vanha");
        virhe3.setOrgNimi("Organisaatio y");
        // loki
        loki.setPaivitetytLkm(4);
        loki.setPaivitysaika(new GregorianCalendar(2014, 0, 1).getTime());
        loki.setYtjVirheet(new ArrayList<YtjVirhe>() {{add(virhe);add(virhe2);add(virhe3);}});
        loki.setPaivitysTila(YtjPaivitysLoki.YTJPaivitysStatus.ONNISTUNUT_VIRHEITA);

        String outputMessage = ReflectionTestUtils.invokeMethod(organisaatioViestinta, "generateMessageFromPaivitysloki", loki);
        assertTrue(outputMessage.contains(validMessagePart1));
        assertTrue(outputMessage.contains(validMessagePart2));
        assertTrue(outputMessage.contains(validMessagePart3));
        assertTrue(outputMessage.contains(validMessagePart4));
    }
}

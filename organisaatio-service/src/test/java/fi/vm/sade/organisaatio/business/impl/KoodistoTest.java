/*
 * Copyright (c) 2014 The Finnish Board of Education - Opetushallitus
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

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KoodistoTest {

    @Test
    public void test() {
        List<OrganisaatioKoodistoKoodiCodeElements> elements = new LinkedList<>();

        // Koodistossa olevat relaatiot
        List<String> koodistoRelaatiot = new LinkedList<>();
        koodistoRelaatiot.add("aaa_101");
        koodistoRelaatiot.add("aaa_103");
        koodistoRelaatiot.add("ccc_101");
        koodistoRelaatiot.add("ddd_102");
        koodistoRelaatiot.add("fff_101");

        // Uuudet organisaation tiedoista kaivettavat relaatiot
        List<String> entityRelaatiot = new LinkedList<>();
        entityRelaatiot.add("aaa_101");
        entityRelaatiot.add("aaa_102");
        entityRelaatiot.add("bbb_101");
        entityRelaatiot.add("ccc_101");
        entityRelaatiot.add("ddd_101");
        entityRelaatiot.add("eee_101");
        
        // Lopputuloksena koodistoon pitäisi mennä nämä relaatiot
        List<String> koodistoTulosRelaatiot = new LinkedList<>();
        // aaa-relaatio 103 poistuu, 101 säilyy, 102 lisätään
        koodistoTulosRelaatiot.add("aaa_101");
        koodistoTulosRelaatiot.add("aaa_102");
        // bbb-relaatio lisätään
        koodistoTulosRelaatiot.add("bbb_101");
        // ccc-relaatio säilyy
        koodistoTulosRelaatiot.add("ccc_101");
        // ddd-relaatio muuttuu 102 -> 101
        koodistoTulosRelaatiot.add("ddd_101");
        // fff-relaatio säilyy
        koodistoTulosRelaatiot.add("fff_101");
        // eee-relaatio lisätään
        koodistoTulosRelaatiot.add("eee_101");
        
                
        for (String r: koodistoRelaatiot) {
            OrganisaatioKoodistoKoodiCodeElements e = new OrganisaatioKoodistoKoodiCodeElements();
            e.setCodeElementUri(r);
            e.setCodeElementVersion(1);
            e.setPassive(false);
            elements.add(e);
        }
        
        OrganisaatioKoodistoImpl k = new OrganisaatioKoodistoImpl(null, null, null, null);
        boolean result = k.paivitaCodeElements(entityRelaatiot, elements);
        Map<String, Object> elementsResult = new HashMap<String, Object>();
        for (OrganisaatioKoodistoKoodiCodeElements e: elements) {
            elementsResult.put(e.getCodeElementUri(), e);
        }
        
        assertTrue(result);
        
        assertEquals(7, elements.size());
        
        for (String r: koodistoTulosRelaatiot) {
            assertTrue(elementsResult.containsKey(r));
        }
    }
}

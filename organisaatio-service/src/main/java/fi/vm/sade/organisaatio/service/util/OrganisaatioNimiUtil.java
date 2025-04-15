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

package fi.vm.sade.organisaatio.service.util;


import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import fi.vm.sade.organisaatio.model.OrganisaatioNimi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author hpy
 */
public class OrganisaatioNimiUtil {
    private static Logger LOG = LoggerFactory.getLogger("OrganisaatioNimiUtil");

    /**
     * Palauttaa nimihistorian perusteella organisaation nimen.
     * Nimi on joko tämänhetkinen (voimassaoloajaltaan nykyinen) nimi
     * tai sitten uudelle organisaatiolle tulevaisuuden nimi.
     *
     * @param nimet
     * @return
     */
    public static MonikielinenTeksti getNimi(Collection<OrganisaatioNimi> nimet) {
        OrganisaatioNimi currentNimi = null;
        for (OrganisaatioNimi nimi : nimet) {
            if (isValidCurrentNimi(nimi)) {
                if (currentNimi == null) {
                    currentNimi = nimi;
                }
                else if (nimi.getAlkuPvm().after(currentNimi.getAlkuPvm())) {
                    currentNimi = nimi;
                }
            }
        }

        if (currentNimi == null) {
            return getUusinNimi(nimet);
        }

        return currentNimi.getNimi();
    }

    public static MonikielinenTeksti getUusinNimi(Collection<OrganisaatioNimi> nimet) {
        OrganisaatioNimi uusinNimi = null;
        for (OrganisaatioNimi nimi : nimet) {
            if (uusinNimi == null) {
                uusinNimi = nimi;
            }
            else if (nimi.getAlkuPvm().after(uusinNimi.getAlkuPvm())) {
                uusinNimi = nimi;
            }
        }
        if (uusinNimi == null) {
            LOG.warn("Uusin nimi not found!");
            return null;
        }

        return uusinNimi.getNimi();
    }

    public static boolean isValidCurrentNimi(OrganisaatioNimi nimi) {
        if (nimi.getAlkuPvm() != null) {
            Date today = new Date();

            if (OrganisaatioUtil.isSameDay(today, nimi.getAlkuPvm()) ||
                    nimi.getAlkuPvm().before(today)) {
                return true;
            }
        }
        return false;
    }

    public static String createNimihaku(MonikielinenTeksti nimi) {
        String nimihaku =  Optional
                .ofNullable(nimi)
                .map(MonikielinenTeksti::getValues)
                .map(Map::values)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .collect(Collectors.joining(","));
        return nimihaku.isBlank() ? null : nimihaku;
    }
}

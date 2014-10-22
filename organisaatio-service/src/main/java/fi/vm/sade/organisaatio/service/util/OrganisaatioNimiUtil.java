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


import fi.vm.sade.organisaatio.model.OrganisaatioNimi;
import java.util.Date;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hpy
 */
public class OrganisaatioNimiUtil {
    private static Logger LOG = LoggerFactory.getLogger("OrganisaatioNimiUtil");

    public static boolean isCurrentNimi(OrganisaatioNimi nimi) {
        if (nimi.getAlkuPvm() != null) {
            Date today = new Date();

            if (DateUtils.isSameDay(today, nimi.getAlkuPvm()) ||
                    nimi.getAlkuPvm().before(today)) {
                return true;
            }
        }
        return false;
    }
}

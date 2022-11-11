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

package fi.vm.sade.organisaatio.util;

import fi.vm.sade.oid.service.types.NodeClassCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * @author simok
 */
public final class OrganisaatioOidTestUtil {
    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioOidTestUtil.class);

    private static final String root = "1.2.246.562.";
    private static final String[] values = new String[]{"5", "6", "10", "11", "12", "13", "14", "16", "17", "18", "19", "20",
        "22", "24", "27"};
    private static final NodeClassCode[] codes = new NodeClassCode[] {
                                        NodeClassCode.TEKN_5,
                                        NodeClassCode.TEKN_6,
                                        NodeClassCode.TOIMIPAIKAT,
                                        NodeClassCode.ASIAKIRJAT,
                                        NodeClassCode.OHJELMISTOT,
                                        NodeClassCode.LAITTEET,
                                        NodeClassCode.PALVELUT,
                                        NodeClassCode.LASKUTUS,
                                        NodeClassCode.LOGISTIIKKA,
                                        NodeClassCode.SANOMALIIKENNE,
                                        NodeClassCode.REKISTERINPITAJA,
                                        NodeClassCode.NAYTETUNNISTE,
                                        NodeClassCode.TILAP_ASIAKAS,
                                        NodeClassCode.HENKILO,
                                        NodeClassCode.ROOLI
                                        };

    private OrganisaatioOidTestUtil() {

    }

    public static String createOid() {
        return createOid(NodeClassCode.TOIMIPAIKAT);
    }

    public static String createOid(NodeClassCode nodeClass) {

        int valueIndex = -1;
        for (int i = 0; i < codes.length; ++i) {
            if (codes[i].equals(nodeClass)) {
                valueIndex = i;
                break;
            }
        }
        if (valueIndex < 0) {
            // Generate TEKN_5 oid
            valueIndex = 0;
        }

        return generateOid(values[valueIndex]);
    }

    private static String generateOid(String nodeClassValue) {
        LOG.debug("Generating new OID for node class: " + nodeClassValue);
        String newOid = root + nodeClassValue + "." + generateRandom();
        LOG.debug("New oid generated: " + newOid);
        return newOid;
    }

    private static String generateRandom() {

        long min = 1000000000L;
        long max = 10000000000L;

        Random r = new Random();
        long number = min + ((long) (r.nextDouble() * (max - min)));

        String n = Long.toString(number);
        n += luhnChecksum(number);
        return n;
    }

    private static int luhnChecksum(Long oid) {
        String oidStr = oid.toString();

        int sum = 0;
        boolean alternate = false;

        for (int i = oidStr.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(oidStr.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }

        return sum % 10;
    }
}

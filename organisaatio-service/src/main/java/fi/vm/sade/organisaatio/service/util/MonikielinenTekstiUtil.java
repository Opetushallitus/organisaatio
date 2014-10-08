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


import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hpy
 */
public class MonikielinenTekstiUtil {
    private static Logger LOG = LoggerFactory.getLogger("MonikielinenTekstiUtil");

    // Palauttaa monikielisen tekstin jollain kielellä
    public static String getTextInAnyLang(MonikielinenTeksti teksti) {
        if (teksti == null) {
            return null;
        }
        if (teksti.getValues().containsKey("fi")) {
            return teksti.getString("fi");
        }
        else if (teksti.getValues().containsKey("sv")) {
            return teksti.getString("sv");
        }
        else if (teksti.getValues().containsKey("en")) {
            return teksti.getString("en");
        }
        return null;
    }

    // Returns true if the two MonikielinenTekstis have same value in any kieliKoodi.
    public static boolean haveSameText(MonikielinenTekstiTyyppi teksti, MonikielinenTekstiTyyppi teksti2) {
        return MonikielinenTekstiUtil.haveSameText(teksti, teksti2, true);
    }

    // Returns true if the two MonikielinenTekstis have same value in any kieliKoodi (except if ignoreEmptyValues is true and the same values are empty strings).
    public static boolean haveSameText(MonikielinenTekstiTyyppi monikielinenTekstiTyyppi, MonikielinenTekstiTyyppi monikielinenTekstiTyyppi2, boolean ignoreEmptyValues) {
        List<MonikielinenTekstiTyyppi.Teksti> teksti, teksti2;
        teksti = monikielinenTekstiTyyppi.getTeksti();
        teksti2 = monikielinenTekstiTyyppi2.getTeksti();
        for (int i = 0; i < teksti.size(); ++i) {
                MonikielinenTekstiTyyppi.Teksti t = teksti.get(i);
                for (int j = 0; j < teksti2.size(); ++j) {
                   MonikielinenTekstiTyyppi.Teksti t2 = teksti2.get(j);
                   if (t.getKieliKoodi().equals(t2.getKieliKoodi())) {
                       if (t.getValue().equals(t2.getValue())) {
                           if (ignoreEmptyValues && t.getValue().equals("")) {
                               // ignore
                           } else {
                               return true;
                           }
                       }
                   }
                }
            }
        return false;
    }
}

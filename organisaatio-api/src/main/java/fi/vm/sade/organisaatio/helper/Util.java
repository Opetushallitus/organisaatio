/*
 *
 * Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
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
 * European Union Public Licence for more details.
 */
package fi.vm.sade.organisaatio.helper;

import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi.Teksti;

public class Util {

    /**
     * return dto object populated with data, useful in tests.
     * 
     * @param data lang (even), value (odd) pairs
     * @return
     */
    public static MonikielinenTekstiTyyppi getMonikielinenTekstiTyyppi(
            String... data) {
        MonikielinenTekstiTyyppi mkt = new MonikielinenTekstiTyyppi();
        for (int i = 0; i < data.length / 2; i++) {
            Teksti teksti = new Teksti();
            teksti.setKieliKoodi(data[i * 2]);
            teksti.setValue(data[i * 2 + 1]);
            mkt.getTeksti().add(teksti);
        }
        return mkt;
    }

}

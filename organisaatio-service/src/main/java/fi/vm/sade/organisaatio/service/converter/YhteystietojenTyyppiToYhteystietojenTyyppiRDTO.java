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

package fi.vm.sade.organisaatio.service.converter;

import fi.vm.sade.generic.service.conversion.AbstractFromDomainConverter;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import fi.vm.sade.organisaatio.model.YhteystietoElementti;
import fi.vm.sade.organisaatio.model.YhteystietojenTyyppi;
import fi.vm.sade.organisaatio.resource.dto.YhteystietojenTyyppiRDTO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class YhteystietojenTyyppiToYhteystietojenTyyppiRDTO extends AbstractFromDomainConverter<YhteystietojenTyyppi, YhteystietojenTyyppiRDTO>{

    @Override
    public YhteystietojenTyyppiRDTO convert(YhteystietojenTyyppi s) {
        YhteystietojenTyyppiRDTO r = new YhteystietojenTyyppiRDTO();
        r.setNimi(convertMKTToMap(s.getNimi()));
        r.setSovellettavatOppilaitosTyyppis(s.getSovellettavatOppilaitostyyppis());
        r.setSovellettavatOrganisaatioTyyppis(s.getSovellettavatOrganisaatioTyyppis());
        r.setLisatietos(convertLisatietos(s.getLisatietos()));
        return r;
    }

    private Map<String, String> convertMKTToMap(MonikielinenTeksti nimi) {
        Map<String, String> result = new HashMap<String, String>();

        if (nimi != null) {
            result.putAll(nimi.getValues());
        }

        return result;
    }

    private List<Map<String, String>> convertLisatietos(List<YhteystietoElementti> les) {
        List<Map<String, String>> ret = new ArrayList<Map<String, String>>(les.size());
        for (YhteystietoElementti e : les) {
            ret.add(convertYhteystietoElementti(e));
        }
        return ret;
    }

    private Map<String, String> convertYhteystietoElementti(YhteystietoElementti e) {
        Map<String, String> val = new HashMap<String, String>();
        val.put("YhteystietoElementti.nimi", e.getNimi());
        val.put("YhteystietoElementti.nimiSv", e.getNimiSv());
        val.put("YhteystietoElementti.oid", e.getOid());
        val.put("YhteystietoElementti.tyyppi", e.getTyyppi());
        val.put("YhteystietoElementti.kaytossa", Boolean.toString(e.isKaytossa()));
        val.put("YhteystietoElementti.pakollinen", Boolean.toString(e.isPakollinen()));

        YhteystietojenTyyppi yTyyppi = e.getYhteystietojenTyyppi();

        if (yTyyppi != null) {
            Map<String, String> nimiMap = convertMKTToMap(yTyyppi.getNimi());
            for (String kieli : nimiMap.keySet()) {
                val.put("YhteystietojenTyyppi.nimi." + kieli, nimiMap.get(kieli));
            }
            val.put("YhteystietojenTyyppi.oid", yTyyppi.getOid());
        }
        return val;
    }
}

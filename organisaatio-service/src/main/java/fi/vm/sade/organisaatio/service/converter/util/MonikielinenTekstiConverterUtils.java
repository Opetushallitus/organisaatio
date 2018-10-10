package fi.vm.sade.organisaatio.service.converter.util;

import fi.vm.sade.organisaatio.model.MonikielinenTeksti;

import java.util.Map;

public class MonikielinenTekstiConverterUtils {
    public static MonikielinenTeksti convertMapToMonikielinenTeksti(Map<String, String> m) {
        MonikielinenTeksti mt = null;
        if (m != null) {
            mt = new MonikielinenTeksti();
            for (Map.Entry<String, String> e : m.entrySet()) {
                mt.addString(e.getKey(), e.getValue());
            }
        }
        return mt;
    }


}

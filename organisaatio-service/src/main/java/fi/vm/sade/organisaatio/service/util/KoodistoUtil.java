package fi.vm.sade.organisaatio.service.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public final class KoodistoUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(KoodistoUtil.class);

    private KoodistoUtil() {
    }

    private final static BiMap<String, String> RYHMATYYPIT;
    private final static BiMap<String, String> KAYTTORYHMAT;

    static {
        Map<String, String> tmp = new LinkedHashMap<>();
        tmp.put("ryhmatyypit_1#1", "organisaatio");
        tmp.put("ryhmatyypit_2#1", "hakukohde");
        tmp.put("ryhmatyypit_3#1", "perustetyoryhma");
        tmp.put("ryhmatyypit_4#1", "koulutus");
        RYHMATYYPIT = HashBiMap.create(tmp);
    }

    static {
        Map<String, String> tmp = new LinkedHashMap<>();
        tmp.put("kayttoryhmat_1#1", "yleinen");
        tmp.put("kayttoryhmat_2#1", "hakukohde_rajaava");
        tmp.put("kayttoryhmat_3#1", "hakukohde_priorisoiva");
        tmp.put("kayttoryhmat_4#1", "hakukohde_liiteosoite");
        tmp.put("kayttoryhmat_5#1", "perusteiden_laadinta");
        tmp.put("kayttoryhmat_6#1", "kayttooikeus");
        KAYTTORYHMAT = HashBiMap.create(tmp);
    }

    public static String getRyhmatyyppiV1(String koodistoValue) {
        String staticValue = RYHMATYYPIT.get(koodistoValue);
        if (staticValue == null) {
            LOGGER.debug("Tuntematon ryhmätyypit-koodiston koodi {}", koodistoValue);
        }
        return staticValue;
    }

    public static String getRyhmatyyppiV3(String staticValue) {
        String koodistoValue = RYHMATYYPIT.inverse().get(staticValue);
        if (koodistoValue == null) {
            throw new IllegalArgumentException(String.format("Tuntematon ryhmätyyppi %s", staticValue));
        }
        return koodistoValue;
    }

    public static String getKayttoryhmaV1(String koodistoValue) {
        String staticValue = KAYTTORYHMAT.get(koodistoValue);
        if (staticValue == null) {
            LOGGER.debug("Tuntematon käyttöryhmät-koodiston koodi {}", koodistoValue);
        }
        return staticValue;
    }

    public static String getKayttoryhmaV3(String staticValue) {
        String koodistoValue = KAYTTORYHMAT.inverse().get(staticValue);
        if (koodistoValue == null) {
            throw new IllegalArgumentException(String.format("Tuntematon käyttöryhmä %s", staticValue));
        }
        return koodistoValue;
    }

}

package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.varda.rekisterointi.model.KielistettyNimi;
import fi.vm.sade.varda.rekisterointi.model.Organisaatio;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioNimi;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioV4Dto;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;

@Service
public class OrganisaatioService {

    private static final String DEFAULT_KIELI_KOODI_URI_VERSION = "kieli_fi#1";
    private static final String DEFAULT_KIELI_KOODI_ARVO = "fi";
    private static final Map<String, String> KIELI_KOODI_URI_VERSION_TO_KOODI_ARVO = Map.of(
            "kieli_fi#1", "fi",
            "kieli_sv#1", "sv",
            "kieli_en#1", "en"
    );

    public Organisaatio muunnaV4Dto(OrganisaatioV4Dto dto) {
        return Organisaatio.of(
                dto.ytunnus,
                dto.oid,
                dto.alkuPvm,
                kuranttiNimi(dto),
                dto.yritysmuoto,
                dto.tyypit,
                dto.kotipaikkaUri,
                dto.maaUri,
                dto.kieletUris);
    }

    KielistettyNimi kuranttiNimi(OrganisaatioV4Dto dto) {
        LocalDate now = LocalDate.now();
        OrganisaatioNimi kurantti = dto.nimet.stream()
                .filter(nimi -> nullSafeDate(nimi.alkuPvm).isBefore(now) || nullSafeDate(nimi.alkuPvm).equals(now))
                .max(Comparator.comparing(nimi -> nullSafeDate(nimi.alkuPvm)))
                .orElseThrow(() -> new IllegalStateException("Ei voimassa olevaa nimeä organisaatiolle: " + dto.ytunnus));
        String ytjKieli = dto.ytjkieli != null ? dto.ytjkieli : DEFAULT_KIELI_KOODI_URI_VERSION;
        String kieli = kieliKoodiUriVersionToKoodiArvo(ytjKieli);
        String ytjKielinen = kurantti.nimi.getOrDefault(kieli, kurantti.nimi.get(DEFAULT_KIELI_KOODI_ARVO));
        if (ytjKielinen == null) {
            throw new IllegalStateException("Ei YTJ-kielen tai oletuskielen mukaista nimeä organisaatiolle: " + dto.ytunnus);
        }
        return KielistettyNimi.of(ytjKielinen, kieli, kurantti.alkuPvm);
    }

    private LocalDate nullSafeDate(LocalDate date) {
        if (date == null) {
            return LocalDate.MIN;
        }
        return date;
    }

    private static String kieliKoodiUriVersionToKoodiArvo(String koodiUriVersion) {
        return KIELI_KOODI_URI_VERSION_TO_KOODI_ARVO.getOrDefault(koodiUriVersion, "fi");
    }

}

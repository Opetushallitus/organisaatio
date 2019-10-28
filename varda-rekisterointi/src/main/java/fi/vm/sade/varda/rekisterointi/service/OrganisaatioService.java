package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.varda.rekisterointi.model.KielistettyNimi;
import fi.vm.sade.varda.rekisterointi.model.Organisaatio;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioNimi;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioV4Dto;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;

@Service
public class OrganisaatioService {

    private static final String DEFAULT_NAME_LANGUAGE = "fi";

    public Organisaatio muunnaV4Dto(OrganisaatioV4Dto dto) {
        return Organisaatio.of(
                dto.ytunnus,
                dto.oid,
                dto.alkuPvm,
                kuranttiNimi(dto),
                dto.yritysmuoto,
                dto.tyypit,
                dto.kotipaikkaUri,
                dto.maaUri);
    }

    KielistettyNimi kuranttiNimi(OrganisaatioV4Dto dto) {
        LocalDate now = LocalDate.now();
        OrganisaatioNimi kurantti = dto.nimet.stream()
                .filter(nimi -> nullSafeDate(nimi.alkuPvm).isBefore(now) || nullSafeDate(nimi.alkuPvm).equals(now))
                .max(Comparator.comparing(nimi -> nullSafeDate(nimi.alkuPvm)))
                .orElseThrow(() -> new IllegalStateException("Ei voimassa olevaa nimeä organisaatiolle: " + dto.ytunnus));
        String ytjKieli = dto.ytjkieli != null ? dto.ytjkieli : DEFAULT_NAME_LANGUAGE;
        String ytjKielinen = kurantti.nimi.getOrDefault(ytjKieli, kurantti.nimi.get(DEFAULT_NAME_LANGUAGE));
        if (ytjKielinen == null) {
            throw new IllegalStateException("Ei YTJ-kielen tai oletuskielen mukaista nimeä organisaatiolle: " + dto.ytunnus);
        }
        return KielistettyNimi.of(ytjKielinen, ytjKieli, kurantti.alkuPvm);
    }

    private LocalDate nullSafeDate(LocalDate date) {
        if (date == null) {
            return LocalDate.MIN;
        }
        return date;
    }

}

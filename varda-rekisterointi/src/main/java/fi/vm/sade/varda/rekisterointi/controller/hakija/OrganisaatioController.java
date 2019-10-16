package fi.vm.sade.varda.rekisterointi.controller.hakija;

import fi.vm.sade.varda.rekisterointi.client.OrganisaatioClient;
import fi.vm.sade.varda.rekisterointi.exception.NotFoundException;
import fi.vm.sade.varda.rekisterointi.model.KielistettyNimi;
import fi.vm.sade.varda.rekisterointi.model.Organisaatio;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioNimi;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioV4Dto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.util.Comparator;

import static fi.vm.sade.varda.rekisterointi.util.Constants.SESSION_ATTRIBUTE_NAME_BUSINESS_ID;
import static fi.vm.sade.varda.rekisterointi.util.Constants.SESSION_ATTRIBUTE_NAME_ORGANISATION_NAME;
import static fi.vm.sade.varda.rekisterointi.util.FunctionalUtils.exceptionToEmptySupplier;
import static fi.vm.sade.varda.rekisterointi.util.ServletUtils.findSessionAttribute;

@RestController
@RequestMapping(OrganisaatioController.BASE_PATH)
public class OrganisaatioController {

    static final String BASE_PATH = "/hakija/api/organisaatiot";
    private static final String DEFAULT_NAME_LANGUAGE = "fi";
    private static final String DEFAULT_MAA_URI = "maatjavaltiot1_fin";

    private final OrganisaatioClient organisaatioClient;

    public OrganisaatioController(OrganisaatioClient organisaatioClient) {
        this.organisaatioClient = organisaatioClient;
    }

    @GetMapping
    public Organisaatio getOrganisaatio(HttpServletRequest request) {
        String businessId = findSessionAttribute(request, SESSION_ATTRIBUTE_NAME_BUSINESS_ID, String.class)
                .orElseThrow(() -> new NotFoundException("Organisaatiota ei löydy istunnosta"));
        return muunnaV4Dto(organisaatioClient.getV4ByYtunnus(businessId)
                .or(exceptionToEmptySupplier(() -> organisaatioClient.getV4ByYtunnusFromYtj(businessId)))
                .orElseGet(() -> this.createMock(businessId, request)));
    }

    private OrganisaatioV4Dto createMock(String businessId, HttpServletRequest request) {
        String organisationName = findSessionAttribute(request, SESSION_ATTRIBUTE_NAME_ORGANISATION_NAME, String.class).orElse("");
        return OrganisaatioV4Dto.of(businessId, organisationName);
    }

    private Organisaatio muunnaV4Dto(OrganisaatioV4Dto dto) {
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

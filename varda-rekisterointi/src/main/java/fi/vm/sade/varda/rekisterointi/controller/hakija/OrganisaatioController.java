package fi.vm.sade.varda.rekisterointi.controller.hakija;

import fi.vm.sade.varda.rekisterointi.client.OrganisaatioClient;
import fi.vm.sade.varda.rekisterointi.exception.InvalidInputException;
import fi.vm.sade.varda.rekisterointi.exception.NotFoundException;
import fi.vm.sade.varda.rekisterointi.model.Organisaatio;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioDto;
import fi.vm.sade.varda.rekisterointi.service.OrganisaatioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import static fi.vm.sade.varda.rekisterointi.util.Constants.SESSION_ATTRIBUTE_NAME_BUSINESS_ID;
import static fi.vm.sade.varda.rekisterointi.util.Constants.SESSION_ATTRIBUTE_NAME_ORGANISATION_NAME;
import static fi.vm.sade.varda.rekisterointi.util.FunctionalUtils.exceptionToEmptySupplier;
import static fi.vm.sade.varda.rekisterointi.util.ServletUtils.findSessionAttribute;

@RestController
@RequestMapping(OrganisaatioController.BASE_PATH)
@RequiredArgsConstructor
public class OrganisaatioController {

    static final String BASE_PATH = "/hakija/api/organisaatiot";

    private final OrganisaatioClient organisaatioClient;
    private final OrganisaatioService organisaatioService;

    /**
     * Hakee organisaation sessio-attribuutin perusteella. Hakee ensin sessiosta y-tunnuksen,
     * jonka jälkeen hakee sillä ensin organisaatiopalvelusta, ja mikäli ei löytynyt, vielä
     * VTJ:stä.
     *
     * @param request   HTTP-pyyntö (session hakemiseksi)
     *
     * @return organisaatiotiedot (mahdollisesti tyhjät)
     */
    @GetMapping
    public Organisaatio getOrganisaatio(HttpServletRequest request) {
        String businessId = findSessionAttribute(request, SESSION_ATTRIBUTE_NAME_BUSINESS_ID, String.class)
                .orElseThrow(() -> new NotFoundException("Organisaatiota ei löydy istunnosta"));
        Organisaatio organisaatio = organisaatioService.muunnaOrganisaatioDto(organisaatioClient.getOrganisaatioByYtunnus(businessId)
                .or(exceptionToEmptySupplier(() -> organisaatioClient.getOrganisaatioByYtunnusFromYtj(businessId)))
                .orElseGet(() -> this.createMock(businessId, request)));
        if ( organisaatio.isKunta() ) {
            throw new InvalidInputException("ERROR_MUNICIPALITY");
        }
        return organisaatio;
    }

    private OrganisaatioDto createMock(String businessId, HttpServletRequest request) {
        String organisationName = findSessionAttribute(request, SESSION_ATTRIBUTE_NAME_ORGANISATION_NAME, String.class).orElse("");
        return OrganisaatioDto.of(businessId, organisationName);
    }

}

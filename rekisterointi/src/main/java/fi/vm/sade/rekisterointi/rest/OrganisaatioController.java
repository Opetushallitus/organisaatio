package fi.vm.sade.rekisterointi.rest;

import fi.vm.sade.rekisterointi.client.OrganisaatioClient;
import fi.vm.sade.rekisterointi.exception.InvalidInputException;
import fi.vm.sade.rekisterointi.exception.NotFoundException;
import fi.vm.sade.rekisterointi.model.Organisaatio;
import fi.vm.sade.rekisterointi.model.OrganisaatioV4Dto;
import fi.vm.sade.rekisterointi.service.OrganisaatioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import static fi.vm.sade.rekisterointi.util.Constants.SESSION_ATTRIBUTE_NAME_BUSINESS_ID;
import static fi.vm.sade.rekisterointi.util.Constants.SESSION_ATTRIBUTE_NAME_ORGANISATION_NAME;
import static fi.vm.sade.rekisterointi.util.FunctionalUtils.exceptionToEmptySupplier;
import static fi.vm.sade.rekisterointi.util.ServletUtils.findSessionAttribute;

@RestController
@RequestMapping(OrganisaatioController.BASE_PATH)
@RequiredArgsConstructor
public class OrganisaatioController {

  static final String BASE_PATH = "/hakija/api/organisaatiot";

  private final OrganisaatioClient organisaatioClient;
  private final OrganisaatioService organisaatioService;

  /**
   * Hakee organisaation sessio-attribuutin perusteella. Hakee ensin sessiosta
   * y-tunnuksen,
   * jonka jälkeen hakee sillä ensin organisaatiopalvelusta, ja mikäli ei
   * löytynyt, vielä
   * VTJ:stä.
   *
   * @param request HTTP-pyyntö (session hakemiseksi)
   *
   * @return organisaatiotiedot (mahdollisesti tyhjät)
   */
  @GetMapping
  public Organisaatio getOrganisaatio(HttpServletRequest request) {
    String businessId = findSessionAttribute(request, SESSION_ATTRIBUTE_NAME_BUSINESS_ID, String.class)
        .orElseThrow(() -> new NotFoundException("Organisaatiota ei löydy istunnosta"));
    Organisaatio organisaatio = organisaatioService.muunnaV4Dto(organisaatioClient.getV4ByYtunnus(businessId)
        .or(exceptionToEmptySupplier(() -> organisaatioClient.getV4ByYtunnusFromYtj(businessId)))
        .orElseGet(() -> this.createMock(businessId, request)));
    if (organisaatio.isKunta()) {
      throw new InvalidInputException("ERROR_MUNICIPALITY");
    }
    return organisaatio;
  }

  private OrganisaatioV4Dto createMock(String businessId, HttpServletRequest request) {
    String organisationName = findSessionAttribute(request, SESSION_ATTRIBUTE_NAME_ORGANISATION_NAME, String.class)
        .orElse("");
    return OrganisaatioV4Dto.of(businessId, organisationName);
  }

}

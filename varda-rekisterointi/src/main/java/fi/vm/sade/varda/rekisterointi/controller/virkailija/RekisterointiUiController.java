package fi.vm.sade.varda.rekisterointi.controller.virkailija;

import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.exception.DataInconsistencyException;
import fi.vm.sade.varda.rekisterointi.exception.InvalidInputException;
import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import fi.vm.sade.varda.rekisterointi.model.RekisterointiDto;
import fi.vm.sade.varda.rekisterointi.service.HakijaLogoutService;
import fi.vm.sade.varda.rekisterointi.service.RekisterointiService;
import fi.vm.sade.varda.rekisterointi.util.RequestContextImpl;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(RekisterointiUiController.BASE_PATH)
public class RekisterointiUiController {

  static final String BASE_PATH = "/api/rekisterointi";

  public RekisterointiUiController() {
  }

  /**
   * Luo rekisteröintihakemuksen.
   *
   * @param dto            rekisteröintitiedot
   * @param request        HTTP-pyyntö
   * @param authentication tunnistautumistiedot
   *
   * @return osoite, jonne ohjataan rekisteröitymisen jälkeen.
   */
  @PostMapping
  @ApiOperation("Luo rekisteröintihakemus")
  @ApiResponse(code = 200, message = "Hakemus luotu", response = String.class)
  public String register(
      @ApiParam(name = "dto", type = "RekisterointiDto") @RequestBody @Validated RekisterointiDto dto,
      HttpServletRequest request,
      Authentication authentication) {
    return "ok";
  }

}

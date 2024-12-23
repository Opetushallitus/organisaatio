package fi.vm.sade.varda.rekisterointi.controller.virkailija;

import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import fi.vm.sade.varda.rekisterointi.model.RekisterointiDto;
import fi.vm.sade.varda.rekisterointi.service.RekisterointiService;
import fi.vm.sade.varda.rekisterointi.util.RequestContextImpl;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(RekisterointiUiController.BASE_PATH)
public class RekisterointiUiController {
  static final String BASE_PATH = "/api/rekisterointi";
  private final RekisterointiService rekisterointiService;

  public RekisterointiUiController(RekisterointiService rekisterointiService) {
    this.rekisterointiService = rekisterointiService;
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
  public String register(
      @RequestBody @Validated RekisterointiDto dto,
      HttpServletRequest request,
      Authentication authentication) {
    rekisterointiService.create(Rekisterointi.from(dto), RequestContextImpl.of(request, authentication));
    return "ok";
  }

}

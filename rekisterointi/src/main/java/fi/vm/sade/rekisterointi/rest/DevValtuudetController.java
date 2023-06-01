package fi.vm.sade.rekisterointi.rest;

import fi.vm.sade.properties.OphProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;

import static fi.vm.sade.rekisterointi.util.Constants.*;
import static fi.vm.sade.rekisterointi.util.ServletUtils.*;

@Profile("dev")
@Controller
@RequestMapping("/hakija")
public class DevValtuudetController {

  private final OphProperties properties;

  public DevValtuudetController(OphProperties properties) {
    this.properties = properties;
  }

  /**
   * Palaa hakijan rekisteröintiin tietyillä valtuuksilla
   *
   * @param request HTTP-pyyntö
   * @param locale  lokaali
   * @return uudelleenohjaus-view.
   */
  @GetMapping("/valtuudet/redirect")
  public View getRedirect(HttpServletRequest request, Locale locale) {
    setSessionAttribute(request, SESSION_ATTRIBUTE_NAME_BUSINESS_ID, "0772017-4");
    setSessionAttribute(request, SESSION_ATTRIBUTE_NAME_ORGANISATION_NAME, "Meyer Turku Oy");

    String redirectUrl = findSessionAttribute(request, SESSION_ATTRIBUTE_NAME_ORIGINAL_REQUEST, String.class)
        .orElse(properties.url("rekisterointi.hakija"));
    return new RedirectView(redirectUrl);
  }

}

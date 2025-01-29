package fi.vm.sade.rekisterointi.rest;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import fi.vm.sade.properties.OphProperties;
import lombok.AllArgsConstructor;

import java.util.Locale;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import static fi.vm.sade.rekisterointi.configuration.LocaleConfiguration.SESSION_ATTRIBUTE_NAME_LOCALE;
import static fi.vm.sade.rekisterointi.configuration.LocaleConfiguration.DEFAULT_LOCALE;
import static fi.vm.sade.rekisterointi.util.ServletUtils.findSessionAttribute;

@Controller
@AllArgsConstructor
@Profile("!dev")
public class LogoutController {
  private final OphProperties ophProperties;

  @GetMapping("/hakija/logout")
  public View logout(HttpServletRequest request) {
    Optional.ofNullable(request.getSession(false)).ifPresent(HttpSession::invalidate);
    String redirectPath = Optional.ofNullable(request.getParameter("redirect")).orElse("/");
    String redirectUrl = ophProperties.getProperty("url-rekisterointi") + redirectPath;
    Locale locale = findSessionAttribute(request, SESSION_ATTRIBUTE_NAME_LOCALE, Locale.class)
        .orElse(DEFAULT_LOCALE);
    String language = locale.getLanguage();
    String casOppijaLogout = ophProperties.url("cas-oppija.logout", redirectUrl, language);
    return new RedirectView(casOppijaLogout);
  }
}

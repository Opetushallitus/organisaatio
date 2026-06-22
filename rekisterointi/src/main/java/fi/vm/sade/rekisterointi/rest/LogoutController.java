package fi.vm.sade.rekisterointi.rest;

import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@Profile("!dev")
public class LogoutController {
  private final String urlRekisterointi;
  private final String urlOppija;

  public LogoutController(@Value("${url-rekisterointi}") String urlRekisterointi,
      @Value("${url-oppija}") String urlOppija) {
    this.urlRekisterointi = urlRekisterointi;
    this.urlOppija = urlOppija;
  }

  @GetMapping("/hakija/logout")
  public View logout(HttpServletRequest request) {
    Optional.ofNullable(request.getSession(false)).ifPresent(HttpSession::invalidate);
    String redirectPath = Optional.ofNullable(request.getParameter("redirect")).orElse("/");
    String redirectUrl = urlRekisterointi + redirectPath;
    String casOppijaLogout = UriComponentsBuilder.fromUriString(urlOppija)
        .path("/cas-oppija/logout")
        .queryParam("service", "{service}")
        .encode()
        .buildAndExpand(redirectUrl)
        .toUriString();
    return new RedirectView(casOppijaLogout);
  }
}

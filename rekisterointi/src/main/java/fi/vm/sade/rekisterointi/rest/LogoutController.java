package fi.vm.sade.rekisterointi.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
public class LogoutController {
  /**
   * Kirjaa hakijan ulos.
   *
   * @param request HTTP-pyyntö
   *
   * @return logout-view.
   */
  @GetMapping("/hakija/logout")
  public View logout(HttpServletRequest request) {
    Optional.ofNullable(request.getSession(false)).ifPresent(HttpSession::invalidate);
    return new RedirectView("/");
  }

}

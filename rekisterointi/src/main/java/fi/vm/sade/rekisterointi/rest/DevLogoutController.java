package fi.vm.sade.rekisterointi.rest;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@Profile("dev | ci")
public class DevLogoutController {
  @GetMapping("/hakija/logout")
  public View logout(HttpServletRequest request) {
    Optional.ofNullable(request.getSession(false)).ifPresent(HttpSession::invalidate);
    var redirectPath = Optional.ofNullable(request.getParameter("redirect")).orElse("/");
    return new RedirectView(redirectPath);
  }

}

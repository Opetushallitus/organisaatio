package fi.vm.sade.varda.rekisterointi.controller;

import fi.vm.sade.properties.OphProperties;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Optional;

@Controller
public class LogoutController {

    private final OphProperties properties;

    public LogoutController(OphProperties properties) {
        this.properties = properties;
    }

    @GetMapping("/hakija/logout")
    public View logout(HttpServletRequest request) {
        Optional.ofNullable(request.getSession(false)).ifPresent(HttpSession::invalidate);

        String callbackUrl = properties.url("varda-rekisterointi.index");
        String logoutUrl = properties.url("shibbolethVirkailija.logout", callbackUrl);
        return new RedirectView(logoutUrl);
    }

}

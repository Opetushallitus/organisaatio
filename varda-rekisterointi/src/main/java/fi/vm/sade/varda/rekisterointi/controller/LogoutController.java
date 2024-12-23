package fi.vm.sade.varda.rekisterointi.controller;

import fi.vm.sade.varda.rekisterointi.service.HakijaLogoutService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class LogoutController {

    private final HakijaLogoutService logoutService;

    public LogoutController(HakijaLogoutService logoutService) {
        this.logoutService = logoutService;
    }

    /**
     * Kirjaa hakijan ulos.
     *
     * @param request   HTTP-pyyntö
     *
     * @return logout-view.
     */
    @GetMapping("/hakija/logout")
    public View logout(HttpServletRequest request) {
        return new RedirectView(logoutService.logout(request));
    }

}

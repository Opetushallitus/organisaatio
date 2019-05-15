package fi.vm.sade.varda.rekisterointi.controller;

import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.model.User;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Scope("session")
public class IndexController {

    private final OphProperties properties;
    private final User user;

    public IndexController(OphProperties properties, User user) {
        this.properties = properties;
        this.user = user;
    }

    @GetMapping("/")
    public String getIndex(Model model) {
        String loginCallbackUrl = properties.url("varda-rekisterointi.login");
        String loginUrl = properties.url("shibbolethVirkailija.login", loginCallbackUrl);
        model.addAttribute("loginUrl", loginUrl);

        String logoutUrl = properties.url("varda-rekisterointi.logout");
        model.addAttribute("logoutUrl", logoutUrl);

        model.addAttribute("user", user);

        return "index";
    }

}

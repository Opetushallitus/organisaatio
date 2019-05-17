package fi.vm.sade.varda.rekisterointi.controller;

import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.NameContainer;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    private final OphProperties properties;

    public IndexController(OphProperties properties) {
        this.properties = properties;
    }

    @GetMapping("/")
    public String getIndex(Model model, Authentication authentication) {
        String logoutUrl = properties.url("varda-rekisterointi.logout");
        model.addAttribute("logoutUrl", logoutUrl);

        model.addAttribute("nationalIdentificationNumber", authentication.getName());
        if (authentication.getDetails() instanceof NameContainer) {
            NameContainer nameContainer = (NameContainer) authentication.getDetails();
            model.addAttribute("givenName", nameContainer.getGivenName());
            model.addAttribute("surname", nameContainer.getSurname());
        }

        return "index";
    }

}

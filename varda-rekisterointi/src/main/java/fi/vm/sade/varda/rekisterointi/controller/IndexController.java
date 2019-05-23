package fi.vm.sade.varda.rekisterointi.controller;

import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.NameContainer;
import fi.vm.sade.varda.rekisterointi.model.Valtuudet;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
@Scope("session")
public class IndexController {

    private final OphProperties properties;
    private final Valtuudet valtuudet;

    public IndexController(OphProperties properties, Valtuudet valtuudet) {
        this.properties = properties;
        this.valtuudet = valtuudet;
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

        model.addAttribute("businessId", valtuudet.businessId);
        String organisationName = Optional.ofNullable(valtuudet.organisaatio.nimi)
                .map(nimi -> nimi.get("fi")).orElse("");
        model.addAttribute("organisationName", organisationName);

        return "index";
    }

}

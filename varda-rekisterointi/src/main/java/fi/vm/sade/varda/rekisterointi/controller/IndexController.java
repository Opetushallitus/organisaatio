package fi.vm.sade.varda.rekisterointi.controller;

import fi.vm.sade.properties.OphProperties;
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
    public String getIndex(Model model) {
        String loginUrl = properties.url("varda-rekisterointi.hakija");
        model.addAttribute("loginUrl", loginUrl);
        return "index";
    }

}

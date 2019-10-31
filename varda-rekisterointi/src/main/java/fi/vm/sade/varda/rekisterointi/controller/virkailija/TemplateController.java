package fi.vm.sade.varda.rekisterointi.controller.virkailija;

import fi.vm.sade.varda.rekisterointi.Template;
import fi.vm.sade.varda.rekisterointi.service.TemplateService;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;

import static java.util.Collections.emptyMap;

@RestController
@RequestMapping("/virkailija/template")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping(value = "/{template}", produces = "text/html")
    public String getTemplate(@PathVariable Template template,
                              @RequestParam(required = false, defaultValue = "fi") String language) {
        return templateService.getContent(template, new Locale(language), getDefaultVariables(template));
    }

    private static Map<String, Object> getDefaultVariables(Template template) {
        switch (template) {
            case REKISTEROITYMINEN_KAYTTAJA:
                return Map.of("organisaatioNimi", "Päiväkoti oy");
            case REKISTEROITYMINEN_PAAKAYTTAJA:
                return Map.of("etunimi", "Ella");
            case REKISTEROITYMINEN_KUNTA:
                return Map.of("organisaatioLkm", 19);
            case REKISTEROITYMINEN_HYLATTY:
                return Map.of("organisaatioNimi", "Päiväkoti oy", "perustelu", "Väärä kunta");
            case REKISTEROITYMINEN_HYVAKSYTTY:
                return Map.of("organisaatioNimi", "Päiväkoti oy");
            default:
                return emptyMap();
        }
    }

}

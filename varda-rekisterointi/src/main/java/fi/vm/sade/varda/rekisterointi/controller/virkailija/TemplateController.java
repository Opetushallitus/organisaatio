package fi.vm.sade.varda.rekisterointi.controller.virkailija;

import fi.vm.sade.varda.rekisterointi.Template;
import fi.vm.sade.varda.rekisterointi.service.TemplateService;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;

import static fi.vm.sade.varda.rekisterointi.service.EmailService.LOCALES;
import static java.util.Collections.emptyMap;

@RestController
@RequestMapping("/virkailija/template")
public class TemplateController {

    private final TemplateService templateService;
    private final MessageSource messageSource;

    public TemplateController(TemplateService templateService, MessageSource messageSource) {
        this.templateService = templateService;
        this.messageSource = messageSource;
    }

    /**
     * Lataa viestipohjan.
     *
     * @param template  viestipohja
     * @param language  kieli
     *
     * @return viestipohja.
     */
    @GetMapping(value = "/{template}", produces = "text/html")
    public String getTemplate(@PathVariable Template template,
                              @RequestParam(required = false, defaultValue = "fi") String language,
                              @RequestParam(required = false, defaultValue = "varda") String tyyppi) {
        return templateService.getContent(tyyppi, template, new Locale(language), getDefaultVariables(template));
    }

    private Map<String, Object> getDefaultVariables(Template template) {
        switch (template) {
            case REKISTEROITYMINEN_KAYTTAJA:
                return Map.of("messageSource", messageSource, "locales", LOCALES, "organisaatioNimi", "Päiväkoti oy");
            case REKISTEROITYMINEN_PAAKAYTTAJA:
                return Map.of("messageSource", messageSource, "locales", LOCALES, "etunimi", "Ella");
            case REKISTEROITYMINEN_KUNTA:
                return Map.of("messageSource", messageSource, "locales", LOCALES, "organisaatioLkm", 19);
            case REKISTEROITYMINEN_HYLATTY:
                return Map.of("messageSource", messageSource, "locales", LOCALES, "organisaatioNimi", "Päiväkoti oy", "perustelu", "Väärä kunta");
            case REKISTEROITYMINEN_HYVAKSYTTY:
                return Map.of("messageSource", messageSource, "locales", LOCALES, "organisaatioNimi", "Päiväkoti oy");
            default:
                return emptyMap();
        }
    }

}

package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.varda.rekisterointi.Template;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;
import java.util.Map;

import static java.util.Collections.emptyMap;

@Service
public class TemplateService {

    private final TemplateEngine templateEngine;

    public TemplateService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String getPath(String tyyppi) {
        return tyyppi.equals("varda") ? "varda" : "generic";
    }

    public String getContent(String tyyppi, Template template, Locale locale) {
        return getContent(tyyppi, template, locale, emptyMap());
    }

    public String getContent(String tyyppi, Template template, Locale locale, Map<String, Object> variables) {
        return templateEngine.process(getPath(tyyppi) + "/" + template.getPath(), new Context(locale, variables));
    }

}

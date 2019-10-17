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

    public String getContent(Template template, Locale locale) {
        return getContent(template, locale, emptyMap());
    }

    public String getContent(Template template, Locale locale, Map<String, Object> variables) {
        return templateEngine.process(template.getPath(), new Context(locale, variables));
    }

}

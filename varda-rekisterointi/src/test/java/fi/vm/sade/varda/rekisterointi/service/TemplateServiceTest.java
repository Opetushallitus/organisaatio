package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.varda.rekisterointi.Template;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.util.*;

import static java.util.stream.Collectors.toList;

@RunWith(Parameterized.class)
@SpringBootTest
@ActiveProfiles("test")
public class TemplateServiceTest {

    private static final Collection<String> LANGUAGES = List.of("fi", "sv");
    private static final Collection<Locale> LOCALES = LANGUAGES.stream().map(Locale::new).collect(toList());

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private TemplateService templateService;
    @Autowired
    private MessageSource messageSource;

    @Parameterized.Parameters(name = "{0}:{1}")
    public static Collection<Object[]> parameters() {
        return Arrays.stream(Template.values())
                .flatMap(template -> LANGUAGES.stream().map(language -> new Object[]{ template, language }))
                .collect(toList());
    }

    private final Template template;
    private final String language;

    public TemplateServiceTest(Template template, String language) {
        this.template = template;
        this.language = language;
    }

    @Test
    public void renderWorks() {
        Map<String, Object> variables = Map.of("messageSource", messageSource, "locales", LOCALES);
        String tyyppi = template.getPath().equals("taskien-virheraportti.html") ? "generic" : "varda";
        String content = templateService.getContent(tyyppi, template, new Locale(language), variables);
        System.out.println(content);
    }

}

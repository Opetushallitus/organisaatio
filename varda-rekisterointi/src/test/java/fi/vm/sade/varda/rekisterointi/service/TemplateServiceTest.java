package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.varda.rekisterointi.Template;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static java.util.stream.Collectors.toList;

@RunWith(Parameterized.class)
@SpringBootTest
@ActiveProfiles("test")
public class TemplateServiceTest {

    private static final Collection<String> LANGUAGES = List.of("fi", "sv");

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private TemplateService templateService;

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
        String content = templateService.getContent(template, new Locale(language));
        System.out.println(content);
    }

}

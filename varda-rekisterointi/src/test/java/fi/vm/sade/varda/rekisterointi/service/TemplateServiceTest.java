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

import java.util.Locale;

@RunWith(Parameterized.class)
@SpringBootTest
@ActiveProfiles("test")
public class TemplateServiceTest {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private TemplateService templateService;

    @Parameterized.Parameters(name = "{0}")
    public static Template[] parameters() {
        return Template.values();
    }

    private final Template template;

    public TemplateServiceTest(Template template) {
        this.template = template;
    }

    @Test
    public void renderWorks() {
        String content = templateService.getContent(template, new Locale("fi"));
        System.out.println(content);
    }

}

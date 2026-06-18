package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.varda.rekisterointi.Template;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@SpringBootTest
@ActiveProfiles("test")
public class TemplateServiceTest {

    private static final Collection<String> LANGUAGES = List.of("fi", "sv");
    private static final Collection<Locale> LOCALES = LANGUAGES.stream().map(Locale::of).collect(toList());

    @Autowired
    private TemplateService templateService;
    @Autowired
    private MessageSource messageSource;

    public static Stream<Arguments> parameters() {
        return Arrays.stream(Template.values())
                .flatMap(template -> LANGUAGES.stream().map(language -> Arguments.of(template, language)));
    }

    @ParameterizedTest(name = "{0}:{1}")
    @MethodSource("parameters")
    public void renderWorks(Template template, String language) {
        Map<String, Object> variables = Map.of("messageSource", messageSource, "locales", LOCALES);
        String tyyppi = Set.of("taskien-virheraportti.html", "kasittelemattomat.html").contains(template.getPath()) ? "generic" : "varda";
        String content = templateService.getContent(tyyppi, template, Locale.of(language), variables);
        System.out.println(content);
    }

}

package fi.vm.sade.varda.rekisterointi.controller;

import fi.vm.sade.varda.rekisterointi.client.LokalisointiClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/lokalisointi")
public class LokalisointiController {

    private final LokalisointiClient lokalisointiClient;

    public LokalisointiController(LokalisointiClient lokalisointiClient) {
        this.lokalisointiClient = lokalisointiClient;
    }

    @GetMapping
    public Map<String, Map<String, String>> getLokalisointi() {
        return lokalisointiClient.getByCategory("varda-rekisterointi");
    }

    @GetMapping("/kieli")
    public String getLocale(Locale locale) {
        return locale.getLanguage();
    }

    @PutMapping("/kieli")
    public void setLocale() {
        // nop (kts. LocaleConfiguration#localeChangeInterceptor)
    }

}

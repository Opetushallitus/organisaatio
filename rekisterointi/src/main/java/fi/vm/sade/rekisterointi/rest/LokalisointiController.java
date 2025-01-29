package fi.vm.sade.rekisterointi.rest;

import fi.vm.sade.rekisterointi.client.LokalisointiClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/lokalisointi")
public class LokalisointiController {

    private final LokalisointiClient lokalisointiClient;

    public LokalisointiController(LokalisointiClient lokalisointiClient) {
        this.lokalisointiClient = lokalisointiClient;
    }

    /**
     * Hakee &quot;varda-rekisterointi&quot; -kategorian lokalisoinnin.
     *
     * @return lokalisointitiedot (kieli -&gt; avain -&gt; arvo).
     */
    @GetMapping
    public Map<String, Map<String, String>> getLokalisointi(HttpServletRequest request) {
        var category = request.getParameter("category");
        return lokalisointiClient.getByCategory(category);
    }

    /**
     * Hakee käyttäjän kielen.
     *
     * @param locale käyttäjän lokaali
     * @return käyttäjän kieli.
     */
    @GetMapping("/kieli")
    public String getLocale(Locale locale) {
        return locale.getLanguage();
    }

    /**
     * Asettaa käyttäjän kielen.
     */
    @PutMapping("/kieli")
    public void setLocale() {
        // nop (kts. LocaleConfiguration#localeChangeInterceptor)
    }

}

package fi.vm.sade.varda.rekisterointi.controller;

import fi.vm.sade.varda.rekisterointi.client.LokalisointiClient;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
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

    /**
     * Hakee &quot;varda-rekisterointi&quot; -kategorian lokalisoinnin.
     *
     * @return lokalisointitiedot (kieli -&gt; avain -&gt; arvo).
     */
    @GetMapping
    @ApiOperation("Hae lokalisointitiedot")
    @ApiResponse(
            code = 200,
            message = "Lokalisointitiedot: kieli -> avain -> arvo"
    )
    public Map<String, Map<String, String>> getLokalisointi() {
        return lokalisointiClient.getByCategory("varda-rekisterointi");
    }

    /**
     * Hakee käyttäjän kielen.
     *
     * @param locale    käyttäjän lokaali
     * @return käyttäjän kieli.
     */
    @GetMapping("/kieli")
    @ApiOperation("Hae käyttäjän kieli")
    @ApiResponse(
            code = 200,
            message = "Käyttäjän kieli"
    )
    public String getLocale(Locale locale) {
        return locale.getLanguage();
    }

    /**
     * Asettaa käyttäjän kielen.
     */
    @PutMapping("/kieli")
    @ApiOperation("Aseta käyttäjän lokaali")
    @ApiImplicitParam(
            name = "locale",
            value = "Asetettava lokaali",
            paramType = "query"
    )
    public void setLocale() {
        // nop (kts. LocaleConfiguration#localeChangeInterceptor)
    }

}

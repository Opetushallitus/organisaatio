package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.client.LokalisointiClient;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;

@Hidden
@RestController
@RequestMapping("${server.internal.context-path}/lokalisointi")
public class LokalisointiResource {

    private final LokalisointiClient lokalisointiClient;

    public LokalisointiResource(LokalisointiClient lokalisointiClient) {
        this.lokalisointiClient = lokalisointiClient;
    }

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001')")
    public Map<String, Map<String, String>> getLokalisointi() {
        return lokalisointiClient.getByCategory("organisaatio2");
    }

    @GetMapping(path = "/kieli", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001')")
    public String getLocale(Locale locale) {
        return locale.getLanguage();
    }

    @PutMapping("/kieli")
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001')")
    public void setLocale() {
        // nop (kts. LocaleConfiguration#localeChangeInterceptor)
    }

}

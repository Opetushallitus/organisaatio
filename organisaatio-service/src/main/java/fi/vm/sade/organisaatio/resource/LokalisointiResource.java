package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.client.LokalisointiClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping(value = "/lokalisointi")
@Api(value = "/lokalisointi")
public class LokalisointiResource {

    private final LokalisointiClient lokalisointiClient;

    public LokalisointiResource(LokalisointiClient lokalisointiClient) {
        this.lokalisointiClient = lokalisointiClient;
    }

    @GetMapping(path= "", produces = "application/json;charset=UTF-8")
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001')")
    @ApiOperation("Tuo Lokalisoinnit")
    public Map<String, Map<String, String>> getLokalisointi() {
        return lokalisointiClient.getByCategory("organisaatio");
    }

    @GetMapping(path= "/kieli", produces = "application/json;charset=UTF-8")
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001')")
    @ApiOperation("Palauttaa kielen")
    public String getLocale(Locale locale) {
        return locale.getLanguage();
    }

    @PutMapping("/kieli")
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001')")
    @ApiOperation("Asettaa kielen")
    public void setLocale() {
        // nop (kts. LocaleConfiguration#localeChangeInterceptor)
    }

}

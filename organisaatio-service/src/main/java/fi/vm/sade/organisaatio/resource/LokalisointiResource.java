package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.client.LokalisointiClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import java.util.Locale;
import java.util.Map;

@Path("/api//lokalisointi")
@Api(value = "/api/lokalisointi", description = "Lokalisointiin liittyvät operaatiot")
@Controller("LokalisointiResounce")
public class LokalisointiResource {

    private final LokalisointiClient lokalisointiClient;

    public LokalisointiResource(LokalisointiClient lokalisointiClient) {
        this.lokalisointiClient = lokalisointiClient;
    }

    @GET
    @Path("/")
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001')")
    @ApiOperation("Tuo Lokalisoinnit")
    public Map<String, Map<String, String>> getLokalisointi() {
        return lokalisointiClient.getByCategory("organisaatio");
    }

    @GET
    @Path("/kieli")
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001')")
    @ApiOperation("Palauttaa kielen")
    public String getLocale(Locale locale) {
        return locale.getLanguage();
    }

    @PUT
    @Path("/kieli")
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001')")
    @ApiOperation("Asettaa kielen")
    public void setLocale() {
        // nop (kts. LocaleConfiguration#localeChangeInterceptor)
    }

}

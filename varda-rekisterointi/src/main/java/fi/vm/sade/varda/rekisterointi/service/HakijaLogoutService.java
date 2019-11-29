package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.properties.OphProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Optional;

@Service
public class HakijaLogoutService {

    private final OphProperties properties;
    private final Environment environment;

    public HakijaLogoutService(OphProperties properties, Environment environment) {
        this.properties = properties;
        this.environment = environment;
    }

    public String logout(HttpServletRequest request) {
        String callbackUrl = properties.url("varda-rekisterointi.index");
        return logout(request, callbackUrl);
    }

    public String logout(HttpServletRequest request, String callbackUrl) {
        Optional.ofNullable(request.getSession(false)).ifPresent(HttpSession::invalidate);
        if (Boolean.FALSE.equals(environment.getProperty("varda-rekisterointi.shibboleth.logout.enabled", Boolean.class))) {
            return callbackUrl;
        }
        return properties.url("shibbolethVirkailija.logout", callbackUrl);
    }

}

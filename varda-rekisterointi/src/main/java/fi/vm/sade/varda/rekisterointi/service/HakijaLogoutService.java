package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.properties.OphProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Service
public class HakijaLogoutService {

    private final OphProperties properties;
    private final Environment environment;

    public HakijaLogoutService(OphProperties properties, Environment environment) {
        this.properties = properties;
        this.environment = environment;
    }

    /**
     * Kirjaa käyttäjän ulos.
     *
     * @param request HTTP-pyyntö
     * @return uudelleenohjausosoite, johon ohjataan uloskirjautumisen jälkeen.
     */
    public String logout(HttpServletRequest request) {
        String callbackUrl = properties.url("varda-rekisterointi.index");
        return logout(request, callbackUrl);
    }

    /**
     * Kirjaa käyttäjän ulos.
     * @param request       HTTP-pyyntö
     * @param callbackUrl   käytettävä uudelleenohjausosoite
     * @return uudelleenohjausosoite, johon ohjataan uloskirjautumisen jälkeen.
     */
    public String logout(HttpServletRequest request, String callbackUrl) {
        Optional.ofNullable(request.getSession(false)).ifPresent(HttpSession::invalidate);
        return properties.url("varda-rekisterointi.cas.oppija.logout", callbackUrl);
    }

}

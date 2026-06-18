package fi.vm.sade.varda.rekisterointi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
public class HakijaLogoutService {
    private final String virkailijaBaseUrl;
    private final String oppijaBaseUrl;

    public HakijaLogoutService(@Value("${varda-rekisterointi.url-virkailija}") String virkailijaBaseUrl,
                               @Value("${varda-rekisterointi.url-oppija}") String oppijaBaseUrl) {
        this.virkailijaBaseUrl = virkailijaBaseUrl;
        this.oppijaBaseUrl = oppijaBaseUrl;
    }

    /**
     * Kirjaa käyttäjän ulos.
     *
     * @param request HTTP-pyyntö
     * @return uudelleenohjausosoite, johon ohjataan uloskirjautumisen jälkeen.
     */
    public String logout(HttpServletRequest request) {
        String callbackUrl = virkailijaBaseUrl + "/varda-rekisterointi";
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
        return oppijaBaseUrl + "/cas-oppija/logout?service=" + URLEncoder.encode(callbackUrl, StandardCharsets.UTF_8);
    }

}

package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.properties.OphProperties;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HakijaLogoutService {
    private final OphProperties properties;

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

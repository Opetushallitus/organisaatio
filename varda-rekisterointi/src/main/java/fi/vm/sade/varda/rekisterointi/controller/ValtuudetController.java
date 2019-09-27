package fi.vm.sade.varda.rekisterointi.controller;

import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.suomifi.valtuudet.OrganisationDto;
import fi.vm.sade.suomifi.valtuudet.SessionDto;
import fi.vm.sade.suomifi.valtuudet.ValtuudetClient;
import fi.vm.sade.suomifi.valtuudet.ValtuudetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Locale;
import java.util.Optional;

import static fi.vm.sade.varda.rekisterointi.util.Constants.*;
import static fi.vm.sade.varda.rekisterointi.util.ServletUtils.findSessionAttribute;
import static fi.vm.sade.varda.rekisterointi.util.ServletUtils.setSessionAttribute;

@Controller
@RequestMapping("/hakija")
public class ValtuudetController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValtuudetController.class);

    private final OphProperties properties;
    private final ValtuudetClient valtuudetClient;

    public ValtuudetController(OphProperties properties, ValtuudetClient valtuudetClient) {
        this.properties = properties;
        this.valtuudetClient = valtuudetClient;
    }

    @GetMapping("/valtuudet/redirect")
    public View getRedirect(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        Locale locale = request.getLocale();
        String nationalIdentificationNumber = principal.getName();
        String callbackUrl = properties.url("varda-rekisterointi.hakija.valtuudet.callback");
        SessionDto session = valtuudetClient.createSession(ValtuudetType.ORGANISATION, nationalIdentificationNumber);
        String redirectUrl = valtuudetClient.getRedirectUrl(session.userId, callbackUrl, locale.getLanguage());

        setSessionAttribute(request, SESSION_ATTRIBUTE_NAME_SESSION_ID, session.sessionId);
        setSessionAttribute(request, SESSION_ATTRIBUTE_NAME_CALLBACK_URL, callbackUrl);

        return new RedirectView(redirectUrl);
    }

    @GetMapping("/valtuudet/callback")
    public View getCallback(HttpServletRequest request) {
        try {
            return handleCallback(request);
        } finally {
            try {
                findSessionAttribute(request, SESSION_ATTRIBUTE_NAME_SESSION_ID, String.class)
                        .ifPresent(sessionId -> valtuudetClient.destroySession(ValtuudetType.ORGANISATION, sessionId));
            } catch (Exception e) {
                LOGGER.warn("Unable to destroy valtuudet session", e);
            }
        }
    }

    private View handleCallback(HttpServletRequest request) {
        String code = request.getParameter("code");
        if (code == null) {
            String redirectUrl = properties.url("varda-rekisterointi.hakija.logout");
            return new RedirectView(redirectUrl);
        }

        Optional<String> callbackUrl = findSessionAttribute(request, SESSION_ATTRIBUTE_NAME_CALLBACK_URL, String.class);
        Optional<String> sessionId = findSessionAttribute(request, SESSION_ATTRIBUTE_NAME_SESSION_ID, String.class);
        if (!callbackUrl.isPresent() || !sessionId.isPresent()) {
            String redirectUrl = properties.url("varda-rekisterointi.hakija.logout");
            return new RedirectView(redirectUrl);
        }

        String accessToken = valtuudetClient.getAccessToken(code, callbackUrl.get());
        OrganisationDto organisation = valtuudetClient.getSelectedOrganisation(sessionId.get(), accessToken);

        setSessionAttribute(request, SESSION_ATTRIBUTE_NAME_BUSINESS_ID, organisation.identifier);
        setSessionAttribute(request, SESSION_ATTRIBUTE_NAME_ORGANISATION_NAME, organisation.name);

        String redirectUrl = properties.url("varda-rekisterointi.hakija");
        return new RedirectView(redirectUrl);
    }

}

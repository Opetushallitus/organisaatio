package fi.vm.sade.varda.rekisterointi.controller;

import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.suomifi.valtuudet.OrganisationDto;
import fi.vm.sade.suomifi.valtuudet.SessionDto;
import fi.vm.sade.suomifi.valtuudet.ValtuudetClient;
import fi.vm.sade.suomifi.valtuudet.ValtuudetType;
import fi.vm.sade.varda.rekisterointi.model.Valtuudet;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import java.security.Principal;

@Controller
@RequestMapping("/valtuudet")
@Scope("session")
public class ValtuudetController {

    private final OphProperties properties;
    private final Valtuudet valtuudet;
    private final ValtuudetClient valtuudetClient;

    public ValtuudetController(OphProperties properties, Valtuudet valtuudet, ValtuudetClient valtuudetClient) {
        this.properties = properties;
        this.valtuudet = valtuudet;
        this.valtuudetClient = valtuudetClient;
    }

    @GetMapping("/redirect")
    public View start(Principal principal) {
        String nationalIdentificationNumber = principal.getName();
        String callbackUrl = properties.url("varda-rekisterointi.valtuudet.callback");
        SessionDto session = valtuudetClient.createSession(ValtuudetType.ORGANISATION, nationalIdentificationNumber);
        String redirectUrl = valtuudetClient.getRedirectUrl(session.userId, callbackUrl, "fi");

        valtuudet.sessionId = session.sessionId;
        valtuudet.callbackUrl = callbackUrl;

        return new RedirectView(redirectUrl);
    }

    @GetMapping("/callback")
    public View end(@RequestParam String code, Principal principal) {
        String accessToken = valtuudetClient.getAccessToken(code, valtuudet.callbackUrl);
        OrganisationDto organisation = valtuudetClient.getSelectedOrganisation(valtuudet.sessionId, accessToken);
        valtuudet.businessId = organisation.identifier;

        String redirectUrl = properties.url("varda-rekisterointi.index");
        return new RedirectView(redirectUrl);
    }

}

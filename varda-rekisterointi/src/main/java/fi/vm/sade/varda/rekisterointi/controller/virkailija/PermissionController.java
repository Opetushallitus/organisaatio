package fi.vm.sade.varda.rekisterointi.controller.virkailija;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import fi.vm.sade.varda.rekisterointi.model.Permissions;
import fi.vm.sade.varda.rekisterointi.util.Constants;

import java.io.Serializable;

import static fi.vm.sade.varda.rekisterointi.util.AuthenticationUtils.isAuthority;
import static fi.vm.sade.varda.rekisterointi.util.AuthenticationUtils.getRegistrationTypes;

@RequestMapping("/virkailija/api/permission")
@RestController
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionEvaluator permissionEvaluator;

    /**
     * Tarkistaa, löytyykö käyttäjältä haluttu valtuus.
     *
     * @param authentication    autentikointitiedot
     * @param targetType        valtuuden kohteen tyyppi
     * @param permission        tarkistettava valtuus
     * @param targetId          valtuuden kohde
     *
     * @return onko käyttäjällä valtuus?
     */
    @GetMapping("{targetType}/{permission}")
    public boolean hasPermission(Authentication authentication,
                                 @PathVariable String targetType,
                                 @PathVariable String permission,
                                 @RequestParam(required = false) Serializable targetId) {
        return permissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
    }

    /**
     * Palauttaa käyttäjän valtuudet.
     *
     * @param authentication autentikointitiedot
     *
     * @return valtuudet?
     */
    @GetMapping("rekisterointi")
    public Permissions hasPermission(Authentication authentication) {
        return Permissions.of(
            isAuthority(authentication, Constants.PAAKAYTTAJA_AUTHORITY),
            getRegistrationTypes(authentication)
        );
    }

}

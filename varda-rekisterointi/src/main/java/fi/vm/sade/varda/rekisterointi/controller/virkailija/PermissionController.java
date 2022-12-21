package fi.vm.sade.varda.rekisterointi.controller.virkailija;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
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
    @ApiOperation("Tarkista valtuus")
    @ApiResponse(
            code = 200,
            message = "Onko valtuutta (true/false)",
            response = Boolean.class
    )
    public boolean hasPermission(Authentication authentication,
                                 @ApiParam("valtuuden kohteen tyyppi") @PathVariable String targetType,
                                 @ApiParam("tarkistettava valtuus") @PathVariable String permission,
                                 @ApiParam("valtuuden kohteen tunniste") @RequestParam(required = false) Serializable targetId) {
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
    @ApiOperation("Tarkista valtuudet")
    @ApiResponse(
            code = 200,
            message = "Valtuudet",
            response = Permissions.class
    )
    public Permissions hasPermission(Authentication authentication) {
        return Permissions.of(
            isAuthority(authentication, Constants.PAAKAYTTAJA_AUTHORITY),
            getRegistrationTypes(authentication)
        );
    }

}

package fi.vm.sade.varda.rekisterointi.controller.virkailija;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;

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

}

package fi.vm.sade.varda.rekisterointi.controller.virkailija;

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

    @GetMapping("{targetType}/{permission}")
    public boolean hasPermission(Authentication authentication,
                                 @PathVariable String targetType,
                                 @PathVariable String permission,
                                 @RequestParam(required = false) Serializable targetId) {
        return permissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
    }

}

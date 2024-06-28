package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.varda.rekisterointi.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.Serializable;

import static fi.vm.sade.varda.rekisterointi.util.AuthenticationUtils.isAuthority;

@Service
public class PermissionService implements PermissionEvaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionService.class);

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        LOGGER.info("PermissionService#hasPermission({}, {}, {})", authentication.getName(), targetDomainObject, permission);
        LOGGER.warn("Target '{}' is not supported with permission '{}'", targetDomainObject, permission);
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        LOGGER.info("PermissionService#hasPermission({}, {}, {}, {})", authentication.getName(), targetId, targetType, permission);
        if (permission instanceof String) {
            return hasPermission(authentication, targetId, targetType, (String) permission);
        }
        return false;
    }

    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, String permission) {
        switch (targetType) {
            case "rekisterointi":
                switch (permission) {
                    case "create":
                        return isAuthority(authentication, Constants.PAAKAYTTAJA_AUTHORITY);
                }
            default:
                LOGGER.warn("Target '{}={}' is not supported with permission '{}'", targetType, targetId, permission);
                return false;
        }
    }

}

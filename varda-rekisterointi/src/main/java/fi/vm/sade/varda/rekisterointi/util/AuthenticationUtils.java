package fi.vm.sade.varda.rekisterointi.util;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public final class AuthenticationUtils {

    private AuthenticationUtils() {
    }

    public static boolean isAuthority(Authentication authentication, String authority) {
        return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(authority::equals);
    }

    public static String[] getRegistrationTypes(Authentication authentication) {
        return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray(String[]::new);
    }

}

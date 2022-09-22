package fi.vm.sade.varda.rekisterointi.util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public final class AuthenticationUtils {

    private AuthenticationUtils() {
    }

    public static boolean isAuthority(Authentication authentication, String authority) {
        return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(authority::equals);
    }

    public static boolean isOrganisaatioidenRekisteroityminen(String authority) {
        return authority.startsWith("");
    }

    public static Optional<String> mapToRole(String authority) {
        String regex = ".*ORGANISAATIOIDEN_REKISTEROITYMINEN_([^_]+).*";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(authority);
        if (m.find()) {
            return Optional.of(m.group(1));
        }
        return Optional.empty();
    }

    public static String[] mapRoleToRegistrationTypes(String role) {
        switch (role) {
            case "VARDA":
                return new String[]{"varda"};
            case "JOTPA":
                return new String[]{"jotpa"};
            case "OPH":
                return new String[]{"varda", "jotpa"};
            default:
                return new String[]{};
        }
    }

    public static String[] getRegistrationTypes(Authentication authentication) {
        Optional<String> role = getRole(authentication);
        String[] registrationTypes = role
            .map(AuthenticationUtils::mapRoleToRegistrationTypes)
            .orElse(new String[]{});
        return registrationTypes;
    }

    public static Optional<String> getRole(Authentication authentication) {
        return getAuthority(authentication)
            .flatMap(AuthenticationUtils::mapToRole);
    }

    private static Optional<String> getAuthority(Authentication authentication) {
        return authentication
            .getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .filter(a -> a.contains("ORGANISAATIOIDEN_REKISTEROITYMINEN"))
            .findFirst();
    }

}

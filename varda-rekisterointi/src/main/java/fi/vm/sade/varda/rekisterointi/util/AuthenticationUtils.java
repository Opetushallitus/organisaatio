package fi.vm.sade.varda.rekisterointi.util;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static String mapToRole(String authority) {
        String regex = ".*ORGANISAATIOIDEN_REKISTEROITYMINEN_([^_]+).*";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(authority);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private static List<String> mapRoleToRegistrationTypes(String role) {
        switch (role) {
            case "VARDA":
                return List.of("varda");
            case "JOTPA":
                return List.of("jotpa");
            case "OPH":
                return List.of("varda", "jotpa");
            default:
                return List.of();
        }
    }

    public static Set<String> mapRolesToRegistrationTypes(List<String> roles) {
        return roles
            .stream()
            .map(AuthenticationUtils::mapRoleToRegistrationTypes)
            .flatMap(List::stream)
            .collect(Collectors.toSet());
    }

    public static String[] getRegistrationTypes(Authentication authentication) {
        List<String> roles = getRoles(authentication);
        Set<String> registrationTypes = mapRolesToRegistrationTypes(roles);
        return (String[]) registrationTypes.toArray();
    }

    public static List<String> getRoles(Authentication authentication) {
        return getAuthorities(authentication)
            .map(AuthenticationUtils::mapToRole)
            .filter(role -> role != null)
            .collect(Collectors.toList());
    }

    private static Stream<String> getAuthorities(Authentication authentication) {
        return authentication
            .getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .filter(a -> a.contains("ORGANISAATIOIDEN_REKISTEROITYMINEN_"));
    }

}

package fi.vm.sade.organisaatio.config;

import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@Profile("dev")
public class DevAuthenticationProvider extends DaoAuthenticationProvider {
    private static final SimpleGrantedAuthority[] AUTHORITIES = new SimpleGrantedAuthority[] {
            new SimpleGrantedAuthority("ROLE_APP_ORGANISAATIOHALLINTA"),
            new SimpleGrantedAuthority("ROLE_APP_ORGANISAATIOHALLINTA_CRUD"),
            new SimpleGrantedAuthority("ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.24.00000000001")
    };

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        System.out.println("This is a DEV authentication provider.");

        UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(
                "devaajienkuningas", authentication.getCredentials(),
                List.of(AUTHORITIES));
        result.setDetails(authentication.getDetails());

        return result;
    }

}
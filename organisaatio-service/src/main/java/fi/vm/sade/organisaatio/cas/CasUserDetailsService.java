package fi.vm.sade.organisaatio.cas;

import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

public class CasUserDetailsService implements AuthenticationUserDetailsService<CasAssertionAuthenticationToken> {
    @Override
    public UserDetails loadUserDetails(CasAssertionAuthenticationToken token) throws UsernameNotFoundException {
        var attributes = token.getAssertion().getPrincipal().getAttributes();
        var username = (String) attributes.get("oidHenkilo");
        var idpEntityId = (String) attributes.get("idpEntityId");
        var kayttajaTyyppi = (String) attributes.get("kayttajaTyyppi");
        var roles = (List<String>) attributes.getOrDefault("roles", List.of());
        var authorities = roles.stream().map(SimpleGrantedAuthority::new).toList();
        return new CasUserDetails(username, idpEntityId, kayttajaTyyppi, authorities);
    }
}

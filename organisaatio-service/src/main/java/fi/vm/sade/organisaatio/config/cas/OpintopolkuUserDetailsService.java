package fi.vm.sade.organisaatio.config.cas;

import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OpintopolkuUserDetailsService implements AuthenticationUserDetailsService<CasAssertionAuthenticationToken> {
    @Override
    public UserDetails loadUserDetails(CasAssertionAuthenticationToken token) throws UsernameNotFoundException {
        Map<String, Object> attributes =  token.getAssertion().getPrincipal().getAttributes();
        Object rolesAttribute = attributes.get("roles");
        List<String> roles = rolesAttribute instanceof List<?> roleList
                ? roleList.stream().map(String.class::cast).toList()
                : List.of();
        return new UserDetailsImpl((String) attributes.get("oidHenkilo"), roles);
    }

    public static final class UserDetailsImpl implements UserDetails {
        private static final long serialVersionUID = 845522107275827768L;

        private final String oidHenkilo;
        private final Collection<SimpleGrantedAuthority> authorities;

        public UserDetailsImpl(String oidHenkilo, List<String> authorities) {
            this.oidHenkilo = oidHenkilo;
            this.authorities = authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        }

        @Override
        public Collection<SimpleGrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getPassword() {
            return null;
        }

        @Override
        public String getUsername() {
            return oidHenkilo;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}

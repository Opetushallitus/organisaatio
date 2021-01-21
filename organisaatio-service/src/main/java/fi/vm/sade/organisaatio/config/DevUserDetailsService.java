/*package fi.vm.sade.organisaatio.config;

import org.springframework.context.annotation.Profile;
import org.springframework.context.support.BeanDefinitionDsl;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Profile("dev")
@Service
public class DevUserDetailsService implements UserDetailsService {
    private static final SimpleGrantedAuthority[] AUTHORITIES = new SimpleGrantedAuthority[] {
        new SimpleGrantedAuthority("ROLE_APP_ORGANISAATIOHALLINTA"),
        new SimpleGrantedAuthority("ROLE_APP_ORGANISAATIOHALLINTA_CRUD"),
        new SimpleGrantedAuthority("ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.24.00000000001")
    };

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            return User.builder()
                    .authorities(List.of(AUTHORITIES))
                    .password("Suits you, Sir!")
                    .username("devaajienkuningas")
                    .build();
        }

    private Collection<? extends GrantedAuthority> getAuthorities(
            Collection<BeanDefinitionDsl.Role> roles) {
        List<GrantedAuthority> authorities
                = new ArrayList<>();
        for (BeanDefinitionDsl.Role role: roles) {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
            role.getPrivileges().stream()
                    .map(p -> new SimpleGrantedAuthority(p.getName()))
                    .forEach(authorities::add);
        }

        return authorities;
    }
}
*/
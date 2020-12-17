package fi.vm.sade.organisaatio.config;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Profile("dev")
@Service
public class DevUserDetailsService implements UserDetailsService {
    private static final SimpleGrantedAuthority[] AUTHORITIES = new SimpleGrantedAuthority[] {
        new SimpleGrantedAuthority("ROLE_APP_ORGANISAATIOHALLINTA"),
        new SimpleGrantedAuthority("ROLE_APP_ORGANISAATIOHALLINTA"),
        new SimpleGrantedAuthority("ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.24.00000000001")
    };

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            return User.builder()
                    .authorities(List.of(AUTHORITIES))
                    .password("Suits you, Sir!")
                    .username(username)
                    .build();
        }
}

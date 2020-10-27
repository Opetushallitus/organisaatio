package fi.vm.sade.organisaatio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

@Configuration
@Profile("dev")
public class DevUserDetailsServiceConfiguration {

    private static final SimpleGrantedAuthority[] AUTHORITIES = new SimpleGrantedAuthority[] {
        new SimpleGrantedAuthority("ROLE_APP_ORGANISAATIOHALLINTA"),
        new SimpleGrantedAuthority("APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.24.00000000001")
    };

    @Bean
    public UserDetailsService userDetailsService() {
        return new DevUserDetailsService();
    }

    static class DevUserDetailsService implements UserDetailsService {

        private DevUserDetailsService() {}

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            return User.builder()
                    .authorities(List.of(AUTHORITIES))
                    .password("Suits you, Sir!")
                    .username(username)
                    .build();
        }
    }
}

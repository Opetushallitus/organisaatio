package fi.vm.sade.varda.rekisterointi.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

import static fi.vm.sade.varda.rekisterointi.util.Constants.VIRKAILIJA_ROLE;

@Configuration
@Profile("dev")
public class DevUserDetailsServiceConfiguration {

    @Bean
    public UserDetailsService userDetailsService() {
        return new DevUserDetailsService();
    }

    static class DevUserDetailsService implements UserDetailsService {

        private DevUserDetailsService() {}

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            return User.builder()
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + VIRKAILIJA_ROLE)))
                    .password("Suits you, Sir!")
                    .username(username)
                    .build();
        }
    }
}

package fi.vm.sade.organisaatio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.BeanDefinitionDsl;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Profile("dev")
@Configuration
public class DevUserDetailsServiceConfiguration {
    private static final SimpleGrantedAuthority[] AUTHORITIES = new SimpleGrantedAuthority[] {
        new SimpleGrantedAuthority("ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001"),
        new SimpleGrantedAuthority("ROLE_APP_ORGANISAATIOHALLINTA"),
        new SimpleGrantedAuthority("ROLE_APP_ORGANISAATIOHALLINTA_CRUD")
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {

        return new DevUserDetailsService(passwordEncoder());
    }

    static class DevUserDetailsService implements UserDetailsService {
        PasswordEncoder passwordEncoder;
        private DevUserDetailsService(PasswordEncoder passwordEncoder) {
            this.passwordEncoder = passwordEncoder;
        }

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            return User.builder()
                    .authorities(List.of(AUTHORITIES))
                    .password(this.passwordEncoder.encode("devaaja"))
                    .username("devaaja")
                    .build();
        }
    }
}

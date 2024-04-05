package fi.vm.sade.organisaatio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Profile("dev")
@Configuration
public class DevUserDetailsServiceConfiguration {
    private static final SimpleGrantedAuthority[] OPH_AUTHORITIES = new SimpleGrantedAuthority[]{
            new SimpleGrantedAuthority("ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001"),
            new SimpleGrantedAuthority("ROLE_APP_ORGANISAATIOHALLINTA"),
            new SimpleGrantedAuthority("ROLE_APP_ORGANISAATIOHALLINTA_CRUD")
    };
    private static final SimpleGrantedAuthority[] RESTRICTED_AUTHORITIES = new SimpleGrantedAuthority[]{
            new SimpleGrantedAuthority("ROLE_APP_ORGANISAATIOHALLINTA")
    };
    private static final SimpleGrantedAuthority[] ESPOO_AUTHORITIES = new SimpleGrantedAuthority[]{
            new SimpleGrantedAuthority("APP_ORGANISAATIOHALLINTA"),
            new SimpleGrantedAuthority("APP_ORGANISAATIOHALLINTA_CRUD"),
            new SimpleGrantedAuthority("APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.90008375488"),
    };


    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService() {
        return new DevUserDetailsService(passwordEncoder());
    }

    static class DevUserDetailsService implements UserDetailsService {
        PasswordEncoder passwordEncoder;

        private DevUserDetailsService(PasswordEncoder passwordEncoder) {
            this.passwordEncoder = passwordEncoder;
        }

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            switch (username) {
                case "restricted":
                    return User.builder()
                            .authorities(List.of(RESTRICTED_AUTHORITIES))
                            .password(this.passwordEncoder.encode(username))
                            .username(username)
                            .build();
                case "espoo":
                    return User.builder()
                            .authorities(List.of(ESPOO_AUTHORITIES))
                            .password(this.passwordEncoder.encode(username))
                            .username(username)
                            .build();
                default:
                    return User.builder()
                            .authorities(List.of(OPH_AUTHORITIES))
                            .password(this.passwordEncoder.encode("devaaja"))
                            .username("devaaja")
                            .build();
            }
        }
    }
}

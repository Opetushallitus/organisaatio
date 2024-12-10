package fi.vm.sade.varda.rekisterointi.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static fi.vm.sade.varda.rekisterointi.util.Constants.PAAKAYTTAJA_ROLE;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = false, prePostEnabled = true, securedEnabled = true)
public class DevVirkailijaWebSecurityConfiguration {
    @Profile("dev")
    @Bean
    @Order(1)
    SecurityFilterChain devVirkailijaSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .headers(headers -> headers.disable())
                .csrf(csrf -> csrf.disable())
                .securityMatcher("/virkailija/**")
                .authorizeHttpRequests(authz -> authz.anyRequest().authenticated())
                .httpBasic(withDefaults())
                .authenticationManager(authenticationManager())
                .build();
    }

    AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        return new ProviderManager(authenticationProvider);
    }

    UserDetailsService userDetailsService() {
        UserDetails specialUser = User.withUsername("devaaja")
            .password("{noop}devaaja")
            .roles(PAAKAYTTAJA_ROLE)
            .build();

        return new InMemoryUserDetailsManager(specialUser);
    }
}
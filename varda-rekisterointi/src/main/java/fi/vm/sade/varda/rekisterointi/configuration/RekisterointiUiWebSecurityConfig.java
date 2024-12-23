package fi.vm.sade.varda.rekisterointi.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class RekisterointiUiWebSecurityConfig {
    private static final String ENDPOINT = "/api/rekisterointi";
    private static final String ROLE = "REKISTEROINTI_UI";

    @Value("${varda-rekisterointi.rekisterointi-ui.username}")
    private String username;

    @Value("${varda-rekisterointi.rekisterointi-ui.password}")
    private String password;

    @Bean
    @Order(3)
    SecurityFilterChain rekisterointiUiSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .headers(headers -> headers.disable())
                .csrf(csrf -> csrf.disable())
                .securityMatcher(ENDPOINT)
                .authorizeHttpRequests(authz -> authz.anyRequest().authenticated())
                .httpBasic(withDefaults())
                .authenticationManager(authenticationManager())
                .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .build();
    }

    AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        return new ProviderManager(authenticationProvider);
    }

    UserDetailsService userDetailsService() {
        UserDetails specialUser = User.withUsername(username)
            .password("{noop}" + password)
            .roles(ROLE)
            .build();

        return new InMemoryUserDetailsManager(specialUser);
    }
}

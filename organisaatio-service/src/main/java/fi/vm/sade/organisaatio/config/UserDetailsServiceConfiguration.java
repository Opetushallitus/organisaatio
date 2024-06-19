package fi.vm.sade.organisaatio.config;

import fi.vm.sade.organisaatio.java_utils.KayttookeusServiceUserDetailsService;
import fi.vm.sade.properties.OphProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetailsService;

@Profile("!dev")
@Configuration
public class UserDetailsServiceConfiguration {
    @Bean
    public UserDetailsService userDetailsService(OphProperties properties) {
        var baseUrl = properties.url("host.alb") + "/kayttooikeus-service";
        return new KayttookeusServiceUserDetailsService(baseUrl);
    }
}

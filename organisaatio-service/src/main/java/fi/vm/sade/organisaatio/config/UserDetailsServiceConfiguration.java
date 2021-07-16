package fi.vm.sade.organisaatio.config;

import fi.vm.sade.javautils.kayttooikeusclient.OphUserDetailsServiceImpl;
import fi.vm.sade.properties.OphProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

@ConditionalOnProperty(name = "organisaatio.dev-userdetails.enabled", havingValue = "false")
@Configuration
public class UserDetailsServiceConfiguration {

    private final OphProperties ophProperties;

    public UserDetailsServiceConfiguration(OphProperties ophProperties) {
        this.ophProperties = ophProperties;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        String host = ophProperties.url("url.virkailija");
        return new OphUserDetailsServiceImpl(host, "1.2.246.562.10.00000000001.organisaatio-service");
    }

}

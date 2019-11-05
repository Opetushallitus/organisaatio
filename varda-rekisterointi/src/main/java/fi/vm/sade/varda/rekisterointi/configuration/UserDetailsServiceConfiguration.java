package fi.vm.sade.varda.rekisterointi.configuration;

import fi.vm.sade.javautils.kayttooikeusclient.OphUserDetailsServiceImpl;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.util.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@Profile("!dev")
public class UserDetailsServiceConfiguration {

    private final OphProperties ophProperties;

    public UserDetailsServiceConfiguration(OphProperties ophProperties) {
        this.ophProperties = ophProperties;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        String host = ophProperties.url("kayttooikeus-service.host");
        return new OphUserDetailsServiceImpl(host, Constants.CALLER_ID);
    }

}

package fi.vm.sade.varda.rekisterointi.configuration;

import fi.vm.sade.properties.OphProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class PropertiesConfiguration {

    @Bean
    public OphProperties properties(Environment environment) {
        OphProperties properties = new OphProperties("/varda-rekisterointi-oph.properties");
        properties.addDefault("url-virkailija", environment.getRequiredProperty("varda-rekisterointi.url-virkailija"));
        properties.addDefault("url-tunnistus", environment.getRequiredProperty("varda-rekisterointi.url-tunnistus"));
        properties.addDefault("varda-rekisterointi.service.username", environment.getRequiredProperty("varda-rekisterointi.service.username"));
        properties.addDefault("varda-rekisterointi.service.password", environment.getRequiredProperty("varda-rekisterointi.service.password"));
        return properties;
    }

}

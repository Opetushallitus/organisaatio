package fi.vm.sade.varda.rekisterointi.configuration;

import fi.vm.sade.properties.OphProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@Configuration
@ConfigurationPropertiesScan("fi.vm.sade.varda.rekisterointi.properties")
public class PropertiesConfiguration {

    @Bean("properties")
    public OphProperties properties(Environment environment) {
        OphProperties properties = new OphProperties("/varda-rekisterointi-oph.properties");
        properties.addDefault("url-oppija", environment.getRequiredProperty("varda-rekisterointi.url-oppija"));
        properties.addDefault("url-virkailija", environment.getRequiredProperty("varda-rekisterointi.url-virkailija"));
        properties.addDefault("varda-rekisterointi.service.username", environment.getRequiredProperty("varda-rekisterointi.service.username"));
        properties.addDefault("varda-rekisterointi.service.password", environment.getRequiredProperty("varda-rekisterointi.service.password"));
        properties.addDefault("varda-rekisterointi.palvelukayttaja.client-id", environment.getRequiredProperty("varda-rekisterointi.palvelukayttaja.client-id"));
        properties.addDefault("varda-rekisterointi.palvelukayttaja.client-secret", environment.getRequiredProperty("varda-rekisterointi.palvelukayttaja.client-secret"));
        properties.addDefault("otuva.jwt.issuer-uri", environment.getRequiredProperty("otuva.jwt.issuer-uri"));
        Arrays.asList(
                "varda-rekisterointi.kayttooikeus.ryhma.paivakoti",
                "varda-rekisterointi.kayttooikeus.ryhma.perhepaivahoitaja",
                "varda-rekisterointi.kayttooikeus.ryhma.ryhmaperhepaivakoti",
                "varda-rekisterointi.kayttooikeus.ryhma.jotpa"
        ).forEach((prop) -> properties.addDefault(prop, environment.getRequiredProperty(prop)));
        return properties;
    }

}

package fi.vm.sade.rekisterointi.configuration;

import fi.vm.sade.properties.OphProperties;

import java.util.Arrays;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@ConfigurationPropertiesScan("fi.vm.sade.rekisterointi.properties")
public class PropertiesConfiguration {
  @Bean("properties")
  public OphProperties properties(Environment environment) {
    var profiles = environment.getActiveProfiles();
    OphProperties properties;
    if (Arrays.asList(profiles).contains("dev")) {
      properties = new OphProperties("/rekisterointi-oph.properties", "/rekisterointi-oph-dev.properties");
    } else if (Arrays.asList(profiles).contains("ci")) {
      properties = new OphProperties("/rekisterointi-oph.properties", "/rekisterointi-oph-ci.properties");
    } else {
      properties = new OphProperties("/rekisterointi-oph.properties");
    }
    properties.addDefault("url-oppija", environment.getRequiredProperty("rekisterointi.url-oppija"));
    properties.addDefault("url-virkailija", environment.getRequiredProperty("rekisterointi.url-virkailija"));
    properties.addDefault("rekisterointi.service.username", "dummy");
    properties.addDefault("rekisterointi.service.password", "dummy");
    return properties;
  }

}

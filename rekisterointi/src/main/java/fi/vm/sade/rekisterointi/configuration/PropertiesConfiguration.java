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
      properties = new OphProperties("/rekisterointi_oph.properties", "/rekisterointi-oph-dev.properties");
    } else if (Arrays.asList(profiles).contains("test")) {
      properties = new OphProperties("/rekisterointi_oph.properties", "/rekisterointi-oph-dev.properties");
    } else if (Arrays.asList(profiles).contains("ci")) {
      properties = new OphProperties("/rekisterointi_oph.properties", "/rekisterointi-oph-ci.properties");
    } else {
      properties = new OphProperties("/rekisterointi_oph.properties");
      properties.addDefault("url-oppija", environment.getRequiredProperty("url-oppija"));
      properties.addDefault("url-virkailija", environment.getRequiredProperty("url-virkailija"));
      properties.addDefault("url-rekisterointi", environment.getRequiredProperty("url-rekisterointi"));
      properties.addDefault("url-alb", environment.getRequiredProperty("url-alb"));
      properties.addDefault("rekisterointi.service.username",
          environment.getRequiredProperty("rekisterointi.service.username"));
      properties.addDefault("rekisterointi.service.password",
          environment.getRequiredProperty("rekisterointi.service.password"));
      properties.addDefault("varda-rekisterointi.url", environment.getRequiredProperty("varda-rekisterointi.url"));
      properties.addDefault("varda-rekisterointi.username",
          environment.getRequiredProperty("varda-rekisterointi.username"));
      properties.addDefault("varda-rekisterointi.password",
          environment.getRequiredProperty("varda-rekisterointi.password"));
    }
    return properties;
  }

}

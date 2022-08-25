package fi.vm.sade.rekisterointi.configuration;

import fi.vm.sade.properties.OphProperties;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
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
    OphProperties properties = Arrays.asList(profiles).contains("dev")
        ? new OphProperties("/rekisterointi-oph.properties", "/rekisterointi-oph-dev.properties")
        : new OphProperties("/rekisterointi-oph.properties");
    return properties;
  }

}

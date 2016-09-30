package fi.vm.sade.organisaatio.config;

import fi.vm.sade.properties.OphProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;

/**
 * Created by autio on 6.9.2016.
 */
@Configuration
public class UrlConfiguration extends OphProperties {
    public UrlConfiguration() {
        addFiles("/organisaatio-service-oph.properties");
        addOptionalFiles(Paths.get(System.getProperties().getProperty("user.home"), "/oph-configuration/common.properties").toString());
        addOptionalFiles("/organisaatio-service-test.properties");
    }
}

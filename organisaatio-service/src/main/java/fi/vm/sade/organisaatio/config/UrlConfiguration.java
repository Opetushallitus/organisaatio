package fi.vm.sade.organisaatio.config;

import fi.vm.sade.properties.OphProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;

/**
 * Created by autio on 6.9.2016.
 */
@Configuration
public class UrlConfiguration extends OphProperties {
    private static final Logger LOG = LoggerFactory.getLogger(UrlConfiguration.class);

    public UrlConfiguration() {
        addFiles("/organisaatio-service-oph.properties");
        addOptionalFiles(Paths.get(System.getProperties().getProperty("user.home"), "/oph-configuration/common.properties").toString());
        addOptionalFiles("/organisaatio-service-test.properties");

        LOG.info("property: 'organisaatio-service.ryhmasahkoposti-service.rest.url': " + getProperty("organisaatio-service.ryhmasahkoposti-service.rest.url"));
    }
}

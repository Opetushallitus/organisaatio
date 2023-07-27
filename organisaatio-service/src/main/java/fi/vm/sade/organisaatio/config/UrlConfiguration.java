package fi.vm.sade.organisaatio.config;

import fi.vm.sade.properties.OphProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@PropertySource("classpath:application.properties")
@Configuration
public class UrlConfiguration extends OphProperties {

    @Autowired
    public UrlConfiguration(Environment environment) {
        addFiles("/organisaatio-service-oph.properties");
        this.addOverride("host.virkailija", environment.getRequiredProperty("host.virkailija"));
        this.addOverride("url-virkailija", environment.getRequiredProperty("url-virkailija"));
        this.addDefault("host.alb", environment.getRequiredProperty("host.alb"));
        this.addDefault("organisaatio.service.username", environment.getRequiredProperty("organisaatio.service.username"));
        this.addDefault("organisaatio.service.password", environment.getRequiredProperty("organisaatio.service.password"));
        this.addDefault("organisaatio.service.username.to.koodisto", environment.getRequiredProperty("organisaatio.service.username.to.koodisto"));
        this.addDefault("organisaatio.service.password.to.koodisto", environment.getRequiredProperty("organisaatio.service.password.to.koodisto"));
        this.addDefault("organisaatio.service.username.to.viestinta", environment.getRequiredProperty("organisaatio.service.username.to.viestinta"));
        this.addDefault("organisaatio.service.password.to.viestinta", environment.getRequiredProperty("organisaatio.service.password.to.viestinta"));
        this.addDefault("organisaatio.ui.url", environment.getRequiredProperty("organisaatio.ui.url"));
        this.addDefault("organisaatio.ui.ilmoitukset.url", environment.getRequiredProperty("organisaatio.ui.ilmoitukset.url"));
        this.addDefault("port.koodisto-service", environment.getRequiredProperty("port.koodisto-service"));
        this.addDefault("port.tarjonta-service", environment.getRequiredProperty("port.tarjonta-service"));
        this.addDefault("port.organisaatio-service", environment.getRequiredProperty("port.organisaatio-service"));
        this.frontProperties.setProperty("urlVirkailija", this.require("url-virkailija"));
        this.frontProperties.setProperty("feature_OH_973_poistaKriisiviestinnanSahkopostiosoite", "true");
    }
}

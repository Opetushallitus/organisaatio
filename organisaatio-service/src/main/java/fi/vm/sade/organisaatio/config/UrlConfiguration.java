package fi.vm.sade.organisaatio.config;

import fi.vm.sade.properties.OphProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class UrlConfiguration  extends OphProperties {

    @Autowired
    public UrlConfiguration(Environment environment) {
        addFiles("/organisaatio-service-oph.properties");
        this.addOverride("host.virkailija", environment.getRequiredProperty("host.virkailija"));
        this.addOverride("url-virkailija", environment.getRequiredProperty("url-virkailija"));
        this.addOverride("host.alb", environment.getRequiredProperty("host.alb"));
        this.addOverride("organisaatio.service.username", environment.getRequiredProperty("organisaatio.service.username"));
        this.addOverride("organisaatio.service.password", environment.getRequiredProperty("organisaatio.service.password"));
        this.addOverride("organisaatio.service.username.to.koodisto", environment.getRequiredProperty("organisaatio.service.username.to.koodisto"));
        this.addOverride("organisaatio.service.password.to.koodisto", environment.getRequiredProperty("organisaatio.service.password.to.koodisto"));
        this.addOverride("organisaatio.service.username.to.viestinta", environment.getRequiredProperty("organisaatio.service.password.to.viestinta"));
        this.addOverride("organisaatio.service.password.to.viestinta", environment.getRequiredProperty("organisaatio.service.password.to.viestinta"));
        this.addOverride("organisaatio.ui.url", environment.getRequiredProperty("organisaatio.ui.url"));
        this.addOverride("organisaatio.ui.ilmoitukset.url", environment.getRequiredProperty("organisaatio.ui.ilmoitukset.url"));
        this.addOverride("port.koodisto-service", environment.getRequiredProperty("port.koodisto-service"));
        this.addOverride("port.tarjonta-service", environment.getRequiredProperty("port.tarjonta-service"));
        this.addOverride("port.organisaatio-service", environment.getRequiredProperty("port.organisaatio-service"));
        this.frontProperties.setProperty("urlVirkailija", this.require("url-virkailija"));
    }
}

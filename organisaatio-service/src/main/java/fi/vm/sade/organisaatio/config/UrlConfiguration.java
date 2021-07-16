package fi.vm.sade.organisaatio.config;

import fi.vm.sade.properties.OphProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource(value = "classpath:/organisaatio.properties")
@PropertySource(value = "${user.home}/oph-configuration/organisaatio.properties",
        ignoreResourceNotFound = true)
public class UrlConfiguration {

    @Bean
    public OphProperties properties(Environment environment) {
        OphProperties properties = new OphProperties("/organisaatio-service-oph.properties");
        properties.addDefault("cas.base", environment.getRequiredProperty("cas.base"));
        properties.addDefault("cas.login", environment.getRequiredProperty("cas.login"));
        properties.addDefault("host.virkailija", environment.getRequiredProperty("host.virkailija"));
        properties.addDefault("host.alb", environment.getRequiredProperty("host.alb"));
        properties.addDefault("url.virkailija", environment.getRequiredProperty("url.virkailija"));
        properties.addDefault("organisaatio.service.username", environment.getRequiredProperty("organisaatio.service.username"));
        properties.addDefault("organisaatio.service.password", environment.getRequiredProperty("organisaatio.service.password"));
        properties.addDefault("organisaatio.service.username.to.koodisto", environment.getRequiredProperty("organisaatio.service.username.to.koodisto"));
        properties.addDefault("organisaatio.service.password.to.koodisto", environment.getRequiredProperty("organisaatio.service.password.to.koodisto"));
        properties.addDefault("organisaatio.service.username.to.viestinta", environment.getRequiredProperty("organisaatio.service.password.to.viestinta"));
        properties.addDefault("organisaatio.service.password.to.viestinta", environment.getRequiredProperty("organisaatio.service.password.to.viestinta"));
        properties.addDefault("organisaatio.ui.url", environment.getRequiredProperty("organisaatio.ui.url"));
        properties.addDefault("organisaatio.ui.ilmoitukset.url", environment.getRequiredProperty("organisaatio.ui.ilmoitukset.url"));
        properties.addDefault("port.koodisto-service", environment.getRequiredProperty("port.koodisto-service"));
        properties.addDefault("port.tarjonta-service", environment.getRequiredProperty("port.tarjonta-service"));
        properties.addDefault("port.organisaatio-service", environment.getRequiredProperty("port.organisaatio-service"));

        return properties;
    }
}

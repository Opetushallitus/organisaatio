package fi.vm.sade.organisaatio.config;

import fi.vm.sade.oid.service.simple.OIDServiceSimpleImpl;
import fi.vm.sade.organisaatio.config.properties.YtjProperties;
import fi.vm.sade.organisaatio.service.converter.ConverterFactory;

import fi.vm.sade.organisaatio.ytj.api.YTJService;
import fi.vm.sade.organisaatio.ytj.service.YTJServiceImpl;
import fi.vm.sade.security.OidProvider;
import fi.vm.sade.security.OrganisationHierarchyAuthorizer;
import org.modelmapper.ModelMapper;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationPropertiesScan
@PropertySource(value = "classpath:/organisaatio.properties",
                ignoreResourceNotFound = true)
@PropertySource(value = "${user.home}/oph-configuration/organisaatio.properties",
                ignoreResourceNotFound = true)
public class ApplicationConfig {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public OIDServiceSimpleImpl oidService() {
        return new OIDServiceSimpleImpl();
    }

    @Bean
    public YTJService ytjService(YtjProperties properties) {
        return new YTJServiceImpl(properties.asiakastunnus, properties.avain);
    }

    @Bean
    public OrganisationHierarchyAuthorizer authorizer() {
        return new OrganisationHierarchyAuthorizer();
    }

    @Bean
    public OidProvider oidProvider() {
        return new OidProvider();
    }


    @Bean
    ConverterFactory converterFactory() {
        return new ConverterFactory();
    }
}

package fi.vm.sade.organisaatio.config;

import fi.vm.sade.oid.service.simple.OIDServiceSimpleImpl;
import fi.vm.sade.organisaatio.service.converter.ConverterFactory;

import fi.vm.sade.organisaatio.ytj.api.YTJService;
import fi.vm.sade.organisaatio.ytj.service.YTJServiceImpl;
import fi.vm.sade.security.OidProvider;
import fi.vm.sade.security.OrganisationHierarchyAuthorizer;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Value("${rajapinnat.ytj.asiakastunnus}")
    private String ytjAsiakastunnus;

    @Value("${rajapinnat.ytj.avain}")
    private String ytjAvain;

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public OIDServiceSimpleImpl oidService() {
        return new OIDServiceSimpleImpl();
    }

    @Bean
    public YTJService ytjService() {
        return new YTJServiceImpl(ytjAsiakastunnus, ytjAvain);
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

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
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper;
    }

    @Bean
    public OIDServiceSimpleImpl oidService() {
        OIDServiceSimpleImpl oidService = new OIDServiceSimpleImpl();
        return oidService;
    }

    @Bean
    public YTJService ytjService() {
        YTJServiceImpl ytjService = new YTJServiceImpl();
        ytjService.setAsiakastunnus(ytjAsiakastunnus);
        ytjService.setSalainenavain(ytjAvain);
        return ytjService;
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
        ConverterFactory converterFactory = new ConverterFactory();
        return converterFactory;
    }
}

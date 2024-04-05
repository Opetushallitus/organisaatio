package fi.vm.sade.organisaatio.config;

import fi.vm.sade.organisaatio.service.oid.OidService;
import fi.vm.sade.organisaatio.service.oid.OrganisaatioOIDService;
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

    @Value("${url-ytj}")
    private String ytjUrl;

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    OidService oidService() {
        return new OrganisaatioOIDService();
    }

    @Bean
    public YTJService ytjService() {
        return new YTJServiceImpl(ytjAsiakastunnus, ytjAvain, ytjUrl);
    }

    @Bean
    public OrganisationHierarchyAuthorizer authorizer() {
        return new OrganisationHierarchyAuthorizer();
    }

    @Bean
    public OidProvider oidProvider() {
        return new OidProvider();
    }


}

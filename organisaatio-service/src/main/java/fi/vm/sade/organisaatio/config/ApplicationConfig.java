package fi.vm.sade.organisaatio.config;


import fi.vm.sade.oid.service.simple.OIDServiceSimpleImpl;
import fi.vm.sade.organisaatio.service.converter.ConverterFactory;

import fi.vm.sade.rajapinnat.ytj.service.YTJServiceImpl;
import fi.vm.sade.security.OidProvider;
import fi.vm.sade.security.OrganisationHierarchyAuthorizer;
import org.modelmapper.ModelMapper;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//import org.flywaydb.core.Flyway; // versiosta 3.0 eteenpäin
//import com.googlecode.flyway.core.Flyway;


@Configuration
public class ApplicationConfig {
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
    public YTJServiceImpl ytjService() {
        YTJServiceImpl ytjService = new YTJServiceImpl();
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
    /*
    @Bean(initMethod = "migrate")
    Flyway flyway() {
        Flyway flyway = new Flyway();
        flyway.setBaselineOnMigrate(true);
        //flyway.setInitOnMigrate(true);
        flyway.setTable("schema_version");
        flyway.setDataSource("jdbc:postgresql://localhost:5432/organisaatio", "app", "ophoph");
        return flyway;
    }

   */
}

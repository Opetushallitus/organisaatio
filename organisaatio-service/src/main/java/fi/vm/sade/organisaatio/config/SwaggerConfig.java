package fi.vm.sade.organisaatio.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Server server = new Server();
        server.setUrl("/organisaatio-service");
        return new OpenAPI().servers(List.of(server)).info(apiInfo());

    }

    private Info apiInfo() {
        return new Info()
                .title("Organisaatio API")
                .description("Organisaation tarjoamat rajapinnat");
    }
}

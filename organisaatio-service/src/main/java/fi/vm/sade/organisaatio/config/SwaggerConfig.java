package fi.vm.sade.organisaatio.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info().title("Organisaatio API")
                .description("Organisaation tarjoamat rajapinnat"));
    }

    @Bean
    OpenApiCustomizer customerGlobalHeaderOpenApiCustomiser() {
        return openApi -> {
            openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
                ApiResponses apiResponses = operation.getResponses();
                apiResponses.addApiResponse("302",
                        new ApiResponse().description("Autentikaatio puuttuu (Redirect CAS login sivulle)"));
                apiResponses.addApiResponse("401",
                        new ApiResponse().description("Sessio on vanhentunut tai käyttöoikeudet ei riitä"));
            }));
        };
    }
}

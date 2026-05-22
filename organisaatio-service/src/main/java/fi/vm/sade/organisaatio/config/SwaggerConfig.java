package fi.vm.sade.organisaatio.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

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

    @Bean
    OperationCustomizer prePostAuthorizeOperationCustomizer() {
        return (operation, handlerMethod) -> {
            PreAuthorize preAuthorize = AnnotatedElementUtils.findMergedAnnotation(
                    handlerMethod.getMethod(), PreAuthorize.class);
            PostAuthorize postAuthorize = AnnotatedElementUtils.findMergedAnnotation(
                    handlerMethod.getMethod(), PostAuthorize.class);

            if (preAuthorize == null) {
                preAuthorize = AnnotatedElementUtils.findMergedAnnotation(
                        handlerMethod.getBeanType(), PreAuthorize.class);
            }
            if (postAuthorize == null) {
                postAuthorize = AnnotatedElementUtils.findMergedAnnotation(
                        handlerMethod.getBeanType(), PostAuthorize.class);
            }

            if (preAuthorize != null) {
                operation.addExtension("x-preauthorize", preAuthorize.value());

                String description = operation.getDescription();
                String authText = "**PreAuthorize:** `" + preAuthorize.value() + "`";
                operation.setDescription(description == null || description.isBlank()
                        ? authText
                        : description + "\n\n" + authText);
            }
            if (postAuthorize != null) {
                operation.addExtension("x-postauthorize", postAuthorize.value());

                String description = operation.getDescription();
                String authText = "**PostAuthorize:** `" + postAuthorize.value() + "`";
                operation.setDescription(description == null || description.isBlank()
                        ? authText
                        : description + "\n\n" + authText);
            }

            return operation;
        };
    }
}

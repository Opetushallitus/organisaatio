package fi.vm.sade.varda.rekisterointi.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

@Configuration
@EnableSwagger2
@Profile({"dev", "test", "qa"})
public class SwaggerConfiguration {

    @Bean
    public Docket swaggerConfig() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("varda-rekisterointi")
                .ignoredParameterTypes(Authentication.class, HttpServletRequest.class, Locale.class)
                .select()
                    .paths(path ->
                            (path.startsWith("/varda-rekisterointi/api") ||
                             path.startsWith("/varda-rekisterointi/hakija") ||
                             path.startsWith("/varda-rekisterointi/virkailija")) &&
                            !(path.contains("logout") || path.contains("valtuudet"))
                ).build();
    }

}

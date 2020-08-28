package fi.vm.sade.varda.rekisterointi.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    @Value("${varda-rekisterointi.swagger.enabled:false}")
    private boolean swaggerEnabled;

    @Bean
    public Docket swaggerConfig() {
        return new Docket(DocumentationType.SWAGGER_2)
                .enable(swaggerEnabled)
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

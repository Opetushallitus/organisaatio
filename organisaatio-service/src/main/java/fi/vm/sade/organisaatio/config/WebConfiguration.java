package fi.vm.sade.organisaatio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    @Value("${server.ui.context-path}")
    private String uiPath;
    @Value("${server.swagger.context-path}")
    private String swaggerPath;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController(String.format("%s", uiPath))
                .setViewName("forward:/index.html");
        registry.addViewController(String.format("%s/", uiPath))
                .setViewName("forward:/index.html");
        registry.addViewController(String.format("%s/{spring:\\w+}", uiPath))
                .setViewName("forward:/index.html");
        registry.addViewController(String.format("%s/{spring:\\w+}/**{spring:?!(\\.js|\\.css)$}", uiPath))
                .setViewName("forward:/index.html");

//        // Provide swagger documentation
//        registry.addViewController(String.format("%s", swaggerPath))
//                .setViewName("forward:/swagger-ui/index.html");
//        registry.addViewController(String.format("%s/", swaggerPath))
//                .setViewName("forward:/swagger-ui/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(String.format("%s/static/**", uiPath)).addResourceLocations("/static/");
//
//        registry.addResourceHandler(String.format("%s/**", swaggerPath))
//                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
//                .resourceChain(false);
    }
}

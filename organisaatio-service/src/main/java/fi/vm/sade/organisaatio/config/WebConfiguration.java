package fi.vm.sade.organisaatio.config;

import fi.vm.sade.organisaatio.service.filters.CacheFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
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
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(String.format("%s/static/**", uiPath)).addResourceLocations("/static/");
    }

    @Bean
    public FilterRegistrationBean<CacheFilter> filterRegistrationBean() {
        FilterRegistrationBean < CacheFilter > registrationBean = new FilterRegistrationBean();
        CacheFilter customURLFilter = new CacheFilter();
        registrationBean.setFilter(customURLFilter);
        registrationBean.addUrlPatterns("/internal/koodisto/*");
        registrationBean.addUrlPatterns("/internal/lokalisointi/*");
        registrationBean.addUrlPatterns("/internal/config/*");
        return registrationBean;
    }
}

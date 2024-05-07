package fi.vm.sade.organisaatio.config;

import fi.vm.sade.organisaatio.service.filters.CacheFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/swagger-ui/")
                .setViewName("forward:/swagger-ui/index.html");
        registry.addViewController("/")
                .setViewName("forward:/index.html");
        registry.addViewController("/osoitteet/**")
                .setViewName("forward:/index.html");
        registry.addViewController("/{spring:\\w+}")
                .setViewName("forward:/index.html");
        registry.addViewController(("/{spring:\\w+}/**{spring:?!(\\.js|\\.css)$}"))
                .setViewName("forward:/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("/static/");
    }

    @Bean
    FilterRegistrationBean<CacheFilter> filterRegistrationBean() {
        FilterRegistrationBean<CacheFilter> registrationBean = new FilterRegistrationBean<>();
        CacheFilter filter = new CacheFilter();
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns("/internal/koodisto/*");
        registrationBean.addUrlPatterns("/internal/lokalisointi/*");
        registrationBean.addUrlPatterns("/internal/config/*");
        registrationBean.setOrder(2);
        return registrationBean;
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
      configurer.setUseTrailingSlashMatch(true);
    }
}

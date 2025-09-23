package fi.vm.sade.organisaatio.config;

import ch.qos.logback.access.tomcat.LogbackValve;
import fi.vm.sade.RequestCallerFilter;
import fi.vm.sade.RequestIdFilter;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
public class AccessLogConfiguration {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> containerCustomizer() {
        return container -> container.addContextCustomizers(context -> {
            LogbackValve logbackValve = new LogbackValve();
            logbackValve.setFilename("logback-access.xml");
            context.getPipeline().addValve(logbackValve);
        });
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public RequestCallerFilter requestCallerFilter() {
        return new RequestCallerFilter();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public RequestIdFilter requestIdFilter() {
        return new RequestIdFilter();
    }
}

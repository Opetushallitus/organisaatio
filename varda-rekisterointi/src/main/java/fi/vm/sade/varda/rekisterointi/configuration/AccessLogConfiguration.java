package fi.vm.sade.varda.rekisterointi.configuration;

import org.apache.catalina.valves.AccessLogValve;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccessLogConfiguration {

    // this bean can be removed when upgrading to spring boot 2.2
    @Bean
    @ConditionalOnProperty("server.tomcat.accesslog.max-days")
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatAccessLogCustomizer(@Value("${server.tomcat.accesslog.max-days}") int maxDays) {
        return factory -> factory.getEngineValves()
                .stream()
                .filter(AccessLogValve.class::isInstance)
                .map(AccessLogValve.class::cast)
                .forEach(accessLogValve -> accessLogValve.setMaxDays(maxDays));
    }

}

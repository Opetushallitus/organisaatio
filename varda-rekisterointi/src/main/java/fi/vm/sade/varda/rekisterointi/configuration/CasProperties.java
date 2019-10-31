package fi.vm.sade.varda.rekisterointi.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "cas")
@PropertySource("classpath:varda-rekisterointi-cas.properties")
@Validated
public class CasProperties {

    @NotNull
    private String service;
    @NotNull
    private Boolean sendRenew;
    @NotNull
    private String key;
    private String fallbackUserDetailsProviderUrl;

}

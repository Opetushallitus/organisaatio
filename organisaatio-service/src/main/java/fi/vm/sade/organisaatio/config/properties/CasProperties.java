package fi.vm.sade.organisaatio.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "cas")
@ConstructorBinding
public class CasProperties {

    public final String service;
    public final Boolean sendRenew;
    public final String key;

    public CasProperties(String service,
                         @DefaultValue("false") Boolean sendRenew,
                         @DefaultValue("organisaatio-service") String key) {
        this.service = service;
        this.sendRenew = sendRenew;
        this.key = key;
    }
}

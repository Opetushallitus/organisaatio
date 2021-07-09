package fi.vm.sade.organisaatio.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "ytj")
@ConstructorBinding
public class YtjProperties {

    public final String asiakastunnus;
    public final String avain;

    public YtjProperties(String asiakastunnus, String avain) {
        this.asiakastunnus = asiakastunnus;
        this.avain = avain;
    }
}

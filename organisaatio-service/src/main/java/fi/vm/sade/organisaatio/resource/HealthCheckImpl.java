package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.HealthCheck;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckImpl implements HealthCheck {
    @Override
    public String hello() {
        return String.valueOf(System.currentTimeMillis());
    }
}

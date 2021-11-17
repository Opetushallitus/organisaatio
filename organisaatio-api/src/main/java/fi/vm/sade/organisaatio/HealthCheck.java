package fi.vm.sade.organisaatio;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

public interface HealthCheck {
    @GetMapping(path = "/", produces = MediaType.TEXT_PLAIN_VALUE)
    String hello();

}

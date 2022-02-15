package fi.vm.sade.organisaatio;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
@Hidden
public interface HealthCheck {
    @GetMapping(path = "/", produces = MediaType.TEXT_PLAIN_VALUE)
    String hello();
}

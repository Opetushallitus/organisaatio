package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.client.LokalisointiClient;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Hidden
@RestController
@RequestMapping("${server.internal.context-path}/lokalisointi")
public class LokalisointiResource {

    private final LokalisointiClient lokalisointiClient;

    public LokalisointiResource(LokalisointiClient lokalisointiClient) {
        this.lokalisointiClient = lokalisointiClient;
    }

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Map<String, String>> getLokalisointi() {
        return lokalisointiClient.getByCategory("organisaatio2");
    }

}

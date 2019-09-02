package fi.vm.sade.varda.rekisterointi.controller;

import fi.vm.sade.varda.rekisterointi.client.LokalisointiClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/lokalisointi")
public class LokalisointiController {

    private final LokalisointiClient lokalisointiClient;

    public LokalisointiController(LokalisointiClient lokalisointiClient) {
        this.lokalisointiClient = lokalisointiClient;
    }

    @GetMapping
    public Map<String, Map<String, String>> getLokalisointi() {
        return lokalisointiClient.getByCategory("varda-rekisterointi");
    }

}

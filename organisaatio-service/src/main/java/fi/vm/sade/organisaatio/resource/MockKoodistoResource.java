package fi.vm.sade.organisaatio.resource;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@ConditionalOnProperty(name = "feature.mockapi")
@Hidden
@RestController
@RequestMapping({"/mock/koodisto-service"})
@RequiredArgsConstructor
public class MockKoodistoResource {
    private final ObjectMapper objectMapper;

    @GetMapping(path = "/rest/json/{koodisto}/koodi", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonNode getKoodit(@PathVariable String koodisto, @RequestParam boolean onlyValidKoodis) throws IOException {
        String filename = onlyValidKoodis ? koodisto : koodisto + "-onlyValidKoodis-false";
        return objectMapper.readTree(loadKoodistoFromResources(filename));
    }

    @GetMapping(path = "/rest/codeelement/codes/withrelations/{koodisto}/{versio}", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonNode getKooditWithRealtions(@PathVariable String koodisto, @PathVariable Long versio) throws IOException {
        return objectMapper.readTree(loadKoodistoFromResources(koodisto + "-withrelations"));
    }

    private String loadKoodistoFromResources(String koodisto) throws IOException {
        try (InputStream is = this.getClass().getResourceAsStream("/koodisto/" + koodisto + ".json")) {
            Objects.requireNonNull(is);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}

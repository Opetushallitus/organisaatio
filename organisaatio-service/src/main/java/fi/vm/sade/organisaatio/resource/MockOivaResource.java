package fi.vm.sade.organisaatio.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@ConditionalOnProperty(name = "feature.mockapi")
@Hidden
@RestController
@RequestMapping({"/mock/oiva"})
@RequiredArgsConstructor
public class MockOivaResource {
    private final ObjectMapper objectMapper;

    @GetMapping(path = "/api/export/koulutusluvat", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonNode exportKoulutusluvat() throws IOException {
        return objectMapper.readTree(loadFromResource("/oiva/koulutusluvat.json"));

    }

    private String loadFromResource(String filename) throws IOException {
        try (InputStream is = this.getClass().getResourceAsStream(filename)) {
            Objects.requireNonNull(is);
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        }
    }
}

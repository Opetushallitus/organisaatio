package fi.vm.sade.organisaatio.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.vm.sade.organisaatio.client.viestinvalitys.ViestinvalitysClient.PostAttachmentResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

@ConditionalOnProperty(name = "feature.mockapi")
@Hidden
@RestController
@RequestMapping({"/mock/viestinvalitys"})
@RequiredArgsConstructor
public class MockViestinvalitysResource {
    private final ObjectMapper objectMapper;

    @PostMapping(path = "/v1/liitteet", produces = MediaType.APPLICATION_JSON_VALUE)
    public PostAttachmentResponse uploadAttachment(@NonNull @RequestParam("liite") MultipartFile liite) throws IOException {
        PostAttachmentResponse response = new PostAttachmentResponse();
        response.setLiiteTunniste(UUID.randomUUID().toString());
        return response;
    }
}

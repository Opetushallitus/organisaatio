package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.client.viestinvalitys.*;
import fi.vm.sade.organisaatio.client.viestinvalitys.ViestinvalitysClient.PostAttachmentResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@ConditionalOnProperty(name = "feature.mockapi")
@Hidden
@RestController
@RequestMapping({"/mock/viestinvalitys"})
@RequiredArgsConstructor
public class MockViestinvalitysResource {
    private Boolean enabled = true;

    private static final Pattern TIEDOSTONIMIPATTERN = Pattern.compile("^[0-9A-Za-z\\s\\._\\-+]+$");

    @PostMapping(path = "/v1/liitteet", produces = MediaType.APPLICATION_JSON_VALUE)
    public PostAttachmentResponse uploadAttachment(@NonNull @RequestParam("liite") MultipartFile liite) throws IOException {
        if (!TIEDOSTONIMIPATTERN.matcher(liite.getOriginalFilename()).matches()) {
            throw new BadRequestException(new ApiResponse(400, """
                    {
                        "virheet": [
                            "tiedostonimi: Nimi voi sisältää vain isoja ja pieniä kirjaimia, numeroita, sekä seuraavia merkkejä: +, -, ., _: "
                        ]
                    }
                """));
        }
        PostAttachmentResponse response = new PostAttachmentResponse();
        response.setLiiteTunniste(UUID.randomUUID().toString());
        return response;
    }

    @PostMapping(path = "/v1/lahetykset", produces = MediaType.APPLICATION_JSON_VALUE)
    public LuoLahetysSuccessResponse luoLahetys(@RequestBody Lahetys lahetys) throws IOException {
        if (enabled) {
            if ("invalid@invalid".equals(lahetys.getReplyTo())) {
                throw new BadRequestException(new ApiResponse(400, """
                    {
                        "validointiVirheet": [
                            "replyTo: arvo ei ole validi sähköpostiosoite"
                        ]
                    }
                """));
            }

            var response = new LuoLahetysSuccessResponse();
            response.setLahetysTunniste(UUID.randomUUID().toString());
            return response;
        } else {
            throw new IOException("Integration disabled");
        }
    }

    @PostMapping(path = "/v1/viestit", produces = MediaType.APPLICATION_JSON_VALUE)
    public LuoViestiSuccessResponse luoViesti(@RequestBody Viesti viesti) throws IOException {
        if (enabled) {
            if ("invalid".equals(viesti.getSisalto())) {
                throw new BadRequestException(new ApiResponse(400, """
                    {
                        "validointiVirheet": [
                            "sisalto: puuttuu"
                        ]
                    }
                """));
            }

            var response = new LuoViestiSuccessResponse();
            response.setLahetysTunniste(UUID.randomUUID().toString());
            response.setViestiTunniste(UUID.randomUUID().toString());
            return response;
        } else {
            throw new IOException("Integration disabled");
        }
    }

    @PostMapping(path = "/disableIntegration")
    public void disableIntegration() {
        enabled = false;
    }

    @PostMapping(path = "/enableIntegration")
    public void enableIntegration() {
        enabled = true;
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody String handleBadRequestException(BadRequestException e) {
        return e.getApiResponse().getBody();
    }
}

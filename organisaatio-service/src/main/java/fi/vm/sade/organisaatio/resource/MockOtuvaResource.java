package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.business.exception.YtunnusException;
import fi.vm.sade.organisaatio.client.Oauth2BearerClient;
import fi.vm.sade.organisaatio.config.scheduling.FetchKoodistotTask;
import fi.vm.sade.organisaatio.config.scheduling.FetchKoulutusluvatTask;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioNimiRDTO;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ConditionalOnProperty(name = "feature.mockapi")
@Hidden
@RestController
@RequestMapping({"/mock/kayttooikeus-service"})
@RequiredArgsConstructor
@Slf4j
public class MockOtuvaResource {
    @PostMapping("/oauth2/token")
    public Oauth2BearerClient.Token createTestData(@RequestBody MultiValueMap<String, String> formData) {
        var grantType = formData.getFirst("grant_type");
        var clientId = formData.getFirst("client_id");
        var clientSecret = formData.getFirst("client_secret");

        if (!"client_credentials".equals(formData.getFirst("grant_type"))) {
            throw new RuntimeException("Unsupported grant type");
        }

        if ("organisaatio".equals(clientId) && "foobar".equals(clientSecret)) {
            log.info("Returning mock OAuth2 token");
            return new Oauth2BearerClient.Token("", "Bearer", Integer.MAX_VALUE);
        }

        throw new RuntimeException("Forbidden client_id or client_secret");
    }
}


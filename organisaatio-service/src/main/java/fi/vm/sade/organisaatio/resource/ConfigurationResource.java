package fi.vm.sade.organisaatio.resource;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("${server.internal.context-path}/config")
public class ConfigurationResource {
    @Value("${url-virkailija}")
    private String urlVirkailija;
    @Value("${viestinvalitys.uiurl}")
    private String viestinvalityspalveluUrl;

    @GetMapping(value = "/frontproperties", produces = MediaType.APPLICATION_JSON_VALUE)
    public FrontProperties frontProperties() {
        return new FrontProperties(urlVirkailija, viestinvalityspalveluUrl);
    }

    record FrontProperties(String urlVirkailija, String viestinvalityspalveluUrl) {};
}

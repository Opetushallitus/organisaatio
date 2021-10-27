package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.config.UrlConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@RestController
@RequestMapping("${server.rest.context-path}/config")
public class ConfigurationResource {

    @Autowired
    private UrlConfiguration urlConfiguration;

    @GetMapping(value = "/frontproperties", produces = MediaType.APPLICATION_JSON_VALUE)
    public String frontProperties() {
        return urlConfiguration.frontPropertiesToJson();
    }
}

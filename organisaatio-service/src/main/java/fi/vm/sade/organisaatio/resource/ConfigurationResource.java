package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.config.UrlConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/config")
public class ConfigurationResource {

    @Autowired
    private UrlConfiguration urlConfiguration;

    @GetMapping(value = "/frontproperties", produces = "application/json")
    public String frontProperties() {
        return urlConfiguration.frontPropertiesToJson();
    }
}

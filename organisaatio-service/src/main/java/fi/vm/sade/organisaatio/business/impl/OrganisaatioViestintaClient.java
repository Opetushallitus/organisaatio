package fi.vm.sade.organisaatio.business.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class OrganisaatioViestintaClient extends OrganisaatioBaseClient {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Value("${organisaatio-service.viestinta-service.rest.url}")
    protected String viestintaServiceUrl;

    @Value("${organisaatio.service.username.to.viestinta}")
    private String viestintaClientUsername;

    @Value("${organisaatio.service.password.to.viestinta}")
    private String viestintaClientPassword;

    @PostConstruct
    public void init() {
        setUp(viestintaServiceUrl, viestintaClientUsername, viestintaClientPassword);
    }

    public void post(String message, List<String> receivers) {

    }
}

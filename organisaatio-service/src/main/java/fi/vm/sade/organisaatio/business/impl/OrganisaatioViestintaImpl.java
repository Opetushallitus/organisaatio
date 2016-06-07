package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.business.OrganisaatioViestinta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrganisaatioViestintaImpl implements OrganisaatioViestinta {
    @Value("$viestintapalvelu.email")
    private String email;

    @Autowired
    private OrganisaatioViestintaClient organisaatioViestintaClient;

    @Override
    public void sendEmail(String message) {
        sendEmail(message, new ArrayList<String>(){{add(email);}});
    }

    @Override
    public void sendEmail(String message, List<String> receivers) {
        organisaatioViestintaClient.post(message, receivers);
    }
}

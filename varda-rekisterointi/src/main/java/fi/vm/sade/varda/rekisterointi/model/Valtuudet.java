package fi.vm.sade.varda.rekisterointi.model;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("session")
public class Valtuudet {

    public String sessionId;
    public String callbackUrl;

    public String businessId;
    public OrganisaatioV4Dto organisaatio;

}

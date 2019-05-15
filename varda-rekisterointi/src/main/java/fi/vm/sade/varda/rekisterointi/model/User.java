package fi.vm.sade.varda.rekisterointi.model;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("session")
public class User {

    public String nationalIdentificationNumber;
    public String givenName;
    public String surname;

}

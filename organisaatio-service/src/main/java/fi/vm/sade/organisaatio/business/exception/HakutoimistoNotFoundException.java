package fi.vm.sade.organisaatio.business.exception;

public class HakutoimistoNotFoundException extends OrganisaatioBusinessException {
    public HakutoimistoNotFoundException(String oid) {
        super(String.format("Hakutoimistoa ei löytynyt, ylin organisaatio %s", oid));
    }
    @Override
    public String getErrorKey() {
        return "organisaatio.exception.organisaatio.hakutoimisto.not.found";
    }
}

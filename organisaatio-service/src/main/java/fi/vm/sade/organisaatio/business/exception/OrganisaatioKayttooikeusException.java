package fi.vm.sade.organisaatio.business.exception;

public class OrganisaatioKayttooikeusException extends OrganisaatioBusinessException {

    public OrganisaatioKayttooikeusException(String msg) {
        super(msg);
    }

    @Override
    public String getErrorKey() {
        return "organisaatio.exception.kayttooikeus";
    }

}

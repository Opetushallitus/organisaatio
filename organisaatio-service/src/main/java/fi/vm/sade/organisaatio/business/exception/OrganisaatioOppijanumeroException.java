package fi.vm.sade.organisaatio.business.exception;

public class OrganisaatioOppijanumeroException extends OrganisaatioBusinessException {

    public OrganisaatioOppijanumeroException(String msg) {
        super(msg);
    }

    @Override
    public String getErrorKey() {
        return "organisaatio.exception.oppijanumero";
    }

}

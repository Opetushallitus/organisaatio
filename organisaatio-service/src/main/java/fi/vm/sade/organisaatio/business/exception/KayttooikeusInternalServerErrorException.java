package fi.vm.sade.organisaatio.business.exception;

public class KayttooikeusInternalServerErrorException extends OrganisaatioBusinessException {
    public KayttooikeusInternalServerErrorException(String msg) {
        super(msg);
    }

    @Override
    public String getErrorKey() {
        return "organisaatio.exception.kayttooikeus";
    }
}

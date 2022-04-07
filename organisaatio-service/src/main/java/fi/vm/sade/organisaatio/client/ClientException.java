package fi.vm.sade.organisaatio.client;

import fi.vm.sade.organisaatio.business.exception.OrganisaatioBusinessException;

public class ClientException extends OrganisaatioBusinessException {

    public ClientException(String msg) {
        super(msg);
    }

    @Override
    public String getErrorKey() {
        return "organisaatio.exception.ophclient";
    }

}

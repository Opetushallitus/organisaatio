package fi.vm.sade.organisaatio.service;

import fi.vm.sade.organisaatio.business.exception.OrganisaatioBusinessException;

public class NotAuthorizedException extends
        OrganisaatioBusinessException {

    private static final long serialVersionUID = 1L;

    public NotAuthorizedException(String message) {
        super(message);
    }

    @Override
    public String getErrorKey() {
        return getMessage();
    }

}

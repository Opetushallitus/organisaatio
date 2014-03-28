package fi.vm.sade.organisaatio.service;

import fi.vm.sade.organisaatio.business.exception.OrganisaatioBusinessException;

public class OrganisationPassivationException extends OrganisaatioBusinessException {

    private static final long serialVersionUID = 1L;

    public OrganisationPassivationException(String key, Throwable cause) {
        super(key, cause);
    }
}

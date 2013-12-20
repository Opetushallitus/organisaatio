package fi.vm.sade.organisaatio.service;

public class OrganisationPassivationException extends AbstractOrganisaatioBusinessException {

    private static final long serialVersionUID = 1L;

    public OrganisationPassivationException(String key, Throwable cause) {
        super(key, cause);
    }
}

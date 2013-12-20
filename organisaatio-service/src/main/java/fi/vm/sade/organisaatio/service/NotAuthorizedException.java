package fi.vm.sade.organisaatio.service;

public class NotAuthorizedException extends
        AbstractOrganisaatioBusinessException {

    private static final long serialVersionUID = 1L;

    public NotAuthorizedException(String message) {
        super(message);
    }

}

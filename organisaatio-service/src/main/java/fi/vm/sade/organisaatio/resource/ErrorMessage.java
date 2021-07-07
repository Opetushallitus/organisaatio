package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.generic.service.exception.SadeBusinessException;

public class ErrorMessage {

    private final String errorMessage;
    private final String errorKey;

    public ErrorMessage(String errorMessage, String errorKey) {
        this.errorMessage = errorMessage;
        this.errorKey = errorKey;
    }

    public ErrorMessage(SadeBusinessException sbe) {
        this.errorMessage = sbe.getMessage();
        this.errorKey = sbe.getErrorKey();
    }

    public ErrorMessage(String errorMessage) {
        this(errorMessage, "");
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorKey() {
        return errorKey;
    }

}

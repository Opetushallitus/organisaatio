package fi.vm.sade.organisaatio.business.exception;

public class HakutoimistoNotFoundException extends RuntimeException {
    public HakutoimistoNotFoundException(String message) {
        super(message);
    }
}

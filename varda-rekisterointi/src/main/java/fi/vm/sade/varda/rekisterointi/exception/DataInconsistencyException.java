package fi.vm.sade.varda.rekisterointi.exception;

public class DataInconsistencyException extends SystemException {

    public DataInconsistencyException(String message) {
        super(message);
    }

    public DataInconsistencyException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataInconsistencyException(Throwable cause) {
        super(cause);
    }
}

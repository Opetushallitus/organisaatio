package fi.vm.sade.oid;

public class ExceptionMessage extends java.lang.Exception {

    private Exception exception;

    public ExceptionMessage() {
        super();
    }

    public ExceptionMessage(String message) {
        super(message);
    }

    public ExceptionMessage(String message, Throwable cause) {
        super(message, cause);
    }

    public ExceptionMessage(String message, Exception exception) {
        super(message);
        this.exception = exception;
    }

    public ExceptionMessage(String message, Exception exception, Throwable cause) {
        super(message, cause);
        this.exception = exception;
    }

    public Exception getFaultInfo() {
        return this.exception;
    }
}

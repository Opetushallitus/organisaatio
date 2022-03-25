package fi.vm.sade.organisaatio.ytj.api.exception;

public class YtjConnectionException extends Exception {
    
    private final YtjExceptionType exceptionCode;
    private final String message;
    
    public YtjConnectionException(YtjExceptionType expCode, String msg) {
        this.exceptionCode = expCode;
        this.message = msg;
    }
    
    public YtjExceptionType getExceptionType() {
        return exceptionCode;
    }

    @Override
    public String toString() {
        return exceptionCode + " " + message;
    }
    
    
    @Override
    public String getMessage() {
        return message;
    }
}

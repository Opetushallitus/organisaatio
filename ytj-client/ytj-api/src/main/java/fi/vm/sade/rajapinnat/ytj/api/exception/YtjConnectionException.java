/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.vm.sade.rajapinnat.ytj.api.exception;

/**
 *
 * @author Tuomas Katva
 */
public class YtjConnectionException extends Exception {
    
    private YtjExceptionType exceptionCode;
    private String message;
    
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

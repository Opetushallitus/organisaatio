package fi.vm.sade.organisaatio;

public class LocalizedBusinessException extends Exception {

    private String key;

    public LocalizedBusinessException(String key) {
        super(key);
        this.key = key;
    }

    public LocalizedBusinessException(String message, String key) {
        super(message);
        this.key = key;
    }

    public LocalizedBusinessException(String message, Throwable cause, String key) {
        super(message, cause);
        this.key = key;
    }

    public LocalizedBusinessException(Throwable cause, String key) {
        super(cause);
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}

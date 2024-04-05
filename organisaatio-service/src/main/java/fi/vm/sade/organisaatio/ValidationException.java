package fi.vm.sade.organisaatio;

import javax.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ValidationException extends LocalizedBusinessException {

    public static final String KEY = "validation.exception";
    private List<String> validationMessages = new ArrayList<String>();
    private Set<ConstraintViolation<Object>> violations;

    public ValidationException() {
        super(KEY);
    }

    public ValidationException(String message) {
        super(KEY);
        addValidationMessage(message);
    }

    public ValidationException(String message, Exception cause) {
        super(cause, KEY);
        addValidationMessage(message);
    }

    public ValidationException(String message, String key) {
        super(message, key);
    }

    public ValidationException(Set<javax.validation.ConstraintViolation<Object>> violations) {
        super(KEY);
        this.violations = violations;
    }

    public void addValidationMessage(String msg) {
        validationMessages.add(msg);
    }

    @Override
    public String getMessage() {
        return validationMessages.toString();
    }

    public List<String> getValidationMessages() {
        return validationMessages;
    }

    public void setValidationMessages(List<String> validationMessages) {
        this.validationMessages = validationMessages;
    }

    public Set<ConstraintViolation<Object>> getViolations() {
        return violations;
    }
}

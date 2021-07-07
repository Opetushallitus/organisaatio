package fi.vm.sade.organisaatio.resource;

import org.springframework.http.ResponseEntity;

public class ApiException extends RuntimeException {

    private final ResponseEntity<?> responseEntity;

    public ApiException(ResponseEntity<?> responseEntity) {
        this.responseEntity = responseEntity;
    }

    public final ResponseEntity<?> getResponseEntity() {
        return responseEntity;
    }
}

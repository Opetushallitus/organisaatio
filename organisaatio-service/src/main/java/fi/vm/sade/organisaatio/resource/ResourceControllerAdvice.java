package fi.vm.sade.organisaatio.resource;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice(basePackages = "fi.vm.sade.organisaatio.resource")
public class ResourceControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ApiException.class)
    @ResponseBody
    ResponseEntity<?> handleApiException(Throwable exception) {
        ApiException apiException = (ApiException) exception;
        return apiException.getResponseEntity();
    }

}

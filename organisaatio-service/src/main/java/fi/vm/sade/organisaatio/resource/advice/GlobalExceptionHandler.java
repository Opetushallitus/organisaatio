package fi.vm.sade.organisaatio.resource.advice;

import fi.vm.sade.organisaatio.business.exception.HakutoimistoNotFoundException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioBusinessException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioCrudException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioNotFoundException;
import fi.vm.sade.organisaatio.resource.OrganisaatioResourceException;
import fi.vm.sade.organisaatio.resource.dto.ApiErrorDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String DEBUG_LOG_MESSAGE = "GlobalExceptionHandler handled this exception";
    private static final String WARN_NOT_HANDELED_MESSAGE = "GlobalExceptionHandler passed this exception through";

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException e) {
        log.debug(DEBUG_LOG_MESSAGE, e);
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String errorMessage = e.getMessage();
        if (!violations.isEmpty()) {
            StringBuilder builder = new StringBuilder(errorMessage);
            violations.forEach(violation -> builder.append(" ").append(violation.getPropertyPath()).append(": ").append(violation.getMessage()));
            errorMessage = builder.toString().trim();
        }
        return ResponseEntity.badRequest().body(errorMessage);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiErrorDTO.builder().errorKey("method.argument.type.mismatch").errorMessage(e.getRootCause().getLocalizedMessage()).parameters(List.of(e.getParameter().getParameterName())).build());
    }

    @ExceptionHandler(OrganisaatioNotFoundException.class)
    public ResponseEntity<Object> handleOrganisaatioNotFoundException(OrganisaatioNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiErrorDTO.builder().errorKey(e.getErrorKey()).errorMessage(e.getMessage()).build());
    }


    @ExceptionHandler(HakutoimistoNotFoundException.class)
    public ResponseEntity<Object> handleHakutoimistoNotFoundException(HakutoimistoNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiErrorDTO.builder().errorKey(e.getErrorKey()).errorMessage(e.getMessage()).build());
    }

    @ExceptionHandler(OrganisaatioCrudException.class)
    public ResponseEntity<Object> handleOrganisaatioCrudException(OrganisaatioCrudException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiErrorDTO.builder().errorKey(e.getErrorKey()).errorMessage(e.getMessage()).build());
    }



    @ExceptionHandler(OrganisaatioBusinessException.class)
    public ResponseEntity<Object> handleOrganisaatioBusinessException(OrganisaatioBusinessException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiErrorDTO.builder().errorKey(e.getErrorKey()).errorMessage(e.getMessage()).build());
    }

    @ExceptionHandler(OrganisaatioResourceException.class)
    public ResponseEntity<Object> handleOrganisaatioResourceException(OrganisaatioResourceException e) {
        return ResponseEntity.status(e.getStatus()).body(ApiErrorDTO.builder().errorKey(e.getErrorKey()).errorMessage(e.getErrorMessage()).build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception e) throws Exception {
        log.warn(WARN_NOT_HANDELED_MESSAGE, e);
        throw e;
    }
}

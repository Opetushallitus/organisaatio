package fi.vm.sade.organisaatio.resource.advice;

import fi.vm.sade.organisaatio.business.exception.HakutoimistoNotFoundException;
import fi.vm.sade.organisaatio.business.exception.KayttooikeusInternalServerErrorException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioBusinessException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioNotFoundException;
import fi.vm.sade.organisaatio.client.viestinvalitys.BadRequestException;
import fi.vm.sade.organisaatio.resource.OrganisaatioResourceException;
import fi.vm.sade.organisaatio.resource.dto.ApiErrorDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String DEBUG_LOG_MESSAGE = "GlobalExceptionHandler handled this exception";
    private static final String WARN_NOT_HANDELED_MESSAGE = "GlobalExceptionHandler passed this exception through {}";

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException e) {
        log.debug(DEBUG_LOG_MESSAGE, e);
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        return ResponseEntity.badRequest()
                .body(ApiErrorDTO.builder()
                        .errorKey("constraint.violation")
                        .errorMessage(e.getLocalizedMessage())
                        .parameters(violations.stream().map(ConstraintViolation::getPropertyPath).map(Path::toString).collect(Collectors.toList())).build());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorDTO.builder()
                        .errorKey("method.argument.type.mismatch")
                        .errorMessage(Optional.ofNullable(e.getRootCause()).map(Throwable::getLocalizedMessage).orElse("method.argument.type.mismatch"))
                        .parameters(Optional.of(e.getParameter()).map(MethodParameter::getParameterName).stream().collect(Collectors.toList())).build());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorDTO.builder()
                        .errorKey("mising.servlet.request.parameter")
                        .errorMessage("Missing parameter")
                        .parameters(List.of(e.getParameterName())).build());
    }

    @ExceptionHandler(OrganisaatioNotFoundException.class)
    public ResponseEntity<Object> handleOrganisaatioNotFoundException(OrganisaatioNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiErrorDTO.builder().errorKey(e.getErrorKey()).errorMessage(e.getMessage()).build());
    }


    @ExceptionHandler(HakutoimistoNotFoundException.class)
    public ResponseEntity<Object> handleHakutoimistoNotFoundException(HakutoimistoNotFoundException e) {
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

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleViestinvalitysBadRequestException(BadRequestException bre) {
        return ResponseEntity.status(400).body(bre.getApiResponse().getBody());
    }

    @ExceptionHandler(KayttooikeusInternalServerErrorException.class)
    public ResponseEntity<Object> handleKayttooikeusInternalServerErrorException(KayttooikeusInternalServerErrorException e) {
        return ResponseEntity.status(500).body(ApiErrorDTO.builder().errorKey(e.getErrorKey()).errorMessage(e.getMessage()).build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception e) throws Exception {
        log.warn(WARN_NOT_HANDELED_MESSAGE, e.getClass().getName());
        throw e;
    }
}

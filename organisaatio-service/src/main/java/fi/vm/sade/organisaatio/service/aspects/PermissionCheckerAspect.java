package fi.vm.sade.organisaatio.service.aspects;

import fi.vm.sade.organisaatio.auth.PermissionChecker;
import fi.vm.sade.organisaatio.business.exception.NotAuthorizedException;
import fi.vm.sade.organisaatio.resource.OrganisaatioResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
@RequiredArgsConstructor
public class PermissionCheckerAspect {
    public static final String NOT_AUTHORIZED_TO_READ_ORGANISATION = "Not authorized to read organisation: {}";
    private final PermissionChecker permissionChecker;

    @Pointcut("execution(* fi.vm.sade.organisaatio.resource.OrganisaatioApi.childoids(..)) ")
    private void childoids() {
        // Pointcut for childoids
    }

    @Pointcut("execution(* fi.vm.sade.organisaatio.resource.OrganisaatioApi.parentoids(..)) ")
    private void parentoids() {
        // Pointcut for parentoids
    }

    @Before("(childoids()||parentoids()) && args(oid,..)")
    public void checkReadPermission(String oid) {
        try {
            permissionChecker.checkReadOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            log.warn(NOT_AUTHORIZED_TO_READ_ORGANISATION, oid);
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, nae);
        }
    }
}

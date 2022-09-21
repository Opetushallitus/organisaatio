package fi.vm.sade.organisaatio.service.aspects;

import fi.vm.sade.organisaatio.auth.PermissionChecker;
import fi.vm.sade.organisaatio.business.exception.NotAuthorizedException;
import fi.vm.sade.organisaatio.resource.OrganisaatioResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
@RequiredArgsConstructor
public class PermissionCheckerAspect {
    private final PermissionChecker permissionChecker;

    @Before("@annotation(CheckReadPermission) && args(oid,..)")
    public void checkReadPermission(String oid) {
        try {
            permissionChecker.checkReadOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, String.format("Not authorized to read organisation: %s", oid),"no.permission");
        }
    }
}

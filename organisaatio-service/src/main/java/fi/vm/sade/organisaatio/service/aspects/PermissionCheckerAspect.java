package fi.vm.sade.organisaatio.service.aspects;

import fi.vm.sade.organisaatio.auth.PermissionChecker;
import fi.vm.sade.organisaatio.business.exception.NotAuthorizedException;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
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
    public static final String NO_PERMISSION = "no.permission";
    private final PermissionChecker permissionChecker;

    @Before("@annotation(CheckReadPermission) && args(oid,..)")
    public void checkReadPermission(String oid) {
        boolean globalReadAccess = permissionChecker.isReadAccessToAll();
        if (!globalReadAccess) {
            try {
                permissionChecker.checkReadOrganisation(oid);
            } catch (NotAuthorizedException nae) {
                throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN,
                        String.format("Not authorized to read organisation: %s", oid)
                        , NO_PERMISSION);
            }
        }
    }

    @Before("@annotation(CheckUpdatePermission) && args(oid,org,..)")
    public void checkUpdatePermission(String oid, OrganisaatioRDTOV4 org) {
        try {
            permissionChecker.checkSaveOrganisation(org, true);
        } catch (NotAuthorizedException nae) {
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN,
                    String.format("Not authorized to update organisation: %s", oid)
                    , NO_PERMISSION);
        }
    }

    @Before("@annotation(CheckAddPermission) && args(org,..)")
    public void checkAddPermission(OrganisaatioRDTOV4 org) {
        try {
            permissionChecker.checkSaveOrganisation(org, false);
        } catch (NotAuthorizedException nae) {
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN,
                    String.format("Not authorized to create child organisation for %s", org.getParentOid())
                    , NO_PERMISSION);
        }
    }

    @Before("@annotation(CheckDeletePermission) && args(oid,..)")
    public void checkDeletePermission(String oid) {
        try {
            permissionChecker.checkRemoveOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN,
                    String.format("Not authorized to delete organisation: %s", oid)
                    , NO_PERMISSION);
        }
    }

    @Before("@annotation(CheckUpdateNamePermission) && args(oid,..)")
    public void checkUpdateNamePermission(String oid) {
        try {
            permissionChecker.checkUpdateOrganisationName(oid);
        } catch (NotAuthorizedException nae) {
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN,
                    String.format("Not authorized to update name for organisation: %s", oid)
                    , NO_PERMISSION);
        }
    }
    @Before("@annotation(CheckTarkastusPermission) && args(oid,..)")
    public void checkTarkastusPermission(String oid) {
        try {
            permissionChecker.checkUpdateOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN,
                    String.format("Not authorized to update tarkastus for organisation: %s", oid)
                    , NO_PERMISSION);
        }
    }
}

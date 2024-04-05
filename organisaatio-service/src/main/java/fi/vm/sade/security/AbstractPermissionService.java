package fi.vm.sade.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class AbstractPermissionService implements PermissionService {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    public static final String ANY_ROLE = OrganisationHierarchyAuthorizer.ANY_ROLE;
    public final String ROLE_CRUD;
    public final String ROLE_RU;
    public final String ROLE_R;

    @Deprecated
    @Value("${root.organisaatio.oid}")
    private String rootOrgOid;

    @Autowired(required = false)
    private OrganisationHierarchyAuthorizer authorizer;

    protected AbstractPermissionService(String application) {
        ROLE_CRUD = "APP_" + application + "_CRUD";
        ROLE_RU = "APP_" + application + "_READ_UPDATE";
        ROLE_R = "APP_" + application + "_READ";
    }

    public final String getReadRole() {
        return ROLE_R;
    }

    public final String getReadUpdateRole() {
        return ROLE_RU;
    }

    public final String getCreateReadUpdateDeleteRole() {
        return ROLE_CRUD;
    }

    public final boolean checkAccess(String[] roles) {
        if (authorizer == null) {
            throw new NullPointerException(this.getClass().getSimpleName()
                    + ".authorizer -property is not wired, do it with spring or manually");
        }

        boolean hasAccess = false;

        try {
            authorizer.checkAccess(SecurityContextHolder.getContext().getAuthentication(), roles);
            hasAccess = true;
        } catch (Exception e) {
            hasAccess = false;
        }

        return hasAccess;
    }

    @Override
    public final boolean userCanRead() {
        return checkAccess(new String[] { ROLE_R, ROLE_RU, ROLE_CRUD });
    }

    @Override
    public final boolean userCanReadAndUpdate() {
        return checkAccess(new String[] { ROLE_RU, ROLE_CRUD });
    }

    @Override
    public final boolean userCanCreateReadUpdateAndDelete() {
        return checkAccess(new String[] { ROLE_CRUD });
    }

    protected final boolean userIsMemberOfOrganisation(final String organisaatioOid) {
        return checkAccess(organisaatioOid, ANY_ROLE);
    }

    public final boolean checkAccess(String targetOrganisaatioOid, String... roles) {
        if (authorizer == null) {
            throw new NullPointerException(this.getClass().getSimpleName()
                    + ".authorizer -property is not wired, do it with spring or manuyally");
        }

        boolean hasAccess = false;
        try {
            authorizer.checkAccess(SecurityContextHolder.getContext().getAuthentication(), targetOrganisaatioOid, roles);
            hasAccess = true;
        } catch (Exception e) {
            if (!(e instanceof NotAuthorizedException)) {
                log.error("checkAccess failed because exception: " + e.getMessage() + ", auth: " + SecurityContextHolder.getContext().getAuthentication(), e);
            }
            hasAccess = false;
        }

        return hasAccess;
    }

    @Deprecated
    public String getRootOrgOid() {
        if (rootOrgOid == null) {
            throw new RuntimeException("rootOrgId is null!");
        }
        return rootOrgOid;
    }

    @Deprecated
    public boolean isOPHUser() {
        return checkAccess(getRootOrgOid(), ANY_ROLE);
    }

    public void setAuthorizer(OrganisationHierarchyAuthorizer authorizer) {
        this.authorizer = authorizer;
    }
}

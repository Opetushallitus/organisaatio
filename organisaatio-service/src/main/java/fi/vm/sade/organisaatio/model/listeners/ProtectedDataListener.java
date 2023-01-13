package fi.vm.sade.organisaatio.model.listeners;

import fi.vm.sade.organisaatio.model.Organisaatio;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.PostLoad;

@Configurable
public class ProtectedDataListener {
    private static final String YKSITYINEN_ELINKEINOHARJOITTAJA = "Yksityinen elinkeinonharjoittaja";

    public static final String ROLE_ANONYMOUS = "ROLE_ANONYMOUS";
    public static final String ROLE_CRUD_OPH = "ROLE_APP_ORGANISAATIOHALLINTA_CRUD_";

    @Value("${root.organisaatio.oid}")
    private String rootOid;

    @PostLoad
    public void handleProtectedData(Organisaatio org) {
        if (isProtected(org) && !canViewProtected()) {
            org.setMaskingActive(true);
        }
    }

    private boolean isProtected(Organisaatio org) {
        return org.isPiilotettu() || YKSITYINEN_ELINKEINOHARJOITTAJA.equals(org.getYritysmuoto());
    }

    private boolean canViewProtected() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return !isAnonymous(auth) && isOPH(auth);
    }

    private boolean isAnonymous(Authentication auth) {
        return auth == null || auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(ROLE_ANONYMOUS));
    }

    private boolean isOPH(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(ROLE_CRUD_OPH + rootOid));
    }
}

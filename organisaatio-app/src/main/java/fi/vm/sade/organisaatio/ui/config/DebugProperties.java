package fi.vm.sade.organisaatio.ui.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author jani
 */
@Configurable(preConstruction = false)
public class DebugProperties {

    @Value("${debug.ui.active:}")
    private String debugUiActive;
    @Value("${debug.role.r:}")
    private String debugRoleR;
    @Value("${debug.role.ru:}")
    private String debugRoleRU;
    @Value("${debug.role.crud:}")
    private String debugRoleCRUD;

    //in configuration file the oids are separated with whitespace
    @Value("${debug.oidSet:}")
    private String oidSet;

    public DebugProperties() {
    }

    /**
     * When the debug mode is on, then the application UI uses
     * pre-configured UI action roles.
     *
     * @return the debugRoleActive
     */
    public boolean isDebugUiActive() {
        return Boolean.valueOf(debugUiActive);
    }

    /**
     * @return the debugRoleR
     */
    public boolean isDebugRoleR() {
        return Boolean.valueOf(debugRoleR);
    }

    /**
     * @return the debugRoleRU
     */
    public boolean isDebugRoleRU() {
        return Boolean.valueOf(debugRoleRU);
    }

    /**
     * @return the debugRoleCRUD
     */
    public boolean isDebugRoleCRUD() {
        return Boolean.valueOf(debugRoleCRUD);
    }

    /**
     * @return organisation hierarchy
     */
    public Set<String> getOrganisations() {
        if (oidSet == null) {
            return Collections.EMPTY_SET;
        }
        final HashSet<String> orgs = new HashSet<String>();
        for (String oid : oidSet.split("\\s+")) {
            orgs.add(oid.trim());
        }
        return orgs;
    }
}

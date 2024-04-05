package fi.vm.sade.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class OrganisationHierarchyAuthorizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganisationHierarchyAuthorizer.class);
    public static final int MAX_CACHE_SIZE = 10000;
    public static final String ANY_ROLE = "*";

    @Autowired
    private OidProvider oidProvider;

    // poor man's cache, use auth object as part of key so objects will last only one authenticated session
    //private Map<String,List<String>> cache = new ConcurrentHashMap<String, List<String>>();
    // not linked to user anymore, remove oldest entries instead
    // http://stackoverflow.com/questions/224868/easy-simple-to-use-lru-cache-in-java
    private static Map<String,List<String>> cache = SimpleCache.<String, List<String>>buildCache(MAX_CACHE_SIZE);

    public OrganisationHierarchyAuthorizer() {
    }

    public OrganisationHierarchyAuthorizer(OidProvider oidProvider) {
        this.oidProvider = oidProvider;
    }

    /**
     * Check if current user has at least one of given requriedRoles to target organisation or it's parents.
     *
     * @param targetOrganisationOid
     * @param requriedRoles
     * @throws NotAuthorizedException
     */
    public void checkAccess(Authentication currentUser, String targetOrganisationOid, String[] requriedRoles) throws NotAuthorizedException {

        // do assertions
        if (currentUser == null) {
            throw new NotAuthorizedException("checkAccess failed, currentUser is null");
        }

        List<String> userRoles = toStringRoles(currentUser.getAuthorities());
        checkAccess(userRoles, targetOrganisationOid, requriedRoles);
    }

    /**
     * @see checkAccess(currentUser, targetOrganisationOid, roles)
     */
    public void checkAccess(List<String> userRoles, String targetOrganisationOid, String[] requiredRoles) throws NotAuthorizedException {

        List<String> targetOrganisationAndParentsOids = getSelfAndParentOidsCached(targetOrganisationOid);
        if (targetOrganisationAndParentsOids == null || targetOrganisationAndParentsOids.size() == 0) {
            throw new NotAuthorizedException("checkAccess failed, no targetOrganisationAndParentsOids null");
        }
        if (requiredRoles == null || requiredRoles.length == 0) {
            throw new NotAuthorizedException("checkAccess failed, no requiredRoles given");
        }

        // do the checks

        // sen sijaan että tarkastettaisiin käyttäjän roolipuussa alaspäin, tarkastetaan kohde-puussa ylöspäin
        // jos käyttäjällä on rooli organisaatioon, tai johonkin sen parenttiin, pääsy sallitaan
        for (String role : requiredRoles) {
            for (String oid : targetOrganisationAndParentsOids) {
                for (String userRole : userRoles) {
                    if (roleMatchesToAuthority(role, userRole) && authorityIsTargetedToOrganisation(userRole, oid)) {
                        return;
                    }
                }
            }
        }
        final String msg = "Not authorized! targetOrganisationAndParentsOids: " + targetOrganisationAndParentsOids + ", requiredRoles: " + Arrays.asList(requiredRoles) + ", userRoles: " + userRoles;
        throw new NotAuthorizedException(msg);
    }

    /**
     * Checks if the current user has at least one of given requiredRoles
     *
     * @param currentUser
     * @param requiredRoles
     * @throws NotAuthorizedException
     */
    public void checkAccess(Authentication currentUser, String[] requiredRoles) throws NotAuthorizedException {
        // do assertions
        if (currentUser == null) {
            throw new NotAuthorizedException("checkAccess failed, currentUser is null");
        }

        if (requiredRoles == null || requiredRoles.length == 0) {
            throw new NotAuthorizedException("checkAccess failed, no requiredRoles given");
        }

        for(String role: requiredRoles) {
            for(GrantedAuthority authority : currentUser.getAuthorities()) {
                if(roleMatchesToAuthority(role, authority.getAuthority())) {
                    return;
                }
            }
        }

        final String msg = "Not authorized! currentUser: " + currentUser + ", requiredRoles: " + Arrays.asList(requiredRoles);
        throw new NotAuthorizedException(msg);
    }

    private List<String> getSelfAndParentOidsCached(String targetOrganisationOid) {
        String cacheKey = targetOrganisationOid; // ei enää user-kohtaista cachea koska organisaatioparentit ei about ikinä muutu
        List<String> cacheResult = cache.get(cacheKey);
        if (cacheResult == null) {
            cacheResult = oidProvider.getSelfAndParentOids(targetOrganisationOid);
            cache.put(cacheKey, cacheResult);
        }
        return cacheResult;
    }

    private static boolean roleMatchesToAuthority(String role, String authority) {
        if (ANY_ROLE.equals(role)) {
            return true;
        }
        role = stripRolePrefix(role);
        return authority.contains(role);
    }

    private static String stripRolePrefix(String role) {
        return role.replace("APP_", "").replace("ROLE_", "");
    }

    private static boolean authorityIsTargetedToOrganisation(String authority, String oid) {
        return authority.endsWith(oid);
    }

    public static OrganisationHierarchyAuthorizer createMockAuthorizer(final String parentOrg, final String[] childOrgs) {
        return new OrganisationHierarchyAuthorizer(new OidProvider(){
            @Override
            public List<String> getSelfAndParentOids(String organisaatioOid) {
                if (parentOrg.equals(organisaatioOid)) {
                    return Arrays.asList(organisaatioOid);
                }
                if (Arrays.asList(childOrgs).contains(organisaatioOid)) {
                    return Arrays.asList(organisaatioOid, parentOrg);
                }
                return new ArrayList<String>();
            }
        });
    }

    /**
     * Filtteröidään käyttäjän rooleista ne, joihin käyttäjällä on haluttu oikeus, ja palautetaan kohdeorganisaatiot
     * Esim:
     *
     * // mille organisaatiolle käyttäjällä on vähintään read-oikeus koodistoon
     * String koodistoTargetOrganisaatioOid = getOrganisaatioTheUserHasPermissionTo("ROLE_APP_KOODISTO_READ", "ROLE_APP_KOODISTO_READ_UPDATE", "ROLE_APP_KOODISTO_CRUD");
     *
     * @param permissionCandidates
     * @return
     */
    public static String getOrganisaatioTheUserHasPermissionTo(String... permissionCandidates) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return getOrganisaatioTheUserHasPermissionTo(authentication, permissionCandidates);
    }

    public static String getOrganisaatioTheUserHasPermissionTo(Authentication authentication, String... permissionCandidates) {
        List<String> userRoles = toStringRoles(authentication.getAuthorities());
        return getOrganisaatioTheUserHasPermissionTo(userRoles, permissionCandidates);
    }

    private static List<String> toStringRoles(Collection<? extends GrantedAuthority> authorities) {
        List<String> userRoles = new ArrayList<String>();
        for (GrantedAuthority authority : authorities) {
            userRoles.add(authority.getAuthority());
        }
        return userRoles;
    }

    public static String getOrganisaatioTheUserHasPermissionTo(List<String> userRoles, String... permissionCandidates) {
        List<String> whatRoles = Arrays.asList(permissionCandidates);
        Set<String> orgs = new HashSet<String>();
        for (String userRole : userRoles) {
            if (!userRole.endsWith("READ") && !userRole.endsWith("READ_UPDATE") && !userRole.endsWith("CRUD")) { // only check user roles that end with org oid
                int x = userRole.lastIndexOf("_");
                if (x != -1) {
                    String rolePart = userRole.substring(0, x);
                    if (whatRoles.contains(rolePart)) {
                        String orgPart = userRole.substring(x + 1);
                        orgs.add(orgPart);
                    }
                }
            }
        }
        if (orgs.isEmpty()) {
            LOGGER.warn("user does not have role "+whatRoles+" to any organisaatios, userRoles: "+userRoles);
            return null;
        }
        if (orgs.size() > 1) {
            throw new RuntimeException("not supported: user has role "+whatRoles+" to more than 1 organisaatios: "+orgs); // ei tuetä tämmöistä keissiä ainakaan vielä
        }
        return orgs.iterator().next();
    }

}

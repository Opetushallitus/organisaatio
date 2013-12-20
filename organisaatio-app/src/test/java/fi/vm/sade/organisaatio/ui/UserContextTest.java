package fi.vm.sade.organisaatio.ui;

import java.util.*;

import fi.vm.sade.security.OidProvider;
import fi.vm.sade.security.OrganisationHierarchyAuthorizer;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.auth.OrganisaatioPermissionServiceImpl;
import fi.vm.sade.organisaatio.service.search.OrganisaatioSearchService;

import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Lists;

public class UserContextTest {

    private String ophOID="1.1";
    
    private OrganisaatioPermissionServiceImpl permissionService;
    
    private OrganisaatioSearchService searchService;
    private String userOid="USER";
    private String subOrgOid = "1.2.2004.2";

    @Before
    public void before(){
        permissionService = new OrganisaatioPermissionServiceImpl(ophOID);
        OidProvider oidProvider = Mockito.mock(OidProvider.class);
        Mockito.stub(oidProvider.getSelfAndParentOids(subOrgOid)).toReturn(
                Lists.newArrayList(ophOID, subOrgOid));
        Mockito.stub(oidProvider.getSelfAndParentOids(ophOID)).toReturn(
                Lists.newArrayList(ophOID));
        OrganisationHierarchyAuthorizer authorizer = new OrganisationHierarchyAuthorizer(oidProvider);
        permissionService.setAuthorizer(authorizer);
        ReflectionTestUtils.setField(permissionService, "rootOrgOid", ophOID);
        searchService = Mockito.mock(OrganisaatioSearchService.class);
        Mockito.stub(searchService.findByOidSet(Mockito.anySet())).toReturn(new ArrayList<OrganisaatioPerustieto>());
    }
    
    @Test
    public void test() {

        //non oph user
        setCurrentUser(userOid, Lists.newArrayList(getAuthority(permissionService.ROLE_CRUD, subOrgOid)));
//        setCurrentUser(userOid, new HashSet<String>(Arrays.asList(subOrgOid)), Arrays.asList(new AccessRight(subOrgOid, "CRUD", "ORGANISAATIOHALLINTA")));
        UserContext context = new UserContext(searchService, permissionService);
        Assert.assertEquals(1, context.getUserOrganisaatios().size());
        Assert.assertEquals(false, context.isOPHUser());
        Assert.assertEquals(true, context.isDoAutoSearch());
        Assert.assertEquals(true, context.isUseRestriction());
        context.setUseRestriction(false);
        Assert.assertEquals(false, context.isUseRestriction());
        
        //oph user
        setCurrentUser(userOid, Lists.newArrayList(getAuthority(permissionService.ROLE_CRUD, ophOID)));
        //setCurrentUser(userOid, new HashSet<String>(Arrays.asList(ophOID)), Arrays.asList(new AccessRight(ophOID, "CRUD", "ORGANISAATIOHALLINTA")));
        context = new UserContext(searchService, permissionService);
        Assert.assertEquals(true, context.isOPHUser());
        Assert.assertEquals(false, context.isDoAutoSearch());
        Assert.assertEquals(false, context.isUseRestriction());
    }
    
    List<GrantedAuthority> getAuthority(String appPermission, String oid) {
        GrantedAuthority orgAuthority = new SimpleGrantedAuthority(String.format("%s", appPermission));
        GrantedAuthority roleAuthority = new SimpleGrantedAuthority(String.format("%s_%s", appPermission, oid));
        return Lists.newArrayList(orgAuthority, roleAuthority);
    }
    
    static void setCurrentUser(final String oid, final List<GrantedAuthority> grantedAuthorities) {
        
        Authentication auth = new TestingAuthenticationToken(oid, null, grantedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

}

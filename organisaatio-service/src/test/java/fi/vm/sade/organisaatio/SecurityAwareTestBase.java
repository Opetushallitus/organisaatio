package fi.vm.sade.organisaatio;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.service.search.OrganisaatioSearchService;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.common.collect.Lists;

import fi.vm.sade.organisaatio.auth.OrganisaatioPermissionServiceImpl;
import fi.vm.sade.security.OidProvider;
import fi.vm.sade.security.OrganisationHierarchyAuthorizer;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

/**
 * By default executes tests as CRUD_USER, override before to customize
 */
public class SecurityAwareTestBase extends AbstractTransactionalJUnit4SpringContextTests {
    
    @Value("${root.organisaatio.oid}")
    protected String ophOid;
    
    @Autowired
    protected OrganisationHierarchyAuthorizer authorizer;
    
    
    
    private OidProvider oidProvider;
    @Autowired
    private OrganisaatioSearchService search;

    /**
     * Set permissions for current user, setup Mock oid provider
     */
    @Before
    public void before() {
        setCurrentUser("ophadmin", getAuthority("APP_" + OrganisaatioPermissionServiceImpl.ORGANISAATIOHALLINTA + "_CRUD", ophOid));
        OidProvider oidProvider = Mockito.mock(OidProvider.class);
//        Mockito.stub(oidProvider.getSelfAndParentOids(otherOrgOid)).toReturn(
//                Lists.newArrayList(ophOid, otherOrgOid));
//        Mockito.stub(oidProvider.getSelfAndParentOids(userOrgOid)).toReturn(
//                ophOid, userOrgOid));
        
        Mockito.stub(oidProvider.getSelfAndParentOids(ophOid)).toReturn(
                Lists.newArrayList(ophOid));

        Mockito.stub(oidProvider.getSelfAndParentOids("1.2.2004.2")).toReturn(
                Lists.newArrayList(ophOid, "1.2.2004.2"));
//        Mockito.stub(oidProvider.getSelfAndParentOids(Mockito.anyString())).toReturn(
//                Lists.newArrayList(ophOid));
        
        //save original oidprovider
        this.oidProvider = Whitebox.getInternalState(authorizer, "oidProvider");
        //set mock oidprovider
        Whitebox.setInternalState(authorizer, "oidProvider", oidProvider);
    }
    
    @After
    public void after(){
        //restore original oidprovider
        Whitebox.setInternalState(authorizer, "oidProvider", this.oidProvider);
    }
    
    protected final List<GrantedAuthority> getAuthority(String appPermission, String oid) {
        GrantedAuthority orgAuthority = new SimpleGrantedAuthority(String.format("%s", appPermission));
        GrantedAuthority roleAuthority = new SimpleGrantedAuthority(String.format("%s_%s", appPermission, oid));
        return Lists.newArrayList(orgAuthority, roleAuthority);
    }
    
    protected final void setCurrentUser(final String oid, final List<GrantedAuthority> grantedAuthorities) {
        Authentication auth = new TestingAuthenticationToken(oid, null, grantedAuthorities);
        setAuthentication(auth);
    }

    protected final void setAuthentication(Authentication auth) {
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
    
    
    protected void printCurrentUser(){
        System.out.println("oph-oid: " + ophOid);
        System.out.println("current user: " + SecurityContextHolder.getContext().getAuthentication());
    }


    protected void assertChildCountFromIndex(String oid, int expectedChildCount) {
        Set<String> oidSet = new HashSet<>();
        oidSet.add(oid);
        List<OrganisaatioPerustieto> list = search.findByOidSet(oidSet);
        Assert.assertEquals("Search result size should match for oid: " + oid, 1, list.size());
        OrganisaatioPerustieto fromIndex = list.get(0);
        Assert.assertEquals("Sub organisation count should match for oid: " + oid, expectedChildCount, fromIndex.getAliOrganisaatioMaara());
    }
}

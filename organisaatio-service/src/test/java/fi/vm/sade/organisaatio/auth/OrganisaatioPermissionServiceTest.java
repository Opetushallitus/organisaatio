package fi.vm.sade.organisaatio.auth;

import com.google.common.collect.Lists;
import fi.vm.sade.oid.service.OIDService;
import fi.vm.sade.oid.service.mock.OIDServiceMock;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.rajapinnat.ytj.api.YTJService;
import fi.vm.sade.rajapinnat.ytj.mock.YTJServiceMock;
import fi.vm.sade.security.OidProvider;
import fi.vm.sade.security.OrganisationHierarchyAuthorizer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;

//TODO combine permission service tests and make this a proper unit test
/*
@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest
@AutoConfigureTestDatabase
public class OrganisaatioPermissionServiceTest {

    @TestConfiguration
    static class OrganisaatioBusinessServiceImplTestContextConfiguration {

        @Bean
        @Primary
        public OrganisationHierarchyAuthorizer authorizer() {
            return new OrganisationHierarchyAuthorizer();
        }

        @Bean
        @Primary
        public OidProvider oidProvider() {
            return new OidProvider();
        }

        @Bean
        @Primary
        public YTJService ytjService() {
            return spy(new YTJServiceMock());
        }

        @Bean
        @Primary
        public OIDService oidService() {
            return new OIDServiceMock();
        }

    }

    @Autowired
    OrganisationHierarchyAuthorizer authorizer;

    public String rootOrgOid ="root";

    public static final String userOid = "nimi";
    public static final String userOrgOid = "1.2.2004.2";
    public static final String otherOrgOid = "1.2.2005.2";


    private OrganisaatioPermissionServiceImpl permissionService = new OrganisaatioPermissionServiceImpl(rootOrgOid);

    private static Organisaatio withParentOids(List<String> parentOids) {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setParentOids(parentOids);
        return organisaatio;
    }

    @Test
    public void testBasic() {
        OrganisaatioRepository organisaatioDaoMock = Mockito.mock(OrganisaatioRepository.class);
        Mockito.when(organisaatioDaoMock.customFindByOid(eq(otherOrgOid))).thenReturn(withParentOids(Collections.singletonList(rootOrgOid)));
        Mockito.when(organisaatioDaoMock.customFindByOid(userOrgOid)).thenReturn(withParentOids(Collections.singletonList(rootOrgOid)));
        Mockito.when(organisaatioDaoMock.customFindByOid(rootOrgOid)).thenReturn(withParentOids(Collections.emptyList()));
        permissionService.setAuthorizer(authorizer);


        //non oph user, outside own hierarchy
        setCurrentUser(userOid, Lists.newArrayList(getAuthority(
                permissionService.ROLE_CRUD, userOrgOid)));
        OrganisaatioRDTO org = getOrganisaatio(userOid, otherOrgOid, OrganisaatioTyyppi.KOULUTUSTOIMIJA);
        Assert.assertFalse(permissionService.userCanUpdateYTJ());
        Assert.assertFalse(permissionService.userCanCreateOrganisation(OrganisaatioContext.get(org)));
        Assert.assertFalse(permissionService.userCanDeleteOrganisation(OrganisaatioContext.get(org)));
        Assert.assertFalse(permissionService.userCanUpdateOrganisation(OrganisaatioContext.get(org)));
        Assert.assertFalse(permissionService.userCanMoveOrganisation(OrganisaatioContext.get(org)));

        //yhteystietojentyyppi
        Assert.assertFalse(permissionService.userCanEditYhteystietojenTyypit());
        Assert.assertFalse(permissionService.userCanDeleteYhteystietojenTyyppi());

        //alku, loppupäivä
        Assert.assertFalse(permissionService.userCanEditDates(OrganisaatioContext.get(org)));

        //non oph user inside own hierarchy
        setCurrentUser(userOid, Lists.newArrayList(getAuthority(
                permissionService.ROLE_CRUD, userOrgOid)));
        org = getOrganisaatio(userOid, null, userOrgOid, OrganisaatioTyyppi.OPPILAITOS);
        Assert.assertFalse(permissionService.userCanCreateOrganisation(OrganisaatioContext.get(org)));
        org = getOrganisaatio(userOid, null, userOrgOid, OrganisaatioTyyppi.TOIMIPISTE);
        Assert.assertTrue(permissionService.userCanCreateOrganisation(OrganisaatioContext.get(org)));
        org = getOrganisaatio(userOid, userOrgOid, OrganisaatioTyyppi.OPPILAITOS);
        Assert.assertFalse(permissionService.userCanDeleteOrganisation(OrganisaatioContext.get(org)));
        Assert.assertTrue(permissionService.userCanUpdateOrganisation(OrganisaatioContext.get(org)));
        Assert.assertTrue(permissionService.userCanMoveOrganisation(OrganisaatioContext.get(org)));

        assertEditOrganisation(OrganisaatioTyyppi.MUU_ORGANISAATIO, false);
        assertEditOrganisation(OrganisaatioTyyppi.TYOELAMAJARJESTO, false);
        assertEditOrganisation(OrganisaatioTyyppi.KOULUTUSTOIMIJA, true);
        assertEditOrganisation(OrganisaatioTyyppi.TOIMIPISTE, true);
        assertEditOrganisation(OrganisaatioTyyppi.OPPILAITOS, true);

        //sallitut tyypit
        Assert.assertFalse(permissionService.userCanCreateOrganisationOfType(OrganisaatioTyyppi.KOULUTUSTOIMIJA));
        Assert.assertFalse(permissionService.userCanCreateOrganisationOfType(OrganisaatioTyyppi.OPPILAITOS));
        Assert.assertFalse(permissionService.userCanCreateOrganisationOfType(OrganisaatioTyyppi.MUU_ORGANISAATIO));
        Assert.assertFalse(permissionService.userCanCreateOrganisationOfType(OrganisaatioTyyppi.TYOELAMAJARJESTO));
        Assert.assertTrue(permissionService.userCanCreateOrganisationOfType(OrganisaatioTyyppi.TOIMIPISTE));

        //nimen muokkaus
        Assert.assertFalse(permissionService.userCanEditName(OrganisaatioContext.get(getOrganisaatio("nimi", "oid", OrganisaatioTyyppi.KOULUTUSTOIMIJA))));
        Assert.assertFalse(permissionService.userCanEditName(OrganisaatioContext.get(getOrganisaatio("nimi", "oid", OrganisaatioTyyppi.OPPILAITOS))));
        Assert.assertFalse(permissionService.userCanEditName(OrganisaatioContext.get(getOrganisaatio("nimi", "oid", OrganisaatioTyyppi.MUU_ORGANISAATIO))));
        Assert.assertFalse(permissionService.userCanEditName(OrganisaatioContext.get(getOrganisaatio("nimi", "oid", OrganisaatioTyyppi.TYOELAMAJARJESTO))));
        Assert.assertTrue(permissionService.userCanEditName(OrganisaatioContext.get(getOrganisaatio("nimi", "oid", OrganisaatioTyyppi.TOIMIPISTE))));

        //alku, loppupäivä
        Assert.assertFalse(permissionService.userCanEditDates(OrganisaatioContext.get(org)));

        //non oph user, opetuspiste
        setCurrentUser(userOid, Lists.newArrayList(getAuthority(permissionService.ROLE_CRUD,userOrgOid)));
        org = getOrganisaatio(userOid, userOrgOid, OrganisaatioTyyppi.TOIMIPISTE);
        //alku, loppupäivä
        Assert.assertTrue(permissionService.userCanEditDates(OrganisaatioContext.get(org)));

        //oph CRUD user
        setCurrentUser(userOid, Lists.newArrayList(getAuthority(permissionService.ROLE_CRUD,  rootOrgOid)));

        //can edit all types
        assertEditOrganisation(OrganisaatioTyyppi.MUU_ORGANISAATIO, true);
        assertEditOrganisation(OrganisaatioTyyppi.TYOELAMAJARJESTO, true);
        assertEditOrganisation(OrganisaatioTyyppi.KOULUTUSTOIMIJA, true);
        assertEditOrganisation(OrganisaatioTyyppi.TOIMIPISTE, true);
        assertEditOrganisation(OrganisaatioTyyppi.OPPILAITOS, true);


        //yhteystietojentyyppi
        Assert.assertTrue(permissionService.userCanEditYhteystietojenTyypit());
        Assert.assertTrue(permissionService.userCanDeleteYhteystietojenTyyppi());

        //alku, loppupäivä
        Assert.assertTrue(permissionService.userCanEditDates(OrganisaatioContext.get(org)));

        //sallitut tyyppit
        Assert.assertTrue(permissionService.userCanCreateOrganisationOfType(OrganisaatioTyyppi.KOULUTUSTOIMIJA));
        Assert.assertTrue(permissionService.userCanCreateOrganisationOfType(OrganisaatioTyyppi.OPPILAITOS));
        Assert.assertTrue(permissionService.userCanCreateOrganisationOfType(OrganisaatioTyyppi.MUU_ORGANISAATIO));
        Assert.assertTrue(permissionService.userCanCreateOrganisationOfType(OrganisaatioTyyppi.TYOELAMAJARJESTO));
        Assert.assertTrue(permissionService.userCanCreateOrganisationOfType(OrganisaatioTyyppi.TOIMIPISTE));

        //nimen muokkaus
        Assert.assertTrue(permissionService.userCanEditName(OrganisaatioContext.get(getOrganisaatio("nimi", "oid", OrganisaatioTyyppi.KOULUTUSTOIMIJA))));
        Assert.assertTrue(permissionService.userCanEditName(OrganisaatioContext.get(getOrganisaatio("nimi", "oid", OrganisaatioTyyppi.OPPILAITOS))));
        Assert.assertTrue(permissionService.userCanEditName(OrganisaatioContext.get(getOrganisaatio("nimi", "oid", OrganisaatioTyyppi.MUU_ORGANISAATIO))));
        Assert.assertTrue(permissionService.userCanEditName(OrganisaatioContext.get(getOrganisaatio("nimi", "oid", OrganisaatioTyyppi.TYOELAMAJARJESTO))));
        Assert.assertTrue(permissionService.userCanEditName(OrganisaatioContext.get(getOrganisaatio("nimi", "oid", OrganisaatioTyyppi.TOIMIPISTE))));

        //oph RU user
        setCurrentUser(userOid, Lists.newArrayList(getAuthority(permissionService.ROLE_RU,  rootOrgOid)));

        //yhteystietojentyyppi
        Assert.assertTrue(permissionService.userCanEditYhteystietojenTyypit());
        Assert.assertFalse(permissionService.userCanDeleteYhteystietojenTyyppi());

        //alku, loppupäivä
        Assert.assertTrue(permissionService.userCanEditDates(OrganisaatioContext.get(org)));

        //sallitut tyyppit
        Assert.assertFalse(permissionService.userCanCreateOrganisationOfType(OrganisaatioTyyppi.KOULUTUSTOIMIJA));
        Assert.assertFalse(permissionService.userCanCreateOrganisationOfType(OrganisaatioTyyppi.OPPILAITOS));
        Assert.assertFalse(permissionService.userCanCreateOrganisationOfType(OrganisaatioTyyppi.MUU_ORGANISAATIO));
        Assert.assertFalse(permissionService.userCanCreateOrganisationOfType(OrganisaatioTyyppi.TYOELAMAJARJESTO));
        Assert.assertFalse(permissionService.userCanCreateOrganisationOfType(OrganisaatioTyyppi.TOIMIPISTE));

        //nimen muokkaus
        Assert.assertTrue(permissionService.userCanEditName(OrganisaatioContext.get(getOrganisaatio("nimi", "oid", OrganisaatioTyyppi.KOULUTUSTOIMIJA))));
        Assert.assertTrue(permissionService.userCanEditName(OrganisaatioContext.get(getOrganisaatio("nimi", "oid", OrganisaatioTyyppi.OPPILAITOS))));
        Assert.assertTrue(permissionService.userCanEditName(OrganisaatioContext.get(getOrganisaatio("nimi", "oid", OrganisaatioTyyppi.MUU_ORGANISAATIO))));
        Assert.assertTrue(permissionService.userCanEditName(OrganisaatioContext.get(getOrganisaatio("nimi", "oid", OrganisaatioTyyppi.TYOELAMAJARJESTO))));
        Assert.assertTrue(permissionService.userCanEditName(OrganisaatioContext.get(getOrganisaatio("nimi", "oid", OrganisaatioTyyppi.TOIMIPISTE))));

        assertEditOrganisation(OrganisaatioTyyppi.MUU_ORGANISAATIO, false);
        assertEditOrganisation(OrganisaatioTyyppi.TYOELAMAJARJESTO, false);
        assertEditOrganisation(OrganisaatioTyyppi.KOULUTUSTOIMIJA, true);
        assertEditOrganisation(OrganisaatioTyyppi.TOIMIPISTE, true);
        assertEditOrganisation(OrganisaatioTyyppi.OPPILAITOS, true);

    }


    private void assertEditOrganisation(OrganisaatioTyyppi tyyppi, boolean expectedResult) {
        OrganisaatioRDTO org = getOrganisaatio(userOid, userOrgOid, tyyppi);
        Assert.assertEquals(expectedResult, permissionService.userCanUpdateOrganisation(OrganisaatioContext.get(org)));
    }

    private OrganisaatioRDTO getOrganisaatio(String nimi, String oid, OrganisaatioTyyppi tyyppi) {
        return getOrganisaatio(nimi, oid, null, tyyppi);
    }

    private OrganisaatioRDTO getOrganisaatio(String nimi, String oid, String parentOid, OrganisaatioTyyppi tyyppi) {
        OrganisaatioRDTO org = new OrganisaatioRDTO();
        Map<String,String> nimiMap = new HashMap<>();
        nimiMap.put("fi", nimi);
        org.setNimi(nimiMap);
        org.getTyypit().add(tyyppi.value());
        org.setOid(oid);
        org.setParentOid(parentOid);
        return org;
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

 */

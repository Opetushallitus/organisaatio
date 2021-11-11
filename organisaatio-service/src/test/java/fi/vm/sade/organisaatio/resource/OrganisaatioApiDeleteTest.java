package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioStatus;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.business.impl.OrganisaatioTarjonta;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.util.OrganisaatioRDTOTestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@Transactional
@ComponentScan(basePackages = "fi.vm.sade.organisaatio")
@SpringBootTest
@AutoConfigureTestDatabase
class OrganisaatioApiDeleteTest {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @TestConfiguration
    public static class TestConfig {
        @Bean
        @Primary
        public OrganisaatioTarjonta tarjontaMock() {
            return Mockito.mock(OrganisaatioTarjonta.class);
        }

    }

    @Autowired
    OrganisaatioTarjonta tarjontaMock;

    @Autowired
    private OrganisaatioApi resource;


    @Test
    void testDeleteOidNoAuth() {
        Assertions.assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> resource.deleteOrganisaatio("123"), "AuthenticationCredentialsNotFoundException was expected");
    }

    @Test
    @WithMockUser(roles = {"APP_ORGANISAATIOHALLINTA", "APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.24.00000000001"})
    void testDeleteOidNotFound() {
        OrganisaatioResourceException ex = Assertions.assertThrows(OrganisaatioResourceException.class,
                () -> resource.deleteOrganisaatio("1.2.246.562.24.00000000000"), "OrganisaatioResourceException was expected");
        assertEquals(HttpStatus.NOT_FOUND, ex.getResponseEntity().getStatusCode());
    }

    @Test
    @WithMockUser(roles = {"APP_ORGANISAATIOHALLINTA", "APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.24.00000000001"})
    void testDeleteAllowed() {
        OrganisaatioRDTOV4 parent = createOrganisaatio("Parent", null);
        OrganisaatioRDTOV4 child = createOrganisaatio("Child", parent);
        assertEquals(OrganisaatioStatus.AKTIIVINEN.name(), child.getStatus());
        resource.deleteOrganisaatio(child.getOid());
        OrganisaatioRDTOV4 deleted = resource.getOrganisaatioByOID(child.getOid(), false);
        assertEquals(OrganisaatioStatus.POISTETTU.name(), deleted.getStatus());
    }

    @Test
    @WithMockUser(roles = {"APP_ORGANISAATIOHALLINTA", "APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.24.00000000001", "APP_ORGANISAATIOHALLINTA_CRUD"})
    void testCannotDeleteWhenTarjonta() {
        OrganisaatioRDTOV4 a = createOrganisaatio("A", null);
        OrganisaatioRDTOV4 ad = createOrganisaatio("AD", a);
        when(tarjontaMock.alkaviaKoulutuksia(ad.getOid())).thenReturn(true);
        String oid = ad.getOid();
        OrganisaatioResourceException ex = Assertions.assertThrows(OrganisaatioResourceException.class,
                () -> resource.deleteOrganisaatio(oid), "OrganisaatioResourceException was expected");
        assertEquals(HttpStatus.BAD_REQUEST, ex.getResponseEntity().getStatusCode());
    }

    @Test
    @WithMockUser(roles = {"APP_ORGANISAATIOHALLINTA", "APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.24.00000000001"})
    void testDeleteOidNotAllowed() {
        OrganisaatioRDTOV4 parent = createOrganisaatio("Parent", null);
        createOrganisaatio("Child", parent);
        String parentOid = parent.getOid();
        OrganisaatioResourceException ex = Assertions.assertThrows(OrganisaatioResourceException.class,
                () -> resource.deleteOrganisaatio(parentOid), "OrganisaatioResourceException was expected");
        assertEquals(HttpStatus.BAD_REQUEST, ex.getResponseEntity().getStatusCode());
    }

    private OrganisaatioRDTOV4 createOrganisaatio(String nimi, OrganisaatioRDTOV4 parent) {
        LOG.info("createOrganisaatio({})", nimi);
        OrganisaatioRDTOV4 o = OrganisaatioRDTOTestUtil.createOrganisaatioV4(nimi, OrganisaatioTyyppi.MUU_ORGANISAATIO.koodiValue(), parent, true);
        return resource.newOrganisaatio(o).getOrganisaatio();
    }
}

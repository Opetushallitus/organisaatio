package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioStatus;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioCrudException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioNotFoundException;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.util.OrganisaatioRDTOTestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest
class OrganisaatioApiDeleteTest {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

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
        OrganisaatioNotFoundException ex = Assertions.assertThrows(OrganisaatioNotFoundException.class,
                () -> resource.deleteOrganisaatio("1.2.246.562.24.00000000000"), "OrganisaatioNotFoundException was expected");
        assertEquals("organisaatio.exception.organisaatio.not.found", ex.getErrorKey());
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
    @WithMockUser(roles = {"APP_ORGANISAATIOHALLINTA", "APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.24.00000000001"})
    void testDeleteOidNotAllowed() {
        OrganisaatioRDTOV4 parent = createOrganisaatio("Parent", null);
        createOrganisaatio("Child", parent);
        String parentOid = parent.getOid();
        Assertions.assertThrows(OrganisaatioCrudException.class,
                () -> resource.deleteOrganisaatio(parentOid), "OrganisaatioResourceException was expected");
    }

    private OrganisaatioRDTOV4 createOrganisaatio(String nimi, OrganisaatioRDTOV4 parent) {
        LOG.info("createOrganisaatio({})", nimi);
        OrganisaatioRDTOV4 o = OrganisaatioRDTOTestUtil.createOrganisaatioV4(nimi, OrganisaatioTyyppi.MUU_ORGANISAATIO, null, parent != null ? parent.getOid() : null);
        return resource.newOrganisaatio(o).getOrganisaatio();
    }
}

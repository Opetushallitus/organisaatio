package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.dto.v4.ResultRDTOV4;
import fi.vm.sade.organisaatio.util.OrganisaatioRDTOTestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@Sql("/data/truncate_tables.sql")
class OrganisaatioApiAuthenticatedTest {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrganisaatioApi resource;

    @Test
    @WithMockUser(roles = {"APP_ORGANISAATIOHALLINTA", "APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.24.00000000001", "APP_ORGANISAATIOHALLINTA_CRUD"})
    void testChangeParentOid() {

        // Change parent from root -> root2
        OrganisaatioRDTOV4 oldParent = createOrganisaatio("ChangeParentOid-OldParent", null);
        OrganisaatioRDTOV4 newParent = createOrganisaatio("ChangeParentOid-NewParent", null);
        OrganisaatioRDTOV4 child = createOrganisaatio("ChangeParentOid-Child", oldParent);
        createOrganisaatio("ChangeParentOid-GrandChild1", child);
        createOrganisaatio("ChangeParentOid-GrandChild2", child);
        child.setParentOid(newParent.getOid());
        ResultRDTOV4 updated = resource.updateOrganisaatio(child.getOid(), child);
        assertEquals(newParent.getOid(), updated.getOrganisaatio().getParentOid(), "Parent oid should match!");
        LOG.info("Path: {}", updated.getOrganisaatio().getParentOidPath());
        List<OrganisaatioRDTOV4> children = resource.children(updated.getOrganisaatio().getOid(), false);
        assertEquals(2, children.size(), "Children count should match!");
        for (OrganisaatioRDTOV4 childItem : children) {
            LOG.info("Child oid path: {}", childItem.getParentOidPath());
            assertEquals(updated.getOrganisaatio().getParentOidPath() + childItem.getParentOid() + "|",
                    childItem.getParentOidPath(), "Child parent oid path should match!");
        }
    }

    @Test
    @WithAnonymousUser
    void testChangeParentOidNoAuth() {
        Assertions.assertThrows(AccessDeniedException.class, () -> resource.updateOrganisaatio("123", null), "Exception was expected");
    }

    private OrganisaatioRDTOV4 createOrganisaatio(String nimi, OrganisaatioRDTOV4 parent) {
        LOG.info("createOrganisaatio({})", nimi);
        OrganisaatioRDTOV4 o = OrganisaatioRDTOTestUtil.createOrganisaatioV4(nimi, OrganisaatioTyyppi.MUU_ORGANISAATIO, null, parent != null ? parent.getOid() : null);
        return resource.newOrganisaatio(o).getOrganisaatio();
    }
}

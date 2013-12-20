package fi.vm.sade.organisaatio.dao;

import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.model.dto.OrgStructure;
import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.Assert.assertEquals;

@ContextConfiguration(locations = {"classpath:spring/test-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class OrganisaatioDAOImplTest {

    Random r = new Random(0);

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioDAOImplTest.class);

    @Autowired
    OrganisaatioDAOImpl organisaatioDAO;

    @Autowired
    OrganisaatioSuhdeDAOImpl organisaatioSuhdeDAO;

    @Autowired
    OrganisaatioService organisaatioService;

    @Test
    public void doTest() {
        LOG.info("doTest()...");
        Organisaatio a = createOrganisaatio("A", null, false, null, null);
        Organisaatio b = createOrganisaatio("B", a, false, generateParentOidPath(a), generateParentIdPath(a));
        Organisaatio c = createOrganisaatio("C", b, false, generateParentOidPath(b), generateParentIdPath(b));
        Organisaatio d = createOrganisaatio("D", c, false, generateParentOidPath(c), generateParentIdPath(c));
        Organisaatio e = createOrganisaatio("E", d, false, generateParentOidPath(d), generateParentIdPath(d));

        final String oid = e.getOid();
        List<String> oids = organisaatioDAO.findParentOidsTo(oid);
        Assert.assertEquals(5, oids.size());
        Assert.assertEquals(a.getOid(), oids.get(0));
        Assert.assertEquals(b.getOid(), oids.get(1));
        Assert.assertEquals(c.getOid(), oids.get(2));
        Assert.assertEquals(d.getOid(), oids.get(3));
        Assert.assertEquals(e.getOid(), oids.get(4));
    }

    @Test
    public void parentTest() {
        Organisaatio a = createOrganisaatio("A", null, false, null, null);
        Organisaatio b = createOrganisaatio("B", a, false, generateParentOidPath(a), generateParentIdPath(a));

        b = organisaatioDAO.findByOid(b.getOid());

        Assert.assertEquals(a.getOid(), b.getParent().getOid());
    }

    @Test
    public void testGetOrganizationStructure() {
        Organisaatio a = createOrganisaatio("A", null, false, null, null);
        Organisaatio b = createOrganisaatio("B", a, false, generateParentOidPath(a), generateParentIdPath(a));
        Organisaatio c = createOrganisaatio("C", a, false, generateParentOidPath(a), generateParentIdPath(a));
        Organisaatio d = createOrganisaatio("D", b, false, generateParentOidPath(b), generateParentIdPath(b));
        Organisaatio e = createOrganisaatio("E", c, false, generateParentOidPath(c), generateParentIdPath(c));
        Organisaatio f = createOrganisaatio("F", a, true, generateParentOidPath(a), generateParentIdPath(a));
        Organisaatio g = createOrganisaatio("G", e, true, generateParentOidPath(e), generateParentIdPath(e));

        List<OrgStructure> structure = organisaatioDAO.getOrganizationStructure(Arrays.asList(c.getOid()));
        assertEquals(3, structure.size());
    }

    @Test
    public void findChildrenTest() {
        Organisaatio a = createOrganisaatio("A", null, false, null, null);
        Organisaatio b = createOrganisaatio("B", a, false, generateParentOidPath(a), generateParentIdPath(a));
        Organisaatio c = createOrganisaatio("C", a, false, generateParentOidPath(a), generateParentIdPath(a));
        Organisaatio d = createOrganisaatio("D", b, false, generateParentOidPath(b), generateParentIdPath(b));
        Organisaatio e = createOrganisaatio("E", c, false, generateParentOidPath(c), generateParentIdPath(c));
        Organisaatio f = createOrganisaatio("F", a, true, generateParentOidPath(a), generateParentIdPath(a));
        Organisaatio g = createOrganisaatio("G", e, true, generateParentOidPath(e), generateParentIdPath(e));

        List<Organisaatio> children = this.organisaatioDAO.findChildren(a.getOid(), false, true);
        Assert.assertTrue(children.size() == 2);
        Assert.assertTrue(organisaatiotContain(children, b));
        Assert.assertTrue(organisaatiotContain(children, c));
        Assert.assertTrue(!organisaatiotContain(children, f));

        children = organisaatioDAO.findChildren(c.getOid(), false, true);
        Assert.assertTrue(children.size() == 1);
        Assert.assertTrue(organisaatiotContain(children, e));

        children = organisaatioDAO.findChildren(e.getOid(), false, true);
        Assert.assertTrue(children.size() == 0);

        children = organisaatioDAO.findChildren(d.getOid(), false, true);
        Assert.assertTrue(children.size() == 0);

        children = this.organisaatioDAO.findChildren(a.getOid(), true, true);
        Assert.assertTrue(children.size() == 3);
        Assert.assertTrue(organisaatiotContain(children, f));

    }

    private String generateParentOidPath(Organisaatio parent) {
        if (parent == null) {
            return null;
        }
        return (!StringUtils.isEmpty(parent.getParentOidPath()))
                ? parent.getParentOidPath() + parent.getOid() + "|"
                : "|" + parent.getOid() + "|";
    }

    private String generateParentIdPath(Organisaatio parent) {
        if (parent == null) {
            return null;
        }
        return (!StringUtils.isEmpty(parent.getParentIdPath()))
                ? parent.getParentIdPath() + parent.getId() + "|"
                : "|" + parent.getId() + "|";
    }


    private boolean organisaatiotContain(List<Organisaatio> organisaatiot, Organisaatio o) {
        for (Organisaatio curOrg : organisaatiot) {
            if (curOrg.getId() == o.getId()) {
                return true;
            }
        }
        return false;
    }

    private Organisaatio createOrganisaatio(String nimi, Organisaatio parent, boolean isPoistettu, String parentOidPath, String parentIdPath) {
        LOG.info("createOrganisaatio({})", nimi);

        Organisaatio o = new Organisaatio();


        o.setOid(Long.toString(r.nextLong()));
        o.setOrganisaatioPoistettu(isPoistettu);

        o.setNimi(new MonikielinenTeksti());
        o.getNimi().addString("FI", nimi);

        List<Yhteystieto> oYhteystiedot = new ArrayList<Yhteystieto>();
        oYhteystiedot.add(createOsoite());
        o.setYhteystiedot(oYhteystiedot);

        o = organisaatioDAO.insert(o);

        if (parent != null) {
            OrganisaatioSuhde suhde = organisaatioSuhdeDAO.addChild(parent.getId(), o.getId(), null, null);
            o.getParentSuhteet().add(suhde);
            organisaatioSuhdeDAO.getEntityManager().flush();
            LOG.info("YHTEYSTIEDOT: " + o.getYhteystiedot().size());
            LOG.info("PARENTS: " + o.getParentSuhteet().size());
            o.setParentOidPath(parentOidPath);
            o.setParentIdPath(parentIdPath);
        }

        return o;
    }

    private Osoite createOsoite() {
        Osoite o = new Osoite(Osoite.TYYPPI_KAYNTIOSOITE, "katu", "0000", "Helsinki", UUID.randomUUID().toString());
        return o;
    }

    private void setParentPaths(Organisaatio o) {
        String parentOidpath = "";
        String parentIdPath = "";
        for (Organisaatio curOrg : this.organisaatioDAO.findParentsTo(o.getOid())) {
            parentOidpath += "|" + curOrg.getOid();
            parentIdPath += "|" + curOrg.getId();
        }
        parentOidpath += "|";
        parentIdPath += "|";
        o.setParentOidPath(parentOidpath);
        o.setParentIdPath(parentIdPath);
    }

}

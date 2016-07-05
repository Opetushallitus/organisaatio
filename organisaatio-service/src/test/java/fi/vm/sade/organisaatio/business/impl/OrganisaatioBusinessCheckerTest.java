package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.SecurityAwareTestBase;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioMuokkausTiedotDTO;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

/**
 * Tests for {@link fi.vm.sade.organisaatio.business.impl.OrganisaatioBusinessChecker} class.
 */
@ContextConfiguration(locations = {"classpath:spring/test-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles({"embedded-solr"})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OrganisaatioBusinessCheckerTest extends SecurityAwareTestBase {

    @Autowired
    private OrganisaatioBusinessChecker checker;

    String oid1 = "1.2.2004.1";
    Organisaatio organisaatio = new Organisaatio();
    OrganisaatioMuokkausTiedotDTO muokatutTiedot = new OrganisaatioMuokkausTiedotDTO(); // oid, alkupvm, loppupvm
    HashMap<String, OrganisaatioMuokkausTiedotDTO> data = new HashMap<>();

    Organisaatio parent = new Organisaatio();
    Organisaatio child1 = new Organisaatio();
    Organisaatio root = new Organisaatio();

    @Before
    public void setUp() {
        organisaatio.setOid(oid1);
        organisaatio.setAlkuPvm(checker.getMIN_DATE().getTime());

        parent.setOid("1234.5");
        OrganisaatioSuhde os1 = new OrganisaatioSuhde();
        os1.setParent(parent);
        os1.setChild(child1);
        os1.setSuhdeTyyppi(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.HISTORIA);
        List<OrganisaatioSuhde> children = new ArrayList<>();
        children.add(os1);
        root.setOid("1234.4");
        OrganisaatioSuhde os3 = new OrganisaatioSuhde();
        os3.setSuhdeTyyppi(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.HISTORIA);
        os3.setAlkuPvm(new GregorianCalendar(2016, 0, 1).getTime());
        os3.setChild(parent);
        os3.setParent(root);
        List<OrganisaatioSuhde> parentSuhde = new ArrayList<>();
        parentSuhde.add(os3);
        ReflectionTestUtils.setField(parent, "childSuhteet", children);
        ReflectionTestUtils.setField(parent, "parentSuhteet", parentSuhde);
    }

    @Test
    public void testRootLevelOrgWithMinDate() throws Exception {
        muokatutTiedot.setAlkuPvm(checker.getMIN_DATE().getTime());
        data.put(oid1, muokatutTiedot);
        Assert.assertEquals("", checker.checkPvmConstraints(organisaatio, null, null, data));
    }

    @Test
    public void testRootLevelOrgBeforeMinDate() throws Exception {
        muokatutTiedot.setAlkuPvm(new GregorianCalendar(1800, 0, 1).getTime());
        data.put(oid1, muokatutTiedot);
        Assert.assertFalse("".equals(checker.checkPvmConstraints(organisaatio, null, null, data)));
    }

    @Test
    public void testRootLevelOrgEndsWithMaxDate() throws Exception {
        muokatutTiedot.setLoppuPvm(checker.getMAX_DATE().toDate());
        data.put(oid1, muokatutTiedot);
        Assert.assertEquals("", checker.checkPvmConstraints(organisaatio, null, null, data));
    }

    @Test
    public void testRootLevelOrgEndsAfterMaxDate() throws Exception {
        muokatutTiedot.setLoppuPvm(new GregorianCalendar(2500, 0, 1).getTime());
        data.put(oid1, muokatutTiedot);
        Assert.assertFalse("".equals(checker.checkPvmConstraints(organisaatio, null, null, data)));
    }

    @Test
    public void testRootLevelOrgEndsBeforeStartDate() throws Exception {
        muokatutTiedot.setAlkuPvm(new GregorianCalendar(2500, 0, 1).getTime());
        muokatutTiedot.setLoppuPvm(new GregorianCalendar(2017, 0, 1).getTime());
        data.put(oid1, muokatutTiedot);
        Assert.assertFalse("".equals(checker.checkPvmConstraints(organisaatio, null, null, data)));
    }

    @Test
    public void testOrgWithNoChildrenValidAlkupvm() {
        muokatutTiedot.setAlkuPvm(new GregorianCalendar(2016, 0, 1).getTime());
        ReflectionTestUtils.setField(parent, "childSuhteet", new ArrayList());
        data.put(parent.getOid(), muokatutTiedot);
        Assert.assertTrue("".equals(checker.checkPvmConstraints(parent, null, null, data)));
    }

    @Test
    public void testNullDates() {
        muokatutTiedot.setAlkuPvm(null);
        muokatutTiedot.setLoppuPvm(null);
        data.put(parent.getOid(), muokatutTiedot);
        Assert.assertTrue("".equals(checker.checkPvmConstraints(parent, null, null, data)));
    }

    @Test
    public void parentWithEndDateChildWithout() {
        // end date in modified data
        muokatutTiedot.setLoppuPvm(new GregorianCalendar(2017, 0, 1).getTime());
        data.put(parent.getOid(), muokatutTiedot);
        // here it differs from OrganisationDateValidator
        Assert.assertFalse("".equals(checker.checkPvmConstraints(parent, null, null, data)));
        // no side effects, in comparison to OrganisationDateValidator
        Assert.assertEquals(parent.getLakkautusPvm(), child1.getLakkautusPvm());
    }

    @Test
    public void parentWithStartDateChildWithout() {
        parent.setAlkuPvm(new GregorianCalendar(1980, 0, 1).getTime());
        data.put(parent.getOid(), muokatutTiedot);
        Assert.assertTrue("".equals(checker.checkPvmConstraints(parent, null, null, data)));
    }

    @Test
    public void childWithStartDateParentWithout() {
        parent.setAlkuPvm(null);
        child1.setAlkuPvm(new GregorianCalendar(1980, 0, 1).getTime());
        data.put(parent.getOid(), muokatutTiedot);
        Assert.assertTrue("".equals(checker.checkPvmConstraints(parent, null, null, data)));
    }

    @Test
    public void sameStartDates() {
        child1.setAlkuPvm(new GregorianCalendar(1980, 0, 1).getTime());
        parent.setAlkuPvm(new GregorianCalendar(1980, 0, 1).getTime());
        data.put(parent.getOid(), muokatutTiedot);
        Assert.assertTrue("".equals(checker.checkPvmConstraints(parent, null, null, data)));
    }

    @Test
    public void olderChild() {
        child1.setAlkuPvm(new GregorianCalendar(1970, 0, 1).getTime());
        // method validates modified info, so doesn't matter what value parent has
        muokatutTiedot.setAlkuPvm(new GregorianCalendar(1980, 0, 1).getTime());
        data.put(parent.getOid(), muokatutTiedot);
        // TODO this should change
        Assert.assertFalse("".equals(checker.checkPvmConstraints(parent, null, null, data)));
    }

    @Test
    public void youngerChild() {
        child1.setAlkuPvm(new GregorianCalendar(1990, 0, 1).getTime());
        // method validates modified info, so doesn't matter what value parent has
        muokatutTiedot.setAlkuPvm(new GregorianCalendar(1980, 0, 1).getTime());
        data.put(parent.getOid(), muokatutTiedot);
        Assert.assertTrue("".equals(checker.checkPvmConstraints(parent, null, null, data)));
    }

    @Test
    public void childWithLaterEndDate() {
        // end date in modified data
        muokatutTiedot.setLoppuPvm(new GregorianCalendar(2017, 0, 1).getTime());
        child1.setLakkautusPvm(new GregorianCalendar(2018, 0, 1).getTime());
        data.put(parent.getOid(), muokatutTiedot);
        Assert.assertFalse("".equals(checker.checkPvmConstraints(parent, null, null, data)));
    }

    @Test
    public void childWithEndDate() {
        child1.setLakkautusPvm(new GregorianCalendar(2018, 0, 1).getTime());
        data.put(parent.getOid(), muokatutTiedot);
        Assert.assertTrue("".equals(checker.checkPvmConstraints(parent, null, null, data)));
    }
}

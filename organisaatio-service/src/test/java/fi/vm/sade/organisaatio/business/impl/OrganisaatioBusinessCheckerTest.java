package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.SecurityAwareTestBase;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioMuokkausTiedotDTO;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Tests for {@link OrganisaatioBusinessChecker} class.
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@SpringBootTest
@AutoConfigureTestDatabase
public class OrganisaatioBusinessCheckerTest extends SecurityAwareTestBase {

    @Autowired
    private OrganisaatioBusinessChecker checker;

    private final String oid1 = "1.2.2004.1";
    private final Organisaatio organisaatio = new Organisaatio();
    private final OrganisaatioMuokkausTiedotDTO muokatutTiedot = new OrganisaatioMuokkausTiedotDTO(); // oid, alkupvm, loppupvm
    private final HashMap<String, OrganisaatioMuokkausTiedotDTO> data = new HashMap<>();

    private final Organisaatio parent = new Organisaatio();
    private final Organisaatio child1 = new Organisaatio();
    private final Organisaatio root = new Organisaatio();

    @BeforeEach
    public void setUp() {
        organisaatio.setOid(oid1);
        organisaatio.setAlkuPvm(checker.getMIN_DATE().getTime());

        parent.setOid("1234.5");
        OrganisaatioSuhde os1 = new OrganisaatioSuhde();
        os1.setParent(parent);
        os1.setChild(child1);
        os1.setSuhdeTyyppi(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.HISTORIA);
        Set<OrganisaatioSuhde> children = new HashSet<>();
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
        assertEquals("", checker.checkPvmConstraints(organisaatio, null, null, data));
    }

    @Test
    public void testRootLevelOrgBeforeMinDate() throws Exception {
        muokatutTiedot.setAlkuPvm(new GregorianCalendar(1800, 0, 1).getTime());
        data.put(oid1, muokatutTiedot);
        assertNotEquals(checker.checkPvmConstraints(organisaatio, null, null, data), "");
    }

    @Test
    public void testRootLevelOrgEndsWithMaxDate() throws Exception {
        muokatutTiedot.setLoppuPvm(checker.getMAX_DATE().toDate());
        data.put(oid1, muokatutTiedot);
        assertEquals("", checker.checkPvmConstraints(organisaatio, null, null, data));
    }

    @Test
    public void testRootLevelOrgEndsAfterMaxDate() throws Exception {
        muokatutTiedot.setLoppuPvm(new GregorianCalendar(2500, 0, 1).getTime());
        data.put(oid1, muokatutTiedot);
        assertNotEquals(checker.checkPvmConstraints(organisaatio, null, null, data), "");
    }

    @Test
    public void testRootLevelOrgEndsBeforeStartDate() throws Exception {
        muokatutTiedot.setAlkuPvm(new GregorianCalendar(2500, 0, 1).getTime());
        muokatutTiedot.setLoppuPvm(new GregorianCalendar(2017, 0, 1).getTime());
        data.put(oid1, muokatutTiedot);
        assertNotEquals(checker.checkPvmConstraints(organisaatio, null, null, data), "");
    }

    @Test
    public void testOrgWithNoChildrenValidAlkupvm() {
        muokatutTiedot.setAlkuPvm(new GregorianCalendar(2016, 0, 1).getTime());
        ReflectionTestUtils.setField(parent, "childSuhteet", new HashSet<>());
        data.put(parent.getOid(), muokatutTiedot);
        assertEquals(checker.checkPvmConstraints(parent, null, null, data), "");
    }

    @Test
    public void testNullDates() {
        muokatutTiedot.setAlkuPvm(null);
        muokatutTiedot.setLoppuPvm(null);
        data.put(parent.getOid(), muokatutTiedot);
        assertEquals(checker.checkPvmConstraints(parent, null, null, data), "");
    }

    @Test
    public void parentWithEndDateChildWithout() {
        // end date in modified data
        muokatutTiedot.setLoppuPvm(new GregorianCalendar(2017, 0, 1).getTime());
        data.put(parent.getOid(), muokatutTiedot);
        // here it differs from OrganisationDateValidator
        assertNotEquals(checker.checkPvmConstraints(parent, null, null, data), "");
        // no side effects, in comparison to OrganisationDateValidator
        assertEquals(parent.getLakkautusPvm(), child1.getLakkautusPvm());
    }

    @Test
    public void parentWithStartDateChildWithout() {
        parent.setAlkuPvm(new GregorianCalendar(1980, 0, 1).getTime());
        data.put(parent.getOid(), muokatutTiedot);
        assertEquals(checker.checkPvmConstraints(parent, null, null, data), "");
    }

    @Test
    public void childWithStartDateParentWithout() {
        parent.setAlkuPvm(null);
        child1.setAlkuPvm(new GregorianCalendar(1980, 0, 1).getTime());
        data.put(parent.getOid(), muokatutTiedot);
        assertEquals(checker.checkPvmConstraints(parent, null, null, data), "");
    }

    @Test
    public void sameStartDates() {
        child1.setAlkuPvm(new GregorianCalendar(1980, 0, 1).getTime());
        parent.setAlkuPvm(new GregorianCalendar(1980, 0, 1).getTime());
        data.put(parent.getOid(), muokatutTiedot);
        assertEquals(checker.checkPvmConstraints(parent, null, null, data), "");
    }

    @Test
    public void olderChild() {
        child1.setAlkuPvm(new GregorianCalendar(1970, 0, 1).getTime());
        // method validates modified info, so doesn't matter what koodiValue parent has
        muokatutTiedot.setAlkuPvm(new GregorianCalendar(1980, 0, 1).getTime());
        data.put(parent.getOid(), muokatutTiedot);
        assertEquals(checker.checkPvmConstraints(parent, null, null, data), "");
    }

    @Test
    public void youngerChild() {
        child1.setAlkuPvm(new GregorianCalendar(1990, 0, 1).getTime());
        // method validates modified info, so doesn't matter what koodiValue parent has
        muokatutTiedot.setAlkuPvm(new GregorianCalendar(1980, 0, 1).getTime());
        data.put(parent.getOid(), muokatutTiedot);
        assertEquals(checker.checkPvmConstraints(parent, null, null, data), "");
    }

    @Test
    public void childWithLaterEndDate() {
        // end date in modified data
        muokatutTiedot.setLoppuPvm(new GregorianCalendar(2017, 0, 1).getTime());
        child1.setLakkautusPvm(new GregorianCalendar(2018, 0, 1).getTime());
        data.put(parent.getOid(), muokatutTiedot);
        assertNotEquals(checker.checkPvmConstraints(parent, null, null, data), "");
    }

    @Test
    public void childWithEndDate() {
        child1.setLakkautusPvm(new GregorianCalendar(2018, 0, 1).getTime());
        data.put(parent.getOid(), muokatutTiedot);
        assertEquals(checker.checkPvmConstraints(parent, null, null, data), "");
    }
}

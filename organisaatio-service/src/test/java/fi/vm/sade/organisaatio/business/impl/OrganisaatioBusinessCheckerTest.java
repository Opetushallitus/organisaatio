package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.business.exception.OrganisaatioDateException;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioMuokkausTiedotDTO;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link OrganisaatioBusinessChecker} class.
 */
public class OrganisaatioBusinessCheckerTest {
    private OrganisaatioBusinessChecker checker = new OrganisaatioBusinessChecker();

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
        organisaatio.setAlkuPvm(Date.from(Instant.from(LocalDate.of(1900, 1, 1).atStartOfDay(ZoneId.systemDefault()))));

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
    public void testRootLevelOrgEndsBeforeStartDate() throws Exception {
        muokatutTiedot.setAlkuPvm(new GregorianCalendar(2500, 0, 1).getTime());
        muokatutTiedot.setLoppuPvm(new GregorianCalendar(2017, 0, 1).getTime());
        data.put(oid1, muokatutTiedot);
        assertThrows(OrganisaatioDateException.class, () -> checker.checkPvmConstraints(organisaatio, data));
    }

    @Test
    public void testOrgWithNoChildrenValidAlkupvm() {
        muokatutTiedot.setAlkuPvm(new GregorianCalendar(2016, 0, 1).getTime());
        ReflectionTestUtils.setField(parent, "childSuhteet", new HashSet<>());
        data.put(parent.getOid(), muokatutTiedot);
        checker.checkPvmConstraints(parent, data);
    }

    @Test
    public void testNullDates() {
        muokatutTiedot.setAlkuPvm(null);
        muokatutTiedot.setLoppuPvm(null);
        data.put(parent.getOid(), muokatutTiedot);
        checker.checkPvmConstraints(parent, data);
    }

    @Test
    public void parentWithStartDateChildWithout() {
        parent.setAlkuPvm(new GregorianCalendar(1980, 0, 1).getTime());
        data.put(parent.getOid(), muokatutTiedot);
        checker.checkPvmConstraints(parent, data);
    }

    @Test
    public void childWithStartDateParentWithout() {
        parent.setAlkuPvm(null);
        child1.setAlkuPvm(new GregorianCalendar(1980, 0, 1).getTime());
        data.put(parent.getOid(), muokatutTiedot);
        checker.checkPvmConstraints(parent, data);
    }

    @Test
    public void sameStartDates() {
        child1.setAlkuPvm(new GregorianCalendar(1980, 0, 1).getTime());
        parent.setAlkuPvm(new GregorianCalendar(1980, 0, 1).getTime());
        data.put(parent.getOid(), muokatutTiedot);
        checker.checkPvmConstraints(parent, data);
    }

    @Test
    public void olderChild() {
        child1.setAlkuPvm(new GregorianCalendar(1970, 0, 1).getTime());
        // method validates modified info, so doesn't matter what koodiValue parent has
        muokatutTiedot.setAlkuPvm(new GregorianCalendar(1980, 0, 1).getTime());
        data.put(parent.getOid(), muokatutTiedot);
        checker.checkPvmConstraints(parent, data);
    }

    @Test
    public void youngerChild() {
        child1.setAlkuPvm(new GregorianCalendar(1990, 0, 1).getTime());
        // method validates modified info, so doesn't matter what koodiValue parent has
        muokatutTiedot.setAlkuPvm(new GregorianCalendar(1980, 0, 1).getTime());
        data.put(parent.getOid(), muokatutTiedot);
        checker.checkPvmConstraints(parent, data);
    }

    @Test
    public void childWithLaterEndDate() {
        // end date in modified data
        muokatutTiedot.setLoppuPvm(new GregorianCalendar(2017, 0, 1).getTime());
        child1.setLakkautusPvm(new GregorianCalendar(2018, 0, 1).getTime());
        data.put(parent.getOid(), muokatutTiedot);
        assertThrows(OrganisaatioDateException.class, () -> checker.checkPvmConstraints(parent, data));
    }

    @Test
    public void childWithEndDate() {
        child1.setLakkautusPvm(new GregorianCalendar(2018, 0, 1).getTime());
        data.put(parent.getOid(), muokatutTiedot);
        checker.checkPvmConstraints(parent, data);
    }
}

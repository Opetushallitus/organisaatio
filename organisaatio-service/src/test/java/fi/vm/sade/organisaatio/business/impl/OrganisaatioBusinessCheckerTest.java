package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.SecurityAwareTestBase;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioMuokkausTiedotDTO;
import fi.vm.sade.organisaatio.model.Organisaatio;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.GregorianCalendar;
import java.util.HashMap;

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

    @Before
    public void setUp() {
        organisaatio.setOid(oid1);
        organisaatio.setAlkuPvm(checker.getMIN_DATE().getTime());
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
}

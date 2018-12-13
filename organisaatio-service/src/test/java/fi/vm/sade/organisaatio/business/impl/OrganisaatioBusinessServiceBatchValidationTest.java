package fi.vm.sade.organisaatio.business.impl;


import fi.vm.sade.organisaatio.dto.v2.OrganisaatioMuokkausTiedotDTO;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

public class OrganisaatioBusinessServiceBatchValidationTest {

    private OrganisaatioBusinessServiceImpl organisaatioBusinessServiceImpl = new OrganisaatioBusinessServiceImpl();
    private OrganisaatioBusinessChecker checker = new OrganisaatioBusinessChecker();

    private Organisaatio parent = new Organisaatio();
    private Organisaatio child1 = new Organisaatio();
    private Organisaatio child2 = new Organisaatio();
    private Organisaatio root = new Organisaatio();

    @Before
    public void setUp() {
        parent.setOid("1234.5");
        OrganisaatioSuhde os1 = new OrganisaatioSuhde();
        os1.setParent(parent);
        os1.setChild(child1);
        os1.setSuhdeTyyppi(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.HISTORIA);
        OrganisaatioSuhde os2 = new OrganisaatioSuhde();
        os2.setParent(parent);
        os2.setChild(child2);
        os2.setSuhdeTyyppi(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.HISTORIA);
        Set<OrganisaatioSuhde> children = new HashSet<>();
        children.add(os1);
        children.add(os2);
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
        ReflectionTestUtils.setField(organisaatioBusinessServiceImpl, "checker", checker);
    }

    @Test
    public void testBatchValidateWithSameDates() {
        parent.setAlkuPvm(new GregorianCalendar(2016, 0, 1).getTime());
        child1.setAlkuPvm(new GregorianCalendar(2016, 0, 1).getTime());
        child2.setAlkuPvm(new GregorianCalendar(2016, 0, 1).getTime());
        final OrganisaatioMuokkausTiedotDTO tiedotDTO = new OrganisaatioMuokkausTiedotDTO() {{
            setAlkuPvm(parent.getAlkuPvm());
            setOid(parent.getOid());
        }};
        // no asserts needed: passes if doesn't throw exception
        organisaatioBusinessServiceImpl.batchValidatePvm(
                new HashMap<String, OrganisaatioMuokkausTiedotDTO>() {{
                    put(tiedotDTO.getOid(), tiedotDTO);
                }},
                new HashMap<String, Organisaatio>() {{
                    put(parent.getOid(), parent);
                }});
    }

    @Test
    public void testBatchValidateWithOlderChild() {
        parent.setAlkuPvm(new GregorianCalendar(2016, 0, 1).getTime());
        child1.setAlkuPvm(new GregorianCalendar(2015, 0, 1).getTime());
        child2.setAlkuPvm(new GregorianCalendar(2017, 0, 1).getTime());
        final OrganisaatioMuokkausTiedotDTO tiedotDTO = new OrganisaatioMuokkausTiedotDTO() {{
            setAlkuPvm(parent.getAlkuPvm());
            setOid(parent.getOid());
        }};
        // no asserts needed: passes if doesn't throw exception
        organisaatioBusinessServiceImpl.batchValidatePvm(
                new HashMap<String, OrganisaatioMuokkausTiedotDTO>() {{
                    put(tiedotDTO.getOid(), tiedotDTO);
                }},
                new HashMap<String, Organisaatio>() {{
                    put(parent.getOid(), parent);
                }});
    }
}

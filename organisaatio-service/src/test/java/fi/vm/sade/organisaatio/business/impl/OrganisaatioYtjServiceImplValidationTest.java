package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.oid.OIDService;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.resource.OIDServiceMock;
import fi.vm.sade.organisaatio.ytj.api.YTJDTO;
import fi.vm.sade.organisaatio.ytj.api.YTJOsoiteDTO;
import fi.ytj.YTunnusDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class OrganisaatioYtjServiceImplValidationTest {
    private final OrganisaatioYtjServiceImpl organisaatioYtjService = new OrganisaatioYtjServiceImpl();
    private final OrganisaatioBusinessChecker checker = new OrganisaatioBusinessChecker();
    private final OrganisaatioBusinessService organisaatioBusinessService = new OrganisaatioBusinessServiceImpl();
    private final OIDService oidService = new OIDServiceMock();
    private YTJDTO ytjdto;
    private final Organisaatio org = new Organisaatio();

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(organisaatioYtjService, "checker", checker);
        ReflectionTestUtils.setField(organisaatioBusinessService, "checker", checker);
        ReflectionTestUtils.setField(organisaatioYtjService, "businessService", organisaatioBusinessService);
        ReflectionTestUtils.setField(organisaatioYtjService, "oidService", oidService);
        ReflectionTestUtils.setField(organisaatioYtjService, "ytjPaivitysLoki", new YtjPaivitysLoki());
        ytjdto = generateValidYtjdto();
        initGeneralOrgData();
        generateOrganisaatioNimi(new GregorianCalendar(2010, Calendar.JANUARY, 1).getTime());
        org.setYtjKieli("kieli_fi#1");
    }

    @Test
    public void noNameHistoryUpdateIfOnlyLetterCaseChanges() {
        ytjdto.setNimi("NIMI");
        // nimen alkupvm
        ytjdto.setAloitusPvm("01.02.2013");
        // original name
        assertEquals("nimi", org.getNimi().getString("fi"));
        assertTrue((Boolean) ReflectionTestUtils.invokeMethod(organisaatioYtjService, "updateOrg", ytjdto, org, false));
        // updated name from YTJ
        assertEquals("NIMI", org.getNimi().getString("fi"));
        assertEquals(1, org.getNimet().size());
        assertEquals(org.getNimet().iterator().next().getAlkuPvm(), new GregorianCalendar(2013, Calendar.FEBRUARY, 1).getTime());
        // same koodiValue in name history
        assertEquals("NIMI", org.getNimet().iterator().next().getNimi().getString("fi"));
    }

    @Test
    public void validationPassesIfYtjHasOlderStartDateAndOrgNoChildren() {
        ytjdto.getYritysTunnus().setAlkupvm("01.02.2000");
        // just to pass name validations, we are not testing that now
        ytjdto.setAloitusPvm("01.01.2010");
        org.setNimet(new ArrayList<>());
        generateOrganisaatioNimi(new GregorianCalendar(2010, Calendar.JANUARY, 1).getTime());
        org.setAlkuPvm(new GregorianCalendar(2010, Calendar.JANUARY, 1).getTime());
        assertTrue((Boolean) ReflectionTestUtils.invokeMethod(organisaatioYtjService, "updateOrg", ytjdto, org, false));
        try {
            // date updated from YTJ
            assertEquals(new SimpleDateFormat("dd.MM.yyyy").parse(ytjdto.getYritysTunnus().getAlkupvm()), org.getAlkuPvm());
        } catch (ParseException e) {
            fail();
        }
        YtjPaivitysLoki loki = (YtjPaivitysLoki) ReflectionTestUtils.getField(organisaatioYtjService, "ytjPaivitysLoki");
        assertTrue(loki.getYtjVirheet().isEmpty());
    }

    @Test
    public void ytjHasLaterStartThanOrgsChildren() {
        ytjdto.getYritysTunnus().setAlkupvm("01.02.2000");
        // just to pass name validations, we are not testing that now
        ytjdto.setAloitusPvm("01.01.2010");

        Organisaatio child = new Organisaatio();
        child.setAlkuPvm(new GregorianCalendar(1999, Calendar.JANUARY, 1).getTime());
        OrganisaatioSuhde os1 = new OrganisaatioSuhde();
        os1.setParent(org);
        os1.setChild(child);
        os1.setSuhdeTyyppi(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.HISTORIA);
        Set<OrganisaatioSuhde> children = new HashSet<>();
        children.add(os1);
        ReflectionTestUtils.setField(org, "childSuhteet", children);
        assertTrue((Boolean) ReflectionTestUtils.invokeMethod(organisaatioYtjService, "updateOrg", ytjdto, org, false));
        try {
            assertEquals(new SimpleDateFormat("dd.MM.yyyy").parse(ytjdto.getYritysTunnus().getAlkupvm()), org.getAlkuPvm());
        } catch (ParseException e) {
            fail();
        }
        YtjPaivitysLoki loki = (YtjPaivitysLoki) ReflectionTestUtils.getField(organisaatioYtjService, "ytjPaivitysLoki");
        assertTrue(loki.getYtjVirheet().isEmpty());
        //Assert.assertTrue(loki.getYtjVirheet().size()==1);
        //Assert.assertEquals(YtjVirhe.YTJVirheKohde.ALKUPVM, loki.getYtjVirheet().get(0).getVirhekohde());
        //Assert.assertEquals("ilmoitukset.log.virhe.alkupvm.tarkistukset", loki.getYtjVirheet().get(0).getVirheviesti());
    }

    private YTJDTO generateValidYtjdto() {
        YTJDTO ytjdto = new YTJDTO();
        ytjdto.setYrityksenKieli("Suomi");
        // nimen alkupvm
        ytjdto.setNimi("nimi");
        ytjdto.setAloitusPvm("01.02.2000");
        ytjdto.setYritysTunnus(new YTunnusDTO());
        // alkupvm
        ytjdto.getYritysTunnus().setAlkupvm("01.02.2000");
        YTJOsoiteDTO osoite = new YTJOsoiteDTO();
        osoite.setKatu("katu1");
        ytjdto.setPostiOsoite(osoite);
        return ytjdto;
    }

    private void initGeneralOrgData() {
        org.setOid("1234.5");
        String kieli = OrganisaatioYtjServiceImpl.ORG_KIELI_KOODI_FI;
        List<String> kielet = new ArrayList<>();
        kielet.add(kieli);
        org.setKielet(kielet);
        Osoite orgOsoite = new Osoite();
        orgOsoite.setOsoite("katu1");
        orgOsoite.setKieli(OrganisaatioYtjServiceImpl.KIELI_KOODI_FI);
        orgOsoite.setOsoiteTyyppi(Osoite.TYYPPI_POSTIOSOITE);
        Set<Yhteystieto> yhteystiedot = new HashSet<>();
        yhteystiedot.add(orgOsoite);
        org.setYhteystiedot(yhteystiedot);
        ReflectionTestUtils.setField(org, "parentSuhteet", new ArrayList<>());
    }

    private void generateOrganisaatioNimi(Date alkupvm) {
        OrganisaatioNimi organisaatioNimi = new OrganisaatioNimi();
        organisaatioNimi.setOrganisaatio(org);
        final MonikielinenTeksti nimi = new MonikielinenTeksti();
        nimi.setValues(new HashMap<String, String>() {{put("fi", "nimi");}});
        organisaatioNimi.setNimi(nimi);
        organisaatioNimi.setAlkuPvm(alkupvm);
        org.setNimi(nimi);
        org.addNimi(organisaatioNimi);
    }
}

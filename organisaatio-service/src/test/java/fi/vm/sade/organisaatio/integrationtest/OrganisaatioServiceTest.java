package fi.vm.sade.organisaatio.integrationtest;

import static fi.vm.sade.organisaatio.integrationtest.OrganisaatioTstUtils.DEFAULT_EMAIL;
import static fi.vm.sade.organisaatio.integrationtest.OrganisaatioTstUtils.DEFAULT_FAKSI;
import static fi.vm.sade.organisaatio.integrationtest.OrganisaatioTstUtils.DEFAULT_KAYNTIOSOITE;
import static fi.vm.sade.organisaatio.integrationtest.OrganisaatioTstUtils.DEFAULT_POSTIOSOITE;
import static fi.vm.sade.organisaatio.integrationtest.OrganisaatioTstUtils.DEFAULT_PUHELIN;
import static fi.vm.sade.organisaatio.integrationtest.OrganisaatioTstUtils.DEFAULT_WWW;
import static fi.vm.sade.organisaatio.integrationtest.OrganisaatioTstUtils.buildCreateOrganisaatioModel;
import static fi.vm.sade.organisaatio.integrationtest.OrganisaatioTstUtils.buildOrganisaatio;
import static fi.vm.sade.organisaatio.integrationtest.OrganisaatioTstUtils.buildYtunnus;
import static fi.vm.sade.organisaatio.integrationtest.OrganisaatioTstUtils.createKoulutustoimija;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.hibernate.internal.SessionImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import fi.vm.sade.generic.common.ValidationException;
import fi.vm.sade.organisaatio.SecurityAwareTestBase;
import fi.vm.sade.organisaatio.api.model.GenericFault;
import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.organisaatio.api.model.types.EmailDTO;
import fi.vm.sade.organisaatio.api.model.types.FindBasicOrganisaatioChildsToOidTypesParameter;
import fi.vm.sade.organisaatio.api.model.types.FindBasicOrganisaatioChildsToOidTypesResponse;
import fi.vm.sade.organisaatio.api.model.types.FindBasicParentOrganisaatioTypesParameter;
import fi.vm.sade.organisaatio.api.model.types.FindBasicParentOrganisaatioTypesResponse;
import fi.vm.sade.organisaatio.api.model.types.HakutoimistoTyyppi;
import fi.vm.sade.organisaatio.api.model.types.KuvailevaTietoTyyppi;
import fi.vm.sade.organisaatio.api.model.types.KuvailevaTietoTyyppiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi.Teksti;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioKuvaTyyppi;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioKuvailevatTiedotTyyppi;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioOidListType;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioPerustietoType;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioSearchCriteriaDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioSearchOidType;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.model.types.OrganizationStructureType;
import fi.vm.sade.organisaatio.api.model.types.OsoiteDTO;
import fi.vm.sade.organisaatio.api.model.types.OsoiteTyyppi;
import fi.vm.sade.organisaatio.api.model.types.PuhelinNumeroTyyppi;
import fi.vm.sade.organisaatio.api.model.types.PuhelinnumeroDTO;
import fi.vm.sade.organisaatio.api.model.types.RemoveByOidType;
import fi.vm.sade.organisaatio.api.model.types.SoMeLinkkiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.SoMeLinkkiTyyppiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.YhteyshenkiloTyyppi;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoDTO;
import fi.vm.sade.organisaatio.auth.OrganisaatioPermissionServiceImpl;
import fi.vm.sade.organisaatio.dao.impl.OrganisaatioDAOImpl;
import fi.vm.sade.organisaatio.dao.impl.OrganisaatioSuhdeDAOImpl;
import fi.vm.sade.organisaatio.dao.impl.YhteystietoDAOImpl;
import fi.vm.sade.organisaatio.model.Email;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;
import fi.vm.sade.organisaatio.model.Osoite;
import fi.vm.sade.organisaatio.model.Puhelinnumero;
import fi.vm.sade.organisaatio.model.Www;
import fi.vm.sade.organisaatio.resource.IndexerResource;
import fi.vm.sade.organisaatio.service.LearningInstitutionExistsException;
import fi.vm.sade.organisaatio.service.NotAuthorizedException;
import fi.vm.sade.organisaatio.service.OrganisaatioCrudException;
import fi.vm.sade.organisaatio.service.OrganisaatioHierarchyException;
import fi.vm.sade.organisaatio.service.OrganisaatioModifiedException;
import fi.vm.sade.organisaatio.service.OrganizationDateException;
import fi.vm.sade.organisaatio.service.YtunnusException;
import fi.vm.sade.organisaatio.service.converter.ConverterFactory;
//import static fi.vm.sade.organisaatio.api.model.OrganisaatioTyyppiDTO.*;


/**
 * @author Antti
 */
@ContextConfiguration(locations = {
        "classpath:spring/test-context.xml"
})
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ActiveProfiles("embedded-solr")
public class OrganisaatioServiceTest extends SecurityAwareTestBase {
    
    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioServiceTest.class);

    @Autowired
    TestDataCreator dataUtil;

    @Autowired
    IndexerResource solrIndexer;

    public static final String MAY_NOT_BE_NULL = "may not be null";
    public static final String INVALID_NIMI_MSG = "Kentän pituuden tulee olla 3 - 100"; //Length of organisation name should be 3 - 100";

    @Autowired
    protected OrganisaatioService organisaatioService;
    @Autowired
    private ConverterFactory converterFactory;
    @Autowired
    protected OrganisaatioDAOImpl organisaatioDAO;
    @Autowired
    private YhteystietoDAOImpl yhteystietoDAO;
    @Autowired
    protected OrganisaatioSuhdeDAOImpl organisaatioSuhdeDAO;
    

    @PersistenceContext
    private EntityManager em;

	private static Date date(int n) {
		return new GregorianCalendar(2000+n, 0, 0).getTime();
	}
	

    @Override
    @Before
    public void before() {
        super.before();
        Locale.setDefault(Locale.US); // because of validaton messages
        dataUtil.createInitialTestData();
        solrIndexer.reBuildIndex(true); //rebuild index

//        {
//            LOG.info("*** START");
//            Query q = em.createQuery("SELECT p FROM Organisaatio p", Organisaatio.class);
//            q.setHint("org.hibernate.cacheable", true);
//            // q.setHint("org.hibernate.cacheMode", "NORMAL / IGNORE / GET / PUT / REFRESH");
//            q.setHint("org.hibernate.cacheRegion", "query.XXX");
//            q.getResultList();
//            LOG.info("*** END");
//        }
//        {
//            LOG.info("*** START 2");
//            Query q = em.createQuery("SELECT p FROM Organisaatio p", Organisaatio.class);
//            q.setHint("org.hibernate.cacheable", true);
//            // q.setHint("org.hibernate.cacheMode", "NORMAL / IGNORE / GET / PUT / REFRESH");
//            q.setHint("org.hibernate.cacheRegion", "query.XXX");
//            q.getResultList();
//            LOG.info("*** END 2");
//        }
    }

    @Override
    @After
    public void after() {
        super.after();
        LOG.info("em = {}", em);
        LOG.info("em.getDelegate() --> {}", em.getDelegate());
        SessionImpl sessionImpl = (SessionImpl) em.getDelegate();
        LOG.info("STATISTICS: {}", sessionImpl.getStatistics());

//        ((HibernateSessionProxy)em.getDelegate()).getSession(EntityMode.YOUR_ENTITY_MODE_HERE).getSessionFactory().getStatistics();
//        em.get
//
//        EntityManagerFactory emf = em.getEntityManagerFactory();
//
//        EntityManagerFactoryImpl emfImpl = (EntityManagerFactoryImpl) emf;
//        LOG.info("STATISTICS: {}", emfImpl.getSessionFactory().getStatistics());
    }


    @Test
    public void createKoulutustoimija_happyPath() throws GenericFault {
        String nimi = "createxxxxx";
        String ytunnus = buildYtunnus();
        String oid = "" + System.currentTimeMillis() + "" + Math.random();

        OrganisaatioDTO koulutustoimija = createKoulutustoimija(organisaatioService, nimi, ytunnus, "yhteyshlo", oid);

        // assert koulutustoimija was saved successfully
        assertOrganisaatioSaved(nimi, ytunnus, koulutustoimija, 6, fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi.KOULUTUSTOIMIJA.value(), null);
    }
    
    @Test
    public void testCreateToimipisteWithRunningNumber() throws GenericFault {
        String parentOid = "1.2.2004.2";
        String nimi = "toimipiste testi";
        String jnro = "001";
        String yhteishakukoodi = "yhteishaku toimipiste testi koodi";
        OrganisaatioDTO toimipiste=  OrganisaatioTstUtils.buildCreateOrganisaatioModel(organisaatioService, nimi, parentOid, OrganisaatioTyyppi.OPETUSPISTE.value());
        String toimipisteOid = toimipiste.getOid();
        toimipiste.setOpetuspisteenJarjNro(jnro);
        toimipiste.setYhteishaunKoulukoodi(yhteishakukoodi);
        
        
        organisaatioService.createOrganisaatio(toimipiste, false);
        
        OrganisaatioDTO readToimipiste = organisaatioService.findByOid(toimipisteOid);
        
        assertTrue(readToimipiste.getOpetuspisteenJarjNro().equals(jnro));
        assertTrue(readToimipiste.getYhteishaunKoulukoodi().equals(yhteishakukoodi));
    }

    @Test
    public void testVersionUpdateCollision() {
        OrganisaatioDTO orgDto = organisaatioService.findByOid("1.2.2004.1");
        LOG.error("dto version : " + orgDto.getVersion());
        orgDto.setDomainNimi("XXX");
        try {
        orgDto = organisaatioService.updateOrganisaatio(orgDto, false);
        } catch (Exception exp) {
            fail("EXCEPTION IN VERSION COLLISION TEST: " + exp.getMessage());
        }
        assertTrue("VERSION " + orgDto.getVersion(),orgDto.getVersion() == 4);
    }

    @Test
    public void testVersionCollision() {
        OrganisaatioDTO orgDto = organisaatioService.findByOid("1.2.2004.1");
        try {
            orgDto.setVersion(123);
            orgDto.setMaa("BEIRUT");
            organisaatioService.updateOrganisaatio(orgDto, false);
            fail("WTF, OPTIMISTIC LOCKING DOES NOT WORK. testVersionCollision()");
        } catch (Exception exp) {
            //expected
            assertEquals(OrganisaatioModifiedException.class,  exp.getClass());
        }
    }

    @Ignore
    @Test//(expected = IllegalStateException.class)
    public void createKoulutustoimija_withExistingYhteystiedot() throws GenericFault {
        // create yhteyshenkilo and yhteystieto in db
        YhteystietoDTO postiosoite = converterFactory.convertToDTO(yhteystietoDAO.insert(new Osoite(OsoiteTyyppi.POSTI.value(), "katu 1", "12345", "Helsinki", null)));
        YhteystietoDTO kayntiosoite = converterFactory.convertToDTO(yhteystietoDAO.insert(new Osoite(OsoiteTyyppi.KAYNTI.value(), "katu 1", "12345", "Helsinki", null)));
        YhteystietoDTO puhelinnumero = converterFactory.convertToDTO(yhteystietoDAO.insert(new Puhelinnumero("0501234567", PuhelinNumeroTyyppi.PUHELIN.value(), null)));
        YhteystietoDTO faksi = converterFactory.convertToDTO(yhteystietoDAO.insert(new Puhelinnumero("0501234567", PuhelinNumeroTyyppi.FAKSI.value(), null)));
        YhteystietoDTO email = converterFactory.convertToDTO(yhteystietoDAO.insert(new Email(DEFAULT_EMAIL.getEmail(), null)));
        Www wwwO = new Www();
        wwwO.setWwwOsoite(DEFAULT_WWW.getWwwOsoite());
        YhteystietoDTO www = converterFactory.convertToDTO(yhteystietoDAO.insert(wwwO));
        String oid = "" + System.currentTimeMillis() + "" + Math.random();



        int countYhteystieto = yhteystietoDAO.findAll().size();

        // create koulutustoimija with existing yhteystiedot and yhteyshenkilo
        createKoulutustoimija(organisaatioService, buildOrganisaatio("kld78u", buildYtunnus()), Arrays.asList(postiosoite, kayntiosoite, puhelinnumero, faksi, email, www), oid);

        assertEquals(countYhteystieto, yhteystietoDAO.findAll().size());
    }
    
    @Test
    public void testYtunnusUniqueness() throws Exception {
    	
        String oid1 = "" + System.currentTimeMillis() + "" + Math.random();
        String oid2 = "" + System.currentTimeMillis() + "" + Math.random();
    	String ytunnus = buildYtunnus();

    	// luodaan organisaatio -> onnistuu
    	createKoulutustoimija(organisaatioService, "Testorg-1",  ytunnus, "Yhteys Henkilö", oid1);

    	// luodaan organisaatio samalla y-tunnuksella -> ei onnistu
    	try {
			createKoulutustoimija(organisaatioService, "Testorg-1",  ytunnus, "Yhteys Henkilö", oid2);
			fail("Y-tunnus uniqueness failed");
		} catch (YtunnusException e) {} // ignore
    	
    	// poistetaan ensin luotu organisaatio
    	RemoveByOidType rmb = new RemoveByOidType();
    	rmb.setOid(oid1);
    	organisaatioService.removeOrganisaatioByOid(rmb);

    	// luodaan organisaatio samalla y-tunnuksella -> onnistuu
		createKoulutustoimija(organisaatioService, "Testorg-1",  ytunnus, "Yhteys Henkilö", oid2);

    }

    @Ignore
    @Test
    public void createKoulutustoimija_cannotCreateWithoutMandatoryYhteystiedot() {
        String nimi = "createKoulutustoimija_cannotCreateWithoutMandatoryYhteystiedot";
        assertCreateKoulutustoimijaThrowsValidationException("validation.exception", nimi, new ArrayList<YhteystietoDTO>());//"postiosoite - " + MAY_NOT_BE_NULL, nimi, new ArrayList<YhteystietoDTO>());
        // without puhelinnumero
        assertCreateKoulutustoimijaThrowsValidationException("validation.exception", nimi, Arrays.asList(DEFAULT_KAYNTIOSOITE, DEFAULT_POSTIOSOITE, DEFAULT_FAKSI, DEFAULT_EMAIL, DEFAULT_WWW));//"puhelin - " + MAY_NOT_BE_NULL, nimi, Arrays.asList(DEFAULT_KAYNTIOSOITE, DEFAULT_POSTIOSOITE, DEFAULT_FAKSI, DEFAULT_EMAIL, DEFAULT_WWW));
        // without postiosoite
        assertCreateKoulutustoimijaThrowsValidationException("validation.exception", nimi, Arrays.asList(DEFAULT_KAYNTIOSOITE, DEFAULT_PUHELIN, DEFAULT_FAKSI, DEFAULT_EMAIL, DEFAULT_WWW));//"postiosoite - " + MAY_NOT_BE_NULL, nimi, Arrays.asList(DEFAULT_KAYNTIOSOITE, DEFAULT_PUHELIN, DEFAULT_FAKSI, DEFAULT_EMAIL, DEFAULT_WWW));
        // without kayntiosoite
        assertCreateKoulutustoimijaThrowsValidationException("validation.exception", nimi, Arrays.asList(DEFAULT_PUHELIN, DEFAULT_POSTIOSOITE, DEFAULT_FAKSI, DEFAULT_EMAIL, DEFAULT_WWW));//"kayntiosoite - " + MAY_NOT_BE_NULL, nimi, Arrays.asList(DEFAULT_PUHELIN, DEFAULT_POSTIOSOITE, DEFAULT_FAKSI, DEFAULT_EMAIL, DEFAULT_WWW));
    }

    private void assertCreateKoulutustoimijaThrowsValidationException(String expectedError, String nimi, List<? extends YhteystietoDTO> yhteystiedot) {
        try {
            createKoulutustoimija(organisaatioService, buildOrganisaatio(nimi, buildYtunnus()), yhteystiedot, "" + System.currentTimeMillis() + "" + Math.random());
            fail("should not succeed");
        } catch (GenericFault e) {
            assertTrue("wrong error message, expected to contain: '" + expectedError + ", but was: '" + e.getMessage() + "'", e.getMessage().contains(expectedError));
        }
    }

    @Test
    public void createKoulutustoimija_cannotCreateWithSameYTunnus() throws GenericFault {
        OrganisaatioTstUtils.createKoulutustoimija(organisaatioService, "create_cannotCreateWithSameYTunnus", "1234567-0", "yhteyshlo", ""  + System.currentTimeMillis() + "" + Math.random());
        try {
            OrganisaatioTstUtils.createKoulutustoimija(organisaatioService, "create_cannotCreateWithSameYTunnus2", "1234567-0", "yhteyshlo", "" + System.currentTimeMillis() + "" + Math.random());
            fail("should not succeed");
        } catch (YtunnusException e) {
            //expected
        }
    }

    @Test
    public void testCreateWithSameOppilaitoskoodi() throws GenericFault {
        OrganisaatioDTO org = OrganisaatioTstUtils.buildOrganisaatio("nimi1", "1111111-2");
        org.getYhteystiedot().addAll(OrganisaatioTstUtils.DEFAULT_YHTEYSTIEDOT);
        org.setOppilaitosKoodi("123456");
        org.getTyypit().add(OrganisaatioTyyppi.MUU_ORGANISAATIO);
        organisaatioService.createOrganisaatio(org, false);
        org.setYtunnus("222222-2");
        org.setOid(org.getOid() + "1");
        try {
            organisaatioService.createOrganisaatio(org, false);
            fail("should not succeed");
        } catch (LearningInstitutionExistsException le) {
            assertEquals("oppilaitos.exists.with.code", le.getKey());
        }
    }

    @Test
    public void testCreateWithIllegalDates() throws GenericFault {
        OrganisaatioDTO org = OrganisaatioTstUtils.buildOrganisaatio("nimi1", "1111111-2");
        org.getYhteystiedot().addAll(OrganisaatioTstUtils.DEFAULT_YHTEYSTIEDOT);
        org.setOppilaitosKoodi("123456");
        org.setParentOid("1.2.2004.1");
        org.getTyypit().add(OrganisaatioTyyppi.OPPILAITOS);
        org.setAlkuPvm(date(2));
        org.setLakkautusPvm(date(1));
        try {
            organisaatioService.createOrganisaatio(org, false);
            fail("should not succeed");
        } catch (javax.validation.ValidationException ve) {
            assertEquals("{organisaatio-service.invalid.lopetusPvm}", ve.getMessage());
        }
    }

    @Test
    public void testPassivate() throws GenericFault {
        OrganisaatioDTO template = OrganisaatioTstUtils.buildOrganisaatio("nimi1", "1111111-2");
        template.getYhteystiedot().addAll(OrganisaatioTstUtils.DEFAULT_YHTEYSTIEDOT);
        template.getTyypit().add(OrganisaatioTyyppi.MUU_ORGANISAATIO);
        OrganisaatioDTO parent= organisaatioService.createOrganisaatio(template, false);
        parent.getTyypit().add(OrganisaatioTyyppi.MUU_ORGANISAATIO);
        assertNotNull(parent);
        template.setYtunnus("2222222-2");
        template.setParentOid(parent.getOid());
        template.setOid(template.getOid() + "1");
        OrganisaatioDTO child = organisaatioService.createOrganisaatio(template, false);
        parent.setLakkautusPvm(date(2));
        parent = organisaatioService.updateOrganisaatio(parent, false);
        
        //check child end date matches the date of parent
        Organisaatio org = organisaatioDAO.findByOid(child.getOid());
        assertEquals(date(2), org.getLakkautusPvm());

        //check child can be passivated with same date;
        child.setLakkautusPvm(date(2));
        child.setVersion(org.getVersion());
        child = organisaatioService.updateOrganisaatio(child, false);
        
        org = organisaatioDAO.findByOid(child.getOid());
        assertEquals(date(2), org.getLakkautusPvm());

        //check that child cannot be passivted with different date
        child.setLakkautusPvm(date(3));
        try {
            child = organisaatioService.updateOrganisaatio(child, false);
        } catch (OrganizationDateException de) {
            assertEquals("exception.organisaatio.date", de.getKey());
        }
    }

    
    @Test
    public void testRemove() throws GenericFault {
        final String oid = ""  + System.currentTimeMillis() + "" + Math.random();
        OrganisaatioTstUtils.createKoulutustoimija(organisaatioService, "testemove", "1234567-8", "yhteyshlo", oid);
        Organisaatio org = organisaatioDAO.findByOid(oid);
        Assert.assertFalse(org.isOrganisaatioPoistettu());
        final RemoveByOidType oidType = new RemoveByOidType();
        oidType.setOid(oid);
        organisaatioService.removeOrganisaatioByOid(oidType);
        Assert.assertTrue(org.isOrganisaatioPoistettu());
    }

    @Test
    public void testRemoveFailsWithChildren() throws GenericFault {
        //final String oid1 = ""  + System.currentTimeMillis() + "" + Math.random();
        //final String oid2 = ""  + System.currentTimeMillis() + "" + Math.random();
        //OrganisaatioTstUtils.createKoulutustoimija(organisaatioService, "testemove", "1234567-8", "yhteyshlo", oid1);
    	OrganisaatioDTO o1 = OrganisaatioTstUtils.createOrganisaatio(organisaatioService, true, Arrays.asList("Koulutustoimija"), Arrays.asList("Oppilaitos"), Arrays.asList("Opetuspiste"));
        
        Organisaatio org = organisaatioDAO.findByOid(o1.getParentOid());
        Assert.assertFalse(org.isOrganisaatioPoistettu());
        final RemoveByOidType oidType = new RemoveByOidType();
        oidType.setOid(o1.getParentOid());
        try {
			organisaatioService.removeOrganisaatioByOid(oidType);        
			//Assert.assertTrue(org.isOrganisaatioPoistettu());
			fail("Ei olisi pitäny onnistua");
		} catch (OrganisaatioCrudException e) {
			assertEquals("child.orgs.found", e.getMessage());
		}
    }
    
    private void passivate(Organisaatio org, Date passDate) {
    	org.setLakkautusPvm(passDate);
    	organisaatioDAO.update(org);
    	if (org.getParent()!=null) {
    		passivate(org.getParent(), passDate);
    	}
    }

    @Test
    public void testRemovePassiveWithPassiveChildren() throws GenericFault {
    	OrganisaatioDTO o1 = OrganisaatioTstUtils.createOrganisaatio(organisaatioService, true,
    			Arrays.asList("Koulutustoimija"),
    			Arrays.asList("Oppilaitos"),
    			Arrays.asList("Opetuspiste"));
    	
        Organisaatio org = organisaatioDAO.findByOid(o1.getOid());
    	passivate(org, date(0));
        Assert.assertFalse(org.isOrganisaatioPoistettu());
        final RemoveByOidType oidType = new RemoveByOidType();
        oidType.setOid(org.getParent().getOid());
		organisaatioService.removeOrganisaatioByOid(oidType);
		Assert.assertTrue(org.getParent().isOrganisaatioPoistettu());
		Assert.assertTrue(org.isOrganisaatioPoistettu());

    }
    
    @Ignore
    @Test
    public void createKoulutustoimija_cannotCreateWithSameNimi() throws GenericFault {
        OrganisaatioTstUtils.createKoulutustoimija(organisaatioService, "create_cannotCreateWithSameNimi", buildYtunnus(), "yhteyshlo", "" + System.currentTimeMillis() + "" + Math.random());
        try {
            OrganisaatioTstUtils.createKoulutustoimija(organisaatioService, "create_cannotCreateWithSameNimi", buildYtunnus(), "yhteyshlo", "" + System.currentTimeMillis() + "" + Math.random());
            fail("should not succeed");
        } catch (GenericFault e) {
            assertEquals("validation.exception", e.getMessage());//"[constraint violation! unique problem, row with same value already exists?]", e.getMessage());
        }
    }

    @Ignore
    @Test
    public void findAll() throws ValidationException {
        /*OrganisaatioDTO o1 = OrganisaatioTstUtils.createKoulutustoimija(organisaatioService, "findAll1", buildYtunnus(), "yhteyshlo");
        OrganisaatioDTO o2 = OrganisaatioTstUtils.createKoulutustoimija(organisaatioService, "findAll2", buildYtunnus(), "yhteyshlo");
        assertTrue(organisaatioService.findAll().toString().contains(o1.getYtunnus()));
        assertTrue(organisaatioService.findAll().toString().contains(o2.getYtunnus()));*/
    }

    @Test
    public void createOrganisaatio_happyPath() throws GenericFault {
        String nameRoot = "createOrganisaatio_failsIfOrganisaatioExists";
        // luodaan päätaso
        OrganisaatioDTO root = OrganisaatioTstUtils.createKoulutustoimija(organisaatioService, nameRoot, buildYtunnus(), "yhteyshlo", "" + System.currentTimeMillis() + "" + Math.random());
        // luodaan organisaatio
        String nameSub = "createOrganisaatio_failsIfOrganisaatioExists_sub";
        OrganisaatioDTO model = buildCreateOrganisaatioModel(organisaatioService, nameSub, root.getOid(), fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi.OPPILAITOS.value());
        model.setAlkuPvm(new Date(root.getAlkuPvm().getTime()-1));
        //OH-116
        try {
            organisaatioService.createOrganisaatio(model, false);
            fail("should not succeed");
        } catch (Throwable t) {
            //expected
        }
        OrganisaatioDTO organisaatio = organisaatioService.createOrganisaatio(model, true);
             
        OrganisaatioDTO organisaatioFat = organisaatioService.findByOid(organisaatio.getOid());
        // assertoidaan että meni oikein
        assertOrganisaatioSaved(nameSub, model.getYtunnus(), organisaatioFat, 6, fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi.OPPILAITOS.value(), root.getOid());
        assertEquals(DEFAULT_FAKSI.getPuhelinnumero(), getPuhNro(organisaatioFat, PuhelinNumeroTyyppi.FAKSI));
    }

    private String getPuhNro(OrganisaatioDTO orgDTO, PuhelinNumeroTyyppi tyyppi) {
        for (YhteystietoDTO curY : orgDTO.getYhteystiedot()) {
            if (curY instanceof PuhelinnumeroDTO) {
                PuhelinnumeroDTO puh = (PuhelinnumeroDTO) curY;
                if (puh.getTyyppi().value().equals(tyyppi.value())) {
                    return puh.getPuhelinnumero();
                }
            }
        }
        return null;
    }

    @Test
    public void createOrganisaatio_technicalRoundtrip() throws GenericFault {
        // luo organisaatio, tee model result organisaatiosta, nullaa id + vaihda uniikit kentät, ja saveta -> pitäisi toimia

        OrganisaatioDTO model = buildCreateOrganisaatioModel(organisaatioService, "fash892", null, fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi.MUU_ORGANISAATIO.value());
        OrganisaatioDTO result = organisaatioService.createOrganisaatio(model, false);
        model = organisaatioService.findByOid(result.getOid());//read(result.getId()));
        model.setOid("" + System.currentTimeMillis());
        model.setNimi(setNimiValue("fi", "jaskd89das"));//setNimiFi("jaskd89das"); // nullataan uniikit nimi+ytunnus
        model.setYtunnus(buildYtunnus());
        organisaatioService.createOrganisaatio(model, false);
    }

    private MonikielinenTekstiTyyppi setNimiValue(String lang, String value) {
    	MonikielinenTekstiTyyppi nimiT = new MonikielinenTekstiTyyppi();
    	Teksti nimifi = new Teksti();
    	nimifi.setKieliKoodi(lang);
    	nimifi.setValue(value);
    	nimiT.getTeksti().add(nimifi);
    	return nimiT;
    }

    @Test
    public void testFindBasicOrgParents() {
        FindBasicParentOrganisaatioTypesResponse perusVanhemmat = organisaatioService.findBasicParentOrganisaatios(new FindBasicParentOrganisaatioTypesParameter());
        assertTrue(perusVanhemmat.getOrganisaatioPerustiedot().size() > 0);
    }

    @Test
    public void findBasicOrgParents() {
        FindBasicParentOrganisaatioTypesResponse perusVanhemmat = organisaatioService.findBasicParentOrganisaatios(new FindBasicParentOrganisaatioTypesParameter());
        boolean childsFound = false;
        if (perusVanhemmat.getOrganisaatioPerustiedot().size() < 1) {
            fail();
        } else {
            for (OrganisaatioPerustietoType perus: perusVanhemmat.getOrganisaatioPerustiedot()) {
                FindBasicOrganisaatioChildsToOidTypesParameter param = new FindBasicOrganisaatioChildsToOidTypesParameter();
                param.setOid(perus.getOid());
                FindBasicOrganisaatioChildsToOidTypesResponse response = organisaatioService.findBasicOrganisaatioChildsToOid(param);
                if (response.getOrganisaatioPerustiedot() != null && response.getOrganisaatioPerustiedot().size() > 1) {
                    childsFound = true;
                    break;
                }
            }
        }
        assertTrue(childsFound);
    }

    @Test
    public void createOrganisaatio_failsIfAlreadyExists() throws Exception {
        OrganisaatioDTO root = OrganisaatioTstUtils.createKoulutustoimija(organisaatioService, "yudas67", buildYtunnus(), "yhteyshlo", "" + System.currentTimeMillis() + "" + Math.random());
        OrganisaatioDTO model = buildCreateOrganisaatioModel(organisaatioService, "ghwq5a", root.getOid(), fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi.OPPILAITOS.value());
        OrganisaatioDTO result = organisaatioService.createOrganisaatio(model, false);
        OrganisaatioDTO model2 = organisaatioService.findByOid(result.getOid());
        model2.setParentOid(root.getOid());//setParentId(root.getId());
        model2.setNimi(setNimiValue("fi","adsf78" ));
        model2.setYtunnus(buildYtunnus());
        try {
            organisaatioService.createOrganisaatio(model2, false);
            fail("should fail");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("cannot re-create"));
        }
    }

    @Test(expected = OrganisaatioHierarchyException.class)
    public void createOrganisaatio_failsIfNoParent() throws GenericFault {
        OrganisaatioDTO model = buildCreateOrganisaatioModel(organisaatioService, "asdasd", null, fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi.OPPILAITOS.value());
        model.setParentOid(null);
        organisaatioService.createOrganisaatio(model, false);
    }

    @Test
    public void createOrganisaatio_failsIfIllegalParentId() throws GenericFault {
        OrganisaatioDTO model = buildCreateOrganisaatioModel(organisaatioService, "asdasd", "78253634L", fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi.OPETUSPISTE.value());
        model.setParentOid(null);//setParentId(null);
        try {
            organisaatioService.createOrganisaatio(model, false);
            fail("should not succeed");
        } catch (OrganisaatioHierarchyException he) {
            //expected
        }
    }

    private void assertOrganisaatioSaved(String nimi, String ytunnus, OrganisaatioDTO organisaatio,
                                         int expectedYhteystiedot, String expectedTyyppi, Object expectedParentId) {

        assertEquals(nimi, getNimiValue("fi" , organisaatio.getNimi()));
        assertEquals(ytunnus, organisaatio.getYtunnus());
        assertEquals("tyypit doesn't match", 1, organisaatio.getTyypit().size());
        assertEquals(expectedTyyppi, organisaatio.getTyypit().get(0).value());

        // assert organisaatio's main yhteyshenkilo was saved successfully
        // and yhteystiedot
        assertEquals(expectedYhteystiedot, organisaatio.getYhteystiedot().size());

        // assert jpa level stuff
        Organisaatio organisaatioJPA = organisaatioDAO.findByOid(organisaatio.getOid());
        assertEquals(Organisaatio.class, organisaatioJPA.getClass());

        assertEquals(expectedYhteystiedot, organisaatioJPA.getYhteystiedot().size());
    }

    private String getNimiValue(String lang, MonikielinenTekstiTyyppi nimiT) {
    	for (Teksti curTeksti : nimiT.getTeksti()) {
    		if (curTeksti.getKieliKoodi().equals(lang)) {
    			return curTeksti.getValue();
    		}
    	}
    	return "";
    }

    @Test
    public void findParentsTo() throws GenericFault {
        OrganisaatioDTO model1 = buildCreateOrganisaatioModel(organisaatioService, "jhdfs_1", null, fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi.MUU_ORGANISAATIO.value());
        OrganisaatioDTO org1 = organisaatioService.createOrganisaatio(model1, false);
        OrganisaatioDTO model2 = buildCreateOrganisaatioModel(organisaatioService, "jhdfs_2", org1.getOid(), fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi.MUU_ORGANISAATIO.value());
        OrganisaatioDTO org2 = organisaatioService.createOrganisaatio(model2, false);
        OrganisaatioDTO model3 = buildCreateOrganisaatioModel(organisaatioService, "jhdfs_3", org2.getOid(), fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi.MUU_ORGANISAATIO.value());
        OrganisaatioDTO org3 = organisaatioService.createOrganisaatio(model3, false);
        List<OrganisaatioDTO> path = organisaatioService.findParentsTo(org3.getOid());

        LOG.info("org1: "+org1.getOid());
        LOG.info("org2: "+org2.getOid());
        LOG.info("org3: "+org3.getOid());
        LOG.info("PATH: "+path);

        // just make sure parents are right
        assertNotNull(org2.getParentOid());
        assertEquals(org2.getParentOid(), org1.getOid());//getParentOid());

        // assert organisaatio path
        assertEquals(3, path.size());
        assertEquals(getNimiValue("fi", org1.getNimi()), getNimiValue("fi", path.get(0).getNimi()));
        assertEquals(getNimiValue("fi", org2.getNimi()), getNimiValue("fi" , path.get(1).getNimi()));
        assertEquals(getNimiValue("fi", org3.getNimi()), getNimiValue("fi" , path.get(2).getNimi()));

    }

    private OsoiteDTO getOsoite(OrganisaatioDTO orgDTO, OsoiteTyyppi tyyppi) {
        for (YhteystietoDTO curY : orgDTO.getYhteystiedot()) {
            if (curY instanceof OsoiteDTO) {
                OsoiteDTO curOs = (OsoiteDTO)curY;
                if (curOs.getOsoiteTyyppi().value().equals(tyyppi.value())) {
                    return curOs;
                }
            }
        }
        return null;
    }

    @Test
    public void update_happypath() throws GenericFault {
        OrganisaatioDTO model = buildCreateOrganisaatioModel(organisaatioService, "hjdassa", null, fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi.MUU_ORGANISAATIO.value());

        // Create and reload
        model = organisaatioService.createOrganisaatio(model, false);

        // Realod explicitly
        OrganisaatioDTO fat = organisaatioService.findByOid(model.getOid());

        // Make sure versions match
        assertEquals(fat.getVersion(), model.getVersion());


        int countYhteystiedot = yhteystietoDAO.findAll().size();

        assertEquals(6, fat.getYhteystiedot().size());
        assertEquals(1, fat.getTyypit().size());

        // muutetaan ja varmistetaan että childien määrä kannassa tai organisaatiolla ei muuttunut
        model = fat;
        // muutetaan osoitetta -> pitäisi muuttua
        OsoiteDTO curPostios = getOsoite(model, OsoiteTyyppi.POSTI);
        if (curPostios != null) {
           curPostios.setOsoite("CHANGED");
        }

        fat = organisaatioService.findByOid(organisaatioService.updateOrganisaatio(model, false).getOid());

        assertEquals(countYhteystiedot, yhteystietoDAO.findAll().size());

        assertEquals(6, fat.getYhteystiedot().size());
        assertEquals(1, fat.getTyypit().size());

        assertEquals("CHANGED", getOsoite(fat, OsoiteTyyppi.POSTI).getOsoite());

    }

    @Ignore
    @Test
    public void testMLNimialidation() throws GenericFault {
        assertCreateOrganisaatioThrowsValidationException("xx", "validvalue", "validvalue", "validation.exception");//INVALID_NIMI_MSG);
    }

    private void assertCreateOrganisaatioThrowsValidationException(String nimiFi, String nimiSv, String nimiEn, String expectedErrorMsg) throws GenericFault {
        try {
            OrganisaatioDTO model = OrganisaatioTstUtils.buildCreateOrganisaatioModel(organisaatioService, null, null, fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi.KOULUTUSTOIMIJA.value());
            MonikielinenTekstiTyyppi nimiT = new MonikielinenTekstiTyyppi();
            Teksti nimifi = new Teksti();
            nimifi.setKieliKoodi("fi");
            nimifi.setValue(nimiFi);
            nimiT.getTeksti().add(nimifi);
            Teksti nimisv = new Teksti();
            nimisv.setKieliKoodi("sv");
            nimisv.setValue(nimiSv);
            nimiT.getTeksti().add(nimisv);
            Teksti nimien = new Teksti();
            nimien.setKieliKoodi("en");
            nimien.setValue(nimiEn);
            nimiT.getTeksti().add(nimien);
            model.setNimi(nimiT);
            organisaatioService.createOrganisaatio(model, false);
            fail("should not succeed");
        } catch (GenericFault e) {
            assertEquals(expectedErrorMsg, e.getMessage());
        }
    }

    private OrganisaatioSearchCriteriaDTO createOrgSearchCriteria(String organisaatioTyyppi, String oppilaitosTyyppi, String searchStr,
        boolean lakkautetut, boolean suunnitellut, List<String> oids) {
        OrganisaatioSearchCriteriaDTO sc = new OrganisaatioSearchCriteriaDTO();
        sc.setOrganisaatioTyyppi(organisaatioTyyppi);//organisaatioTyyppi = organisaatioTyyppi;
        sc.setOppilaitosTyyppi(oppilaitosTyyppi);
        sc.setSearchStr(searchStr);
        sc.setLakkautetut(lakkautetut);
        sc.setSuunnitellut(suunnitellut);
        sc.setMaxResults(100);
        if (oids != null) {
            sc.getOidResctrictionList().addAll(oids);
        }
        return sc;

    }

    @Test
    public void testSearchOrganisaatios() throws GenericFault {

        //Finding all koulutustoimijat
        OrganisaatioSearchCriteriaDTO searchCriteria = createOrgSearchCriteria(fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi.KOULUTUSTOIMIJA.value(), null, null, true, true,null);
        List<OrganisaatioDTO> result = organisaatioService.searchOrganisaatios(searchCriteria);
        assertEquals(2, result.size());

        //List roots 
        ArrayList<String> oidList = new ArrayList<String>();
        oidList.add("1.2.2004.1");
        oidList.add("1.2.2004.5");
        searchCriteria = createOrgSearchCriteria(null, null, null, true, true,oidList);
        result = organisaatioService.searchOrganisaatios(searchCriteria);
        for (OrganisaatioDTO org:result) {
            LOG.debug("ORG: {}",org.getOid() );
        }
        assertEquals(4, result.size());


        //Finding all organisaatios with bar in name 
        searchCriteria = createOrgSearchCriteria(null, null, "bar", true, true,null);
        result = organisaatioService.searchOrganisaatios(searchCriteria);
        assertEquals(2, result.size());

//        //Asserting that if searching for ytunnus searchStr bar returns empty
//        searchCriteria = createOrgSearchCriteria(null, null, "bar", true, false, true, true,null);
//        result = organisaatioService.searchOrganisaatios(searchCriteria);
//        assertEquals(0, result.size());

        //Finding organisaatio with y-tunnus 1234567-1 
//        searchCriteria = createOrgSearchCriteria(null, null, "1234567-1", true, false, true, true,null);
//        result = organisaatioService.searchOrganisaatios(searchCriteria);
//        assertEquals(1, result.size());

        //Finding only organisaatios that are of oppilaitostyyppi Ammattikorkeakoulut
        searchCriteria = createOrgSearchCriteria(null, "Ammattikorkeakoulut", null, true, true,null);
        result = organisaatioService.searchOrganisaatios(searchCriteria);
        assertEquals(2, result.size());
    }
//    @Test
//    public void testFindAll() {
//         List<String> oidList = new ArrayList<String>();
//         oidList.add("1.2.246.562.24.00000000001");
//         List<OrganisaatioDTO> orgs =  organisaatioService.findByOidList(oidList,Integer.MAX_VALUE);
//         LOG.info("FINDALL ORG COUNT:" + orgs.size());
//         assertTrue(orgs != null && orgs.size() == 7);
//         //max results
//        OrganisaatioSearchCriteriaDTO search = new OrganisaatioSearchCriteriaDTO();
//        search.setMaxResults(1);
//        try {
//            organisaatioService.searchBasicOrganisaatios(search);
//        } catch (OrganisaatioCrudException ce) {
//            Assert.assertEquals("organisaatioSearch.tooManyResults", ce.getKey());
//        }
//    }

    @Test
    public void testGetOrganizationStructureAll() {

        List<OrganizationStructureType> organizationStructure = organisaatioService.getOrganizationStructure(null);

        int count = 0;
        int rootcount =0;
        for(OrganizationStructureType s :  organizationStructure) {
            Assert.assertTrue(StringUtils.isBlank(s.getParentOid())|| s.getParentOid().lastIndexOf("|") < 0);
            count ++;
            if(StringUtils.isBlank(s.getParentOid())) {
                rootcount ++;
            }
        }
        Assert.assertEquals(7, count);
        Assert.assertEquals(1, rootcount);
    }

    @Test
    public void testGetOrganizationStructureRoot(){
        List<OrganizationStructureType> organizationStructure = organisaatioService.getOrganizationStructure(Arrays.asList("1.2.246.562.24.00000000001"));
        int count = 0;
        int rootcount = 0;
        for(OrganizationStructureType s :  organizationStructure) {
            Assert.assertTrue(StringUtils.isBlank(s.getParentOid())|| s.getParentOid().lastIndexOf("|") < 0);
            count ++;
            if(StringUtils.isBlank(s.getParentOid())) {
                rootcount ++;
            }
        }
        Assert.assertEquals(7, count);
        Assert.assertEquals(1, rootcount);
    }

    @Test
    public void testGetOrganizationStructure(){
        List<OrganizationStructureType> organizationStructure =
                organisaatioService.getOrganizationStructure(Arrays.asList("1.2.2004.1", "1.2.2004.2"));
        int count = 0;
        int rootcount =0;
        for(OrganizationStructureType s :  organizationStructure) {
            Assert.assertTrue(StringUtils.isBlank(s.getParentOid())|| s.getParentOid().lastIndexOf("|") < 0);
            count ++;
            if(StringUtils.isBlank(s.getParentOid())) {
                rootcount ++;
            }
        }
        Assert.assertEquals(4, count);
        Assert.assertEquals(0, rootcount);
    }

    @Test
    public void testFindeChildrenOidsByOid() {
    	OrganisaatioSearchOidType kysely = new OrganisaatioSearchOidType();
    	kysely.setSearchOid("1.2.2004.1");
    	
    	OrganisaatioOidListType vastaus = organisaatioService.findChildrenOidsByOid(kysely);
        LOG.info("FIND CHILDREN OID LIST ORG COUNT:" + vastaus.getOrganisaatioOidList().size());
        assertTrue(!vastaus.getOrganisaatioOidList().isEmpty());
    }

    @Test
    public void testFindAllOids() {
        OrganisaatioSearchOidType param = new OrganisaatioSearchOidType();
        param.setSearchOid("1.2.246.562.24.00000000001");

        OrganisaatioOidListType result = organisaatioService.findChildrenOidsByOid(param);
        assertTrue(result.getOrganisaatioOidList() != null && result.getOrganisaatioOidList().size() == 6);
    }

//    @Test
//    public void testSearchBasicOrganisaatios() throws GenericFault {
//
//        //Finding all organisaatios
//        /*OrganisaatioSearchCriteriaDTO searchCriteria = createOrgSearchCriteria(null, null, null, false, false, true, true,null);
//        List<OrganisaatioPerustietoType> result = organisaatioService.searchBasicOrganisaatios(searchCriteria);
//        assertEquals(6, result.size());*/
//
//        //Finding all koulutustoimijat with their children, i.e. with current test data this means all test organisaatios
//    	OrganisaatioSearchCriteriaDTO searchCriteria = createOrgSearchCriteria(fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi.KOULUTUSTOIMIJA.value(), null, null, true, true,null);
//    	List<OrganisaatioPerustietoType> result = organisaatioService.searchBasicOrganisaatios(searchCriteria);
//        assertEquals(6, result.size());
//
//        //List roots and all their children
//        ArrayList<String> oidList = new ArrayList<String>();
//        oidList.add("1.2.2004.1");
//        oidList.add("1.2.2004.5");
//        searchCriteria = createOrgSearchCriteria(null, null, null, true, true,oidList);
//        result = organisaatioService.searchBasicOrganisaatios(searchCriteria);
//        assertEquals(6, result.size()); //XXX earlier this was 4, but if restriction is 1 and 5 it should match them + everything under them (=6), no?
//
//        //List oids from middle of the hierarchy and their children
//        oidList = new ArrayList<String>();
//        oidList.add("1.2.2004.3");
//        searchCriteria = createOrgSearchCriteria(null, null, null, true, true,oidList);
//        result = organisaatioService.searchBasicOrganisaatios(searchCriteria);
//        assertEquals(2, result.size());
//
//        //Finding all organisaatios with bar in name with parents
//        searchCriteria = createOrgSearchCriteria(null, null, "bar", true, true, null);
//        result = organisaatioService.searchBasicOrganisaatios(searchCriteria);
//        assertEquals(5, result.size());
//
//        //Asserting that if searching for ytunnus searchStr bar returns empty
////        searchCriteria = createOrgSearchCriteria(null, null, "bar", true, true,null);
////        result = organisaatioService.searchBasicOrganisaatios(searchCriteria);
////        assertEquals(0, result.size());
//
//        //Finding organisaatio with y-tunnus 1234567-1 with parents and children
//        searchCriteria = createOrgSearchCriteria(null, null, "1234567-1", true, true,null);
//        result = organisaatioService.searchBasicOrganisaatios(searchCriteria);
//        assertEquals(4, result.size());
//
//        //Finding only currently active organisaatios with parents and children
//        /*searchCriteria = createOrgSearchCriteria(null, null, null, false, false, false, false,null);
//        result = organisaatioService.searchBasicOrganisaatios(searchCriteria);
//        assertEquals(4, result.size());*/
//
//        //Finding only organisaatios that are of oppilaitostyyppi Ammattikorkeakoulut with parents and children
//        searchCriteria = createOrgSearchCriteria(null, "Ammattikorkeakoulut", null, true, true,null);
//        result = organisaatioService.searchBasicOrganisaatios(searchCriteria);
//        assertEquals(4, result.size());
//   
//   		//Search with only restriction list
//        searchCriteria = createOrgSearchCriteria(null,  null,  null, true,  true,  Lists.newArrayList("1.2.2004.3"));
//        result = organisaatioService.searchBasicOrganisaatios(searchCriteria);
//        assertEquals(2, result.size());
//
//        //search for string "bar" with restrictions
//        searchCriteria = createOrgSearchCriteria(null, null, "bar", true, true, Lists.newArrayList("1.2.2004.3"));
//        result = organisaatioService.searchBasicOrganisaatios(searchCriteria);
//        assertEquals(2, result.size());
//        //assertEquals(1, result.size()); //XXX no parent? //XXX wtf?
//    }

    @Test
    public void testFindByOid() throws GenericFault {
        LOG.info("testFindByOid() --- ");

        OrganisaatioDTO curOrg = organisaatioService.findByOid("1.2.2004.1");
        LOG.info("version={}", curOrg.getVersion());

        assertEquals("root test koulutustoimija", getNimiValue("fi", curOrg.getNimi()));

        curOrg.setNimi(setNimiValue("fi", getNimiValue("fi", curOrg.getNimi()) + "."));
        curOrg = organisaatioService.updateOrganisaatio(curOrg, false);
        LOG.info("DTO version after update={}", curOrg.getVersion());

        curOrg.setNimi(setNimiValue("fi", getNimiValue("fi", curOrg.getNimi()) + "."));
        curOrg = organisaatioService.updateOrganisaatio(curOrg, false);
        LOG.info("DTO version after update={}", curOrg.getVersion());

        curOrg.setNimi(setNimiValue("fi", getNimiValue("fi", curOrg.getNimi()) + "."));
        curOrg = organisaatioService.updateOrganisaatio(curOrg, false);
        LOG.info("DTO version after update={}", curOrg.getVersion());

        try {
        curOrg.setVersion(21984);
        LOG.info("DTO version changed to ==> {} - save should fail now.", curOrg.getVersion());

        // THIS SHOULD FAIL!
        curOrg = organisaatioService.updateOrganisaatio(curOrg, false);
        LOG.info("DTO version after update={}", curOrg.getVersion());
        //IF THIS IS EXECUTED OPTIMISTIC LOCKING DOES NOT WORK
        fail("OPTIMISTIC LOCKING DOES NOT WORK");
        } catch (Exception exp) {
            assertTrue("OPTIMISTIC LOCKING WORKS",true);
        }

    }

    @Test
    public void testFindByOidList() {
        List<OrganisaatioDTO> result = organisaatioService.findByOidList(Arrays.asList("1.2.2004.1", null, "not_existing_oid"),5);
        assertTrue(result.size() > 0);
    }

    @Test
    public void testFindChildren() {
        OrganisaatioDTO curOrg = organisaatioService.findByOid("1.2.2004.1");
        assertEquals("root test koulutustoimija", getNimiValue("fi", curOrg.getNimi()));
        List<OrganisaatioDTO> children = organisaatioService.findChildrenTo(curOrg.getOid());
        assertEquals(2, children.size());
    }

    @Test
    public void testKuvailevatTiedot() throws GenericFault{
        OrganisaatioDTO organisaatio = organisaatioService.findByOid("1.2.2004.1");
        
        EmailDTO emailOrg = new EmailDTO();
        emailOrg.setEmail("email1@foo.bar");
        emailOrg.setYhteystietoOid("org-oid");
        
        organisaatio.getYhteystiedot().add(emailOrg);
        
        OrganisaatioKuvailevatTiedotTyyppi kuvailevatTiedot = new OrganisaatioKuvailevatTiedotTyyppi();
       
        HakutoimistoTyyppi hakutoimistoTyyppi = new HakutoimistoTyyppi();
        YhteyshenkiloTyyppi yhteyshenkilo = new YhteyshenkiloTyyppi();
        yhteyshenkilo.setEmail("email");
        yhteyshenkilo.setKokoNimi("Koko Nimi");
        yhteyshenkilo.setPuhelin("puhelin");
        yhteyshenkilo.setTitteli("Titteli");
        hakutoimistoTyyppi.setEctsYhteyshenkilo(yhteyshenkilo);
        
        
        MonikielinenTekstiTyyppi nimi = new MonikielinenTekstiTyyppi();
        Teksti teksti = new Teksti();
        teksti.setKieliKoodi("fi");
        teksti.setValue("nimi");
        
        nimi.getTeksti().add(teksti);
        hakutoimistoTyyppi.setOpintotoimistoNimi(nimi);
        
        kuvailevatTiedot.setHakutoimisto(hakutoimistoTyyppi);
        
        //some linkit
        SoMeLinkkiTyyppi some1 = new SoMeLinkkiTyyppi();
        some1.setSisalto("http://www.foo.bar/");
        some1.setTyyppi(SoMeLinkkiTyyppiTyyppi.GOOGLE_PLUS);
        
        kuvailevatTiedot.getSoMeLinkit().add(some1);

        organisaatio.setKuvailevatTiedot(kuvailevatTiedot);
        
        //attach kuva
        OrganisaatioKuvaTyyppi kuva = new OrganisaatioKuvaTyyppi();
        kuva.setFileName("filename");
        kuva.setMimeType("mime/type");
        kuva.setKuva(new byte[]{0,1,2});
        kuvailevatTiedot.setKuva(kuva);
        
        //attach email
        EmailDTO email = new EmailDTO();
        email.setEmail("me@here.bar");
        email.setYhteystietoOid("1");
        kuvailevatTiedot.getHakutoimisto().getOpintotoimistoYhteystiedot().add(email);
       
        //attach osoite
        final OsoiteDTO osoite = new OsoiteDTO();
        osoite.setMaa("suomi");
        osoite.setYhteystietoOid("2");
        osoite.setOsoiteTyyppi(OsoiteTyyppi.KAYNTI);
        osoite.setPostinumero("12345");
        osoite.setPostitoimipaikka("Helsinki");
        osoite.setOsoite("jokukatu 2");
        kuvailevatTiedot.getHakutoimisto().getOpintotoimistoYhteystiedot().add(osoite);

        //attach another osoite
        final OsoiteDTO osoite2 = new OsoiteDTO();
        osoite2.setMaa("suomi");
        osoite2.setYhteystietoOid("3");
        osoite2.setOsoiteTyyppi(OsoiteTyyppi.POSTI);
        osoite2.setPostinumero("12345");
        osoite2.setPostitoimipaikka("Helsinki");
        osoite2.setOsoite("jokukatu 2");
        kuvailevatTiedot.getHakutoimisto().getOpintotoimistoYhteystiedot().add(osoite2);

        //attach kuvailevat lisätiedot
        KuvailevaTietoTyyppi ktt = new KuvailevaTietoTyyppi();
        ktt.setTyyppi(KuvailevaTietoTyyppiTyyppi.AIEMMIN_HANKITTU_OSAAMINEN);
        MonikielinenTekstiTyyppi container = new MonikielinenTekstiTyyppi();
        Teksti sisaltoFi = new Teksti();
        sisaltoFi.setKieliKoodi("fi");
        sisaltoFi.setValue("kuvaileva teksti");
        container.getTeksti().add(sisaltoFi);
        ktt.setSisalto(container);
        kuvailevatTiedot.getVapaatKuvaukset().add(ktt);
        
        organisaatio = organisaatioService.updateOrganisaatio(organisaatio, false);

        assertNotNull("no kuvailevattiedot found!", organisaatio.getKuvailevatTiedot());
 
        //validate hakutoimisto
        //nimi
        assertEquals("Count did not match", 1, organisaatio.getKuvailevatTiedot().getHakutoimisto().getOpintotoimistoNimi().getTeksti().size());
        assertEquals("Kielikoodi did not match", "fi", organisaatio.getKuvailevatTiedot().getHakutoimisto().getOpintotoimistoNimi().getTeksti().get(0).getKieliKoodi());
        assertEquals("Nimi did not match", "nimi", organisaatio.getKuvailevatTiedot().getHakutoimisto().getOpintotoimistoNimi().getTeksti().get(0).getValue());

        //yhteystiedot
        assertEquals("Count did not match", 3, organisaatio.getKuvailevatTiedot().getHakutoimisto().getOpintotoimistoYhteystiedot().size());
        List<String> ytOidList = Lists.newArrayList("1","2","3");

        for(YhteystietoDTO yt: organisaatio.getKuvailevatTiedot().getHakutoimisto().getOpintotoimistoYhteystiedot()) {
            assertTrue("Unknown oid!", ytOidList.contains(yt.getYhteystietoOid()));
        }

        YhteyshenkiloTyyppi yhenkilo = organisaatio.getKuvailevatTiedot().getHakutoimisto().getEctsYhteyshenkilo();
        
        assertNotNull("ECTS Yhteyshenkilö not present", yhenkilo);
        assertEquals("Wrong email", "email", yhenkilo.getEmail());
        assertEquals("Wrong nimi", "Koko Nimi", yhenkilo.getKokoNimi());
        assertEquals("Wrong puhelin", "puhelin", yhenkilo.getPuhelin());
        assertEquals("Wrong titteli", "Titteli", yhenkilo.getTitteli());
        
        //validate kuva
        assertNotNull("No kuva found!", organisaatio.getKuvailevatTiedot().getKuva());
        assertEquals("Filename mismatch", "filename", organisaatio.getKuvailevatTiedot().getKuva().getFileName());
        assertEquals("Mime type mismatch", "mime/type", organisaatio.getKuvailevatTiedot().getKuva().getMimeType());
        assertTrue("Image data mismatch", Arrays.equals(new byte[]{0,1,2},organisaatio.getKuvailevatTiedot().getKuva().getKuva()));
        
        //validate some linkit
        assertEquals("some link count mismatch", 1, organisaatio.getKuvailevatTiedot().getSoMeLinkit().size());
        assertEquals("some link content mismatch", "http://www.foo.bar/", organisaatio.getKuvailevatTiedot().getSoMeLinkit().get(0).getSisalto());
        
        //validate vapaat kuvaukset
        assertEquals("kuvailevat tiedot count mismatch", 1, organisaatio.getKuvailevatTiedot().getVapaatKuvaukset().size());
        assertEquals("kuvailevat tiedot tyyppi mismatch", KuvailevaTietoTyyppiTyyppi.AIEMMIN_HANKITTU_OSAAMINEN, organisaatio.getKuvailevatTiedot().getVapaatKuvaukset().get(0).getTyyppi());
        assertEquals("kuvailevat tiedot content lang mismatch", "fi", organisaatio.getKuvailevatTiedot().getVapaatKuvaukset().get(0).getSisalto().getTeksti().get(0).getKieliKoodi());
        assertEquals("kuvailevat tiedot content value mismatch", "kuvaileva teksti", organisaatio.getKuvailevatTiedot().getVapaatKuvaukset().get(0).getSisalto().getTeksti().get(0).getValue());
               
        //change kuva
        organisaatio.getKuvailevatTiedot().getKuva().setFileName("filename2");
        organisaatio.getKuvailevatTiedot().getKuva().setMimeType("mime/type2");
        organisaatio.getKuvailevatTiedot().getKuva().setKuva(new byte[]{2,1,0});
        organisaatio = organisaatioService.updateOrganisaatio(organisaatio, false);

        //validate changed kuva
        assertNotNull("No kuva found!", organisaatio.getKuvailevatTiedot().getKuva());
        assertEquals("Filename mismatch", "filename2", organisaatio.getKuvailevatTiedot().getKuva().getFileName());
        assertEquals("Mime type mismatch", "mime/type2", organisaatio.getKuvailevatTiedot().getKuva().getMimeType());
        assertTrue("Image data mismatch", Arrays.equals(new byte[]{2,1,0},organisaatio.getKuvailevatTiedot().getKuva().getKuva()));

        //add some link, same type as the earlier
        SoMeLinkkiTyyppi some2 = new SoMeLinkkiTyyppi();
        some2.setSisalto("http://www.fase.bar/");
        some2.setTyyppi(SoMeLinkkiTyyppiTyyppi.MUU);
        SoMeLinkkiTyyppi some3 = new SoMeLinkkiTyyppi();
        some3.setSisalto("http://www.fase.bar/");
        some3.setTyyppi(SoMeLinkkiTyyppiTyyppi.MUU);
        
        organisaatio.getKuvailevatTiedot().getSoMeLinkit().add(some2);
        organisaatio.getKuvailevatTiedot().getSoMeLinkit().add(some3);
        organisaatio = organisaatioService.updateOrganisaatio(organisaatio, false);

        //validate some link count
        assertEquals("some link count mismatch", 3, organisaatio.getKuvailevatTiedot().getSoMeLinkit().size());

        
        //add language to existing kuvaus
        Teksti kuvaileva = new Teksti();
        kuvaileva.setKieliKoodi("en");
        kuvaileva.setValue("en-value");
        organisaatio.getKuvailevatTiedot().getVapaatKuvaukset().get(0).getSisalto().getTeksti().add(kuvaileva);
        organisaatio = organisaatioService.updateOrganisaatio(organisaatio, false);

        //validate vapaa kuvaus
        assertEquals("kuvailevat tiedot count mismatch", 1, organisaatio.getKuvailevatTiedot().getVapaatKuvaukset().size());
        assertEquals("kuvailevat tiedot tyyppi mismatch", KuvailevaTietoTyyppiTyyppi.AIEMMIN_HANKITTU_OSAAMINEN, organisaatio.getKuvailevatTiedot().getVapaatKuvaukset().get(0).getTyyppi());
        assertEquals("incorrect number of languages", 2, organisaatio.getKuvailevatTiedot().getVapaatKuvaukset().get(0).getSisalto().getTeksti().size());

        //remove language from existing kuvaus
        organisaatio.getKuvailevatTiedot().getVapaatKuvaukset().get(0).getSisalto().getTeksti().remove(0);
        organisaatio = organisaatioService.updateOrganisaatio(organisaatio, false);
        
        //validate
        assertEquals("kuvailevat tiedot count mismatch", 1, organisaatio.getKuvailevatTiedot().getVapaatKuvaukset().size());
        assertEquals("kuvailevat tiedot tyyppi mismatch", KuvailevaTietoTyyppiTyyppi.AIEMMIN_HANKITTU_OSAAMINEN, organisaatio.getKuvailevatTiedot().getVapaatKuvaukset().get(0).getTyyppi());
        assertEquals("incorrect number of languages", 1, organisaatio.getKuvailevatTiedot().getVapaatKuvaukset().get(0).getSisalto().getTeksti().size());
        
        //update kuvailevat tiedot
        ktt = new KuvailevaTietoTyyppi();
        ktt.setTyyppi(KuvailevaTietoTyyppiTyyppi.ESTEETOMYYS);
        container = new MonikielinenTekstiTyyppi();
        sisaltoFi = new Teksti();
        sisaltoFi.setKieliKoodi("fi");
        sisaltoFi.setValue("kuvaileva teksti");
        container.getTeksti().add(sisaltoFi);
        ktt.setSisalto(container);
        organisaatio.getKuvailevatTiedot().getVapaatKuvaukset().add(ktt);
    

        organisaatio = organisaatioService.updateOrganisaatio(organisaatio, false);
        //validate kuvaus count
        assertEquals("kuvailevat tiedot count mismatch", 2, organisaatio.getKuvailevatTiedot().getVapaatKuvaukset().size());
  
        //change nimi
        nimi = new MonikielinenTekstiTyyppi();
        teksti = new Teksti();
        teksti.setKieliKoodi("en");
        teksti.setValue("name");
        nimi.getTeksti().add(teksti);
        organisaatio.getKuvailevatTiedot().getHakutoimisto().setOpintotoimistoNimi(nimi);

        organisaatio = organisaatioService.updateOrganisaatio(organisaatio, false);

        //validate changed nimi
        assertEquals("Count did not match", 1, organisaatio.getKuvailevatTiedot().getHakutoimisto().getOpintotoimistoNimi().getTeksti().size());
        assertEquals("Kielikoodi did not match", "en", organisaatio.getKuvailevatTiedot().getHakutoimisto().getOpintotoimistoNimi().getTeksti().get(0).getKieliKoodi());
        assertEquals("Nimi did not match", "name", organisaatio.getKuvailevatTiedot().getHakutoimisto().getOpintotoimistoNimi().getTeksti().get(0).getValue());

        //change osoite
        organisaatio.getKuvailevatTiedot().getHakutoimisto().getOpintotoimistoYhteystiedot().remove(0);
        
        organisaatio = organisaatioService.updateOrganisaatio(organisaatio, false);

        //verify osoite removed
        assertEquals("Count did not match", 2, organisaatio.getKuvailevatTiedot().getHakutoimisto().getOpintotoimistoYhteystiedot().size());

        //poista kuva
        organisaatio.getKuvailevatTiedot().setKuva(null);
        organisaatio = organisaatioService.updateOrganisaatio(organisaatio, false);
        Assert.assertNull(organisaatio.getKuvailevatTiedot().getKuva());
        
    }
    
    /*Tests that current parent and children of organisaatio is found correctly*/
    @Test
    @Ignore
    public void testGetCurrentParent() throws GenericFault {
        
        //Creating test organisaatios (three parents and a child)
        OrganisaatioDTO root1 = OrganisaatioTstUtils.buildOrganisaatio("nimi1", "1111111-2");
        root1.getYhteystiedot().addAll(OrganisaatioTstUtils.DEFAULT_YHTEYSTIEDOT);
        root1.getTyypit().add(OrganisaatioTyyppi.KOULUTUSTOIMIJA);
        root1 = organisaatioService.createOrganisaatio(root1, false);
        
        Organisaatio root1E = this.organisaatioDAO.findByOid(root1.getOid());
        
        OrganisaatioDTO root2 = OrganisaatioTstUtils.buildOrganisaatio("nimi2", "1111111-3");
        root2.getYhteystiedot().addAll(OrganisaatioTstUtils.DEFAULT_YHTEYSTIEDOT);
        root2.getTyypit().add(OrganisaatioTyyppi.KOULUTUSTOIMIJA);
        root2 = organisaatioService.createOrganisaatio(root2, false);
        
        Organisaatio root2E = this.organisaatioDAO.findByOid(root2.getOid());
        
        OrganisaatioDTO root3 = OrganisaatioTstUtils.buildOrganisaatio("nimi3", "1111111-4");
        root3.getYhteystiedot().addAll(OrganisaatioTstUtils.DEFAULT_YHTEYSTIEDOT);
        root3.getTyypit().add(OrganisaatioTyyppi.KOULUTUSTOIMIJA);
        root3 = organisaatioService.createOrganisaatio(root3, false);
        
        Organisaatio root3E = this.organisaatioDAO.findByOid(root3.getOid());
        
        OrganisaatioDTO child = OrganisaatioTstUtils.buildOrganisaatio("nimi4", null);
        child.getYhteystiedot().addAll(OrganisaatioTstUtils.DEFAULT_YHTEYSTIEDOT);
        child.getTyypit().add(OrganisaatioTyyppi.OPPILAITOS);
        child.setOppilaitosKoodi("13245");
        child.setParentOid(root1.getOid());
        child = organisaatioService.createOrganisaatio(child, false);
        
        Organisaatio childE = this.organisaatioDAO.findByOid(child.getOid());
        
        
        /*Creating the organisaatioSuhde entities:
         * child's parent is root1 in the past, root2 currently, and root3 in the future
         */
        Calendar pastStart = Calendar.getInstance();
        pastStart.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR) - 2);
        
        Calendar currentStart = Calendar.getInstance();
        currentStart.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR) - 1);
        
        Calendar futureStart = Calendar.getInstance();
        futureStart.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR) + 1);
        
        OrganisaatioSuhde suhde = this.organisaatioSuhdeDAO.findChildrenTo(root1E.getId(), null).get(0);//addChild(root1E.getId(), childE.getId(), pastStart.getTime());
        suhde.setAlkuPvm(pastStart.getTime());
        this.organisaatioSuhdeDAO.update(suhde);
        this.organisaatioSuhdeDAO.addChild(root2E.getId(), childE.getId(), currentStart.getTime(), null);
        this.organisaatioSuhdeDAO.addChild(root3E.getId(), childE.getId(), futureStart.getTime(), null);
        childE.setParentSuhteet(this.organisaatioSuhdeDAO.findBy("child", childE));
        
        
        /*
         * Asserting that the parent child relationship is found correctly
         */
        child = this.organisaatioService.findByOid(child.getOid());
        
        assertTrue(root2.getOid().equals(child.getParentOid()));
        
        OrganisaatioSearchOidType param = new OrganisaatioSearchOidType();
        param.setSearchOid(root2.getOid());
        assertTrue(this.organisaatioService.findChildrenOidsByOid(param).getOrganisaatioOidList().get(0).getOrganisaatioOid().equals(child.getOid()));
       
        param.setSearchOid(root1.getOid());
        assertTrue(this.organisaatioService.findChildrenOidsByOid(param).getOrganisaatioOidList().isEmpty());
        
        param.setSearchOid(root3.getOid());
        assertTrue(this.organisaatioService.findChildrenOidsByOid(param).getOrganisaatioOidList().isEmpty());
    }
    
    private void assertDateRange(Date expectedFrom, Date expectedTo, String oid) {
    	OrganisaatioDTO od = organisaatioService.findByOid(oid);
    	assertEquals(expectedFrom, od.getAlkuPvm());
    	assertEquals(expectedTo, od.getLakkautusPvm());
    }
    
    private void setDateRange(Date from, Date to, String oid) throws Exception {
    	OrganisaatioDTO od = organisaatioService.findByOid(oid);
    	od.setAlkuPvm(from);
    	od.setLakkautusPvm(to);
    	organisaatioService.updateOrganisaatio(od, false);
    }
    
    @Test
    public void testHierarchyDateModifications() throws Exception {
    	Date t0 = new GregorianCalendar(2000, 1,1).getTime();
    	Date t1 = new GregorianCalendar(2010, 1,1).getTime();
    	Date t2 = new GregorianCalendar(2020, 1,1).getTime();
    	Date t3 = new GregorianCalendar(2030, 1,1).getTime();
    	
        OrganisaatioDTO root1 = OrganisaatioTstUtils.buildOrganisaatio("nimi1", "1111111-2");
        root1.getYhteystiedot().addAll(OrganisaatioTstUtils.DEFAULT_YHTEYSTIEDOT);
        root1.getTyypit().add(OrganisaatioTyyppi.KOULUTUSTOIMIJA);
        root1.setAlkuPvm(t0);
        root1.setLakkautusPvm(t2);
        root1 = organisaatioService.createOrganisaatio(root1, false);
        assertDateRange(t0, t2, root1.getOid());
        
        OrganisaatioDTO sub1 = OrganisaatioTstUtils.buildOrganisaatio("nimi2", "1111112-2");
        sub1.getYhteystiedot().addAll(OrganisaatioTstUtils.DEFAULT_YHTEYSTIEDOT);
        sub1.getTyypit().add(OrganisaatioTyyppi.OPPILAITOS);
        sub1.setAlkuPvm(t0);
        sub1.setLakkautusPvm(t1);
        sub1.setParentOid(root1.getOid());
        sub1 = organisaatioService.createOrganisaatio(sub1, false);
        assertDateRange(t0, t1, sub1.getOid());
        
        OrganisaatioDTO sub2 = OrganisaatioTstUtils.buildOrganisaatio("nimi3", "1111113-2");
        sub2.getYhteystiedot().addAll(OrganisaatioTstUtils.DEFAULT_YHTEYSTIEDOT);
        sub2.getTyypit().add(OrganisaatioTyyppi.OPPILAITOS);
        sub2.setAlkuPvm(t0);
        sub2.setLakkautusPvm(t2);
        sub2.setParentOid(root1.getOid());
        sub2 = organisaatioService.createOrganisaatio(sub2, false);
        assertDateRange(t0, t2, sub2.getOid());

        setDateRange(t1, t3, root1.getOid());
        assertDateRange(t1, t3, root1.getOid());        
        assertDateRange(t0, t1, sub1.getOid());
        assertDateRange(t0, t2, sub2.getOid());

        setDateRange(t0, t2, root1.getOid());
        assertDateRange(t0, t2, root1.getOid());
        assertDateRange(t0, t1, sub1.getOid());
        assertDateRange(t0, t2, sub2.getOid());

        setDateRange(t0, t1, root1.getOid());
        assertDateRange(t0, t1, root1.getOid());
        assertDateRange(t0, t1, sub1.getOid());
        assertDateRange(t0, t2, sub2.getOid());

        setDateRange(t1, t3, root1.getOid());
        assertDateRange(t1, t3, root1.getOid());
        assertDateRange(t0, t1, sub1.getOid());
        assertDateRange(t0, t2, sub2.getOid());
        
    }
    
    @Test
    public void testProtectedResources() throws GenericFault{
        OrganisaatioDTO org = OrganisaatioTstUtils.createKoulutustoimija(organisaatioService, "testemove", "1234567-8", "yhteyshlo", "oid");
        //unauthenticated user
        setAuthentication(null);

        try {
            organisaatioService.createOrganisaatio(org, true);
            fail("unauthenticated user should not be able to access the service");
        } catch (NotAuthorizedException rte) {
            assertNoPermission(rte);
        }

        try {
            organisaatioService.updateOrganisaatio(org, true);
            fail("unauthenticated user should not be able to access the service");
        } catch (NotAuthorizedException rte) {
            assertNoPermission(rte);
        }

        try {
            organisaatioService.removeOrganisaatioByOid(new RemoveByOidType("oid"));
            fail("unauthenticated user should not be able to access the service");
        } catch (NotAuthorizedException rte) {
            assertNoPermission(rte);
        }
        
        try {
            organisaatioService.createYhteystietojenTyyppi(null);
            fail("unauthenticated user should not be able to access the service");
        } catch (NotAuthorizedException rte) {
            assertNoPermission(rte);
        }

        try {
            organisaatioService.updateYhteystietojenTyyppi(null);
            fail("unauthenticated user should not be able to access the service");
        } catch (NotAuthorizedException rte) {
            assertNoPermission(rte);
        }

        try {
            organisaatioService.removeYhteystietojenTyyppiByOid("oid");
            fail("unauthenticated user should not be able to access the service");
        } catch (NotAuthorizedException rte) {
            assertNoPermission(rte);
        }
    }
    
    
    /**
     * Test detailed rules, dates, name, type
     * @throws GenericFault 
     */
    @Test
    public void testProtectedResourcesVirkailija() throws GenericFault{
        //1.2.2004.2 = oppilaitos
        
        setCurrentUser("useroid", super.getAuthority("APP_" + OrganisaatioPermissionServiceImpl.ORGANISAATIOHALLINTA + "_CRUD", "1.2.2004.2"));

        //start date change for oppilaitos should not succeed
        OrganisaatioDTO organisaatio = organisaatioService.findByOid("1.2.2004.2");
        organisaatio.setAlkuPvm(new Date());
        try {
            organisaatio = organisaatioService.updateOrganisaatio(organisaatio, false);
            fail("Should not succeed!");
        } catch (NotAuthorizedException rte) {
            assertNoPermission(rte);
        }

        //end date change for oppilaitos should not succeed
        organisaatio = organisaatioService.findByOid("1.2.2004.2");
        organisaatio.setLakkautusPvm(new Date());
        try {
            organisaatio = organisaatioService.updateOrganisaatio(organisaatio, false);
            fail("Should not succeed!");
        } catch (NotAuthorizedException rte) {
            assertNoPermission(rte);
        }
        
        //name change for oppilaitos should not succeed
        organisaatio = organisaatioService.findByOid("1.2.2004.2");
        organisaatio.getNimi().getTeksti().get(0).setValue("Uusi nimi");
        try {
            organisaatio = organisaatioService.updateOrganisaatio(organisaatio, false);
            fail("Should not succeed!");
        } catch (NotAuthorizedException rte) {
            assertNoPermission(rte);
        }
        
//        //add type koulutustoimija should not succeed
//        organisaatio = organisaatioService.findByOid("1.2.2004.2");
//        organisaatio.getTyypit().add(OrganisaatioTyyppi.KOULUTUSTOIMIJA);
//        try {
//            organisaatioService.updateOrganisaatio(organisaatio, false);
//            fail("Should not succeed!");
//        } catch (NotAuthorizedException rte) {
//            assertNoPermission(rte);
//        }


        // domain name change should succceed
        organisaatio = organisaatioService.findByOid("1.2.2004.2");
        organisaatio.setDomainNimi("uusi-domain.fi");
        try {
            organisaatioService.updateOrganisaatio(organisaatio, false);
        } catch (NotAuthorizedException rte) {
            fail("should succeed!");
        }

        //ytt add should not succeed
        try {
            organisaatioService.createYhteystietojenTyyppi(null);
            fail("Should not succeed!");
        } catch (NotAuthorizedException rte) {
            assertNoPermission(rte);
        }

        //ytt edit should not succeed
        try {
            organisaatioService.updateYhteystietojenTyyppi(null);
            fail("Should not succeed!");
        } catch (NotAuthorizedException rte) {
            assertNoPermission(rte);
        }
        
        //virkailija should be able to add toimipiste
        OrganisaatioDTO toimipiste = new OrganisaatioDTO();
        toimipiste.getTyypit().add(OrganisaatioTyyppi.OPETUSPISTE);
        toimipiste.setOid("toimipiste-oid");
        toimipiste.setParentOid("1.2.2004.2");
        toimipiste.getYhteystiedot().add(OrganisaatioTstUtils.DEFAULT_POSTIOSOITE);
        toimipiste.setNimi(new MonikielinenTekstiTyyppi(Lists.newArrayList(new Teksti("nimi",  "fi"))));
        organisaatio = organisaatioService.createOrganisaatio(toimipiste, false);

        //name change should be ok
        toimipiste.setNimi(new MonikielinenTekstiTyyppi(Lists.newArrayList(new Teksti("uudinimi",  "fi"))));
    }

    private void assertNoPermission(RuntimeException rte) {
        assertTrue(rte.getClass().getName(), rte.getMessage()!=null && rte.getMessage().equals("no.permission"));
    }
    
}

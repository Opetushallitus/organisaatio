package fi.vm.sade.organisaatio.integrationtest;

import static fi.vm.sade.organisaatio.integrationtest.OrganisaatioTstUtils.buildCreateOrganisaatioModel;
import static fi.vm.sade.organisaatio.integrationtest.OrganisaatioTstUtils.buildYtunnus;
import static fi.vm.sade.organisaatio.integrationtest.OrganisaatioTstUtils.createKoulutustoimija;
import static fi.vm.sade.organisaatio.integrationtest.OrganisaatioTstUtils.createOrganisaatio;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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

import fi.vm.sade.organisaatio.SecurityAwareTestBase;
import fi.vm.sade.organisaatio.api.model.GenericFault;
import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi.Teksti;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.model.types.OsoiteDTO;
import fi.vm.sade.organisaatio.api.model.types.OsoiteTyyppi;
import fi.vm.sade.organisaatio.api.model.types.PuhelinNumeroTyyppi;
import fi.vm.sade.organisaatio.api.model.types.SearchCriteriaDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoArvoDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoElementtiDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoElementtiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.YhteystietojenTyyppiDTO;
import fi.vm.sade.organisaatio.dao.impl.YhteystietoArvoDAOImpl;
import fi.vm.sade.organisaatio.dao.impl.YhteystietoDAOImpl;
import fi.vm.sade.organisaatio.dao.impl.YhteystietoElementtiDAOImpl;
import fi.vm.sade.organisaatio.dao.impl.YhteystietojenTyyppiDAOImpl;
import fi.vm.sade.organisaatio.model.YhteystietoArvo;
import fi.vm.sade.organisaatio.model.YhteystietoElementti;
import fi.vm.sade.organisaatio.service.OrganisaatioCrudException;
import fi.vm.sade.organisaatio.service.converter.ConverterFactory;
//service.model.OrganisaatioService;

/**
 * @author Antti
 */
@ContextConfiguration(locations = {
        "classpath:spring/test-context.xml"
})
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ActiveProfiles("embedded-solr")
public class YhteystietojenTyyppiServiceTest extends SecurityAwareTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(YhteystietojenTyyppiServiceTest.class);

    int defaultYhteystiedot = 6; // how many Yhteystieto's will be saved to organisaatio by default (postiosoite+käyntiosoite+www+email+puhnro+faksi) = 6
    @Autowired
    protected OrganisaatioService organisaatioService;

    @Autowired
    TestDataCreator dataUtil;

    @Autowired
    private ConverterFactory converterFactory;
    @Autowired
    protected YhteystietojenTyyppiDAOImpl yhteystietojenTyyppiDAO;
    @Autowired
    protected YhteystietoElementtiDAOImpl yhteystietoElementtiDAO;

    @Autowired
    YhteystietoArvoDAOImpl yhteystietoArvoDAO;
    @Autowired
    YhteystietoDAOImpl yhteystietoDAO;

    @Before
    public void setLocale() {
        Locale.setDefault(new Locale("fi")); // because of validaton messages
        dataUtil.createInitialTestData();
    }

    @After
    public void deleteLisatiedot() throws GenericFault { // other tests will fail after this if there are lisatiedot in the database if not cleaned
        try {
            for (YhteystietojenTyyppiDTO lisatiedot : organisaatioService.findYhteystietojenTyyppis(new SearchCriteriaDTO())) {//organisaatioService.findYhteystietoArvosForOrganisaatio(organisaatioOid)//yhteystietojenTyyppiService.findAll()) {
                lisatiedot.getSovellettavatOrganisaatios().clear();
                organisaatioService.updateYhteystietojenTyyppi(lisatiedot);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Ignore
    @Test
    public void roundtrip_metadata() throws GenericFault {//ValidationException, OrganisaatioHierarchyException {
        // luodaan muu-tyyppinen organisaatio, tarvitaan myöhemmin
        OrganisaatioDTO muuOrg = createOrganisaatio(organisaatioService, true, null, null, asList(OrganisaatioTyyppi.MUU_ORGANISAATIO.value()));

        // validointia tehdään kun yritetään tallentaa tyhjä lisätiedot
        YhteystietojenTyyppiDTO dto = new YhteystietojenTyyppiDTO();
        try {
            //yhteystietojenTyyppiService.insert(dto);
            organisaatioService.createYhteystietojenTyyppi(dto);
            fail("should fail");
        } catch (GenericFault e) {//ValidationException e) {
            // caught expected exception
            if (e.getMessage().contains("organisaatio.hierarchy.exception")) {
                throw e;
            }
        }

        // luodaan organisaation lisatiedot
        MonikielinenTekstiTyyppi nimi = new MonikielinenTekstiTyyppi();
        Teksti nimiFi = new Teksti();
        nimiFi.setKieliKoodi("fi");
        nimiFi.setValue("rehtori");
        nimi.getTeksti().add(nimiFi);
        Teksti nimiSv = new Teksti();
        nimiSv.setKieliKoodi("sv");
        nimiSv.setValue("rektor");
        nimi.getTeksti().add(nimiSv);
        Teksti nimiEn = new Teksti();
        nimiEn.setKieliKoodi("en");
        nimiEn.setValue("principal");
        nimi.getTeksti().add(nimiEn);
        dto.setNimi(nimi);
        //dto.//setNimiTieto(createYTEl(false, "nimi", TEKSTI));
        //setNimikeTieto(createYTEl(false, "nimike", TEKSTI));
        dto.getAllLisatietokenttas().add(createYTEl(false, "nimi", YhteystietoElementtiTyyppi.TEKSTI));
        dto.getAllLisatietokenttas().add(createYTEl(false, "nimike", YhteystietoElementtiTyyppi.TEKSTI));

        dto.getAllLisatietokenttas().add(createYTEl(true, "postiosoite", YhteystietoElementtiTyyppi.OSOITE));
        dto.getAllLisatietokenttas().add(createYTEl(true, "faksi", YhteystietoElementtiTyyppi.FAKSI));
        dto.getAllLisatietokenttas().add(createYTEl(false, "www", YhteystietoElementtiTyyppi.WWW));
        dto.getSovellettavatOrganisaatios().add(OrganisaatioTyyppi.MUU_ORGANISAATIO);//new OrganisaatiotyypinYhteystiedotDTO(organisaatioService.findOrganisaatioTyyppi(MUU_STRING)));
        dto.getSovellettavatOrganisaatios().add(OrganisaatioTyyppi.OPETUSPISTE);
        dto.setOid("" + System.currentTimeMillis() + "" + Math.random());
        //new OrganisaatiotyypinYhteystiedotDTO(organisaatioService.findOrganisaatioTyyppi(OPETUSPISTE_STRING)));
        YhteystietojenTyyppiDTO original = dto;
        dto = organisaatioService.createYhteystietojenTyyppi(dto);//yhteystietojenTyyppiService.insert(dto);
        assertNotNull(dto);

        // findataan lisätiedot
        assertEquals(1, organisaatioService.findYhteystietojenTyyppis(new SearchCriteriaDTO()).size());//yhteystietojenTyyppiService.findAll().size());
        dto = organisaatioService.readYhteystietojenTyyppi(dto.getOid());//yhteystietojenTyyppiService.read(dto.getId());

        // ennen muutoksia assertoidaan muutettavien kenttien nykyinen tila kannasta
        assertEquals(1, yhteystietoElementtiDAO.findByLisatietoIdAndKentanNimi(dto.getOid(), "faksi").size());
        //assertEquals(2, organisaatiotyypinYhteystiedotDAO.findAll().size());
        assertEquals(false, yhteystietoElementtiDAO.findByLisatietoIdAndKentanNimi(dto.getOid(), "nimi").get(0).isPakollinen());
        // muutetaan lisätietoja
        change(dto);





        // update changes
        organisaatioService.updateYhteystietojenTyyppi(dto);//yhteystietojenTyyppiService.update(dto);
        dto = organisaatioService.readYhteystietojenTyyppi(dto.getOid());//yhteystietojenTyyppiService.read(dto.getId());

        // tehdään originaaliin samat muutokset ja assertoidaan että se vastaa tuoretta insertoitua ja updatettua versiota
        change(original);
        original.setOid(dto.getOid());//setId(dto.getId()); // id:t pitää muuttaa käsin originaalista että equals voi onnistua
        //original.getSovellettavatOrganisaatios().get(0).setId(dto.getSovellettavatOrganisaatios().get(0).getId());
        original.getAllLisatietokenttas().get(0).setOid(dto.getAllLisatietokenttas().get(0).getOid());
        original.getAllLisatietokenttas().get(1).setOid(dto.getAllLisatietokenttas().get(1).getOid());
        original.getAllLisatietokenttas().get(2).setOid(dto.getAllLisatietokenttas().get(2).getOid());
        original.getAllLisatietokenttas().get(3).setOid(dto.getAllLisatietokenttas().get(3).getOid());

        assertEquals(original, dto);

        // assertoidaan kannan tila
        assertEquals(1, yhteystietojenTyyppiDAO.findAll().size());
        assertEquals(4, yhteystietoElementtiDAO.findAllKaytossa().size()); // nimi + postiosoite + www + puhelin - muut poistettu
        assertFalse(yhteystietoElementtiDAO.findByLisatietoIdAndKentanNimi(dto.getOid(), "faksi").get(0).isKaytossa()); // faksi on poistunut
        //assertEquals(1, .size()); // toinen sovellettava organisaatio poistunut
        assertEquals(true, yhteystietoElementtiDAO.findByLisatietoIdAndKentanNimi(dto.getOid(), "nimi").get(0).isPakollinen()); // nimi muuttunut pakolliseksi

        // muu-tyyppiselle pitäisi nyt saada haettua ym OrganisaationLisätiedot
        List<YhteystietojenTyyppiDTO> metadata = organisaatioService.findYhteystietoMetadataForOrganisaatio(getTyypitStr(muuOrg));
        assertEquals(1, metadata.size());
        assertEquals(dto, metadata.get(0));

        // oppilaitokselle taas ym lisätiedot ei pitäisi kuulua
        OrganisaatioDTO oppilaitos = createOrganisaatio(organisaatioService, true, asList(new String[]{OrganisaatioTyyppi.KOULUTUSTOIMIJA.value()}), asList(new String[]{OrganisaatioTyyppi.KOULUTUSTOIMIJA.value()}), asList(new String[]{OrganisaatioTyyppi.OPPILAITOS.value()}));
        assertEquals(0, organisaatioService.findYhteystietoMetadataForOrganisaatio(getTyypitStr(oppilaitos)).size());
    }

    private List<String> getTyypitStr(OrganisaatioDTO org) {
        List<String> tyypitStr = new ArrayList<String>();
        for (OrganisaatioTyyppi curOT : org.getTyypit()) {
            tyypitStr.add(curOT.value());
        }
        return tyypitStr;
    }

    private YhteystietoElementtiDTO createYTEl (boolean pakollinen, String nimi, YhteystietoElementtiTyyppi tyyppi) {
        YhteystietoElementtiDTO yeDto = new YhteystietoElementtiDTO();
        yeDto.setKaytossa(true);
        yeDto.setPakollinen(pakollinen);
        yeDto.setNimi(nimi);
        yeDto.setTyyppi(tyyppi);
        yeDto.setOid("" + System.currentTimeMillis() + "" + Math.random());
        return yeDto;
    }

    private void change(YhteystietojenTyyppiDTO dto) {
        // lisätään puhelin
        dto.getAllLisatietokenttas().add(createYTEl(true, "puhelin", YhteystietoElementtiTyyppi.PUHELIN));
        // poistetaan faksi
        YhteystietoElementtiDTO faksi = getLisatietokentta(dto, YhteystietoElementtiTyyppi.FAKSI, null);//dto.getLisatietokentta("faksi");
        dto.getAllLisatietokenttas().remove(faksi);
        // poistetaan nimike
        YhteystietoElementtiDTO nimike = getLisatietokentta(dto, YhteystietoElementtiTyyppi.TEKSTI, "nimike");
        dto.getAllLisatietokenttas().remove(nimike);
        // muutetaan nimi pakolliseksi
        YhteystietoElementtiDTO nimi = getLisatietokentta(dto, YhteystietoElementtiTyyppi.TEKSTI, "nimi");
        nimi.setPakollinen(true);
        // poistetaan opetuspiste litietyistä organisaatiotyypeistä
        //OrganisaatiotyypinYhteystiedotDTO sovOrg = dto.getSovellettavaOrganisaatio(OPETUSPISTE_STRING);
        dto.getSovellettavatOrganisaatios().remove(OrganisaatioTyyppi.OPETUSPISTE);
    }

    public YhteystietoElementtiDTO getLisatietokentta(YhteystietojenTyyppiDTO dto, YhteystietoElementtiTyyppi tyyppi, String nimi) {
        YhteystietoElementtiDTO ytEl = null;
        for (YhteystietoElementtiDTO curYtel : dto.getAllLisatietokenttas()) {
            if ((curYtel.getTyyppi().value().equals(tyyppi.value())) && !tyyppi.value().equals(YhteystietoElementtiTyyppi.TEKSTI.value())) {
                return curYtel;
            } else if (tyyppi.value().equals(YhteystietoElementtiTyyppi.TEKSTI.value()) && curYtel.getNimi().equals(nimi)) {
                return curYtel;
            }
        }
        return ytEl;
    }

    @Test
    public void roundtrip_arvot() throws GenericFault {
        OrganisaatioDTO parent = createKoulutustoimija(organisaatioService, "hjdfasyu", buildYtunnus(), "yhteyshlo", null);

        // luodaan organisaation lisätiedot -metadata määritykset
        //      -organisaatiotyyppi: opetuspiste
        //      -pakollinen tieto: nimi
        //      -pakollinen tieto: postiosoite
        YhteystietojenTyyppiDTO lisatiedot = createYTT("test lisatiedot");
        lisatiedot.getAllLisatietokenttas().add(createYTEl(true, "nimi", YhteystietoElementtiTyyppi.TEKSTI));
        lisatiedot.getAllLisatietokenttas().add(createYTEl(true, "postiosoite", YhteystietoElementtiTyyppi.OSOITE));
        lisatiedot = organisaatioService.createYhteystietojenTyyppi(lisatiedot);//yhteystietojenTyyppiService.insert(lisatiedot);

        int countArvot = yhteystietoArvoDAO.findAll().size();
        int countYhteystiedot = yhteystietoDAO.findAll().size();

        // luodaan uusi organisaatio tyyppiä opetuspiste, jolle asetetaan lisätietokenttien arvot
        OrganisaatioDTO model = buildCreateOrganisaatioModel(organisaatioService, buildYtunnus(), parent.getOid(), OrganisaatioTyyppi.OPPILAITOS.value());
        model.getYhteystietoArvos().add(createYTA(null, getLisatietokentta(lisatiedot, YhteystietoElementtiTyyppi.TEKSTI, "nimi").getOid(), new String[] {"foobar", "kieli_sv#1"}));
        model.getYhteystietoArvos().add(createYTA(null, getLisatietokentta(lisatiedot, YhteystietoElementtiTyyppi.OSOITE, null).getOid()  , OrganisaatioTstUtils.createOsoite(OsoiteTyyppi.POSTI, "katuosoite 1", "12345", "helsinki")));
        OrganisaatioDTO organisaatio = organisaatioService.createOrganisaatio(model, false);
        assertEquals("countArvot", countArvot+2, yhteystietoArvoDAO.findAll().size());
        int yhteystietoAfterCount = yhteystietoDAO.findAll().size();
        assertEquals("countyhteystiedot", countYhteystiedot+1, yhteystietoAfterCount);

        // haetaan lisätietokenttien arvot ja verifioidaan ne
        List<YhteystietoArvoDTO> arvos = organisaatioService.findYhteystietoArvosForOrganisaatio(organisaatio.getOid());
        assertEquals("arvos.size", 2, arvos.size());
        assertEquals("foobar", ((String[])(arvos.get(0).getArvo()))[0]);
        assertEquals("katuosoite 1", ((OsoiteDTO)arvos.get(1).getArvo()).getOsoite());

        // päivitetään arvot
        arvos.get(0).setArvo(new String[] {"foobar2", "kieli_fi#1" });
        ((OsoiteDTO)arvos.get(1).getArvo()).setOsoite("katuosoite 12");

        // tallennetaan ja verifioidaan muutokset (mutta kannassa sama määrä rivejä)
        model = organisaatioService.findByOid(organisaatio.getOid());
        setYhteystietoArvos(model, arvos);  //model.//setYhteystietoArvos(arvos);
        organisaatio = organisaatioService.updateOrganisaatio(model, false);//update(model);
        assertEquals(countArvot + 2, yhteystietoArvoDAO.findAll().size());
        assertEquals(countYhteystiedot + 1, yhteystietoDAO.findAll().size());
        assertEquals(2, arvos.size());
        assertEquals("foobar2", ((String[])(arvos.get(0).getArvo()))[0]);
        assertEquals("katuosoite 12", ((OsoiteDTO) arvos.get(1).getArvo()).getOsoite());
    }

    private void setYhteystietoArvos(OrganisaatioDTO org, List<YhteystietoArvoDTO> arvos) {
        for (YhteystietoArvoDTO curArvo : arvos) {
            org.getYhteystietoArvos().add(curArvo);
        }
    }

    private YhteystietoArvoDTO createYTA(String orgOid, String yttElOid, Object arvo) {
        YhteystietoArvoDTO yTA = new YhteystietoArvoDTO();
        yTA.setOrganisaatioOid(orgOid);
        yTA.setKenttaOid(yttElOid);
        yTA.setArvo(arvo);
        yTA.setYhteystietoArvoOid("" + System.currentTimeMillis() + "" + Math.random());
        return yTA;
    }

    private YhteystietojenTyyppiDTO createYTT(String nimi, OrganisaatioTyyppi... acceptedTyyppis) {
        YhteystietojenTyyppiDTO ytt = new YhteystietojenTyyppiDTO();
        MonikielinenTekstiTyyppi nimiT = new MonikielinenTekstiTyyppi();
        Teksti nimiFi = new Teksti();
        nimiFi.setKieliKoodi("fi");
        nimiFi.setValue(nimi);
        nimiT.getTeksti().add(nimiFi);

        ytt.setNimi(nimiT);
        ytt.setOid("" + System.currentTimeMillis() + "" + Math.random());

        if (acceptedTyyppis==null || acceptedTyyppis.length==0) {
        	acceptedTyyppis = OrganisaatioTyyppi.values();
        }

        ytt.getSovellettavatOrganisaatios().addAll(Arrays.asList(acceptedTyyppis));
        return ytt;
    }

    @Test
    public void saveLisatietokenttaArvo_kenttaPlusOrganisaatioIsUnique() throws GenericFault {
        OrganisaatioDTO parent = createKoulutustoimija(organisaatioService, "dfasqdas", buildYtunnus(), "yhteyshlo", null);
        YhteystietojenTyyppiDTO lisatiedot = createYTT("test lisatiedot");
        lisatiedot.getAllLisatietokenttas().add(createYTEl(true, "nimi", YhteystietoElementtiTyyppi.TEKSTI));
        lisatiedot = organisaatioService.createYhteystietojenTyyppi(lisatiedot);//.insert(lisatiedot);
        OrganisaatioDTO model;

        // tallennetaan onnistuneesti lisätietokenttään yksi arvo
        model = buildCreateOrganisaatioModel(organisaatioService, buildYtunnus(), parent.getOid(), OrganisaatioTyyppi.OPPILAITOS.value());
        model.getYhteystietoArvos().add(createYTA(null, getLisatietokentta(lisatiedot, YhteystietoElementtiTyyppi.TEKSTI, "nimi").getOid(), new String[] {"foobar", "kieli_fi#1"}));
        organisaatioService.createOrganisaatio(model, false);

        // yritetään tallentaa kahdesti arvo samalle organisaatiolle ko lisätietokenttään
        model = buildCreateOrganisaatioModel(organisaatioService, buildYtunnus(), parent.getOid(), OrganisaatioTyyppi.OPPILAITOS.value());
        model.getYhteystietoArvos().add(createYTA(null, getLisatietokentta(lisatiedot, YhteystietoElementtiTyyppi.TEKSTI,  "nimi").getOid(), new String[] {"foobar", "kieli_fi#1"}));
        model.getYhteystietoArvos().add(createYTA(null, getLisatietokentta(lisatiedot, YhteystietoElementtiTyyppi.TEKSTI,  "nimi").getOid(), new String[] {"foobar2", "kieli_fi#1"}));
        try {
            organisaatioService.createOrganisaatio(model, false);
            fail("should fail");
        } catch (Exception ve) {
            assertNotNull(ve);
            //PersistenceException pe = (PersistenceException) ve.getCause();
            //SQLException sqlException = ((ConstraintViolationException) pe.getCause()).getSQLException();
            //assertTrue(sqlException.getMessage().contains("unique constraint or index violation"));
//            SQLException nextException = sqlException.getNextException();
            //assertTrue(nextException.getMessage().contains("duplicate key value violates unique constraint \"lisatietokentanarvo_kentta_id_organisaatio_id_key\""));

        }
    }

    @Test
    public void saveLisatietokenttaArvo_cascadeDeleteOrphanArvo() throws GenericFault {
        OrganisaatioDTO parent = createKoulutustoimija(organisaatioService, "bhnjdasydas", buildYtunnus(), "yhteyshlo", null);
        YhteystietojenTyyppiDTO lisatiedot = createYTT("test lisatiedot");
        lisatiedot.getAllLisatietokenttas().add(createYTEl(false, "postiosoite", YhteystietoElementtiTyyppi.OSOITE));
        lisatiedot = organisaatioService.createYhteystietojenTyyppi(lisatiedot);//.insert(lisatiedot);
        OrganisaatioDTO model;

        int countArvot = yhteystietoArvoDAO.findAll().size();
        int countYhteystiedot = yhteystietoDAO.findAll().size();

        // luodaan organisaatio ja tallennetaan lisätietokentän arvo organisaatiolle
        model = buildCreateOrganisaatioModel(organisaatioService, buildYtunnus(), parent.getOid(), OrganisaatioTyyppi.OPPILAITOS.value());
        model.getYhteystietoArvos().add(createYTA(null, getLisatietokentta(lisatiedot, YhteystietoElementtiTyyppi.OSOITE, null).getOid(), OrganisaatioTstUtils.createOsoite(OsoiteTyyppi.POSTI, "katuosoite 1", "12345", "helsinki")));
        OrganisaatioDTO organisaatio = organisaatioService.createOrganisaatio(model, false);
        assertEquals(countArvot+1, yhteystietoArvoDAO.findAll().size());
        assertEquals(countYhteystiedot+1, yhteystietoDAO.findAll().size());

        // poistetaan arvo ja tallennetaan, onnistuu koska kenttä ei ole pakollinen
        model = organisaatioService.findByOid(organisaatio.getOid());  //read(organisaatio.getId()));
        model.getYhteystietoArvos().clear();//addAll(new ArrayList<YhteystietoArvoDTO>());
        organisaatio = organisaatioService.updateOrganisaatio(model, false);//update(model);

        // varmistetaan että arvo ja yhteystieto johon se osoitti, poistui kannasta
        assertEquals(countArvot, yhteystietoArvoDAO.findAll().size());
        assertEquals(countYhteystiedot, yhteystietoDAO.findAll().size());
    }

    @Ignore
    @Test
    public void saveLisatietokenttaArvo_nullArvoInNotPakollinenKentta() throws GenericFault {
        OrganisaatioDTO parent = createKoulutustoimija(organisaatioService, "hnkawtys", buildYtunnus(), "yhteyshlo", null);
        YhteystietojenTyyppiDTO lisatiedot = createYTT("test lisatiedot");
        lisatiedot.getAllLisatietokenttas().add(this.createYTEl(false, "nimi", YhteystietoElementtiTyyppi.TEKSTI));
        lisatiedot = organisaatioService.createYhteystietojenTyyppi(lisatiedot);//insert(lisatiedot);
        OrganisaatioDTO model;

        int countArvot = yhteystietoArvoDAO.findAll().size();

        // tallennetaan onnistuneesti lisätietokenttään null arvo
        model = buildCreateOrganisaatioModel(organisaatioService, buildYtunnus(), parent.getOid(), OrganisaatioTyyppi.OPETUSPISTE.value());
        model.getYhteystietoArvos().add(createYTA(null,  getLisatietokentta(lisatiedot, YhteystietoElementtiTyyppi.TEKSTI, "nimi").getOid(), null));
        OrganisaatioDTO organisaatio = organisaatioService.createOrganisaatio(model, false);

        // assertoidaan että arvo-rivi syntyi kantaan ja se on oikein organisaatiollakin, ja sen arvo on null
        assertEquals(countArvot, yhteystietoArvoDAO.findAll().size());
        assertNull(organisaatioService.findYhteystietoArvosForOrganisaatio(organisaatio.getOid()).get(0).getArvo());

        // assertoidaan myös että arvo on fatdtto:ssa ja siitä populoidussa formmodelissa
        OrganisaatioDTO fatDTO = organisaatioService.findByOid(organisaatio.getOid());
        assertEquals(1, fatDTO.getYhteystietoArvos().size());
        assertEquals(1, fatDTO.getYhteystietoArvos().size());
    }

    @Ignore
    @Test
    public void saveLisatietokenttaArvo_validate() throws GenericFault {

        // jokaiselle kentälle tulee olla olla kentän arvo (vaikka itse arvo olisikin null, ja kenttä ei olisi pakollinen)
        assert_saveLisatietokenttaArvo_validate(asList(createYTEl(false, "nimi", YhteystietoElementtiTyyppi.TEKSTI)), asList("foobar"), null);
        assert_saveLisatietokenttaArvo_validate(asList(createYTEl(false, "nimi", YhteystietoElementtiTyyppi.TEKSTI)), asList(), "YhteystietoArvo must exist for each YhteystietoElementti");
        // itse arvo ei voi olla null, jos kenttä on pakollinen
        assert_saveLisatietokenttaArvo_validate(asList(createYTEl(true, "nimi", YhteystietoElementtiTyyppi.TEKSTI)), asList("asd"), null);
        assert_saveLisatietokenttaArvo_validate(asList(createYTEl(true, "nimi", YhteystietoElementtiTyyppi.TEKSTI)), asList(new Object[]{null}), "arvo required for mandatory YhteystietoElementti: nimi");
        // tyyppikohtainen validointi: string size 3-100
        assert_saveLisatietokenttaArvo_validate(asList(createYTEl(true, "nimi", YhteystietoElementtiTyyppi.TEKSTI)), asList("asd"), null);
        assert_saveLisatietokenttaArvo_validate(asList(createYTEl(true, "nimi", YhteystietoElementtiTyyppi.TEKSTI)), asList("as"), "arvoText - Kent\u00e4n pituuden tulee olla 3 - 100 (was: as)");
        // tyyppikohtainen osoite:
        assert_saveLisatietokenttaArvo_validate(asList(createYTEl(true, "xnimi", YhteystietoElementtiTyyppi.OSOITE)), asList(OrganisaatioTstUtils.createOsoite(OsoiteTyyppi.POSTI, "katux", "12345", "helsinki")), null);
        assert_saveLisatietokenttaArvo_validate(asList(createYTEl(true, "xnimi", YhteystietoElementtiTyyppi.OSOITE)), asList(OrganisaatioTstUtils.createOsoite(OsoiteTyyppi.POSTI, "katux", "1234x", "helsinki")), "Postinumeron tulee olla muotoa 12345");
        // tyyppikohtainen puhelinnumero:
        assert_saveLisatietokenttaArvo_validate(asList(createYTEl(true, "xnimi", YhteystietoElementtiTyyppi.PUHELIN)), asList(OrganisaatioTstUtils.createPuhelin(PuhelinNumeroTyyppi.PUHELIN, "123")), null);
        assert_saveLisatietokenttaArvo_validate(asList(createYTEl(true, "xnimi", YhteystietoElementtiTyyppi.PUHELIN)), asList(OrganisaatioTstUtils.createPuhelin(PuhelinNumeroTyyppi.PUHELIN, "xxx")), "Ei kelvollinen puhelinnumero");
        // tyyppikohtainen email:
        assert_saveLisatietokenttaArvo_validate(asList(createYTEl(true, "xnimi", YhteystietoElementtiTyyppi.EMAIL)), asList(OrganisaatioTstUtils.createEmail("asd@asd.asd")), null);
        assert_saveLisatietokenttaArvo_validate(asList(createYTEl(true, "xnimi", YhteystietoElementtiTyyppi.EMAIL)), asList(OrganisaatioTstUtils.createEmail("invalidemail")), "Emailin tulee olla muotoa");
        // tyyppikohtainen www:
        assert_saveLisatietokenttaArvo_validate(asList(createYTEl(true, "xnimi", YhteystietoElementtiTyyppi.WWW)), asList(OrganisaatioTstUtils.createWww("http://asd.asd")), null);
        assert_saveLisatietokenttaArvo_validate(asList(createYTEl(true, "xnimi", YhteystietoElementtiTyyppi.WWW)), asList(OrganisaatioTstUtils.createWww("invalidwww")), "Ei kelvollinen www-osoite");
    }

    public void assert_saveLisatietokenttaArvo_validate(List<YhteystietoElementtiDTO> kentat, List arvot, String expectedError) throws GenericFault {

        boolean expectedSuccess = expectedError == null;
        // valmistellaan organisaatiohierarkia ja luodaan lisätietojen metadata
        String ytunnus = buildYtunnus();
        OrganisaatioDTO parent = createKoulutustoimija(organisaatioService, ytunnus, ytunnus, "yhteyshlo", null);
        YhteystietojenTyyppiDTO lisatiedot = createYTT("test lisatiedot");
        lisatiedot.getSovellettavatOrganisaatios().add(OrganisaatioTyyppi.OPETUSPISTE);//  new OrganisaatiotyypinYhteystiedotDTO(organisaatioService.findOrganisaatioTyyppi(OPETUSPISTE_STRING)));
        lisatiedot.getAllLisatietokenttas().addAll(kentat);//setAllLisatietokenttas(kentat);


        boolean lisatiedotInserted = false;
        // tallennetaan ja assertoidaan
        if (expectedSuccess) {
            lisatiedot = organisaatioService.createYhteystietojenTyyppi(lisatiedot);//insert(lisatiedot);
            lisatiedotInserted = true;
            OrganisaatioDTO model;

            // asetetaan lisätietojen arvot
            model = buildCreateOrganisaatioModel(organisaatioService, buildYtunnus(), parent.getOid(), OrganisaatioTyyppi.OPETUSPISTE.value());
            for (int i = 0; i < arvot.size(); i++) {
                Object arvo = arvot.get(i);
                model.getYhteystietoArvos().add(createYTA(null, lisatiedot.getAllLisatietokenttas().get(i).getOid(), arvo));
            }

            OrganisaatioDTO organisaatio = organisaatioService.createOrganisaatio(model, false);
            // ok, poistetaan arvot samantien
           /* try {
           for (YhteystietoArvo arvo : yhteystietoArvoDAO.findBy("organisaatio.id", organisaatio.getId())) {

                YhteystietoElementti ye = arvo.getKentta();
                yhteystietoArvoDAO.remove(arvo);
                yhteystietoElementtiDAO.remove(ye);
            }
            yhteystietoArvoDAO.getEntityManager().flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }*/

        } else {
            try {
                lisatiedot = organisaatioService.createYhteystietojenTyyppi(lisatiedot);//yhteystietojenTyyppiService.insert(lisatiedot);
                lisatiedotInserted = true;
                OrganisaatioDTO model;

                // asetetaan lisätietojen arvot
                model = buildCreateOrganisaatioModel(organisaatioService, buildYtunnus(), parent.getOid(), OrganisaatioTyyppi.OPETUSPISTE.value());
                for (int i = 0; i < arvot.size(); i++) {
                    Object arvo = arvot.get(i);
                    model.getYhteystietoArvos().add(createYTA(null, lisatiedot.getAllLisatietokenttas().get(i).getOid(), arvo));
                }
                organisaatioService.createOrganisaatio(model, false);
                fail("should fail");
            } catch (GenericFault e) {
                assertTrue(e.getMessage().contains("validation.exception"));//"invalid ValidationException.message, expected contains: '" + expectedError + "', was: '" + e.getMessage() + "'", e.getMessage().contains(expectedError));
            }
        }
       /* try {
        for (YhteystietoArvo arvo : yhteystietoArvoDAO.findAll()) {//findBy("organisaatio.id", organisaatio.getId())) {
            YhteystietoElementti ye = arvo.getKentta();
            yhteystietoArvoDAO.remove(arvo);
            yhteystietoElementtiDAO.remove(ye);
        }
        yhteystietoArvoDAO.getEntityManager().flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }*/

        // disabloidaan lisätiedot ettei sotke tulevia assertteja
        //if (lisatiedotInserted) {
            deleteLisatiedot();
        //}
    }

    @Test
    public void deleteLisatietokenttaAfterSomeOrganisaatioHasInputtedArvoAlready() throws GenericFault {

        // task: OVT-658
        // "Yhteystietojen viite-eheys mikäli yhteystietojenTyypiltä poistetaan jokin kenttä,
        // johon jokin organisaatio on ja syöttänyt arvon (tarvitaan kenttiin mm 'käytössä' field,
        // sekä hyvät testit)

        // luodaan yhteystietojentyyppi jossa on yksi kenttä
        OrganisaatioDTO parent = createKoulutustoimija(organisaatioService, "hjuassasd", buildYtunnus(), "yhteyshlo", null);
        YhteystietojenTyyppiDTO lisatiedot = createYTT("lisatiedot adshj5vgf");
        lisatiedot.getAllLisatietokenttas().add(createYTEl(false, "postiosoite", YhteystietoElementtiTyyppi.OSOITE));
        lisatiedot = organisaatioService.createYhteystietojenTyyppi(lisatiedot);//yhteystietojenTyyppiService.insert(lisatiedot);
        OrganisaatioDTO model;

        // luodaan organisaatio ja tallennetaan lisätietokentän arvo organisaatiolle
        model = buildCreateOrganisaatioModel(organisaatioService, buildYtunnus(), parent.getOid(), OrganisaatioTyyppi.OPPILAITOS.value());
        model.getYhteystietoArvos().add(createYTA(null, getLisatietokentta(lisatiedot, YhteystietoElementtiTyyppi.OSOITE, "postiosoite").getOid(), OrganisaatioTstUtils.createOsoite(OsoiteTyyppi.POSTI, "katuosoite 1", "12345", "helsinki")));
        OrganisaatioDTO organisaatio = organisaatioService.createOrganisaatio(model, false);

        int countArvot = yhteystietoArvoDAO.findAll().size();
        int countYhteystiedot = yhteystietoDAO.findAll().size();
        int countKentat = yhteystietoElementtiDAO.findAll().size();

        // poistetaan kenttä yhteystietojentyyppistä
        assertEquals(1, getOsoites(lisatiedot).size());
        lisatiedot.getAllLisatietokenttas().clear();
        organisaatioService.updateYhteystietojenTyyppi(lisatiedot);//update(lisatiedot);
        lisatiedot = organisaatioService.readYhteystietojenTyyppi(lisatiedot.getOid());//read(lisatiedot.getId());
        assertEquals(0, getOsoites(lisatiedot).size());

        // assertoidaan että kenttä poistui mutta arvo jäi
        // ..tai oikeastaan kenttä ei poistunut vaan merkattiin poistetuksi
        assertEquals(countArvot, yhteystietoArvoDAO.findAll().size());
        assertEquals(countYhteystiedot, yhteystietoDAO.findAll().size());
        assertEquals(countKentat, yhteystietoElementtiDAO.findAll().size());
        assertFalse(yhteystietoElementtiDAO.findByLisatietoIdAndKentanNimi(lisatiedot.getOid(), "postiosoite").get(0).isKaytossa());

        // luodaan sama kenttä uudestaan yhteystietojen tyypille
        lisatiedot.getAllLisatietokenttas().add(createYTEl(false, "postiosoite", YhteystietoElementtiTyyppi.OSOITE));
        organisaatioService.updateYhteystietojenTyyppi(lisatiedot);
        lisatiedot = organisaatioService.readYhteystietojenTyyppi(lisatiedot.getOid());
        assertEquals(1, getOsoites(lisatiedot).size());

        // assertoidaan että sama kenttä objekti merkattiin taas käytössä olevaksi
        assertEquals(countKentat, yhteystietoElementtiDAO.findAll().size());
        assertTrue(yhteystietoElementtiDAO.findByLisatietoIdAndKentanNimi(lisatiedot.getOid(), "postiosoite").get(0).isKaytossa());

        // assertoidaan että vanhalle kentälle syötetty arvo liittyy nyt juuri luotuun uuteen kenttään
        YhteystietoElementti kentta = yhteystietoElementtiDAO.findByLisatietoIdAndKentanNimi(lisatiedot.getOid(), "postiosoite").get(0);
        YhteystietoArvo arvo = yhteystietoArvoDAO.findByOrganisaatioAndNimi(organisaatio.getOid(), "postiosoite");
        assertEquals(kentta.getId(), arvo.getKentta().getId());
    }

    private List<YhteystietoElementtiDTO> getOsoites(YhteystietojenTyyppiDTO ytt) {
        List<YhteystietoElementtiDTO> osoites = new ArrayList<YhteystietoElementtiDTO>();
        for (YhteystietoElementtiDTO ytel : ytt.getAllLisatietokenttas()) {
            if (ytel.getTyyppi().value().equals(YhteystietoElementtiTyyppi.OSOITE.value())) {
                osoites.add(ytel);
            }
        }
        return osoites;
    }

    @Test
    public void testRemoveYhteystietojenTyyppi() throws GenericFault {
    	 // luodaan yhteystietojentyyppi jossa on yksi kenttä
        OrganisaatioDTO parent = createKoulutustoimija(organisaatioService, "hjuassasd", buildYtunnus(), "yhteyshlo", null);
        YhteystietojenTyyppiDTO lisatiedot = createYTT("lisatiedot adshj5vgf");
        lisatiedot.getAllLisatietokenttas().add(createYTEl(false, "postiosoite", YhteystietoElementtiTyyppi.OSOITE));
        lisatiedot = organisaatioService.createYhteystietojenTyyppi(lisatiedot);//yhteystietojenTyyppiService.insert(lisatiedot);

        OrganisaatioDTO model;
        // luodaan organisaatio ja tallennetaan lisätietokentän arvo organisaatiolle
        model = buildCreateOrganisaatioModel(organisaatioService, buildYtunnus(), parent.getOid(), OrganisaatioTyyppi.OPPILAITOS.value());
        model.getYhteystietoArvos().add(createYTA(null, getLisatietokentta(lisatiedot, YhteystietoElementtiTyyppi.OSOITE, "postiosoite").getOid(), OrganisaatioTstUtils.createOsoite(OsoiteTyyppi.POSTI, "katuosoite 1", "12345", "helsinki")));
        OrganisaatioDTO organisaatio = organisaatioService.createOrganisaatio(model, false);

        //Luodaan yhteystietojentyyppi jolla ei ole yhtään arvoa
        YhteystietojenTyyppiDTO lisatiedot1 = createYTT("lisatiedot adshj6vgf");
        lisatiedot1.getAllLisatietokenttas().add(createYTEl(false, "postiosoite", YhteystietoElementtiTyyppi.OSOITE));
        lisatiedot1 = organisaatioService.createYhteystietojenTyyppi(lisatiedot1);//yhteystietojenTyyppiService.insert(lisatiedot);

        int yttInitialSize = this.yhteystietojenTyyppiDAO.findAll().size();

        assertTrue(yttInitialSize >= 2);

        //Poistetaan ytt, jolla ei ole arvoja, tämän kuuluu onnistua
        try {
        	this.organisaatioService.removeYhteystietojenTyyppiByOid(lisatiedot1.getOid());
        } catch (OrganisaatioCrudException ex) {
        	fail("Removal of " + lisatiedot1.getNimi().getTeksti().get(0).getValue() +  "should succeed");
        }

        int yttSecondarySize = this.yhteystietojenTyyppiDAO.findAll().size();
        assertTrue(yttSecondarySize == (yttInitialSize - 1));

        //Poistetaan ytt, jolla on jo arvoja, tämän kuuluu epäonnistua
        try {
        	this.organisaatioService.removeYhteystietojenTyyppiByOid(lisatiedot.getOid());
        	fail("Removal of yhteystietojentyyppi: " + lisatiedot.getNimi().getTeksti().get(0).getValue() + " should fail");
        } catch (OrganisaatioCrudException ex) {
        	assertTrue(ex.getMessage().contains("yhteystietojenTyyppi.inUse"));
        }

        assertTrue(this.yhteystietojenTyyppiDAO.findAll().size() == yttSecondarySize);

    }

}
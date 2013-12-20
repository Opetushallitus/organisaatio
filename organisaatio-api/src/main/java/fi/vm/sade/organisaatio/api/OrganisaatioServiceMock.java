/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.vm.sade.organisaatio.api;

import static fi.vm.sade.organisaatio.helper.OrganisaatioDisplayHelper.getClosest;
import static fi.vm.sade.organisaatio.helper.OrganisaatioDisplayHelper.getTyyppisStrForOrganisaatio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebParam;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import fi.vm.sade.organisaatio.api.model.GenericFault;
import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.organisaatio.api.model.types.*;
import fi.vm.sade.organisaatio.helper.OrganisaatioDisplayHelper;

/*
 *
 * Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * European Union Public Licence for more details.
 */

/**
 *
 * @author Tuomas Katva
 */
public class OrganisaatioServiceMock implements OrganisaatioService {

    protected final Logger log = Logger.getLogger(this.getClass().getName());


    public static final String ORGANISAATIO_WITH_COMPLETE_EXTRA_VALUES = "" + 1L;
    public static final String ORGANISAATIO_WITH_INCOMPLETE_EXTRA_VALUES = "" + 0L;

    public static final String KENTTA_1_OID = "" + 101L;
    public static final String KENTTA_2_OID = "" + 102L;
    public static final String KENTTA_3_OID = "" + 103L;
    public static final String KENTTA_C_OID = "" + 100L;

    public static final String GENERIC_ERROR = "generic.error";

    public static final String ROOT1 = "root test koulutustoimija";
    public static final String NODE2 = "node2 foo";
    public static final String NODE22 = "node22 foo bar";
    protected long counter = 0;
    List<OrganisaatioDTO> repo = new ArrayList<OrganisaatioDTO>();
    private HashMap<String, String> oidParentOid = new HashMap<String, String>();
    List<YhteystietojenTyyppiDTO> yttRepo = new ArrayList<YhteystietojenTyyppiDTO>();
    List<YhteystietoDTO> ytRepo = new ArrayList<YhteystietoDTO>();

    public OrganisaatioServiceMock() {
        reset();
        log.addHandler(new ConsoleHandler());

    }

    protected Long newId() {
        return counter++;
    }

    public void reset() {
        counter = 0;
        repo.clear();
        yttRepo.clear();
        ytRepo.clear();
        initRepoWithFatDtos();
    }

    public OrganisaatioDTO insert (OrganisaatioDTO dto) {
        repo.add(dto);
        return dto;
    }

    public void update(OrganisaatioDTO dto) {
        repo.remove(dto);
        repo.add(dto);
    }

    @Override
    public FindBasicParentOrganisaatioTypesResponse findBasicParentOrganisaatios(FindBasicParentOrganisaatioTypesParameter parameters) {
        return new FindBasicParentOrganisaatioTypesResponse();
    }

    @Override
    public FindBasicOrganisaatioChildsToOidTypesResponse findBasicOrganisaatioChildsToOid(FindBasicOrganisaatioChildsToOidTypesParameter parameters) {
        return new FindBasicOrganisaatioChildsToOidTypesResponse();
    }



    @Override
    public RemoveByOidResponseType removeOrganisaatioByOid(RemoveByOidType parameters) throws GenericFault {
        OrganisaatioDTO foundOrg = null;
        for (OrganisaatioDTO org:repo) {
            if (org.getOid().trim().equals(parameters.getOid().trim())) {
                foundOrg = org;
            }
        }
        if (foundOrg != null) {
            repo.remove(foundOrg);
        }

        return new RemoveByOidResponseType();
    }

    @Override
    public OrganisaatioOidListType findChildrenOidsByOid(OrganisaatioSearchOidType parameters) {
        System.out.println(String.format("mock searching children for '%s'", parameters.getSearchOid()));
        OrganisaatioOidListType response = new OrganisaatioOidListType();
        HashSet<String> oids = new HashSet<String>();
        final String theOid = parameters.getSearchOid();
        oids.add(theOid);
        int oldCount = -1;
        while (oids.size() != oldCount) {
            oldCount = oids.size();
            HashSet<String> additions = new HashSet<String>();
            for (OrganisaatioDTO org : repo) {
                for (String oid : oids) {
                    
                    if (oid.equals(org.getParentOid()) && !oids.contains(org.getOid())) {
                        additions.add(org.getOid());
                    }
                }
            }
            oids.addAll(additions);
        }
        for(String oid: oids) {
            if(theOid.equals(oid)) continue;
            OrganisaatioOidType oidType = new OrganisaatioOidType();
            oidType.setOrganisaatioOid(oid);
            response.getOrganisaatioOidList().add(oidType);
        }
        
        return response;
    }

    @Override
    public List<OrganisaatioDTO> findParentsByOidList(List<String> oids) {
        return new ArrayList<OrganisaatioDTO>();
    }

    private XMLGregorianCalendar convertDateToXmlGregorianCalendar(Date date) {
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(date);
        XMLGregorianCalendar cal = null;
        try {
          cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException ex) {
            //Logger.getLogger(OrganisaatioServiceMock.class.getName()).log(Level.SEVERE, null, ex);
            log.log(Level.INFO, ex.getMessage());

        }
        return cal;
    }

    private List<OrganisaatioTyyppi> getDefTyypit() {
        List<OrganisaatioTyyppi> tyypit = new ArrayList<OrganisaatioTyyppi>();
        tyypit.add(OrganisaatioTyyppi.OPETUSPISTE);
        tyypit.add(OrganisaatioTyyppi.OPPILAITOS);
        return tyypit;
    }

    protected void initRepoWithFatDtos() {
        Calendar futureStart = Calendar.getInstance();
        futureStart.set(2013, 5, 29);
        Calendar pastStop = Calendar.getInstance();
        pastStop.set(2011, 5, 29);
        OrganisaatioDTO root = create("www."+ROOT1,ROOT1, "1234567-1", null, null, null, null, "1.2.2004.1");
        OrganisaatioDTO node1 = create("www."+"node1 asd","node1 asd", "123456", root, futureStart.getTime(), null, "Ammattikorkeakoulut", "1.2.2004.2");
        OrganisaatioDTO node2 = create("www."+NODE2,NODE2, "123457", root, null, pastStop.getTime(), "Yliopistot", "1.2.2004.3");
        OrganisaatioDTO node22 = create("www."+NODE22,NODE22, "123458", node2, null, null, "Yliopistot", "1.2.2004.4");
        OrganisaatioDTO root2 = create("www.root2 test2 koulutustoimija2","root2 test2 koulutustoimija2", "1234567-5", null, futureStart.getTime(), null, null, "1.2.2004.5");
        OrganisaatioDTO nodex = create("www.nodex_bar","nodex bar", "123459", root2, null, pastStop.getTime(), "Ammattikorkeakoulut", "1.2.2004.6");
        OrganisaatioDTO svensk = create("www.asf.se","Ab Svenska folkhögskolan - SFV","1727829-5",null, null, null, null, "1.2.2004.7");
        OrganisaatioDTO lyck = create("www.lyckeby.se","Lyckeby Industrial Ab","1027158-4",null, futureStart.getTime(), null, null, "1.2.2004.8");
        OrganisaatioDTO essa = create("www.essacraft.fi","Essecraft Oy","1025308-7",null, null, pastStop.getTime(), null, "1.2.2004.9");
        OrganisaatioDTO esa = create("www.esa.fi","Esan Kirjapaino Oy","1008710-3",null, null, null, null, "1.2.2004.10");

        //Yhteystietojentyypit
        List<OrganisaatioTyyppi> orgTyyppis = new ArrayList<OrganisaatioTyyppi>();
        orgTyyppis.add(OrganisaatioTyyppi.OPPILAITOS);//new OrganisaatiotyypinYhteystiedotDTO(new OrganisaatioTyyppiDTO(OrganisaatioTyyppiDTO.OPPILAITOS_STRING)));
        orgTyyppis.add(OrganisaatioTyyppi.MUU_ORGANISAATIO);  //new OrganisaatiotyypinYhteystiedotDTO(new OrganisaatioTyyppiDTO(OrganisaatioTyyppiDTO.MUU_STRING)));
        orgTyyppis.add(OrganisaatioTyyppi.OPETUSPISTE);//new OrganisaatiotyypinYhteystiedotDTO(new OrganisaatioTyyppiDTO(OrganisaatioTyyppiDTO.OPETUSPISTE_STRING)));

        createYtt("Etsivä nuorisotyö", newExtraField(createOid(), false, "Lankanumero", YhteystietoElementtiTyyppi.PUHELIN), orgTyyppis);
        createYtt("Kriisiviestintä", newExtraField(createOid(), false, "www-osoite", YhteystietoElementtiTyyppi.WWW), orgTyyppis);
        createYtt("Koulutusviestintä", newExtraField(createOid(), false, "Postiosoite", YhteystietoElementtiTyyppi.OSOITE), new ArrayList<OrganisaatioTyyppi>());

        ytRepo.add(createOsoite(OsoiteTyyppi.KAYNTI.value(), "Käyntikatu 1", "00510", "Helsinki"));
        ytRepo.add(createOsoite(OsoiteTyyppi.KAYNTI.value(), "Käyntikatu 2", "00510", "Helsinki"));
        ytRepo.add(createOsoite(OsoiteTyyppi.KAYNTI.value(), "Käyntikatu 3", "00510", "Helsinki"));

        ytRepo.add(createOsoite(OsoiteTyyppi.POSTI.value(), "Postikatu 1", "00510", "Helsinki"));
        ytRepo.add(createOsoite(OsoiteTyyppi.POSTI.value(), "Postikatu 2", "00510", "Helsinki"));
        ytRepo.add(createOsoite(OsoiteTyyppi.POSTI.value(), "Postikatu 3", "00510", "Helsinki"));

        ytRepo.add(createEmail("yhteystieto1@test.oph.fi"));
        ytRepo.add(createEmail("yhteystieto2@test.oph.fi"));
        ytRepo.add(createEmail("yhteystieto3@test.oph.fi"));

        ytRepo.add(createPuhelin(PuhelinNumeroTyyppi.PUHELIN, "0501234560"));
        ytRepo.add(createPuhelin(PuhelinNumeroTyyppi.PUHELIN, "0501234561"));

        ytRepo.add(createPuhelin(PuhelinNumeroTyyppi.FAKSI, "0501234562"));
        ytRepo.add(createPuhelin(PuhelinNumeroTyyppi.FAKSI, "0501234563"));

        ytRepo.add(createWww("http://www.mockwww1.oph.fi"));
        ytRepo.add(createWww("http://www.mockwww2.oph.fi"));
    }

    private WwwDTO createWww(String www) {
        WwwDTO wwwDTO = new WwwDTO();
        wwwDTO.setWwwOsoite(www);
        wwwDTO.setYhteystietoOid(createOid());
        return wwwDTO;
    }

    private EmailDTO createEmail(String email) {
        EmailDTO newE = new EmailDTO();
        newE.setEmail(email);
        newE.setYhteystietoOid(createOid());
        return newE;
    }

    private PuhelinnumeroDTO createPuhelin(PuhelinNumeroTyyppi tyyppi, String numero) {
        PuhelinnumeroDTO puhelin = new PuhelinnumeroDTO();
        puhelin.setTyyppi(tyyppi);
        puhelin.setPuhelinnumero(numero);
        puhelin.setYhteystietoOid(createOid());
        return puhelin;
    }

    private void createYtt(String nimi, YhteystietoElementtiDTO extraField, List<OrganisaatioTyyppi> orgTyyppis) {
        YhteystietojenTyyppiDTO group1 = new YhteystietojenTyyppiDTO();
        //group1.setNimiFi(nimi);//"Etsivä nuorisotyö");
        group1.getAllLisatietokenttas().add(extraField);//newExtraField(System.currentTimeMillis() + "" + Math.random(), false, "Lankanumero", YhteystietoElementtiTyyppi.PUHELIN));
        group1.getSovellettavatOrganisaatios().addAll(orgTyyppis);
        group1.setOid(createOid());
        yttRepo.add(group1);
    }


    private static YhteystietoElementtiDTO newExtraField(String oid, boolean mandatory, String name, YhteystietoElementtiTyyppi type) {
        YhteystietoElementtiDTO newField = new YhteystietoElementtiDTO();//;mandatory, name, type);
        newField.setPakollinen(mandatory);
        newField.setNimi(name);
        newField.setTyyppi(type);
        newField.setOid(oid);
        return newField;
    }

    private OrganisaatioDTO create(String domainNimi,String nimi, String ytunnus, OrganisaatioDTO parent, Date start, Date stop, String oppilaitosTyyppi, String oid) {
        OrganisaatioDTO organisaatio = new OrganisaatioDTO();
        organisaatio.setDomainNimi(domainNimi);
        organisaatio.setOid(oid);
        // organisaatio.setNimiFi(nimi);
        // organisaatio.setNimiLyhenne(nimi);
        organisaatio.setKotipaikka("Helsinki");
        organisaatio.setYritysmuoto("oy");
        organisaatio.setAlkuPvm((start != null) ? start : null);
        organisaatio.setLakkautusPvm((stop != null) ? stop : null);
        if (parent != null) {
            organisaatio.setParentOid(parent.getOid());
            organisaatio.getTyypit().addAll(getDefTyypit());
            organisaatio.setOppilaitosKoodi(ytunnus);
            organisaatio.setOppilaitosTyyppi(oppilaitosTyyppi);
        } else {
            organisaatio.getTyypit().addAll(Arrays.asList(new OrganisaatioTyyppi[]{OrganisaatioTyyppi.KOULUTUSTOIMIJA}));
            organisaatio.setYtunnus(ytunnus);
        }
        save(organisaatio);
        return organisaatio;
    }

    private OrganisaatioDTO create(String nimi, String ytunnus, OrganisaatioDTO parent, Date start, Date stop, String oppilaitosTyyppi, String oid) {
        OrganisaatioDTO organisaatio = new OrganisaatioDTO();
        organisaatio.setOid(oid);
        // organisaatio.setNimiFi(nimi);
        // organisaatio.setNimiLyhenne(nimi);
        organisaatio.setKotipaikka("Helsinki");
        organisaatio.setYritysmuoto("oy");
        organisaatio.setAlkuPvm(start);
        organisaatio.setLakkautusPvm(stop);
        if (parent != null) {
            organisaatio.setParentOid(parent.getOid());
            organisaatio.getTyypit().addAll(getDefTyypit());
            organisaatio.setOppilaitosKoodi(ytunnus);
            organisaatio.setOppilaitosTyyppi(oppilaitosTyyppi);
        } else {
            organisaatio.getTyypit().addAll(Arrays.asList(new OrganisaatioTyyppi[]{OrganisaatioTyyppi.KOULUTUSTOIMIJA}));
            organisaatio.setYtunnus(ytunnus);
        }
        save(organisaatio);
        return organisaatio;
    }

    private OsoiteDTO createOsoite(String type, String osoite, String postinumero, String postitoimipaikka) {
        //"type?", "Mannerheiminkatu 1", "00100", "Helsinki"
        OsoiteDTO osoiteDTO = new OsoiteDTO();
        try {
            osoiteDTO.setOsoiteTyyppi(OsoiteTyyppi.fromValue(type));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        osoiteDTO.setOsoite(osoite);
        osoiteDTO.setPostinumero(postinumero);
        osoiteDTO.setPostitoimipaikka(postitoimipaikka);
        osoiteDTO.setYhteystietoOid(createOid());
        return osoiteDTO;
    }

    private OrganisaatioDTO save(OrganisaatioDTO organisaatio) {
        repo.add(organisaatio);
        oidParentOid.put(organisaatio.getOid(), organisaatio.getParentOid());
        return organisaatio;
    }

    public List<OrganisaatioDTO> findAll() {
        return new ArrayList<OrganisaatioDTO>(repo);
    }

    @Override
    public List<OrganisaatioDTO> findByOidList(List<String> oids, int maxResults) {
        List<OrganisaatioDTO> orgs = new ArrayList<OrganisaatioDTO>();
        for(OrganisaatioDTO org: repo) {
            if(oids.contains(org.getOid())) {
                orgs.add(org);
            }
        }
        return orgs;
    }



    @Override
    public List<OrganisaatioDTO> searchOrganisaatios(OrganisaatioSearchCriteriaDTO organisaatioSearchCriteria) {
        Collection<OrganisaatioDTO> allOrgs = findAll();
        if ((organisaatioSearchCriteria.getSearchStr() == null)
                && organisaatioSearchCriteria.getOppilaitosTyyppi() == null
                && organisaatioSearchCriteria.getOrganisaatioDomainNimi() == null
                && organisaatioSearchCriteria.getOppilaitosTyyppi() == null) {
            List<OrganisaatioDTO> orgs = new ArrayList<OrganisaatioDTO>();
            for (OrganisaatioDTO curOrg : allOrgs) {
                orgs.add(curOrg);
            }
            return orgs;
        }
        List<OrganisaatioDTO> resultOrgs = new ArrayList<OrganisaatioDTO>();
        String orgTyypSpec = organisaatioSearchCriteria.getOrganisaatioTyyppi();
        log.log(Level.INFO, "searchOrganisatios ----- orgTyyppiSpec: {0}", orgTyypSpec);

        for (OrganisaatioDTO curOrg : allOrgs) {
            if (((orgTyypSpec == null) || OrganisaatioDisplayHelper.getTyyppisStrForOrganisaatio(curOrg).contains(orgTyypSpec))
                    && voimassaoloMatches(curOrg, organisaatioSearchCriteria)
                    && textMatches(curOrg, organisaatioSearchCriteria)
                    && oppilaitosTyyppiMatches(curOrg, organisaatioSearchCriteria)) {
                log.log(Level.INFO, "searchOrganisaatios ---- MATCH!!!");

                resultOrgs.add(curOrg);
                resultOrgs.addAll(union(resultOrgs, this.findParentsTo(curOrg.getOid())));
            }
        }
        return resultOrgs;

    }

    @Override
    public OrganisaatioDTO findByOid(String oid) {
        OrganisaatioDTO org = null;
        if (oid != null) {
            for (OrganisaatioDTO dto : repo) {
                if (oid.equals(dto.getOid())) {
                    return dto;
                }
            }
        }
        log.log(Level.INFO, "Organisaatio was null");

        /*
        if (org == null) {
            org = repo.get((repo.size()-1));
           //DEBUGSAWAY:log.debug("Organisaatio was null")
        }*/
        return org;
    }

    @Override
    public OrganisaatioDTO createOrganisaatio(OrganisaatioDTO organisaatio, boolean skipValidation) throws GenericFault {
        create(organisaatio);
        checkOrganisaatioHierarchy(organisaatio);
        log.log(Level.INFO, "CREATED child organisaatio: {0}, parent: {1} - {2}", new Object[]{getClosest(Locale.getDefault(), organisaatio), organisaatio.getParentOid(), findByOid(organisaatio.getParentOid())});

        return organisaatio;
    }

//    @Override
//    public void updateYhteystieto(YhteystietoDTO yhteystieto) throws GenericFault {
//        List<YhteystietoDTO> newYts = new ArrayList<YhteystietoDTO>();
//        for (YhteystietoDTO curYt : this.ytRepo) {
//            if (curYt.getYhteystietoOid().equals(yhteystieto.getYhteystietoOid())) {
//                newYts.add(yhteystieto);
//            } else {
//                newYts.add(curYt);
//            }
//        }
//    }

    public List<OrganisaatioDTO> findAllChildrenWithOid(String parentOidParam) {
        return findChildrenTo(parentOidParam);
    }



    @Override
    public YhteystietojenTyyppiDTO createYhteystietojenTyyppi(YhteystietojenTyyppiDTO yhteystietojenTyyppi) throws GenericFault {
        yhteystietojenTyyppi = populateIds(yhteystietojenTyyppi);
        this.yttRepo.add(yhteystietojenTyyppi);
        return yhteystietojenTyyppi;
    }

    @Override
    public List<YhteystietojenTyyppiDTO> findYhteystietoMetadataForOrganisaatio(List<String> organisaatioTyyppi) {
        if (organisaatioTyyppi == null || organisaatioTyyppi.isEmpty()) {
            return new ArrayList<YhteystietojenTyyppiDTO>();
        }
        List<YhteystietojenTyyppiDTO> ltMetadatasForOrganisaatio = new ArrayList<YhteystietojenTyyppiDTO>();
        for (YhteystietojenTyyppiDTO curLtd : yttRepo) {
            if (isMatchingOrganisaatiotyyppi(curLtd.getSovellettavatOrganisaatios(), organisaatioTyyppi)) {
                ltMetadatasForOrganisaatio.add(curLtd);
            }
        }
        //return repo.subList(0, Math.min(organisaatioTyypit.size(), repo.size()));
        return ltMetadatasForOrganisaatio;
    }

    private boolean isMatchingOrganisaatiotyyppi(List<OrganisaatioTyyppi> tyyppis, Collection<String> organisaatioTyypit) {
        for (OrganisaatioTyyppi curType : tyyppis) {
            if (organisaatioTyypit.contains(curType.value())) {
                return true;
            }
        }
         return false;
     }

    private YhteystietojenTyyppiDTO populateIds(YhteystietojenTyyppiDTO dto) {

        if (dto.getOid() == null) {
            dto.setOid(createOid());

        }

        List<YhteystietoElementtiDTO> fields = dto.getAllLisatietokenttas();
        if (fields != null) {
            for (YhteystietoElementtiDTO field : fields) {
                if (field != null && field.getOid() == null) {
                    field.setOid(createOid());
                }
            }
        }

        return dto;

    }

//    @Override
//    public YhteystietoDTO createYhteystieto(YhteystietoDTO yhteystieto) throws GenericFault {
//        if (yhteystieto.getYhteystietoOid() == null) {
//            yhteystieto.setYhteystietoOid(createOid());
//        }
//        this.ytRepo.add(yhteystieto);
//        return yhteystieto;
//
//    }

    @Override
    public void updateYhteystietojenTyyppi(YhteystietojenTyyppiDTO yhteystietojenTyyppi) throws GenericFault {
        //throw new UnsupportedOperationException("Not supported yet.");
        yhteystietojenTyyppi = this.populateIds(yhteystietojenTyyppi);
        List<YhteystietojenTyyppiDTO> newYttRepo = new ArrayList<YhteystietojenTyyppiDTO>();
        for (YhteystietojenTyyppiDTO curYtt : this.yttRepo) {
            if (curYtt.getOid().equals(yhteystietojenTyyppi.getOid())) {
                newYttRepo.add(yhteystietojenTyyppi);
            } else {
                newYttRepo.add(curYtt);
            }
        }
    }

//    @Override
//    public List<YhteystietoDTO> findYhteystietos(SearchCriteriaDTO yhteystietoSearchCriteria) {
//        return this.ytRepo;
//    }

    @Override
    public List<YhteystietoArvoDTO> findYhteystietoArvosForOrganisaatio(String organisaatioOid) {
        List<YhteystietoArvoDTO> values = new ArrayList<YhteystietoArvoDTO>();

               // value for IM in group 2?
        if (organisaatioOid == ORGANISAATIO_WITH_COMPLETE_EXTRA_VALUES) {
            values.add(createYta(organisaatioOid, KENTTA_C_OID, "skype:foo.bar"));
        }

        // value for phone number in sample group 1
        values.add(createYta(organisaatioOid, KENTTA_1_OID, "+358-9-123 123"));


        // value for www address in group 2
        values.add(createYta(organisaatioOid, KENTTA_2_OID, "http://domain.com/index.html"));

        // value for address in group 2
        OsoiteDTO osoite = createOsoite("type?", "Mannerheiminkatu 1", "00100", "Helsinki");
        values.add(createYta(organisaatioOid, KENTTA_3_OID, osoite));

        return values;
    }

    @Override
    public List<OrganizationStructureType> getOrganizationStructure(@WebParam(name = "oids", targetNamespace = "http://model.api.organisaatio.sade.vm.fi/types") List<String> oids) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private YhteystietoArvoDTO createYta(String organisaatioOid, String kenttaOid, Object arvo) {
        YhteystietoArvoDTO yta = new YhteystietoArvoDTO();
        yta.setYhteystietoArvoOid(createOid());
        yta.setOrganisaatioOid(organisaatioOid);
        yta.setKenttaOid(kenttaOid);
        yta.setArvo(arvo);
        return yta;

    }

    @Override
    public String ping(String arg0) throws GenericFault {
        return arg0;
    }

    @Override
    public YhteystietojenTyyppiDTO readYhteystietojenTyyppi(String yhteystietojenTyyppiOid) {
        for (YhteystietojenTyyppiDTO curYtt : yttRepo) {
            if (curYtt.getOid().equals(yhteystietojenTyyppiOid)) {
                return curYtt;
            }
        }
        return null;
    }

//    @Override
//    public YhteystietoDTO readYhteystieto(String yhteystietoOid) {
//       for (YhteystietoDTO curYt : this.ytRepo) {
//           if (curYt.getYhteystietoOid().equals(yhteystietoOid)) {
//               return curYt;
//           }
//       }
//       return null;
//    }

    @Override
    public OrganisaatioDTO updateOrganisaatio(OrganisaatioDTO organisaatio, boolean skipValidation) throws GenericFault {

        log.log(Level.INFO, "OrganisaatioServiceMock.update...");
        // poistetaan alkup
        List<OrganisaatioDTO> newRepo = new ArrayList<OrganisaatioDTO>();
        for (OrganisaatioDTO temp : repo) {

            if (temp.getOid().equals(organisaatio.getOid())) {
                newRepo.add(organisaatio);
            }
            else {
                newRepo.add(temp);
            }
        }
        // tallennetaan
        //return createOrganisaatio(organisaatio);
        repo = newRepo;
        return organisaatio;
    }

    @Override
    public List<YhteystietojenTyyppiDTO> findYhteystietojenTyyppis(SearchCriteriaDTO yhteystietojenTyyppiSearchCriteria) {
        return yttRepo;
    }

    @Override
    public List<OrganisaatioDTO> findChildrenTo(String oid) {
        List<OrganisaatioDTO> children = new ArrayList<OrganisaatioDTO>();
        if (oid != null) {
            OrganisaatioDTO o = this.findByOid(oid);
            Collection<OrganisaatioDTO> allOrgs = findAll();
            for (OrganisaatioDTO temp : allOrgs) {
                if ((temp != null) && (temp.getParentOid() != null) && temp.getParentOid().equals(oid)) {
                    children.add(temp);
                }
            }
        }
        return children;
    }

    @Override
    public List<OrganisaatioDTO> findParentsTo(String oid) {
        List<OrganisaatioDTO> path = new ArrayList<OrganisaatioDTO>();
        if (oid != null) {
            OrganisaatioDTO o = findByOid(oid);
            List<OrganisaatioDTO> parentPath = findParentsTo(o.getParentOid());
            for (OrganisaatioDTO temp : parentPath) {
                path.add(temp);
            }
            path.add(o);
        }
        return path;
    }



    private List<OrganisaatioDTO> union(List<OrganisaatioDTO> resultOrgs, List<OrganisaatioDTO> ancestorList) {
        //if (curOrg.getParentId() != null) {
        List<OrganisaatioDTO> distincts = new ArrayList<OrganisaatioDTO>();
        for (OrganisaatioDTO curOrg : ancestorList) {
            if (!resultOrgs.contains(curOrg)) {
                distincts.add(curOrg);
            }
        }
        return distincts;
    }

    private boolean oppilaitosTyyppiMatches(OrganisaatioDTO organisaatio, OrganisaatioSearchCriteriaDTO searchSpec) {
        String oppilaitosTyyppi = searchSpec.getOppilaitosTyyppi();
        return (oppilaitosTyyppi == null)
                || oppilaitosTyyppi.equalsIgnoreCase(organisaatio.getOppilaitosTyyppi());
    }

    private boolean textMatches(OrganisaatioDTO organisaatio, OrganisaatioSearchCriteriaDTO searchSpec) {
        log.log(Level.INFO, "Search string is: {0}", searchSpec.getSearchStr());

        String searchStr = (searchSpec.getSearchStr() != null) ? searchSpec.getSearchStr() : "";
        if (searchStr.length() <= 0) {
            return true;
        }
        return isPropertyMatch(getClosest(Locale.getDefault(), organisaatio),
                searchStr)
                || isPropertyMatch(organisaatio.getYtunnus(), searchStr)
                || isPropertyMatch(organisaatio.getOppilaitosKoodi(), searchStr);
    }

    private boolean isPropertyMatch(String val, String searchStr) {
        return (val != null) && val.toLowerCase().contains(searchStr.toLowerCase());
    }

    private boolean voimassaoloMatches(OrganisaatioDTO organisaatio, OrganisaatioSearchCriteriaDTO searchSpec) {
        if ((organisaatio.getAlkuPvm() == null) && (organisaatio.getLakkautusPvm() == null)) {
            return true;
        }
        if (searchSpec.isLakkautetut() && searchSpec.isSuunnitellut()) {
            return true;
        }
        if (searchSpec.isLakkautetut() && !searchSpec.isSuunnitellut()
                && ((organisaatio.getAlkuPvm() == null)
                        || (organisaatio.getAlkuPvm().getTime() <= System.currentTimeMillis()))) {
            return true;
        }
        if (!searchSpec.isLakkautetut() && searchSpec.isSuunnitellut()
                && ((organisaatio.getLakkautusPvm() == null)
                        || (organisaatio.getLakkautusPvm().getTime() >= System.currentTimeMillis()))) {
            return true;
        }
        if (!searchSpec.isLakkautetut() && !searchSpec.isSuunnitellut()
                && ((organisaatio.getAlkuPvm() == null)
                        || (organisaatio.getAlkuPvm().getTime() <= System.currentTimeMillis()))
                && ((organisaatio.getLakkautusPvm() == null)
                        || (organisaatio.getLakkautusPvm().getTime() >= System.currentTimeMillis()))) {
            return true;
        }
        return false;
    }

    private void checkOrganisaatioHierarchy(OrganisaatioDTO model) throws GenericFault {
        String parentOid = model.getParentOid();
        if (parentOid == null) {
            return;
        }
        for (OrganisaatioDTO curDto : repo) {
            if (curDto.getOid().equals(parentOid)
                    && (curDto.getParentOid() != null)
                    && !isOverlap(getTyyppisStrForOrganisaatio(model),  getTyyppisStrForOrganisaatio(curDto))) {
                throw new GenericFault("exception.organisaatio.hierarchy");
            }
        }
    }

    private boolean isOverlap(List<String> tyypit, List<String> parentTyypit) {
        boolean isOverlap =false;
        for (String curTyyppi : tyypit) {
            if (parentTyypit.contains(curTyyppi)) {
                isOverlap = true;
            }
        }
        return isOverlap;
    }

    private OrganisaatioDTO create(OrganisaatioDTO model) throws GenericFault {
        if (model.getOid() == null) {
            model.setOid(createOid());
        }

        log.log(Level.INFO, "CREATE: {0} - {1}", new Object[]{getClosest(Locale.getDefault(),  model), model});
        if (getClosest(Locale.getDefault(),  model).equals("CAUSE_ERROR")) {
            throw new GenericFault(GENERIC_ERROR);
        }

        try {
            return save(model);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String createOid() {
        return System.currentTimeMillis() + "" + Math.random();
    }


	@Override
	public void removeYhteystietojenTyyppiByOid(String oid)
			throws GenericFault {
		// TODO Auto-generated method stub

	}


}

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
package fi.vm.sade.organisaatio.service;

import fi.vm.sade.organisaatio.dao.impl.OrganisaatioDAOImpl;
import fi.vm.sade.organisaatio.dao.impl.YhteystietojenTyyppiDAOImpl;
import fi.vm.sade.organisaatio.dao.impl.OrganisaatioSuhdeDAOImpl;
import fi.vm.sade.organisaatio.dao.impl.YhteystietoArvoDAOImpl;
import fi.vm.sade.organisaatio.dao.impl.YhteystietoDAOImpl;
import fi.vm.sade.organisaatio.dao.impl.YhteystietoElementtiDAOImpl;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fi.vm.sade.generic.common.DateHelper;
import fi.vm.sade.generic.common.ValidationException;
import fi.vm.sade.log.model.Tapahtuma;
import fi.vm.sade.organisaatio.api.OrganisaatioValidationConstraints;
import fi.vm.sade.organisaatio.api.model.GenericFault;
import fi.vm.sade.organisaatio.api.model.types.*;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi.Teksti;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.model.dto.OrgStructure;
import fi.vm.sade.organisaatio.model.lop.BinaryData;
import fi.vm.sade.organisaatio.model.lop.OrganisaatioMetaData;
import fi.vm.sade.organisaatio.resource.IndexerResource;
import fi.vm.sade.organisaatio.service.auth.PermissionChecker;
import fi.vm.sade.organisaatio.service.converter.ConverterFactory;
import fi.vm.sade.organisaatio.service.converter.EntityToOrganisaatioKuvailevatTiedotTyyppiFunction;
import fi.vm.sade.organisaatio.service.converter.MonikielinenTekstiTyyppiToEntityFunction;
import fi.vm.sade.organisaatio.service.converter.OrganisaatioBasicConverter;
import fi.vm.sade.organisaatio.service.search.OrganisaatioSearchService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import javax.jws.WebParam;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Antti Salonen
 */
@Transactional(rollbackFor = Throwable.class, readOnly = true)
public class OrganisaatioServiceImpl
        implements fi.vm.sade.organisaatio.api.model.OrganisaatioService {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioServiceImpl.class);
    @Autowired
    PermissionChecker permissionChecker;
    @Autowired
    private ConverterFactory converterFactory;
    @Autowired
    private YhteystietoDAOImpl yhteystietoDAO;
    @Autowired
    protected YhteystietoElementtiDAOImpl yhteystietoElementtiDAO;
    @Autowired
    protected YhteystietoArvoDAOImpl yhteystietoArvoDAO;
    private OrganisaatioDAOImpl organisaatioDAO;
    @Autowired
    private YhteystietojenTyyppiDAOImpl yhteystietojenTyyppiDAO;
    @Autowired
    private OrganisaatioSuhdeDAOImpl organisaatioSuhdeDAO;
    @Value("${root.organisaatio.oid}")
    private String ophOid;
    @Autowired
    private OrganisaatioSearchService solrSearch;
    @Autowired
    private IndexerResource solrIndexer;
    private static final String parentSeparator = "|";
    private static final String parentSplitter = "\\|";




    @Autowired
    public void setDao(OrganisaatioDAOImpl dao) {
        this.organisaatioDAO = dao;
    }

    private Organisaatio removeOrganisaatioByOid(String oid, Set<String> reindex) {
        Organisaatio removed = organisaatioDAO.markRemoved(oid);

        for (Organisaatio o : organisaatioDAO.findChildren(oid, false, true)) {
            removeOrganisaatioByOid(o.getOid(), reindex);
        }

        reindex.add(oid);

        return removed.getParent();
    }



    @Override
    public RemoveByOidResponseType removeOrganisaatioByOid(
            RemoveByOidType parameters) throws GenericFault {
        permissionChecker.checkRemoveOrganisation(parameters.getOid());
        Set<String> reindex = new HashSet<String>();
        Organisaatio po = removeOrganisaatioByOid(parameters.getOid(), reindex);

        solrIndexer.delete(new ArrayList<String>(reindex));

        // päivittää aliorganisaatioiden lukumäärän parenttiin
        if (po != null) {
            solrIndexer.index(Arrays.asList(po));
        }

        return new RemoveByOidResponseType();
    }

    @Override
    public FindBasicParentOrganisaatioTypesResponse findBasicParentOrganisaatios(FindBasicParentOrganisaatioTypesParameter parameters) {
        FindBasicParentOrganisaatioTypesResponse response = new FindBasicParentOrganisaatioTypesResponse();
        response.getOrganisaatioPerustiedot().addAll(OrganisaatioBasicConverter.convertToPerustietos(organisaatioDAO.findChildren(ophOid, false, true)));//findBasicOrgInfo(OPH_OID)));
        return response;
    }

    @Override
    public FindBasicOrganisaatioChildsToOidTypesResponse findBasicOrganisaatioChildsToOid(FindBasicOrganisaatioChildsToOidTypesParameter parameters) {
        LOG.debug("findBasicOrganisaatioChildsToOid({})", parameters.getOid());

        FindBasicOrganisaatioChildsToOidTypesResponse response = new FindBasicOrganisaatioChildsToOidTypesResponse();
        response.getOrganisaatioPerustiedot().addAll(OrganisaatioBasicConverter.convertToPerustietos(organisaatioDAO.findChildren(parameters.getOid(), false, true)));//findBasicOrgInfo(parameters.getOid())));
        return response;
    }

    @Override
    public List<OrganisaatioDTO> findParentsByOidList(@WebParam(name = "oids", targetNamespace = "http://model.api.organisaatio.sade.vm.fi/types") List<String> oids) {
        return convertListToDto(organisaatioDAO.findByOidList(oids, oids.size()));

    }

    public List<OrganizationStructureType> getOrganizationStructure() {
        return getOrganizationStructure(null);
    }

    private String getTekstiByLang(MonikielinenTeksti teksti, String lang) {
        String returnValue = null;

        if (teksti != null && teksti.getValues().containsKey(lang)) {
            returnValue = teksti.getValues().get(lang);
        }

        return returnValue;
    }

    @Override
    public List<OrganizationStructureType> getOrganizationStructure(@WebParam(name = "oids", targetNamespace = "http://model.api.organisaatio.sade.vm.fi/types") List<String> oids) {
        if (oids == null) {
            oids = new ArrayList<String>();
        }
        List<OrganizationStructureType> r = new ArrayList<OrganizationStructureType>();
        //TODO: refaktoroi konvertteriin
        for (OrgStructure o : organisaatioDAO.getOrganizationStructure(oids)) {
            OrganizationStructureType ot = new OrganizationStructureType();

            String nameFi = getTekstiByLang(o.getName(), "fi");
            String nameSv = getTekstiByLang(o.getName(), "sv");
            String nameEn = getTekstiByLang(o.getName(), "en");

            ot.setNameFi(nameFi);
            ot.setNameSv(nameSv);
            ot.setNameEn(nameEn);
            ot.setOid(o.getOid());
            ot.setLakkautusPvm(o.getLakkautusPvm() != null ? DateHelper.DateToXmlCal(o.getLakkautusPvm()) : null);
            ot.setPoistettu(o.getPoistettu());

            String parentOidPath = o.getOidPath();

            if (StringUtils.isNotBlank(parentOidPath)) {   //parse parentoid if present
                String[] ancestors = parentOidPath.split("\\|");
                if (ancestors.length > 0) {
                    String parentOid = ancestors[ancestors.length - 1];
                    ot.setParentOid(parentOid);
                }
            }
            r.add(ot);
        }
        return r;
    }

    @Override
    public OrganisaatioOidListType findChildrenOidsByOid(OrganisaatioSearchOidType parameters) {
        LOG.warn("findChildrenOidsByOid({})", parameters.getSearchOid());

        Collection<String> tmp = null;

        // Root oid?
        if (parameters.getSearchOid().trim().equalsIgnoreCase(ophOid)) {
            tmp = organisaatioDAO.findAllOids(false);
            tmp.remove(ophOid);
        } else {
            tmp = organisaatioDAO.listDescendantOids(parameters.getSearchOid(), false);
        }

        // Create result
        OrganisaatioOidListType oidList = new OrganisaatioOidListType();
        for (String oid : tmp) {
            OrganisaatioOidType oidType = new OrganisaatioOidType();
            oidType.setOrganisaatioOid(oid);
            oidList.getOrganisaatioOidList().add(oidType);
        }

        return oidList;
    }

    @Override
    public String ping(String param) throws GenericFault { // TODO: move to some testservice
        LOG.debug(" *** OrganisaatioServiceImpl.ping: " + param);

        if ("!runtimeexception".equals(param)) {
            throw new RuntimeException("ping throwing test runtime exception");
        }
        if ("!businessexception".equals(param)) {
            throw new GenericFault("ping test validation error");
        }

        if ("!!".equals(param)) {
            throw new LearningInstitutionExistsException();
        }

        return this.getClass().getName();
    }

    @Override
    @Transactional(rollbackFor = Throwable.class, readOnly = false)
    public OrganisaatioDTO createOrganisaatio(OrganisaatioDTO model, boolean skipValidation) throws GenericFault {
        permissionChecker.checkSaveOrganisation(model, false);

        LOG.debug("createOrganisaatio: " + model);
        try {

            OrganisaatioDTO org = save(model, false, skipValidation);

            return org;
        } catch (ValidationException ex) {
            throw convertValidationExceptionToGenericFault(ex);
        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class, readOnly = false)
    public OrganisaatioDTO updateOrganisaatio(OrganisaatioDTO model, boolean skipValidation) throws GenericFault {
        permissionChecker.checkSaveOrganisation(model, true);

        LOG.info("update: {} -- version={}", model, model.getVersion());

        try {
            if (model.getOid() != null) {
                LOG.debug("ORGANISAATIO: oid = {}, version={}", model.getOid(), model.getVersion());
                //Changed passivation such that the updated organization is not saved twice (which generated lock exception).

                // Has the "passvation" date changed?
                Date oldLakkautusPvm = findByOid(model.getOid()).getLakkautusPvm();
                if (!isEqualDate(model.getLakkautusPvm(), oldLakkautusPvm)) {
                    handleDescendantPassivation(model, oldLakkautusPvm);
                }
            }
            OrganisaatioDTO org = save(model, true, skipValidation);

            return org;
        } catch (OptimisticLockException e) {
            throw new OrganisaatioModifiedException(e);
        } catch (ValidationException ex) {
            throw convertValidationExceptionToGenericFault(ex);
        }
    }

    private void handleDescendantPassivation(OrganisaatioDTO model, Date oldLakkautusPvm) {
        List<Organisaatio> organisaatiot = organisaatioDAO.findChildren(model.getOid(), false, true);
        if (organisaatiot != null) {
            for (Organisaatio org : organisaatiot) {
                checkAndPassivateChildOrganisations(org.getOid(), model.getAlkuPvm(), model.getLakkautusPvm(), oldLakkautusPvm, model.getLakkautusPvm());
            }
        }
    }

    /**
     * Passivate organisation and children recursively. Also moves start date
     * (aloitusPvm) so that it's inside parents selected time period if child
     * start date after parents end date.
     *
     * @param oid
     * @param parentAloitusPvm
     * @param lakkautusPvm
     */
    private void checkAndPassivateChildOrganisations(String oid, Date parentAloitusPvm, Date lakkautusPvm, Date oldLakkautusPvm, Date parentLakkautusPvm) {
        LOG.info("checkAndPassivateChildOrganisations()");

        try {
            // Passivate this organisation
            Organisaatio org = organisaatioDAO.findByOid(oid);
            LOG.debug("org: " + org.getOid());

            //Jos lapsella on myöhäisempi aloituspvm kuin vanhemmalla laitetaan vanhemman aloituspvm passivoitavalle lapselle
            if (org.getAlkuPvm() != null && lakkautusPvm != null && org.getAlkuPvm().after(lakkautusPvm)) {
                org.setAlkuPvm(parentAloitusPvm);
            }


            LOG.debug("Checking parents: {}", org.getOid());
            List<OrganisaatioDTO> parentOrgs = findParentsTo(oid);
            LOG.debug("Found parents: {}", org.getOid());

            //Tarkistetaan että vanhemman lakkautus pvm ei ole tyhjä jos lapselle on tyhjä, tai että lapsen lakkautuspvm ei ole vanhemman jälkeen.
            //Jos vanhempi on tekninen root ei muita tarkistuksia tarvitse tehdä koska sillä ei voi olla lakkautuspvm:mää
            if (parentOrgs == null || parentOrgs.get(0).getOid().equals(ophOid) || parentOrgs.get(0).getLakkautusPvm() == null && lakkautusPvm == null) {
                LOG.debug("Updating root parent: {}", org.getOid());
                org.setLakkautusPvm(lakkautusPvm);
                organisaatioDAO.update(org);
                solrIndexer.index(Lists.newArrayList(org));
            } else if (!parentOrgs.get(0).getOid().equals(ophOid)) { //Vanhempi ei ole uber org
                LOG.debug("Updating non-root parent: {}", org.getOid());
                //Ja vanhemman lakkautus pvm on joko tyhja tai lapsen lakkautuspvm on ennen tai sama kuin vanhemman lakkautus pvm
                Date nlpvm = OrganisaatioServiceAlgorithms.getUpdatedLakkautusPvm(org.getLakkautusPvm(), lakkautusPvm, oldLakkautusPvm, parentLakkautusPvm);
                if (org.getLakkautusPvm() == null) {
                    LOG.debug("Doing actual update for: {}", org.getOid());
                    org.setLakkautusPvm(nlpvm);
                    organisaatioDAO.update(org);
                    solrIndexer.index(Lists.newArrayList(org));
                }
                LOG.debug("Update done for: {}", org.getOid());
            }

            LOG.debug("all done with: {}", org.getOid());

            // Tarkistetaan löytyykö oid:lla lapsiorganisaatioita, jos löytyy kutsutaan rekursiivisesti samaa metodia ja päivitetään
            // organisaation lapsille sama lakkautuspäivämäärä
            List<Organisaatio> organisaatiot = organisaatioDAO.findChildren(oid, false, true);
            if (organisaatiot != null) {
                LOG.debug("children: " + organisaatiot.size());
                for (Organisaatio co : organisaatiot) {
                    checkAndPassivateChildOrganisations(co.getOid(), parentAloitusPvm, lakkautusPvm, oldLakkautusPvm, org.getLakkautusPvm());
                }
            }


        } catch (Exception exp) {

            throw new OrganisationPassivationException("exception.error.passivating.child", exp);
        }

    }

    /**
     * Convert to generic fault.
     *
     * @param valExp
     * @return
     */
    private GenericFault convertValidationExceptionToGenericFault(ValidationException valExp) {
        GenericFaultInfoType faultInfo = new GenericFaultInfoType();
        faultInfo.setErrorCode(valExp.getKey());
        faultInfo.setExplanation(valExp.getMessage());
        GenericFault fault = new GenericFault(valExp.getMessage(), faultInfo, valExp);
        return fault;
    }

    private boolean isAllowed(Organisaatio org, YhteystietoArvo yad) {
        if (org.getOppilaitosTyyppi() != null
                && yad.getKentta().getYhteystietojenTyyppi().getSovellettavatOppilaitostyyppis().contains(org.getOppilaitosTyyppi())) {
            return true;
        }
        for (String otype : org.getTyypit()) {
            if (yad.getKentta().getYhteystietojenTyyppi().getSovellettavatOrganisaatioTyyppis().contains(otype)) {
                return true;
            }
        }
        return false;
    }

    private List<YhteystietoArvo> mergeYhteystietoArvos(Organisaatio org, List<YhteystietoArvo> nys) {

        Map<String, YhteystietoArvo> ov = new HashMap<String, YhteystietoArvo>();

        for (YhteystietoArvo ya : yhteystietoArvoDAO.findByOrganisaatio(org)) {
            if (!isAllowed(org, ya)) {
                yhteystietoArvoDAO.remove(ya);
            } else {
                ov.put(ya.getKentta().getOid(), ya);
            }
        }

        List<YhteystietoArvo> ret = new ArrayList<YhteystietoArvo>();

        for (YhteystietoArvo ya : nys) {
            if (!isAllowed(org, ya)) {
                continue;
            }
            YhteystietoArvo o = ov.get(ya.getKentta().getOid());
            if (o != null) {
                o.setArvoText(ya.getArvoText());
                yhteystietoArvoDAO.update(o);
                ret.add(o);
            } else {
                ya.setOrganisaatio(org);
                ret.add(ya);
            }
        }

        return ret;
    }

    private void filterAddresses(List<? extends YhteystietoDTO> osoitteet) {
        for (Iterator<? extends YhteystietoDTO> ydi = osoitteet.iterator(); ydi.hasNext(); ) {
            YhteystietoDTO yd = ydi.next();
            if (yd instanceof OsoiteDTO) {
                OsoiteDTO od = (OsoiteDTO) yd;
                if (Strings.isNullOrEmpty(od.getOsoite())
                        && Strings.isNullOrEmpty(od.getPostitoimipaikka())) {
                    ydi.remove();
                }
            }
        }
    }

    /**
     * Save the organisation / model.
     *
     * @param model
     * @param updating
     * @return
     * @throws ValidationException
     */
    private OrganisaatioDTO save(OrganisaatioDTO model, boolean updating, boolean skipParentDateValidation) throws ValidationException {
        //Tarkistetaan OID
        if (model.getOid() == null) {
            throw new ValidationException("Oid cannot be null");//trying to update organisaatio that doesn't exist (is is null)");
        } else if (!updating) {
            if ((model.getOid() != null) && (this.findByOid(model.getOid(), false) != null)) {
                throw new RuntimeException("organisaatio already exists, cannot re-create, use update instead");
            }

            if (model.getOppilaitosKoodi() != null && model.getOppilaitosKoodi().length() > 0) {
                if (checkLearningInstitutionCodeIsUniqueAndNotUsed(model)) {
                    throw new LearningInstitutionExistsException("oppilaitos.exists.with.code");
                }
            }
        }
        // validointi: y-tunnus
        if (model.getYtunnus() != null && model.getYtunnus().length() == 0) {
            model.setYtunnus(null);
        }
        if (model.getYtunnus() != null && !Pattern.matches(OrganisaatioValidationConstraints.YTUNNUS_PATTERN, model.getYtunnus())) {
            throw new ValidationException("validation.Organisaatio.ytunnus");
        }

        // validointi: virastotunnus
        if (model.getVirastoTunnus() != null && model.getVirastoTunnus().length() == 0) {
            model.setVirastoTunnus(null);
        }
        if (model.getVirastoTunnus() != null && !Pattern.matches(OrganisaatioValidationConstraints.VIRASTOTUNNUS_PATTERN, model.getVirastoTunnus())) {
            throw new ValidationException("validation.Organisaatio.virastotunnus");
        }

        //Setting the parent paths
        Organisaatio entity = convertToJPA(model, updating); //this entity is populated with new data

        createParentPath(entity, model.getParentOid());

        //Generating the opetuspiteenJarjNro
        String opJarjNro = null;
        if (!updating && StringUtils.isEmpty(model.getOpetuspisteenJarjNro())) {
            opJarjNro = generateOpetuspisteenJarjNro(entity, model);
        } else {
            opJarjNro = model.getOpetuspisteenJarjNro();
        }

        //If inserting, check if ytunnus allready exists in the database
        if (!updating && entity.getYtunnus() != null) {
            checkYtunnusIsUniqueAndNotUsed(entity.getYtunnus());
        }

        entity.setOrganisaatioPoistettu(false);
        //Check if organization has parent and if it has check that passivation dates match to parent
        Organisaatio parentOrg = (model.getParentOid() != null && !model.getParentOid().equalsIgnoreCase(ophOid))
                ? organisaatioDAO.findByOid(model.getParentOid()) : null;



        //OVT-4765 do not validate start date against parent date when updating
        if (updating) {
            LOG.info("this is an update, not validating parent dates.");
            skipParentDateValidation = true;
        }
        //OH-116
        if (parentOrg != null) {
            OrganisationDateValidator dateValidator = new OrganisationDateValidator(skipParentDateValidation);
            if (!dateValidator.apply(Maps.immutableEntry(parentOrg, entity))) {
                throw new OrganizationDateException();
            }
        }

        // Validoidaan organisaation lisätiedot
        List<String> orgTypes = new ArrayList<String>();
        String tyypitStr = "";
        for (fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi curTyyppi : model.getTyypit()) {
            orgTypes.add(curTyyppi.value());
            tyypitStr += curTyyppi.value() + "|";
        }
        entity.setOrganisaatiotyypitStr(tyypitStr);
        validateLisatiedot(model.getYhteystietoArvos(), orgTypes);


        // Konvertoidaan oliograafi
        filterAddresses(model.getYhteystiedot());
        List<Yhteystieto> yhteystiedot = converterFactory.convertYhteystiedotToJPA(model.getYhteystiedot(), Yhteystieto.class, true);

        // Asetetaan yhteystiedot
        entity.setYhteystiedot(yhteystiedot);

        // filtteröi organisaatiotyypin mukaan
        entity.setYhteystietoArvos(mergeYhteystietoArvos(entity, converterFactory.convertYhteystietoArvosToJPA(model.getYhteystietoArvos(), YhteystietoArvo.class, true)));

        //kuvailevat tiedot
        mergeOrganisaatioKuvailevatTiedot(model, entity);

        // Tarkistetaan organisaatio hierarkia
        checkOrganisaatioHierarchy(entity, model.getParentOid());

        for (Yhteystieto yhtTieto : entity.getYhteystiedot()) {
            yhtTieto.setOrganisaatio(entity);
        }

        // Generate natural key, OVT-4954
        // "Jos kyseessä on koulutustoimija pitäisi palauttaa y-tunnus."
        // "Jos oppilaitos, palautetaan oppilaitosnumero."
        // "Jos toimipiste, palautetaan oppilaitosnro+toimipisteenjärjestysnumero(konkatenoituna)sekä yhkoulukoodi."
        entity.setToimipisteKoodi(calculateAndUpdateToimipisteKoodi2(entity));

        // call super.insert OR update which saves & validates jpa
        if (updating) {
            organisaatioDAO.update(entity);
            entity = organisaatioDAO.read(entity.getId());
        } else {
            entity = organisaatioDAO.insert(entity);
        }

        /*
         * Saving the parent relationship
         */
        if (parentOrg == null) {
            // Koulutustoimija in root level is stored under OPH
            Organisaatio uberParent = organisaatioDAO.findByOid(ophOid);
            entity = saveParentSuhde(entity, uberParent, opJarjNro);
        } else {
            entity = saveParentSuhde(entity, parentOrg, opJarjNro);
            if (!updating && entity.getParent() != null) {
                solrIndexer.index(Arrays.asList(parentOrg));
            }
        }

        //index to solr
        solrIndexer.index(Lists.newArrayList(entity));

        OrganisaatioDTO dto = convertToDTO(entity);
        dto.setKuvailevatTiedot(new EntityToOrganisaatioKuvailevatTiedotTyyppiFunction(converterFactory).apply(entity.getMetadata()));

        return dto;
    }

    /*
     * Generating the opetuspiteenJarjNro for an opetuspiste. The opetuspiteenJarjNro is the count of the cescendants of the parent oppilaitos
     * + 1.
     */
    private String generateOpetuspisteenJarjNro(Organisaatio entity,
                                                OrganisaatioDTO model) {
        //Opetuspisteen jarjestysnumero is only generated to opetuspiste which is not also an oppilaitos
        if (model.getTyypit().contains(OrganisaatioTyyppi.OPETUSPISTE) && !model.getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS)) {
            Organisaatio oppilaitosE = findClosestOppilaitos(entity);

            List<OrganisaatioSuhde> children = new ArrayList<OrganisaatioSuhde>();
            getDescendantSuhteet(oppilaitosE, children);
            int nextVal = children.size() + 1;

            String jarjNro = (nextVal < 10) ? String.format("%s%s", "0", nextVal) : String.format("%s", nextVal);
            entity.setOpetuspisteenJarjNro(jarjNro);
            return jarjNro;
        }
        return null;
    }

    private Organisaatio findClosestOppilaitos(Organisaatio orgE) {
        if (orgE.getParentOidPath() == null) {
            return null;
        }

        String[] ancestorOids = orgE.getParentOidPath().split(parentSplitter);
        for (int i = ancestorOids.length - 1; i >= 0; --i) {
            Organisaatio curOrgE = this.organisaatioDAO.findByOid(ancestorOids[i]);
            if (curOrgE != null
                    && curOrgE.getTyypit() != null
                    && curOrgE.getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS.value())) {
                return curOrgE;
            }
        }

        return null;
    }

    private void getDescendantSuhteet(Organisaatio parentE,
                                      List<OrganisaatioSuhde> children) {
        if (parentE == null) {
            return;
        }
        List<OrganisaatioSuhde> curChildren = this.organisaatioSuhdeDAO.findBy("parent", parentE);
        if (curChildren != null) {
            for (OrganisaatioSuhde curChildSuhde : curChildren) {
                getDescendantSuhteet(curChildSuhde.getChild(), children);
            }
            children.addAll(curChildren);
        }

    }

    private void createParentPath(Organisaatio entity, String parentOid) {
        if (parentOid == null) {
            parentOid = ophOid;
        }
        String parentOidPath = "";
        String parentIdPath = "";
        List<Organisaatio> parents = organisaatioDAO.findParentsTo(parentOid);
        for (Organisaatio curParent : parents) {
            parentOidPath += parentSeparator + curParent.getOid();
            parentIdPath += parentSeparator + curParent.getId();
        }
        if (!parents.isEmpty()) {
            parentOidPath += parentSeparator;
            parentIdPath += parentSeparator;
        }
        entity.setParentOidPath(parentOidPath);
        entity.setParentIdPath(parentIdPath);
    }

    private Organisaatio saveParentSuhde(Organisaatio child, Organisaatio parent, String opJarjNro) {
        OrganisaatioSuhde curSuhde = organisaatioSuhdeDAO.findParentTo(child.getId(), null);
        if (parent != null && (curSuhde == null || curSuhde.getParent().getId() != parent.getId())) {
            curSuhde = organisaatioSuhdeDAO.addChild(parent.getId(), child.getId(), Calendar.getInstance().getTime(), opJarjNro);
        }
        child.setParentSuhteet(organisaatioSuhdeDAO.findBy("child", child));
        return this.organisaatioDAO.findByOid(child.getOid());
    }

    private void mergeOrganisaatioKuvailevatTiedot(OrganisaatioDTO model, Organisaatio entity) {
        OrganisaatioKuvailevatTiedotTyyppi kuvailevatTiedot = model.getKuvailevatTiedot();        //hakutoimisto
        if (kuvailevatTiedot != null) {
            //locate existing metadata or create new
            final Organisaatio tmp = entity.getOid() != null ? organisaatioDAO.findByOid(entity.getOid()) : null;
            final OrganisaatioMetaData metadata = tmp != null ? tmp.getMetadata() : null;
            //attach metadata if not available
            if (metadata == null) {
                entity.setMetadata(new OrganisaatioMetaData());
                entity.getMetadata().setOrganisation(entity);
            } else {
                entity.setMetadata(metadata);
            }
            HakutoimistoTyyppi hakutoimistoTyyppi = kuvailevatTiedot.getHakutoimisto();
            if (hakutoimistoTyyppi != null) {
                mergeHakutoimisto(hakutoimistoTyyppi, entity);
            }
            mergeKuva(kuvailevatTiedot.getKuva(), entity);
            mergeSomeLinkit(kuvailevatTiedot.getSoMeLinkit(), entity);
            mergeKuvailevatTiedot(kuvailevatTiedot.getVapaatKuvaukset(), entity);
        } else {
            entity.setMetadata(null);
        }
    }

    private void mergeKuvailevatTiedot(List<KuvailevaTietoTyyppi> vapaatKuvaukset, Organisaatio entity) {
        final OrganisaatioMetaData metadata = entity.getMetadata();

        final Map<KuvailevaTietoTyyppiTyyppi, MonikielinenTeksti> keyValue = new HashMap<KuvailevaTietoTyyppiTyyppi, MonikielinenTeksti>();
        for (KuvailevaTietoTyyppiTyyppi tyyppi : KuvailevaTietoTyyppiTyyppi.values()) {
            MonikielinenTeksti teksti = metadata.getNamedValue(tyyppi.toString());
            if (teksti != null) {
                keyValue.put(tyyppi, teksti);
                teksti.getValues().clear();
            }
        }

        for (KuvailevaTietoTyyppi kuvailevaTieto : vapaatKuvaukset) {
            //MonikielinenTeksti data = keyValue.get(kuvailevaTieto.getTyyppi());
            for (Teksti teksti : kuvailevaTieto.getSisalto().getTeksti()) {
                metadata.setNamedValue(kuvailevaTieto.getTyyppi().toString(), teksti.getKieliKoodi(), teksti.getValue());
            }
            keyValue.remove(kuvailevaTieto.getTyyppi());
        }

    }

    private void mergeSomeLinkit(List<SoMeLinkkiTyyppi> soMeLinkit, Organisaatio entity) {
        final OrganisaatioMetaData metadata = entity.getMetadata();

        final Map<SoMeLinkkiTyyppiTyyppi, MonikielinenTeksti> keyValue = new HashMap<SoMeLinkkiTyyppiTyyppi, MonikielinenTeksti>();
        for (SoMeLinkkiTyyppiTyyppi tyyppi : SoMeLinkkiTyyppiTyyppi.values()) {
            MonikielinenTeksti teksti = metadata.getNamedValue(tyyppi.toString());
            if (teksti != null) {
                keyValue.put(tyyppi, teksti);
                teksti.getValues().clear(); //clear existing
            }
        }

        //key = type
        //language code = integer
        //value = actual data
        int c = 0;
        for (SoMeLinkkiTyyppi linkki : soMeLinkit) {
            //MonikielinenTeksti data = keyValue.get(linkki.getTyyppi());
            //create
            metadata.setNamedValue(linkki.getTyyppi().toString(), Integer.toString(c++), linkki.getSisalto());
            keyValue.remove(linkki.getTyyppi());
        }
    }

    private void mergeKuva(OrganisaatioKuvaTyyppi dto, Organisaatio entity) {
        if (dto == null) {
            entity.getMetadata().setKuva(null);
            return;
        }
        final BinaryData kuva = new BinaryData();
        kuva.setFilename(dto.getFileName());
        kuva.setMimeType(dto.getMimeType());
        kuva.setData(dto.getKuva());
        entity.getMetadata().setKuva(kuva);
    }

    private void mergeHakutoimisto(HakutoimistoTyyppi dto, Organisaatio entity) {
        final OrganisaatioMetaData metadata = entity.getMetadata();

        // clear
        if (dto == null) {
            metadata.setHakutoimistoEctsNimi(null);
            metadata.setHakutoimistoEctsPuhelin(null);
            metadata.setHakutoimistoEctsEmail(null);
            metadata.setHakutoimistoEctsTehtavanimike(null);
            metadata.getYhteystiedot().clear();
            return;
        }

        //convert yhteyshenkilö
        if (dto.getEctsYhteyshenkilo() != null) {
            metadata.setHakutoimistoEctsNimi(dto.getEctsYhteyshenkilo().getKokoNimi());
            metadata.setHakutoimistoEctsPuhelin(dto.getEctsYhteyshenkilo().getPuhelin());
            metadata.setHakutoimistoEctsEmail(dto.getEctsYhteyshenkilo().getEmail());
            metadata.setHakutoimistoEctsTehtavanimike(dto.getEctsYhteyshenkilo().getTitteli());
        } else {
            metadata.setHakutoimistoEctsNimi(null);
            metadata.setHakutoimistoEctsPuhelin(null);
            metadata.setHakutoimistoEctsEmail(null);
            metadata.setHakutoimistoEctsTehtavanimike(null);
        }

        List<Yhteystieto> yhteystiedot = Lists.newArrayList();
        for (YhteystietoDTO yhteystieto : dto.getOpintotoimistoYhteystiedot()) {
            Yhteystieto yt = converterFactory.convertYhteystietoToJPA(yhteystieto, true);
            yhteystiedot.add(yt);
        }

        metadata.getYhteystiedot().clear();
        metadata.getYhteystiedot().addAll(yhteystiedot);

        MonikielinenTeksti nimi = new MonikielinenTekstiTyyppiToEntityFunction().apply(dto.getOpintotoimistoNimi());
        metadata.setHakutoimistoNimi(nimi);
    }

    /**
     * Check that given code has not been used.
     *
     * @param org
     * @return
     */
    private boolean checkLearningInstitutionCodeIsUniqueAndNotUsed(OrganisaatioDTO org) {
        List<Organisaatio> orgs = organisaatioDAO.findBy("oppilaitosKoodi", org.getOppilaitosKoodi().trim());
        if (orgs != null && orgs.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    // OVT-464
    private void checkOrganisaatioHierarchy(Organisaatio organisaatio, String parentOid) {
        LOG.debug("checkOrganisaatioHierarchy()");

        final OrganisationHierarchyValidator validator = new OrganisationHierarchyValidator(ophOid);
        Organisaatio parentOrg = (parentOid != null) ? this.organisaatioDAO.findByOid(parentOid) : null;
        if (validator.apply(Maps.immutableEntry(parentOrg, organisaatio))) {
            //check children
            if (organisaatio.getId() != null) { // we can haz children only if we are already saved
                List<Organisaatio> children = organisaatioDAO.findChildren(organisaatio.getId());
                for (Organisaatio child : children) {
                    if (!validator.apply(Maps.immutableEntry(organisaatio, child))) {
                        throw new OrganisaatioHierarchyException();
                    }
                }
            }
        } else {
            throw new OrganisaatioHierarchyException();
        }
    }

    @Override
    public List<OrganisaatioDTO> findParentsTo(String organisaatioId) {
        LOG.debug("findParentsTo({})", organisaatioId);
        if (organisaatioId == null || organisaatioId.trim().equalsIgnoreCase(ophOid)) {
            return new ArrayList<OrganisaatioDTO>();
        } else {

            List<OrganisaatioDTO> path = new ArrayList<OrganisaatioDTO>();
            OrganisaatioDTO organisaatio = this.convertToDTO(this.organisaatioDAO.findByOid(organisaatioId));
            LOG.debug("parent path: {}", organisaatio.getParentOidPath());
            String[] oids = (organisaatio.getParentOidPath() != null) ? organisaatio.getParentOidPath().split(parentSplitter) : new String[0];
            LOG.debug("Oids length: {}", oids.length);


            for (String curOid : oids) {
                LOG.debug("Cur oid: {}", curOid);
                if (curOid != null && !curOid.isEmpty() && !curOid.equals(ophOid)) {
                    path.add(convertToDTO((this.organisaatioDAO.findByOid(curOid))));
                }
            }
            path.add(organisaatio);
            return path;

        }
    }

    /**
     * Convert list of entities to list of DTOs.
     *
     * @param organisaatios
     * @return
     */
    private List<OrganisaatioDTO> convertListToDto(List<Organisaatio> organisaatios) {
        List<OrganisaatioDTO> orgDtos = new ArrayList<OrganisaatioDTO>();
        for (Organisaatio org : organisaatios) {
            orgDtos.add(convertToDTO(org));
        }
        return orgDtos;
    }

    protected OrganisaatioDTO convertToFatDTO(Organisaatio entity) {
        return converterFactory.convertToFatDTO(entity);
    }

    protected OrganisaatioDTO convertToDTO(Organisaatio entity) {
        return converterFactory.convertToDTO(entity, OrganisaatioDTO.class);
    }

    protected Organisaatio convertToJPA(OrganisaatioDTO dto, boolean merge) {
        return converterFactory.convertOrganisaatioToEntity(dto, merge);
    }

    protected void validateLisatiedot(List<YhteystietoArvoDTO> yhteystietoArvos, List<String> tyypitAsString) throws ValidationException {
        //Kenttä = YhteystietoElementti
        //(Arvo voi olla tekstityyppiä tai yhteystieto-luokan aliluokka)

        List<YhteystietojenTyyppiDTO> metadata = findYhteystietoMetadataForOrganisaatio(tyypitAsString);
        List<YhteystietoElementti> kenttasRequired = new ArrayList<YhteystietoElementti>();
        List<YhteystietoElementti> kenttasFromArvos = new ArrayList<YhteystietoElementti>();

        //Haetaan kaikki pakolliset kentät/elementit kyseiselle yhteystietotyypille
        for (YhteystietojenTyyppiDTO lisatiedotDTO : metadata) {
            for (YhteystietoElementtiDTO yd : lisatiedotDTO.getAllLisatietokenttas()) {
                if (yd.isPakollinen() && yd.isKaytossa()) {
                    kenttasRequired.add(converterFactory.convertYhteystietoElementtiToJPA(yd, false));
                }
            }
            //kenttasRequired.addAll(converterFactory.convertYhteystietoElementtisToJPA(lisatiedotDTO.getAllLisatietokenttas(), YhteystietoElementti.class, false));
        }
        //Haetaan kaikki annettujen arvojen kentät (eli täytetyt kentät)
        for (YhteystietoArvoDTO arvo : yhteystietoArvos) {
            kenttasFromArvos.add(yhteystietoElementtiDAO.findBy("oid", arvo.getKenttaOid()).get(0));
        }

        //Tarkistetaan onko kaikilla Yhteystietotyypille määritellyillä pakollisilla kentillä arvo
        if (!kenttasFromArvos.containsAll(kenttasRequired)) {
            throw new ValidationException("YhteystietoArvo must exist for each YhteystietoElementti");
        }
        //Tarkistetaan arvon pakollisuus ja onko "arvolla" joko tekstityyppistä tai yhteystietotyyppistä arvoa.
        for (YhteystietoArvoDTO arvo : yhteystietoArvos) {
            YhteystietoElementti kentta = yhteystietoElementtiDAO.findBy("oid", arvo.getKenttaOid()).get(0);
            Object theArvo = arvo.getArvo();
            // itse arvo ei voi olla null, jos kenttä on pakollinen
            if (kentta.isPakollinen() && theArvo == null) {
                throw new ValidationException("arvo required for mandatory YhteystietoElementti: " + kentta.getNimi());
            }

        }
    }

    @Override
    public List<OrganisaatioDTO> searchOrganisaatios(OrganisaatioSearchCriteriaDTO searchSpec) {
        LOG.debug("searchOrganisaatios(): sspec={}", searchSpec);

        if (this.isEmptyRestrictions(searchSpec)
                && (searchSpec.getSearchStr() == null || searchSpec.getSearchStr().length() < 3)) {
            throw new OrganisaatioCrudException("organisaatioSearch.tooManyResults");
        }

        //Long startTime = System.currentTimeMillis();
        LOG.debug("Starting root expansion");
        //List<String> oidHierarchy = createOidHierarchy(searchSpec.getOidResctrictionList());
        //LOG.debug("Root expansion took: " + (System.currentTimeMillis() - startTime) + " milliseconds");
        //LOG.debug("oidHierarchySize: " + oidHierarchy.size());

        List<OrganisaatioDTO> matchingOrgs = OrganisaatioBasicConverter.convertSmallToDTOs(organisaatioDAO.findBySearchCriteriaExact(searchSpec.getOrganisaatioTyyppi(), searchSpec.getOppilaitosTyyppi(),
                searchSpec.getKunta(),
                searchSpec.getSearchStr(),
                searchSpec.isSuunnitellut(), searchSpec.isLakkautetut(),
                searchSpec.getMaxResults(), searchSpec.getOidResctrictionList()));

        return matchingOrgs;
    }

    @Override
    public OrganisaatioDTO findByOid(String oid) {
        return findByOid(oid, true);
    }

    @Override
    public List<OrganisaatioDTO> findByOidList(List<String> oids, int maxResults) {
        LOG.debug("findByOidList(oids={}, max={})", oids, maxResults);
        if (oids.contains(ophOid)) {
            //   return convertListToDto(removeRootOrganization(organisaatioDAO.findAll()));
            return convertListToDto(organisaatioDAO.findAll());
        } else {
            // Get result (max MAX RESULTS branches)
            List<Organisaatio> orgsE = Lists.newArrayList(Iterables.filter(organisaatioDAO.findDescendantsByOidList(oids, maxResults), new Predicate<Organisaatio>() {
                final HashSet<String> seen = new HashSet<String>();

                @Override
                public boolean apply(Organisaatio org) {
                    if (!seen.contains(org.getOid())) {
                        seen.add(org.getOid());
                        return true;
                    }
                    return false;
                }
            }));

            return converterFactory.convertToDTO(orgsE, OrganisaatioDTO.class);
        }
    }

    @Override
    public List<OrganisaatioDTO> findChildrenTo(String organisaatioId) {
        LOG.debug("findChildrenTo({})", organisaatioId);
        return convertListToDto(organisaatioDAO.findChildren(organisaatioId, false, true));
    }

    @Override
    @Transactional(rollbackFor = Throwable.class, readOnly = false)
    public YhteystietojenTyyppiDTO createYhteystietojenTyyppi(YhteystietojenTyyppiDTO yhteystietojenTyyppi) throws GenericFault {
        permissionChecker.checkEditYhteystietojentyyppi();

        LOG.debug("createYhteystietojenTyyppi()");

        YhteystietojenTyyppi entity = converterFactory.convertYhteystietojenTyyppiToJPA(yhteystietojenTyyppi, true);//convertToJPA(dto, true);

        try {

            entity = this.yhteystietojenTyyppiDAO.insert(entity);

        } catch (PersistenceException e) {
            // TODO: unique kenttien validointi etukäteen? myös updateen?
            if (e.getCause().toString().contains("ConstraintViolationException")) {
                throw convertValidationExceptionToGenericFault(new ValidationException("constraint violation! unique problem, row with same value already exists?", e));
            }
            throw e;
        }

        return converterFactory.convertToDTO(entity, YhteystietojenTyyppiDTO.class);
    }

    @Override
    public List<YhteystietojenTyyppiDTO> findYhteystietoMetadataForOrganisaatio(List<String> organisaatioTyyppi) {
        LOG.debug("findYhteystietoMetadataForOrganisaatio()");

        if (organisaatioTyyppi == null || organisaatioTyyppi.isEmpty()) {
            return new ArrayList<YhteystietojenTyyppiDTO>();
        }

        List<YhteystietojenTyyppi> entitys = yhteystietojenTyyppiDAO.findLisatietoMetadataForOrganisaatio(organisaatioTyyppi);
        if (entitys == null) {
            return null;
        }
        List<YhteystietojenTyyppiDTO> result = new ArrayList<YhteystietojenTyyppiDTO>();
        for (YhteystietojenTyyppi entity : entitys) {
            result.add((YhteystietojenTyyppiDTO) converterFactory.convertToDTO(entity));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public void updateYhteystietojenTyyppi(YhteystietojenTyyppiDTO yhteystietojenTyyppi) throws GenericFault {
        permissionChecker.checkEditYhteystietojentyyppi();

        LOG.debug("updateYhteystietojenTyyppi()");

        YhteystietojenTyyppi entity = converterFactory.convertYhteystietojenTyyppiToJPA(yhteystietojenTyyppi, true);//convertToJPA(dto, true);
        // updateJPA(entity);
        if (entity == null) {
            throw new RuntimeException("Entity is null.");
        }
        LOG.debug("Updating record: " + entity);

        yhteystietojenTyyppiDAO.update(entity);
    }

    @Override
    public List<YhteystietoArvoDTO> findYhteystietoArvosForOrganisaatio(
            String organisaatioOid) {
        LOG.debug("findYhteystietoArvosForOrganisaatio({})", organisaatioOid);

        return converterFactory.convertToDTO(yhteystietoArvoDAO.findBy("organisaatio.oid", organisaatioOid), YhteystietoArvoDTO.class);
    }

    @Override
    public YhteystietojenTyyppiDTO readYhteystietojenTyyppi(
            String yhteystietojenTyyppiOid) {
        LOG.debug("readYhteystietojenTyyppi({})", yhteystietojenTyyppiOid);
        return converterFactory.convertToDTO(this.yhteystietojenTyyppiDAO.findBy("oid", yhteystietojenTyyppiOid).get(0));
    }

    @Override
    public List<YhteystietojenTyyppiDTO> findYhteystietojenTyyppis(
            SearchCriteriaDTO yhteystietojenTyyppiSearchCriteria) {
        LOG.debug("findYhteystietojenTyyppis()");

        List<YhteystietojenTyyppi> entitys = this.yhteystietojenTyyppiDAO.findAll();
        if (entitys == null) {
            return null;
        }
        List<YhteystietojenTyyppiDTO> result = new ArrayList<YhteystietojenTyyppiDTO>();
        for (YhteystietojenTyyppi entity : entitys) {
            result.add((YhteystietojenTyyppiDTO) converterFactory.convertToDTO(entity));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class, readOnly = false)
    public void removeYhteystietojenTyyppiByOid(String oid)
            throws GenericFault {
        permissionChecker.checkEditYhteystietojentyyppi();

        LOG.debug("removeYhteystietojenTyyppiByOid({})", oid);

        List<YhteystietojenTyyppi> tyypit = this.yhteystietojenTyyppiDAO.findBy("oid", oid);
        if (tyypit.isEmpty()) {
            throw new OrganisaatioCrudException("yhteystietojentyyppi.notFound");
        }
        YhteystietojenTyyppi tyyppiToRemove = tyypit.get(0);
        List<YhteystietoArvo> arvos = this.yhteystietoArvoDAO.findByYhteystietojenTyyppi(tyyppiToRemove);
        if (arvos.isEmpty()) {
            this.yhteystietojenTyyppiDAO.remove(tyyppiToRemove);
        } else {
            throw new OrganisaatioCrudException("yhteystietojenTyyppi.inUse");
        }
    }

    private boolean isEmptyRestrictions(OrganisaatioSearchCriteriaDTO organisaatioSearchCriteria) {
        return organisaatioSearchCriteria.getKunta() == null
                && organisaatioSearchCriteria.getOppilaitosTyyppi() == null
                && organisaatioSearchCriteria.getOrganisaatioTyyppi() == null
                //&& !organisaatioSearchCriteria.isLakkautetut()
                //&& !organisaatioSearchCriteria.isSuunnitellut()
                && organisaatioSearchCriteria.getOidResctrictionList().isEmpty();
    }

    /**
     * This is called when new organisaatio is saved - so there cannot be any
     * existing ytunnus.
     *
     * @param ytunnus
     */
    private void checkYtunnusIsUniqueAndNotUsed(String ytunnus) {
        if (ytunnus != null && !organisaatioDAO.isYtunnusAvailable(ytunnus)) {
            throw new YtunnusException();
        }
    }

    /*
     * Finds organisaatio by oid but does not convert the LOP tiedot
     * if the parameter withLop is false.
     */
    private OrganisaatioDTO findByOid(String oid, boolean withLop) {
        LOG.debug("findByOid({})", oid);
        //long t = System.currentTimeMillis();
        Organisaatio orgE = organisaatioDAO.findByOid(oid);
        if (orgE == null) {
            //LOG.info("findByOId({}) no result in {} ms", oid, System.currentTimeMillis()-t);
            return null;
        }

        OrganisaatioDTO dto = convertToDTO(orgE);

        if (withLop) {
            OrganisaatioMetaData metadataE = orgE.getMetadata() != null ? orgE.getMetadata() : orgE.getParentMetadata();
            dto.setKuvailevatTiedot(new EntityToOrganisaatioKuvailevatTiedotTyyppiFunction(converterFactory).apply(metadataE));
            //If organisation does not have it's own metadata, oids from the parent metadata are removed
            //because parent metadata is used as template data for the organisation.
            if (orgE.getMetadata() == null) {
                clearYtOids(dto.getKuvailevatTiedot());
            }
        }


        //LOG.info("findByOId({}) took {} ms", oid, System.currentTimeMillis()-t);
        return dto;
    }

    private void clearYtOids(OrganisaatioKuvailevatTiedotTyyppi lopTiedot) {
        if (lopTiedot != null
                && lopTiedot.getHakutoimisto() != null
                && lopTiedot.getHakutoimisto().getOpintotoimistoYhteystiedot() != null) {
            for (YhteystietoDTO curYT : lopTiedot.getHakutoimisto().getOpintotoimistoYhteystiedot()) {
                curYT.setYhteystietoOid(null);
            }
        }
    }

    /**
     * Compare that two dates "DATE" (day, month, year) are equal.
     *
     * @param dateA
     * @param dateB
     * @return
     */
    private boolean isEqualDate(Date dateA, Date dateB) {
        boolean result;

        if (dateA == null && dateB == null) {
            // Null == null
            result = true;
        } else if (dateA == null || dateB == null) {
            // Other one is null
            result = false;
        } else {
            Calendar calA = Calendar.getInstance();
            calA.setTime(dateA);

            Calendar calB = Calendar.getInstance();
            calB.setTime(dateB);

            // Compare DAY, MONTH and YEAR
            result =
                    (calA.get(Calendar.DAY_OF_MONTH) == calB.get(Calendar.DAY_OF_MONTH))
                            && (calA.get(Calendar.MONTH) == calB.get(Calendar.MONTH))
                            && (calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR));
        }

        LOG.info("isEqualDate({}, {}) --> {}", new Object[]{dateA, dateB, result});
        return result;
    }


    /**
     * Null / empty check.
     *
     * @param s
     * @return
     */
    private boolean isEmpty(String s) {
        return (s == null || s.trim().isEmpty());
    }

    /**
     * Check given organisation type.
     *
     * @param org
     * @param organisaatioTyyppi
     * @return
     */
    private boolean organisaatioIsOfType(Organisaatio org, OrganisaatioTyyppi organisaatioTyyppi) {
        if (organisaatioTyyppi == null || org == null) {
            return false;
        }

        return (org.getTyypit() != null) && (org.getTyypit().contains(organisaatioTyyppi.value()));
    }


    /**
     * Simple recursive operuspiste / toimipiste koodi calculation.
     *
     * Search "up" for OPPILAITOS and return it's OppilaitosKoodi and then append OPETUSPISTE order number(s).
     *
     * @param s
     * @return
     */
    private String calculateAndUpdateToimipisteKoodi2(Organisaatio s) {
        LOG.info("calculateAndUpdateToimipisteKoodi2(org={})", s);

        if (s == null) {
            LOG.info("  org  == null, return ''");
            return "";
        }

        if (organisaatioIsOfType(s, OrganisaatioTyyppi.OPPILAITOS)) {
            LOG.info("  org  == OPPILAITOS, return oppilaitoskoodi: '{}'", s.getOppilaitosKoodi());
            return s.getOppilaitosKoodi();
        }

        if (organisaatioIsOfType(s, OrganisaatioTyyppi.OPETUSPISTE)) {
            LOG.info("  org  == OPETUSPISTE, return parent opk/olk code AND this ones order number: '{}'", s.getOpetuspisteenJarjNro());
            String onum = isEmpty(s.getOpetuspisteenJarjNro()) ? "01" : s.getOpetuspisteenJarjNro();
            Organisaatio parent = null;
            if (s.getId() != null) {
                parent = (s.getParent() != null) ? s.getParent() : this.organisaatioSuhdeDAO.findParentTo(s.getId(), new Date()).getParent();
            } else {
                parent = s.getParent();
            }
            return calculateAndUpdateToimipisteKoodi2(parent) + onum;
        }

        LOG.error("calculateAndUpdateToimipisteKoodi2 == TYPE unknown?: types='{}'", s.getTyypit());

        return "";
    }

}

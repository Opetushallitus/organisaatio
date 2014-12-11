/*
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
package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioHakutulos;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.api.search.OrganisaatioSearchCriteria;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.business.exception.NotAuthorizedException;
import fi.vm.sade.organisaatio.dao.impl.YhteystietoElementtiDAOImpl;
import fi.vm.sade.organisaatio.dao.impl.YhteystietojenTyyppiDAOImpl;
import fi.vm.sade.organisaatio.dto.mapping.SearchCriteriaModelMapper;
import fi.vm.sade.organisaatio.helper.OrganisaatioDisplayHelper;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.YhteystietojenTyyppi;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.organisaatio.resource.dto.YhteystietojenTyyppiRDTO;
import fi.vm.sade.organisaatio.auth.PermissionChecker;
import fi.vm.sade.organisaatio.service.search.OrganisaatioSearchService;
import fi.vm.sade.organisaatio.service.search.SearchCriteria;
import fi.vm.sade.organisaatio.service.util.OrganisaatioPerustietoUtil;


import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.service.exception.SadeBusinessException;
import fi.vm.sade.koodisto.service.KoodiService;
import fi.vm.sade.koodisto.service.KoodistoService;
import fi.vm.sade.koodisto.service.types.SearchKoodisByKoodistoCriteriaType;
import fi.vm.sade.koodisto.service.types.SearchKoodistosCriteriaType;
import fi.vm.sade.koodisto.service.types.common.KoodiType;
import fi.vm.sade.koodisto.service.types.common.KoodiUriAndVersioType;
import fi.vm.sade.koodisto.service.types.common.KoodistoType;
import fi.vm.sade.koodisto.service.types.common.SuhteenTyyppiType;
import fi.vm.sade.koodisto.util.KoodiServiceSearchCriteriaBuilder;
import fi.vm.sade.koodisto.util.KoodistoServiceSearchCriteriaBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Ordering;
import fi.vm.sade.organisaatio.business.OrganisaatioDeleteBusinessService;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAO;
import fi.vm.sade.organisaatio.model.OrganisaatioResult;
import fi.vm.sade.organisaatio.resource.dto.ResultRDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.validation.ValidationException;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.cors.CrossOriginResourceSharing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Antti Salonen
 * @author mlyly
 */
@Component
@Transactional(readOnly = true)
@CrossOriginResourceSharing(allowAllOrigins = true)
public class OrganisaatioResourceImpl implements OrganisaatioResource {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioResourceImpl.class);
    @Autowired
    private OrganisaatioBusinessService organisaatioBusinessService;
    @Autowired
    private OrganisaatioDeleteBusinessService organisaatioDeleteBusinessService;
    @Autowired
    private OrganisaatioSearchService organisaatioSearchService;
    @Autowired
    private OrganisaatioDAO organisaatioDAO;
    @Autowired
    private YhteystietojenTyyppiDAOImpl yhteystietojenTyyppiDAO;
    @Autowired
    protected YhteystietoElementtiDAOImpl yhteystietoElementtiDAO;
    @Autowired
    private ConversionService conversionService;
    @Autowired
    private IndexerResource solrIndexer;
    @Autowired
    private KoodiService koodiService;
    @Autowired
    private KoodistoService koodistoService;

    @Value("${root.organisaatio.oid}")
    private String rootOrganisaatioOid;

    @Value("${koodisto-uris.opetuspisteet}")
    private String toimipistekoodisto;

    @Value("${koodisto-uris.yhteishaunkoulukoodi}")
    private String yhKoulukoodiKoodisto;

    @Autowired
    PermissionChecker permissionChecker;

    @Autowired
    private SearchCriteriaModelMapper searchCriteriaModelMapper;

    @Override
    public OrganisaatioHakutulos searchHierarchy(OrganisaatioSearchCriteria s) {
        final OrganisaatioHakutulos tulos = new OrganisaatioHakutulos();

        if (s.getOppilaitosTyyppi() != null && s.getOppilaitosTyyppi().isEmpty()) {
            s.setOppilaitosTyyppi(null);
        }

        if (s.getOrganisaatioTyyppi() != null && s.getOrganisaatioTyyppi().length() == 0) {
            s.setOrganisaatioTyyppi(null);
        }

        // Map api search criteria to solr search criteria
        SearchCriteria searchCriteria = searchCriteriaModelMapper.map(s, SearchCriteria.class);

//        System.out.println("oidRestrictionList:" + s.getOidRestrictionList());
        List<OrganisaatioPerustieto> organisaatiot = organisaatioSearchService.searchHierarchy(searchCriteria);

        //sorttaa
        final Ordering<OrganisaatioPerustieto> ordering = Ordering.natural().nullsFirst().onResultOf(new Function<OrganisaatioPerustieto, Comparable<String>>() {
            @Override
            public Comparable<String> apply(OrganisaatioPerustieto input) {
                return OrganisaatioDisplayHelper.getClosestBasic(I18N.getLocale(), input);

            }
        ;
        });

        organisaatiot = ordering.immutableSortedCopy(organisaatiot);

        //rakenna hierarkia
        tulos.setOrganisaatiot(OrganisaatioPerustietoUtil.createHierarchy(organisaatiot));

        tulos.setNumHits(organisaatiot.size());
        return tulos;
    }

    // GET /organisaatio/{oid}/children
    @Override
    public List<OrganisaatioRDTO> children(String oid) throws Exception {
        Preconditions.checkNotNull(oid);
        Organisaatio parentOrg = organisaatioDAO.findByOid(oid);
        List<OrganisaatioRDTO> childList = new LinkedList<>();
        if (parentOrg != null) {
            List<OrganisaatioSuhde> suhteet = parentOrg.getChildSuhteet();
            for (OrganisaatioSuhde suhde : suhteet) {
                childList.add(conversionService.convert(suhde.getChild(), OrganisaatioRDTO.class));
            }
        }
        return childList;
    }

    // GET /organisaatio/{oid}/childoids
    @Override
    public String childoids(String oid) throws Exception {
        Preconditions.checkNotNull(oid);
        Organisaatio parentOrg = organisaatioDAO.findByOid(oid);
        List<String> childOidList = new LinkedList<>();
        if (parentOrg != null) {
            List<OrganisaatioSuhde> suhteet = parentOrg.getChildSuhteet();
            for (OrganisaatioSuhde suhde : suhteet) {
                childOidList.add("\"" + suhde.getChild().getOid() + "\"");
            }
        }
        return "{ \"oids\": [" + Joiner.on(",").join(childOidList) + "]}";
    }

    // GET /organisaatio/{oid}/parentoids - used for security purposes
    @Override
    public String parentoids(String oid) throws Exception {
        Preconditions.checkNotNull(oid);
        // find parents
        final List<String> parentOidList = organisaatioSearchService.findParentOids(oid);
        Collections.reverse(parentOidList);

        // NOTE - this assumes everything is under one "root", ie. "OPH"
        if (!parentOidList.contains(rootOrganisaatioOid)) {
            parentOidList.add(0, rootOrganisaatioOid); // add root organisaatio if needed
        }
        if (!parentOidList.contains(oid)) {
            parentOidList.add(oid); // add self if needed
        }
        return Joiner.on(OID_SEPARATOR).join(parentOidList);
    }

    @Override
    public String hello() {
        return "Well Hello! " + new Date();
    }

    // GET /organisaatio?searchTerms=x&count=10&startIndex=100&lastModifiedBefore=X&lastModifiedSince=Y
    @Override
    public List<String> search(String searchTerms, int count, int startIndex, Date lastModifiedBefore, Date lastModifiedSince) {
        LOG.debug("search({}, {}, {}, {}, {})", new Object[]{searchTerms, count, startIndex, lastModifiedBefore, lastModifiedSince});

        // Check the type spesified search
        OrganisaatioTyyppi type = null;
        if (searchTerms != null) {
            for (OrganisaatioTyyppi organisaatioTyyppi : OrganisaatioTyyppi.values()) {
                if (searchTerms.contains("type=" + organisaatioTyyppi.name())) {
                    type = organisaatioTyyppi;
                    break;
                }
            }
        }

        List<String> result = organisaatioDAO.findOidsBy(searchTerms, count, startIndex, lastModifiedBefore, lastModifiedSince, type);
        LOG.debug("  result.size = {}", result.size());
        return result;
    }

    // GET /organisaatio/{oid}
    @Override
    public OrganisaatioRDTO getOrganisaatioByOID(final String oid) {
        LOG.debug("/organisaatio/{} -- getOrganisaatioByOID()", oid);

        // Search order
        // 1. OID
        // 2. Y-TUNNUS
        // 3. VIRASTOTUNNUS
        // 4. OPPILAITOSKOODI
        // 5. TOIMIPISTEKOODI
        Organisaatio o = organisaatioDAO.findByOid(oid);
        if (o == null) {
            o = organisaatioDAO.findByYTunnus(oid);
        }
        if (o == null) {
            o = organisaatioDAO.findByVirastoTunnus(oid);
        }
        if (o == null) {
            o = organisaatioDAO.findByOppilaitoskoodi(oid);
        }
        if (o == null) {
            o = organisaatioDAO.findByToimipistekoodi(oid);
        }
        // If the organisaatio is toimipiste and it does not have a value in yhteishaunKoulukoodi field
        // such is saught from koodisto
        if (o != null) {
            if (o.getTyypit().contains(OrganisaatioTyyppi.TOIMIPISTE.value()) && isEmpty(o.getYhteishaunKoulukoodi())) {
                updateYhKoulukoodi(o);
            }
        }

        OrganisaatioRDTO result = conversionService.convert(o, OrganisaatioRDTO.class);

        LOG.debug("  result={}", result);
        return result;
    }

    /*
     * method that searches for the yhkoodi of the organisaatio. If such is found is updated
     * to the yhteyishanKoulukoodi field of organisaatio.
     */
    private void updateYhKoulukoodi(Organisaatio o) {
        String yhKoodi = null;
        String olkoodi = null;
        Organisaatio parentOl = null;
        if (o.getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS.value())) {
            olkoodi = o.getOppilaitosKoodi();
        } else if ((parentOl = getParentOl(o)) != null) {
            olkoodi = parentOl.getOppilaitosKoodi();
        }
        if (!isEmpty(olkoodi)) {
            yhKoodi = getYhkoodi(olkoodi, this.getOpPisteenJarjNro(o));
        }
        if (!isEmpty(yhKoodi)) {
            o.setYhteishaunKoulukoodi(yhKoodi);
            this.organisaatioDAO.update(o);
        }
    }

    /*
     * Method that seeks the nearest oppilaitos ancestor of organisaatio o.
     */
    private Organisaatio getParentOl(Organisaatio o) {
        Organisaatio parentOl = o.getParent();
        if (parentOl == null || parentOl.getOid().equals(this.rootOrganisaatioOid)) {
            return null;
        }
        if (parentOl.getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS.value())) {
            return parentOl;
        }
        return getParentOl(parentOl);
    }

    private boolean isEmpty(String val) {
        return val == null || val.isEmpty();
    }

    /*
     * method that seeks the yhkoodi for organisaatio in koodisto. Organisaatio is represented by olkoodi and opJnro.
     * Organisaatio - > Organisaatiokoodi (in: opetuspisteet -koodisto) -> yhkoodi (in: yhteishaunkoulukoodisto)
     */
    private String getYhkoodi(String olkoodi, String opJnro) {
        List<KoodiType> koodis = getKoodisByArvoAndKoodisto(olkoodi + opJnro, toimipistekoodisto);
        KoodiType opNroKoodi = null;
        KoodiType yhKoodi = null;
        if (koodis != null && !koodis.isEmpty()) {
            opNroKoodi = koodis.get(0);
            yhKoodi = getSisaltyvaKoodi(opNroKoodi, yhKoulukoodiKoodisto);
        }
        return (yhKoodi == null) ? null : yhKoodi.getKoodiArvo();
    }

    /*
     * Gets the opetuspisteenJarjNro for organisaatio.
     */
    private String getOpPisteenJarjNro(Organisaatio orgE) {
        String opPisteenJarjNro = "";
        if (orgE.getOpetuspisteenJarjNro() != null) {
            opPisteenJarjNro = orgE.getOpetuspisteenJarjNro();
        }
        return opPisteenJarjNro;
    }

    /*
     * Search for koodis by arvo and koodisto.
     */
    private List<KoodiType> getKoodisByArvoAndKoodisto(String arvo, String koodistoUri) {
        try {
            SearchKoodistosCriteriaType koodistoSearchCriteria = KoodistoServiceSearchCriteriaBuilder.latestKoodistoByUri(koodistoUri);

            List<KoodistoType> koodistoResult = koodistoService.searchKoodistos(koodistoSearchCriteria);
            if (koodistoResult.size() != 1) {
                // FIXME: Throw something other than RuntimeException?
                throw new RuntimeException("No koodisto found for koodisto URI " + koodistoUri);
            }
            KoodistoType koodisto = koodistoResult.get(0);

            SearchKoodisByKoodistoCriteriaType koodiSearchCriteria = KoodiServiceSearchCriteriaBuilder.koodisByArvoAndKoodistoUriAndKoodistoVersio(arvo,
                    koodistoUri, koodisto.getVersio());
            return koodiService.searchKoodisByKoodisto(koodiSearchCriteria);
        } catch (Exception exp) {
            exp.printStackTrace();
            return null;
        }
    }

    /*
     * Getting a sisaltyva koodi.
     */
    private KoodiType getSisaltyvaKoodi(KoodiType sourcekoodi, String targetKoodisto) {
        KoodiUriAndVersioType uriAndVersio = new KoodiUriAndVersioType();
        uriAndVersio.setKoodiUri(sourcekoodi.getKoodiUri());
        uriAndVersio.setVersio(sourcekoodi.getVersio());
        List<KoodiType> relatedKoodis = koodiService.listKoodiByRelation(uriAndVersio, false, SuhteenTyyppiType.SISALTYY);
        for (KoodiType curKoodi : relatedKoodis) {
            if (curKoodi.getKoodisto().getKoodistoUri().equals(targetKoodisto)) {
                return curKoodi;
            }
        }
        return null;
    }

    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public ResultRDTO updateOrganisaatio(String oid, OrganisaatioRDTO ordto) {
        LOG.info("Saving " + oid);
        try {
            permissionChecker.checkSaveOrganisation(ordto, true);
        } catch (NotAuthorizedException nae) {
            LOG.warn("Not authorized to update organisation: " + oid);
            throw new OrganisaatioResourceException(nae);
        }

        try {
            OrganisaatioResult result = organisaatioBusinessService.save(ordto, true, true);
            return new ResultRDTO(conversionService.convert(result.getOrganisaatio(), OrganisaatioRDTO.class),
                    result.getInfo()==null ? ResultRDTO.ResultStatus.OK : ResultRDTO.ResultStatus.WARNING, result.getInfo());
        } catch (ValidationException ex) {
            LOG.warn("Error saving " + oid, ex);
            throw new OrganisaatioResourceException(Response.Status.INTERNAL_SERVER_ERROR,
                    ex.getMessage(), "organisaatio.validointi.virhe");
        } catch (SadeBusinessException sbe) {
            LOG.warn("Error saving " + oid, sbe);
            throw new OrganisaatioResourceException(sbe);
        } catch (OrganisaatioResourceException ore) {
            LOG.warn("Error saving " + oid, ore);
            throw ore;
        } catch (Throwable t) {
            LOG.error("Error saving " + oid, t);
            throw new OrganisaatioResourceException(Response.Status.INTERNAL_SERVER_ERROR,
                    t.getMessage(), "generic.error");
        }
    }

    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public String deleteOrganisaatio(String oid) {
        try {
            permissionChecker.checkRemoveOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            LOG.warn("Not authorized to delete organisation: " + oid);
            throw new OrganisaatioResourceException(nae);
        }

        try {
            Organisaatio parent = organisaatioDeleteBusinessService.deleteOrganisaatio(oid);
            LOG.info("Deleted organisaatio: " + oid +" under parent: " + parent.getOid());
        } catch (SadeBusinessException sbe) {
            LOG.warn("Error deleting org", sbe);
            throw new OrganisaatioResourceException(sbe);
        }

        return "{\"message\": \"deleted\"}";
    }

    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public ResultRDTO newOrganisaatio(OrganisaatioRDTO ordto) {
        try {
            permissionChecker.checkSaveOrganisation(ordto, false);
        } catch (NotAuthorizedException nae) {
            LOG.warn("Not authorized to create child organisation for: " + ordto.getParentOid());
            throw new OrganisaatioResourceException(nae);
        }
        try {
            OrganisaatioResult result = organisaatioBusinessService.save(ordto, false, false);
            return new ResultRDTO(conversionService.convert(result.getOrganisaatio(), OrganisaatioRDTO.class),
                    result.getInfo()==null ? ResultRDTO.ResultStatus.OK : ResultRDTO.ResultStatus.WARNING, result.getInfo());
        } catch (ValidationException ex) {
            LOG.warn("Error saving new org", ex);
            throw new OrganisaatioResourceException(Response.Status.INTERNAL_SERVER_ERROR,
                    ex.getMessage(), "organisaatio.validointi.virhe");
        } catch (SadeBusinessException sbe) {
            LOG.warn("Error saving new org", sbe);
            throw new OrganisaatioResourceException(sbe);
        } catch (Throwable t) {
            LOG.warn("Error saving new org", t);
            throw new OrganisaatioResourceException(Response.Status.INTERNAL_SERVER_ERROR,
                    t.getMessage(), "generic.error");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<YhteystietojenTyyppiRDTO> getYhteystietoMetadata(List<String> organisaatioTyyppi) {
        if (organisaatioTyyppi == null || organisaatioTyyppi.isEmpty()) {
            return new ArrayList<>();
        }
        List<YhteystietojenTyyppi> entitys = yhteystietojenTyyppiDAO.findLisatietoMetadataForOrganisaatio(organisaatioTyyppi);
        if (entitys == null) {
            return null;
        }
        List<YhteystietojenTyyppiRDTO> result = new ArrayList<>();
        for (YhteystietojenTyyppi entity : entitys) {
            result.add(conversionService.convert(entity, YhteystietojenTyyppiRDTO.class));
        }
        return result;
    }

    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public String authHello() {
        return "{\"message\": \"Well Hello! " + new Date() + "\"}";
    }

    @Override
    public List<OrganisaatioRDTO> groups(String oid) throws Exception {
        Preconditions.checkNotNull(oid);

        List<Organisaatio> entitys = organisaatioDAO.findGroups();
        if (entitys == null) {
            return null;
        }

        List<OrganisaatioRDTO> groupList = new ArrayList<>();
        for (Organisaatio entity : entitys) {
            groupList.add(conversionService.convert(entity, OrganisaatioRDTO.class));
        }
        return groupList;
    }
}

/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
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
 */

package fi.vm.sade.organisaatio.resource.impl.v2;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import fi.vm.sade.generic.service.exception.SadeBusinessException;
import fi.vm.sade.organisaatio.api.DateParam;
import fi.vm.sade.organisaatio.api.search.OrganisaatioHakutulos;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.auth.PermissionChecker;
import fi.vm.sade.organisaatio.business.Hakutoimisto;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.business.OrganisaatioFindBusinessService;
import fi.vm.sade.organisaatio.business.exception.NotAuthorizedException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioNotFoundException;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAO;
import fi.vm.sade.organisaatio.dto.mapping.*;
import fi.vm.sade.organisaatio.dto.v2.*;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.resource.OrganisaatioResourceException;
import fi.vm.sade.organisaatio.resource.dto.HakutoimistoDTO;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.organisaatio.resource.v2.OrganisaatioResourceV2;
import fi.vm.sade.organisaatio.service.search.OrganisaatioSearchService;
import fi.vm.sade.organisaatio.service.search.SearchCriteria;
import fi.vm.sade.organisaatio.service.util.OrganisaatioPerustietoUtil;
import org.apache.cxf.jaxrs.cors.CrossOriginResourceSharing;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ValidationException;
import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author simok
 */
@Component
@Transactional(readOnly = true)
@CrossOriginResourceSharing(allowAllOrigins = true)
public class OrganisaatioResourceImplV2 implements OrganisaatioResourceV2 {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioResourceImplV2.class);

    @Autowired
    private OrganisaatioBusinessService organisaatioBusinessService;

    @Autowired
    private OrganisaatioFindBusinessService organisaatioFindBusinessService;

    @Autowired
    private OrganisaatioModelMapper organisaatioModelMapper;

    @Autowired
    private OrganisaatioNimiModelMapper organisaatioNimiModelMapper;

    @Autowired
    private OrganisaatioSuhdeModelMapper organisaatioSuhdeModelMapper;

    @Autowired
    private OrganisaatioLiitosModelMapper organisaatioLiitosModelMapper;

    @Autowired
    private GroupModelMapper groupModelMapper;

    @Autowired
    private ConversionService conversionService;

    @Autowired
    private OrganisaatioSearchService organisaatioSearchService;

    @Autowired
    private SearchCriteriaModelMapper searchCriteriaModelMapper;

    @Autowired
    private OrganisaatioDAO organisaatioDAO;

    @Autowired
    PermissionChecker permissionChecker;

    @Override
    @Transactional(readOnly = true)
    public List<OrganisaatioYhteystiedotDTOV2> searchOrganisaatioYhteystiedot(YhteystiedotSearchCriteriaDTOV2 hakuEhdot) {
        LOG.debug("searchOrganisaatioYhteystiedot: " + hakuEhdot.getKieliList());
        LOG.debug("searchOrganisaatioYhteystiedot: " + hakuEhdot.getKuntaList());
        LOG.debug("searchOrganisaatioYhteystiedot: " + hakuEhdot.getOppilaitostyyppiList());
        LOG.debug("searchOrganisaatioYhteystiedot: " + hakuEhdot.getVuosiluokkaList());
        LOG.debug("searchOrganisaatioYhteystiedot: " + hakuEhdot.getYtunnusList());
        LOG.debug("searchOrganisaatioYhteystiedot: " + hakuEhdot.getOidList());
        LOG.debug("searchOrganisaatioYhteystiedot: " + hakuEhdot.getLimit());

        // TODO tarkistetaanko tässä vai business kerroksessa parametrit

        List<Organisaatio> organisaatiot = organisaatioFindBusinessService.findBySearchCriteria(
                hakuEhdot.getKieliList(),
                hakuEhdot.getKuntaList(),
                hakuEhdot.getOppilaitostyyppiList(),
                hakuEhdot.getVuosiluokkaList(),
                hakuEhdot.getYtunnusList(),
                hakuEhdot.getOidList(),
                hakuEhdot.getLimit());

        // Define the target list type for mapping
        Type organisaatioYhteystiedotDTOV2ListType = new TypeToken<List<OrganisaatioYhteystiedotDTOV2>>() {}.getType();

        // Map domain type to DTO
        return organisaatioModelMapper.map(organisaatiot, organisaatioYhteystiedotDTOV2ListType);
    }

    @Override
    public String hello() {
        return "Hello V2! " + new Date();
    }

    @Override
    public OrganisaatioHakutulos searchOrganisaatioHierarkia(OrganisaatioSearchCriteriaDTOV2 hakuEhdot) {
        final OrganisaatioHakutulos tulos = new OrganisaatioHakutulos();

        // Map api search criteria to solr search criteria
        SearchCriteria searchCriteria = searchCriteriaModelMapper.map(hakuEhdot, SearchCriteria.class);

        // Hae organisaatiot
        List<OrganisaatioPerustieto> organisaatiot = organisaatioSearchService.searchHierarchy(searchCriteria);

        // Rakenna hierarkia
        tulos.setOrganisaatiot(OrganisaatioPerustietoUtil.createHierarchy(organisaatiot));

        // Lukumäärä tuloksiin
        tulos.setNumHits(organisaatiot.size());

        return tulos;
    }

    private List<OrganisaatioPerustietoSuppea> convertLaajaToSuppea(List<OrganisaatioPerustieto> organisaatiot, boolean tyypit) {
        List<OrganisaatioPerustietoSuppea> opts = new ArrayList<>();

        for (OrganisaatioPerustieto fullItem : organisaatiot) {
            OrganisaatioPerustietoSuppea item = new OrganisaatioPerustietoSuppea();
            item.setOid(fullItem.getOid());
            item.setNimi(fullItem.getNimi());
            if (tyypit) {
                item.setOrganisaatiotyypit(fullItem.getOrganisaatiotyypit());
                item.setOppilaitostyyppi(fullItem.getOppilaitostyyppi());
            }
            if (item.getChildren() != null) {
                item.setChildren(convertLaajaToSuppea(fullItem.getChildren(), tyypit));
            }
            opts.add(item);
        }

        return opts;
    }

    private OrganisaatioHakutulosSuppeaDTOV2 searchOrganisaatioHierarkiaSuppea(OrganisaatioSearchCriteriaDTOV2 hakuEhdot, boolean tyypit) {
        final OrganisaatioHakutulos tulos = new OrganisaatioHakutulos();

        // Map api search criteria to solr search criteria
        SearchCriteria searchCriteria = searchCriteriaModelMapper.map(hakuEhdot, SearchCriteria.class);

        // Hae organisaatiot
        List<OrganisaatioPerustieto> organisaatiot = organisaatioSearchService.searchHierarchy(searchCriteria);

        // Rakenna hierarkia
        tulos.setOrganisaatiot(OrganisaatioPerustietoUtil.createHierarchy(organisaatiot));

        OrganisaatioHakutulosSuppeaDTOV2 ohts = new OrganisaatioHakutulosSuppeaDTOV2();

        ohts.setNumHits(tulos.getNumHits());
        ohts.setOrganisaatiot(convertLaajaToSuppea(tulos.getOrganisaatiot(), tyypit));

        return ohts;
    }

    @Override
    public OrganisaatioHakutulosSuppeaDTOV2 searchOrganisaatioHierarkiaNimet(OrganisaatioSearchCriteriaDTOV2 hakuEhdot) {
        return searchOrganisaatioHierarkiaSuppea(hakuEhdot, false);
    }

    @Override
    public OrganisaatioHakutulosSuppeaDTOV2 searchOrganisaatioHierarkiaTyypit(OrganisaatioSearchCriteriaDTOV2 hakuEhdot) {
        return searchOrganisaatioHierarkiaSuppea(hakuEhdot, true);
    }

    @Override
    public OrganisaatioHakutulos searchOrganisaatiot(OrganisaatioSearchCriteriaDTOV2 hakuEhdot) {
        final OrganisaatioHakutulos tulos = new OrganisaatioHakutulos();

        // Map api search criteria to solr search criteria
        SearchCriteria searchCriteria = searchCriteriaModelMapper.map(hakuEhdot, SearchCriteria.class);

        // Hae organisaatiot
        List<OrganisaatioPerustieto> organisaatiot = organisaatioSearchService.searchExact(searchCriteria);

        // Organisaatiot tuloksiin
        tulos.setOrganisaatiot(organisaatiot);

        // Lukumäärä tuloksiin
        tulos.setNumHits(organisaatiot.size());

        return tulos;
    }

    private OrganisaatioHakutulosSuppeaDTOV2 searchOrganisaatiotSuppea(OrganisaatioSearchCriteriaDTOV2 hakuEhdot, boolean tyypit) {
        final OrganisaatioHakutulos tulos = new OrganisaatioHakutulos();

        // Map api search criteria to solr search criteria
        SearchCriteria searchCriteria = searchCriteriaModelMapper.map(hakuEhdot, SearchCriteria.class);

        // Hae organisaatiot
        List<OrganisaatioPerustieto> organisaatiot = organisaatioSearchService.searchExact(searchCriteria);

        // Organisaatiot tuloksiin
        tulos.setOrganisaatiot(organisaatiot);

        // Lukumäärä tuloksiin
        tulos.setNumHits(organisaatiot.size());

        OrganisaatioHakutulosSuppeaDTOV2 ohts = new OrganisaatioHakutulosSuppeaDTOV2();

        ohts.setNumHits(tulos.getNumHits());
        ohts.setOrganisaatiot(convertLaajaToSuppea(tulos.getOrganisaatiot(), tyypit));

        return ohts;
    }

    @Override
    public OrganisaatioHakutulosSuppeaDTOV2 searchOrganisaatiotNimet(OrganisaatioSearchCriteriaDTOV2 hakuEhdot) {
        return searchOrganisaatiotSuppea(hakuEhdot, false);
    }

    @Override
    public OrganisaatioHakutulosSuppeaDTOV2 searchOrganisaatiotTyypit(OrganisaatioSearchCriteriaDTOV2 hakuEhdot) {
        return searchOrganisaatiotSuppea(hakuEhdot, true);
    }

    @Override
    public OrganisaatioPaivittajaDTOV2 getOrganisaatioPaivittaja(String oid) throws Exception {
        Preconditions.checkNotNull(oid);

        LOG.debug("searchOrganisaatioPaivittaja: " + oid);

        Organisaatio org = organisaatioDAO.findByOid(oid);

        if (org != null) {
            final OrganisaatioPaivittajaDTOV2 tulos = new OrganisaatioPaivittajaDTOV2();
            tulos.setPaivittaja(org.getPaivittaja());
            tulos.setPaivitysPvm(org.getPaivitysPvm());
            return tulos;
        }

        return null;
    }

    @Override
    public List<OrganisaatioNimiDTOV2> getOrganisaatioNimet(String oid) throws Exception {
        Preconditions.checkNotNull(oid);

        List<OrganisaatioNimi> organisaatioNimet = organisaatioBusinessService.getOrganisaatioNimet(oid);

        // Define the target list type for mapping
        Type organisaatioNimiDTOV2ListType = new TypeToken<List<OrganisaatioNimiDTOV2>>() {}.getType();

        // Map domain type to DTO
        return organisaatioNimiModelMapper.map(organisaatioNimet, organisaatioNimiDTOV2ListType);
    }

    private Map<String, String> convertMKTToMap(MonikielinenTeksti nimi) {
        Map<String, String> result = new HashMap<>();

        if (nimi != null) {
            result.putAll(nimi.getValues());
        }

        return result;
    }

    @Override
    public OrganisaatioLOPTietoDTOV2 getOrganisaationLOPTiedotByOID(String oid) {
        Preconditions.checkNotNull(oid);

        LOG.debug("getOrganisaationLOPTiedotByOID: " + oid);

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

        if (o != null) {
            final OrganisaatioLOPTietoDTOV2 tulos = new OrganisaatioLOPTietoDTOV2();
            tulos.setOid(o.getOid());
            tulos.setNimi(convertMKTToMap(o.getNimi()));
            if (o.getMetadata() != null && o.getMetadata().getValues() != null) {
                for (NamedMonikielinenTeksti namedMonikielinenTeksti : o.getMetadata().getValues()) {
                    tulos.addByKey(namedMonikielinenTeksti.getKey(), convertMKTToMap(namedMonikielinenTeksti.getValue()));
                }
            }
            return tulos;
        }

        return null;
    }

    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public OrganisaatioNimiDTOV2 newOrganisaatioNimi(String oid, OrganisaatioNimiDTOV2 nimidto) throws Exception {
        Preconditions.checkNotNull(oid);

        try {
            permissionChecker.checkUpdateOrganisationName(oid);
        } catch (NotAuthorizedException nae) {
            throw new OrganisaatioResourceException(nae);
        }

        OrganisaatioNimi organisaatioNimi = organisaatioBusinessService.newOrganisaatioNimi(oid, nimidto);

        return organisaatioNimiModelMapper.map(organisaatioNimi, OrganisaatioNimiDTOV2.class);
    }

    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public OrganisaatioNimiDTOV2 updateOrganisaatioNimi(String oid, DateParam date, OrganisaatioNimiDTOV2 nimidto) {
        Preconditions.checkNotNull(oid);
        Preconditions.checkNotNull(date);

        try {
            permissionChecker.checkUpdateOrganisationName(oid);
        } catch (NotAuthorizedException nae) {
            throw new OrganisaatioResourceException(nae);
        }


        OrganisaatioNimi organisaatioNimi = organisaatioBusinessService.updateOrganisaatioNimi(oid, date.getValue(), nimidto);

        return organisaatioNimiModelMapper.map(organisaatioNimi, OrganisaatioNimiDTOV2.class);
    }

    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public String deleteOrganisaatioNimi(String oid, DateParam date) {
        Preconditions.checkNotNull(oid);
        Preconditions.checkNotNull(date);

        try {
            permissionChecker.checkUpdateOrganisationName(oid);
        } catch (NotAuthorizedException nae) {
            throw new OrganisaatioResourceException(nae);
        }

        organisaatioBusinessService.deleteOrganisaatioNimi(oid, date.getValue());

        return "";
    }

    @Override
    public OrganisaatioMuokkausTulosListaDTO muokkaaMontaOrganisaatiota(List<OrganisaatioMuokkausTiedotDTO> tiedot) {
        LOG.debug("muokkaaMontaOrganisaatiota:" + tiedot);

        try {
            OrganisaatioMuokkausTulosListaDTO tulos = organisaatioBusinessService.bulkUpdatePvm(tiedot);
            return tulos;
        } catch (ValidationException ex) {
            LOG.warn("Error saving multiple organizations", ex);
            throw new OrganisaatioResourceException(Response.Status.INTERNAL_SERVER_ERROR,
                    ex.getMessage(), "organisaatio.validointi.virhe");
        } catch (SadeBusinessException sbe) {
            LOG.warn("Error saving multiple organizations", sbe);
            throw new OrganisaatioResourceException(sbe);
        } catch (OrganisaatioResourceException ore) {
            LOG.warn("Error saving multiple organizations", ore);
            throw ore;
        } catch (Throwable t) {
            LOG.error("Error saving multiple organizations", t);
            throw new OrganisaatioResourceException(Response.Status.INTERNAL_SERVER_ERROR,
                    t.getMessage(), "generic.error");
        }
    }


    @Override
    public List<OrganisaatioRDTO> haeMuutetut(DateParam lastModifiedSince, boolean includeImage) {

        Preconditions.checkNotNull(lastModifiedSince);

        LOG.debug("haeMuutetut: " + lastModifiedSince.toString());
        long qstarted = System.currentTimeMillis();

        List<Organisaatio> organisaatiot = organisaatioDAO.findModifiedSince(lastModifiedSince.getValue());

        LOG.debug("Muutettujen haku {} ms", System.currentTimeMillis() - qstarted);
        long qstarted2 = System.currentTimeMillis();

        if (organisaatiot == null || organisaatiot.isEmpty()) {
            return Collections.emptyList();
        }

        List<OrganisaatioRDTO> results = new ArrayList<>();

        for (Organisaatio org : organisaatiot) {
            // Jätetään kuva pois, jos sitä ei haluta
            if (org.getMetadata() != null) {
                org.getMetadata().setIncludeImage(includeImage);
            }

            OrganisaatioRDTO result = conversionService.convert(org, OrganisaatioRDTO.class);
            results.add(result);
        }

        LOG.debug("Muutettujen convertointi {} ms --> yhteensä {} ms", System.currentTimeMillis() - qstarted2, System.currentTimeMillis() - qstarted);

        return results;
    }

    @Override
    public String haeMuutettujenOid(DateParam lastModifiedSince) {

        Preconditions.checkNotNull(lastModifiedSince);
        LOG.debug("haeMuutettujenOid: " + lastModifiedSince.toString());

        List<Organisaatio> organisaatiot = organisaatioDAO.findModifiedSince(lastModifiedSince.getValue());

        List<String> oids = new ArrayList<>();
        for (Organisaatio org : organisaatiot) {
            oids.add(org.getOid());
        }

        return "{ \"oids\": [\"" + Joiner.on("\", \"").join(oids) + "\"]}";
    }

    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public void changeOrganisationRelationship(String oid, boolean merge, DateParam dateParam, String newParentOid) {

        Preconditions.checkNotNull(oid);
        Preconditions.checkNotNull(newParentOid);
        Preconditions.checkNotNull(merge);

        Date date;
        if (dateParam != null && dateParam.getValue() != null) {
            date = dateParam.getValue();
        } else {
            date = new Date();
        }

        Organisaatio organisaatio = organisaatioDAO.findByOid(oid);
        Organisaatio newParent = organisaatioDAO.findByOid(newParentOid);

        if (organisaatio == null) {
            throw new OrganisaatioNotFoundException(oid);
        }
        if (newParent == null) {
            throw new OrganisaatioNotFoundException(newParentOid);
        }

        // Liitetäänkö organisaatio vai siirretäänkö organisaatio
        try {
            if (merge) {
                // Organisaatio yhdistyy toiseen, yhdistyvä organisaatio passivoidaan
                organisaatioBusinessService.mergeOrganisaatio(organisaatio, newParent, date);
            } else {
                // Oppilaitos siirtyy toisen organisaation alle
                organisaatioBusinessService.changeOrganisaatioParent(organisaatio, newParent, date);
            }
        } catch (SadeBusinessException sbe) {
            LOG.warn("Error saving multiple organizations", sbe);
            throw new OrganisaatioResourceException(sbe);
        }
    }

    @Override
    public OrganisaatioHistoriaRDTOV2 getOrganizationHistory(String oid) throws Exception {
        Preconditions.checkNotNull(oid);

        Organisaatio organisaatio = organisaatioDAO.findByOid(oid);

        if (organisaatio == null) {
            throw new OrganisaatioNotFoundException(oid);
        }

        OrganisaatioHistoriaRDTOV2 historia = new OrganisaatioHistoriaRDTOV2();

        // Haetaan organisaatiosuhteet
        List<OrganisaatioSuhde> childSuhteet = organisaatio.getChildSuhteet(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.HISTORIA);
        List<OrganisaatioSuhde> parentSuhteet = organisaatio.getParentSuhteet(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.HISTORIA);
        Type organisaatioSuhdeType = new TypeToken<List<OrganisaatioSuhdeDTOV2>>() {}.getType();

        historia.setChildSuhteet((List<OrganisaatioSuhdeDTOV2>) organisaatioSuhdeModelMapper.map(childSuhteet, organisaatioSuhdeType));
        historia.setParentSuhteet((List<OrganisaatioSuhdeDTOV2>) organisaatioSuhdeModelMapper.map(parentSuhteet, organisaatioSuhdeType));

        // Haetaan organisaation liitokset
        List<OrganisaatioSuhde> liitokset = organisaatio.getChildSuhteet(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.LIITOS);
        List<OrganisaatioSuhde> liittynyt = organisaatio.getParentSuhteet(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.LIITOS);

        Type organisaatioLiitosType = new TypeToken<List<OrganisaatioLiitosDTOV2>>() {}.getType();

        historia.setLiitokset((List<OrganisaatioLiitosDTOV2>) organisaatioLiitosModelMapper.map(liitokset, organisaatioLiitosType));
        historia.setLiittymiset((List<OrganisaatioLiitosDTOV2>) organisaatioLiitosModelMapper.map(liittynyt, organisaatioLiitosType));

        return historia;
    }

    @Override
    public List<OrganisaatioLiitosDTOV2> haeLiitokset(DateParam dateParam) {
        Date date = null;
        if (dateParam != null && dateParam.getValue() != null) {
            date = dateParam.getValue();
        }

        List<OrganisaatioSuhde> liitokset = organisaatioFindBusinessService.findLiitokset(date);

        Type organisaatioLiitosType = new TypeToken<List<OrganisaatioLiitosDTOV2>>() {}.getType();

        return organisaatioLiitosModelMapper.map(liitokset, organisaatioLiitosType);
    }

    @Override
    public List<OrganisaatioGroupDTOV2> groups() throws Exception {
        long qstarted = System.currentTimeMillis();

        List<Organisaatio> entitys = organisaatioFindBusinessService.findGroups();

        LOG.debug("Ryhmien haku {} ms", System.currentTimeMillis() - qstarted);
        long qstarted2 = System.currentTimeMillis();

        Type groupListType = new TypeToken<List<OrganisaatioGroupDTOV2>>() {}.getType();

        List<OrganisaatioGroupDTOV2> groupList = groupModelMapper.map(entitys, groupListType);

        LOG.debug("Ryhmien convertointi {} ms --> yhteensä {} ms", System.currentTimeMillis() - qstarted2, System.currentTimeMillis() - qstarted);

        return groupList;
    }

    @Override
    public HakutoimistoDTO hakutoimisto(String organisaatioOid) {
        Organisaatio organisaatio = organisaatioFindBusinessService.findById(organisaatioOid);
        if (organisaatio == null) {
            throw new OrganisaatioNotFoundException(organisaatioOid);
        }
        OrganisaatioMetaData metadata = organisaatio.getMetadata();
        return metadata == null ? hakutoimistoFromParent(organisaatio) : hakutoimistoFromOrganisaatio(organisaatio);
    }

    private HakutoimistoDTO hakutoimistoFromParent(Organisaatio organisaatio) {
        return organisaatio.getParent() != null ? hakutoimisto(organisaatio.getParent().getOid()) : null;
    }

    private HakutoimistoDTO hakutoimistoFromOrganisaatio(Organisaatio organisaatio) {
        if (Hakutoimisto.hasKayntiosoite(organisaatio)) {
            return new HakutoimistoDTO(Hakutoimisto.hakutoimistonNimet(organisaatio), Hakutoimisto.hakutoimistonOsoitteet(organisaatio));
        } else {
            return hakutoimistoFromParent(organisaatio);
        }
    }
}

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
import fi.vm.sade.organisaatio.api.util.OrganisaatioPerustietoUtil;
import fi.vm.sade.organisaatio.auth.OrganisaatioPermissionServiceImpl;
import fi.vm.sade.organisaatio.auth.PermissionChecker;
import fi.vm.sade.organisaatio.business.Hakutoimisto;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.business.OrganisaatioFindBusinessService;
import fi.vm.sade.organisaatio.business.exception.HakutoimistoNotFoundException;
import fi.vm.sade.organisaatio.business.exception.NotAuthorizedException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioNotFoundException;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAO;
import fi.vm.sade.organisaatio.dto.mapping.*;
import fi.vm.sade.organisaatio.dto.mapping.v2.GroupModelMapperV2;
import fi.vm.sade.organisaatio.dto.v2.*;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.resource.OrganisaatioResourceException;
import fi.vm.sade.organisaatio.resource.dto.HakutoimistoDTO;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.organisaatio.resource.dto.RyhmaCriteriaDtoV2;
import fi.vm.sade.organisaatio.resource.dto.RyhmaCriteriaDtoV3;
import fi.vm.sade.organisaatio.resource.v2.OrganisaatioResourceV2;
import fi.vm.sade.organisaatio.service.search.SearchCriteria;
import fi.vm.sade.organisaatio.service.search.SearchConfig;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
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
    private GroupModelMapperV2 groupModelMapper;

    @Autowired
    private ConversionService conversionService;

    @Autowired
    private SearchCriteriaModelMapper searchCriteriaModelMapper;

    @Autowired
    private OrganisaatioDAO organisaatioDAO;

    @Autowired
    PermissionChecker permissionChecker;

    @Autowired
    OrganisaatioPermissionServiceImpl organisaatioPermissionService;

    // POST /organisaatio/v2/yhteystiedot/hae
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

        Set<Organisaatio> organisaatiot = organisaatioFindBusinessService.findBySearchCriteria(
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

    // GET /organisaatio/v2/hello
    @Override
    public String hello() {
        return "Hello V2! " + new Date();
    }

    // GET /organisaatio/v2/hierarkia/hae
    @Override
    public OrganisaatioHakutulos searchOrganisaatioHierarkia(OrganisaatioSearchCriteriaDTOV2 hakuEhdot) {
        final OrganisaatioHakutulos tulos = new OrganisaatioHakutulos();

        // Map api search criteria to service search criteria
        SearchCriteria searchCriteria = searchCriteriaModelMapper.map(hakuEhdot, SearchCriteria.class);
        searchCriteria.setPoistettu(false);

        searchCriteria.setPiilotettu(Optional.ofNullable(hakuEhdot.getOid()).map(organisaatioPermissionService::userCanReadOrganisation).orElse(false) ? null : false);

        SearchConfig searchConfig = new SearchConfig(!hakuEhdot.getSkipParents(), true, true);

        // Hae organisaatiot
        List<OrganisaatioPerustieto> organisaatiot = organisaatioFindBusinessService.findBy(searchCriteria, searchConfig);

        // Rakenna hierarkia
        tulos.setOrganisaatiot(OrganisaatioPerustietoUtil.createHierarchy(organisaatiot));

        // Lukumäärä tuloksiin
        tulos.setNumHits(organisaatiot.size());

        return tulos;
    }

    private Collection<OrganisaatioPerustietoSuppea> convertLaajaToSuppea(Collection<OrganisaatioPerustieto> organisaatiot, boolean tyypit) {
        Set<OrganisaatioPerustietoSuppea> opts = new HashSet<>();

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

        // Map api search criteria to service search criteria
        SearchCriteria searchCriteria = searchCriteriaModelMapper.map(hakuEhdot, SearchCriteria.class);
        searchCriteria.setPoistettu(false);

        searchCriteria.setPiilotettu(Optional.ofNullable(hakuEhdot.getOid()).map(organisaatioPermissionService::userCanReadOrganisation).orElse(false) ? null : false);

        SearchConfig searchConfig = new SearchConfig(!hakuEhdot.getSkipParents(), true, false);

        // Hae organisaatiot
        List<OrganisaatioPerustieto> organisaatiot = organisaatioFindBusinessService.findBy(searchCriteria, searchConfig);

        // Rakenna hierarkia
        tulos.setOrganisaatiot(OrganisaatioPerustietoUtil.createHierarchy(organisaatiot));

        OrganisaatioHakutulosSuppeaDTOV2 ohts = new OrganisaatioHakutulosSuppeaDTOV2();

        ohts.setNumHits(tulos.getNumHits());
        ohts.setOrganisaatiot(convertLaajaToSuppea(tulos.getOrganisaatiot(), tyypit));

        return ohts;
    }

    // GET /organisaatio/v2/hierarkia/hae/nimi
    @Override
    public OrganisaatioHakutulosSuppeaDTOV2 searchOrganisaatioHierarkiaNimet(OrganisaatioSearchCriteriaDTOV2 hakuEhdot) {
        return searchOrganisaatioHierarkiaSuppea(hakuEhdot, false);
    }

    // GET /organisaatio/v2/hierarkia/hae/tyyppi
    @Override
    public OrganisaatioHakutulosSuppeaDTOV2 searchOrganisaatioHierarkiaTyypit(OrganisaatioSearchCriteriaDTOV2 hakuEhdot) {
        return searchOrganisaatioHierarkiaSuppea(hakuEhdot, true);
    }

    // GET /organisaatio/v2/hae
    @Override
    public OrganisaatioHakutulos searchOrganisaatiot(OrganisaatioSearchCriteriaDTOV2 hakuEhdot) {
        final OrganisaatioHakutulos tulos = new OrganisaatioHakutulos();

        // Map api search criteria to service search criteria
        SearchCriteria searchCriteria = searchCriteriaModelMapper.map(hakuEhdot, SearchCriteria.class);
        searchCriteria.setPoistettu(false);

        searchCriteria.setPiilotettu(Optional.ofNullable(hakuEhdot.getOid()).map(organisaatioPermissionService::userCanReadOrganisation).orElse(false) ? null : false);

        SearchConfig searchConfig = new SearchConfig(false, false, true);

        // Hae organisaatiot
        List<OrganisaatioPerustieto> organisaatiot = organisaatioFindBusinessService.findBy(searchCriteria, searchConfig);

        // Organisaatiot tuloksiin
        tulos.setOrganisaatiot(organisaatiot);

        // Lukumäärä tuloksiin
        tulos.setNumHits(organisaatiot.size());

        return tulos;
    }

    private OrganisaatioHakutulosSuppeaDTOV2 searchOrganisaatiotSuppea(OrganisaatioSearchCriteriaDTOV2 hakuEhdot, boolean tyypit) {
        final OrganisaatioHakutulos tulos = new OrganisaatioHakutulos();

        // Map api search criteria to service search criteria
        SearchCriteria searchCriteria = searchCriteriaModelMapper.map(hakuEhdot, SearchCriteria.class);
        searchCriteria.setPoistettu(false);

        searchCriteria.setPiilotettu(Optional.ofNullable(hakuEhdot.getOid()).map(organisaatioPermissionService::userCanReadOrganisation).orElse(false) ? null : false);

        SearchConfig searchConfig = new SearchConfig(false, false, false);

        // Hae organisaatiot
        List<OrganisaatioPerustieto> organisaatiot = organisaatioFindBusinessService.findBy(searchCriteria, searchConfig);

        // Organisaatiot tuloksiin
        tulos.setOrganisaatiot(organisaatiot);

        // Lukumäärä tuloksiin
        tulos.setNumHits(organisaatiot.size());

        OrganisaatioHakutulosSuppeaDTOV2 ohts = new OrganisaatioHakutulosSuppeaDTOV2();

        ohts.setNumHits(tulos.getNumHits());
        ohts.setOrganisaatiot(convertLaajaToSuppea(tulos.getOrganisaatiot(), tyypit));

        return ohts;
    }

    // GET /organisaatio/v2/hae/nimi
    @Override
    public OrganisaatioHakutulosSuppeaDTOV2 searchOrganisaatiotNimet(OrganisaatioSearchCriteriaDTOV2 hakuEhdot) {
        return searchOrganisaatiotSuppea(hakuEhdot, false);
    }

    // GET /organisaatio/v2/hae/tyyppi
    @Override
    public OrganisaatioHakutulosSuppeaDTOV2 searchOrganisaatiotTyypit(OrganisaatioSearchCriteriaDTOV2 hakuEhdot) {
        return searchOrganisaatiotSuppea(hakuEhdot, true);
    }

    // GET /organisaatio/v2/{oid}/paivittaja
    @Override
    public OrganisaatioPaivittajaDTOV2 getOrganisaatioPaivittaja(String oid) throws Exception {
        Preconditions.checkNotNull(oid);

        try {
            permissionChecker.checkReadOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            LOG.warn("Not authorized to read organisation: " + oid);
            throw new OrganisaatioResourceException(nae);
        }

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

    // GET /organisaatio/v2/{oid}/nimet
    @Override
    public List<OrganisaatioNimiDTOV2> getOrganisaatioNimet(String oid) throws Exception {
        Preconditions.checkNotNull(oid);

        try {
            permissionChecker.checkReadOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            LOG.warn("Not authorized to read organisation: " + oid);
            throw new OrganisaatioResourceException(nae);
        }

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

    // GET /organisaatio/v2/{id}/LOP
    @Override
    public OrganisaatioLOPTietoDTOV2 getOrganisaationLOPTiedotByOID(String oid) {
        Preconditions.checkNotNull(oid);

        try {
            permissionChecker.checkReadOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            LOG.warn("Not authorized to read organisation: " + oid);
            throw new OrganisaatioResourceException(nae);
        }

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

    // PUT /organisaatio/v2/{oid}/nimet
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

    // POST /organisaatio/v2/{oid}/nimet/{date: [0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]}
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

    // DELETE /organisaatio/v2/{oid}/nimet/{date: [0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]}
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

    // PUT /organisaatio/v2/muokkaamonta
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

    // GET /organisaatio/v2/muutetut
    @Override
    public List<OrganisaatioRDTO> haeMuutetut(DateParam lastModifiedSince, boolean includeImage) {

        Preconditions.checkNotNull(lastModifiedSince);

        LOG.debug("haeMuutetut: " + lastModifiedSince.toString());
        long qstarted = System.currentTimeMillis();

        List<Organisaatio> organisaatiot = organisaatioDAO.findModifiedSince(permissionChecker.isReadAccessToAll() ? null : false, lastModifiedSince.getValue());

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

    // GET /organisaatio/v2/muutetut/oid
    @Override
    public String haeMuutettujenOid(DateParam lastModifiedSince) {

        Preconditions.checkNotNull(lastModifiedSince);
        LOG.debug("haeMuutettujenOid: " + lastModifiedSince.toString());

        List<Organisaatio> organisaatiot = organisaatioDAO.findModifiedSince(permissionChecker.isReadAccessToAll() ? null : false, lastModifiedSince.getValue());

        List<String> oids = new ArrayList<>();
        for (Organisaatio org : organisaatiot) {
            oids.add(org.getOid());
        }

        return "{ \"oids\": [\"" + Joiner.on("\", \"").join(oids) + "\"]}";
    }

    // POST /organisaatio/v2/{oid}/organisaatiosuhde
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

    // GET /organisaatio/v2/{oid}/historia
    @Override
    public OrganisaatioHistoriaRDTOV2 getOrganizationHistory(String oid) throws Exception {
        Preconditions.checkNotNull(oid);

        try {
            permissionChecker.checkReadOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            LOG.warn("Not authorized to read organisation: " + oid);
            throw new OrganisaatioResourceException(nae);
        }

        Organisaatio organisaatio = organisaatioDAO.findByOid(oid);

        if (organisaatio == null) {
            throw new OrganisaatioNotFoundException(oid);
        }

        OrganisaatioHistoriaRDTOV2 historia = new OrganisaatioHistoriaRDTOV2();

        // Haetaan organisaatiosuhteet
        Set<OrganisaatioSuhde> childSuhteet = organisaatio.getChildSuhteet(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.HISTORIA);
        Set<OrganisaatioSuhde> parentSuhteet = new HashSet<>(organisaatio.getParentSuhteet(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.HISTORIA));
        Type organisaatioSuhdeSetType = new TypeToken<Set<OrganisaatioSuhdeDTOV2>>() {}.getType();

        historia.setChildSuhteet(organisaatioSuhdeModelMapper.map(childSuhteet, organisaatioSuhdeSetType));
        historia.setParentSuhteet(organisaatioSuhdeModelMapper.map(parentSuhteet, organisaatioSuhdeSetType));

        // Haetaan organisaation liitokset
        Set<OrganisaatioSuhde> liitokset = organisaatio.getChildSuhteet(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.LIITOS);
        Set<OrganisaatioSuhde> liittynyt = new HashSet<>(organisaatio.getParentSuhteet(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.LIITOS));

        Type organisaatioLiitosSetType = new TypeToken<Set<OrganisaatioLiitosDTOV2>>() {}.getType();

        historia.setLiitokset(organisaatioLiitosModelMapper.map(liitokset, organisaatioLiitosSetType));
        historia.setLiittymiset(organisaatioLiitosModelMapper.map(liittynyt, organisaatioLiitosSetType));

        return historia;
    }

    // GET /organisaatio/v2/liitokset
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

    // GET /organisaatio/v2/ryhmat
    @Override
    public List<OrganisaatioGroupDTOV2> groups(RyhmaCriteriaDtoV2 criteria) throws Exception {
        long qstarted = System.currentTimeMillis();

        List<Organisaatio> entitys = organisaatioFindBusinessService.findGroups(conversionService.convert(criteria, RyhmaCriteriaDtoV3.class));

        LOG.debug("Ryhmien haku {} ms", System.currentTimeMillis() - qstarted);
        long qstarted2 = System.currentTimeMillis();

        Type groupListType = new TypeToken<List<OrganisaatioGroupDTOV2>>() {}.getType();

        List<OrganisaatioGroupDTOV2> groupList = groupModelMapper.map(entitys, groupListType);

        LOG.debug("Ryhmien convertointi {} ms --> yhteensä {} ms", System.currentTimeMillis() - qstarted2, System.currentTimeMillis() - qstarted);

        return groupList;
    }

    // GET /organisaatio/v2/{oid}/hakutoimisto
    @Override
    public Response hakutoimisto(String organisaatioOid) {

        try {
            permissionChecker.checkReadOrganisation(organisaatioOid);
        } catch (NotAuthorizedException nae) {
            LOG.warn("Not authorized to read organisation: " + organisaatioOid);
            throw new OrganisaatioResourceException(nae);
        }

        try {
            HakutoimistoDTO hakutoimistoDTO = hakutoimistoRec(organisaatioOid);
            return Response.ok(hakutoimistoDTO).build();
        } catch (OrganisaatioNotFoundException | HakutoimistoNotFoundException e) {
            LOG.warn("Hakutoimiston haku organisaatiolle " + organisaatioOid + " epäonnistui.", e);
            return Response.status(404).build();
        }
    }

    private HakutoimistoDTO hakutoimistoRec(String organisaatioOId) {

        Organisaatio organisaatio = organisaatioFindBusinessService.findById(organisaatioOId);
        if (organisaatio == null) {
            throw new OrganisaatioNotFoundException("Organisaatiota ei löydy: " + organisaatioOId);
        }
        OrganisaatioMetaData metadata = organisaatio.getMetadata();
        return metadata == null ? hakutoimistoFromParent(organisaatio) : hakutoimistoFromOrganisaatio(organisaatio);
    }

    private HakutoimistoDTO hakutoimistoFromParent(Organisaatio organisaatio) {
        if (organisaatio.getParent() != null) {
            return hakutoimistoRec(organisaatio.getParent().getOid());
        }
        throw new HakutoimistoNotFoundException("Hakutoimistoa ei löydy, ylin organisaatio " + organisaatio.getOid());
    }

    private HakutoimistoDTO hakutoimistoFromOrganisaatio(Organisaatio organisaatio) {
        if (Hakutoimisto.hasOsoite(organisaatio)) {
            return new HakutoimistoDTO(Hakutoimisto.hakutoimistonNimet(organisaatio), Hakutoimisto.hakutoimistonOsoitteet(organisaatio));
        } else {
            return hakutoimistoFromParent(organisaatio);
        }
    }
}

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
import fi.vm.sade.organisaatio.api.search.OrganisaatioHakutulos;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.api.util.OrganisaatioPerustietoUtil;
import fi.vm.sade.organisaatio.auth.PermissionChecker;
import fi.vm.sade.organisaatio.business.HakutoimistoService;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.business.OrganisaatioFindBusinessService;
import fi.vm.sade.organisaatio.business.OrganisaatioNimiService;
import fi.vm.sade.organisaatio.business.exception.HakutoimistoNotFoundException;
import fi.vm.sade.organisaatio.business.exception.NotAuthorizedException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioNotFoundException;
import fi.vm.sade.organisaatio.dto.OrganisaatioNimiDTO;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioLiitosModelMapper;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioModelMapper;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioSuhdeModelMapper;
import fi.vm.sade.organisaatio.dto.mapping.v2.GroupModelMapperV2;
import fi.vm.sade.organisaatio.dto.v2.*;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import fi.vm.sade.organisaatio.model.NamedMonikielinenTeksti;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import fi.vm.sade.organisaatio.resource.OrganisaatioResourceException;
import fi.vm.sade.organisaatio.resource.dto.HakutoimistoDTO;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.organisaatio.resource.dto.RyhmaCriteriaDtoV2;
import fi.vm.sade.organisaatio.resource.dto.RyhmaCriteriaDtoV3;
import fi.vm.sade.organisaatio.resource.v2.OrganisaatioResourceV2;
import fi.vm.sade.organisaatio.service.search.SearchConfig;
import fi.vm.sade.organisaatio.service.search.SearchCriteria;
import fi.vm.sade.organisaatio.service.search.SearchCriteriaService;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.*;

/**
 * @author simok
 */
@RestController
@Transactional(readOnly = true)
@RequestMapping("${server.rest.context-path}/organisaatio/v2")
public class OrganisaatioResourceImplV2 implements OrganisaatioResourceV2 {

    private static final Logger logger = LoggerFactory.getLogger(OrganisaatioResourceImplV2.class);
    private static final String NOT_AUTHORIZED_TO_READ_ORGANISATION = "Not authorized to read organisation: {}";

    @Autowired
    private OrganisaatioBusinessService organisaatioBusinessService;

    @Autowired
    private OrganisaatioFindBusinessService organisaatioFindBusinessService;

    @Autowired
    private OrganisaatioNimiService organisaatioNimiService;
    @Autowired
    private OrganisaatioModelMapper organisaatioModelMapper;

    @Autowired
    private OrganisaatioSuhdeModelMapper organisaatioSuhdeModelMapper;

    @Autowired
    private OrganisaatioLiitosModelMapper organisaatioLiitosModelMapper;

    @Autowired
    private GroupModelMapperV2 groupModelMapper;

    @Autowired
    private ConversionService conversionService;

    @Autowired
    private OrganisaatioRepository organisaatioRepository;

    @Autowired
    PermissionChecker permissionChecker;

    @Autowired
    private SearchCriteriaService searchCriteriaService;

    @Autowired
    private HakutoimistoService hakutoimistoService;
    // POST /organisaatio/v2/yhteystiedot/hae
    @Override
    @Transactional(readOnly = true)
    public List<OrganisaatioYhteystiedotDTOV2> searchOrganisaatioYhteystiedot(YhteystiedotSearchCriteriaDTOV2 hakuEhdot) {
        Set<Organisaatio> organisaatiot = organisaatioFindBusinessService.findBySearchCriteria(
                hakuEhdot.getKieliList(),
                hakuEhdot.getKuntaList(),
                hakuEhdot.getOppilaitostyyppiList(),
                hakuEhdot.getVuosiluokkaList(),
                hakuEhdot.getYtunnusList(),
                hakuEhdot.getOidList(),
                hakuEhdot.getLimit());

        // Define the target list type for mapping
        Type organisaatioYhteystiedotDTOV2ListType = new TypeToken<List<OrganisaatioYhteystiedotDTOV2>>() {
        }.getType();

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
        SearchCriteria searchCriteria = searchCriteriaService.getServiceSearchCriteria(hakuEhdot);
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
        SearchCriteria searchCriteria = searchCriteriaService.getServiceSearchCriteria(hakuEhdot);
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
        SearchCriteria searchCriteria = searchCriteriaService.getServiceSearchCriteria(hakuEhdot);
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
        SearchCriteria searchCriteria = searchCriteriaService.getServiceSearchCriteria(hakuEhdot);
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
    public OrganisaatioPaivittajaDTOV2 getOrganisaatioPaivittaja(String oid) {
        Preconditions.checkNotNull(oid);

        try {
            permissionChecker.checkReadOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            logger.warn(NOT_AUTHORIZED_TO_READ_ORGANISATION, oid);
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, nae);
        }

        Organisaatio org = organisaatioRepository.customFindByOid(oid);

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
    public List<OrganisaatioNimiDTO> getOrganisaatioNimet(String oid) {
        Preconditions.checkNotNull(oid);

        try {
            permissionChecker.checkReadOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            logger.warn(NOT_AUTHORIZED_TO_READ_ORGANISATION, oid);
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, nae);
        }
        return organisaatioNimiService.getNimet(oid);
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
            logger.warn(NOT_AUTHORIZED_TO_READ_ORGANISATION, oid);
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, nae);
        }

        logger.debug("getOrganisaationLOPTiedotByOID: {}", oid);

        // Search order
        // 1. OID
        // 2. Y-TUNNUS
        // 3. VIRASTOTUNNUS
        // 4. OPPILAITOSKOODI
        // 5. TOIMIPISTEKOODI
        Organisaatio o = organisaatioRepository.customFindByOid(oid);
        if (o == null) {
            o = organisaatioRepository.findByYTunnus(oid);
        }
        if (o == null) {
            o = organisaatioRepository.findByVirastoTunnus(oid);
        }
        if (o == null) {
            o = organisaatioRepository.findByOppilaitoskoodi(oid);
        }
        if (o == null) {
            o = organisaatioRepository.findByToimipistekoodi(oid);
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


    // GET /organisaatio/v2/muutetut
    @Override
    public List<OrganisaatioRDTO> haeMuutetut(LocalDate lastModifiedSince, boolean includeImage) {

        Preconditions.checkNotNull(lastModifiedSince);

        logger.debug("haeMuutetut: {}", lastModifiedSince);
        long qstarted = System.currentTimeMillis();

        List<Organisaatio> organisaatiot = organisaatioRepository.findModifiedSince(
                !permissionChecker.isReadAccessToAll(), java.sql.Date.valueOf(lastModifiedSince));

        logger.debug("Muutettujen haku {} ms", System.currentTimeMillis() - qstarted);
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

        logger.debug("Muutettujen convertointi {} ms --> yhteensä {} ms", System.currentTimeMillis() - qstarted2, System.currentTimeMillis() - qstarted);

        return results;
    }

    // GET /organisaatio/v2/muutetut/oid
    @Override
    public String haeMuutettujenOid(LocalDate lastModifiedSince) {

        Preconditions.checkNotNull(lastModifiedSince);
        logger.debug("haeMuutettujenOid: {}", lastModifiedSince);

        List<Organisaatio> organisaatiot = organisaatioRepository.findModifiedSince(
                !permissionChecker.isReadAccessToAll(),
                java.sql.Date.valueOf(lastModifiedSince));

        List<String> oids = new ArrayList<>();
        for (Organisaatio org : organisaatiot) {
            oids.add(org.getOid());
        }

        return "{ \"oids\": [\"" + Joiner.on("\", \"").join(oids) + "\"]}";
    }


    // GET /organisaatio/v2/{oid}/historia
    @Override
    public OrganisaatioHistoriaRDTOV2 getOrganizationHistory(String oid) {
        Preconditions.checkNotNull(oid);

        try {
            permissionChecker.checkReadOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            logger.warn(NOT_AUTHORIZED_TO_READ_ORGANISATION, oid);
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, nae);
        }

        Organisaatio organisaatio = organisaatioRepository.customFindByOid(oid);

        if (organisaatio == null) {
            throw new OrganisaatioNotFoundException(oid);
        }

        OrganisaatioHistoriaRDTOV2 historia = new OrganisaatioHistoriaRDTOV2();

        // Haetaan organisaatiosuhteet
        Set<OrganisaatioSuhde> childSuhteet = organisaatio.getChildSuhteet(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.HISTORIA);
        Set<OrganisaatioSuhde> parentSuhteet = new HashSet<>(organisaatio.getParentSuhteet(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.HISTORIA));
        Type organisaatioSuhdeSetType = new TypeToken<Set<OrganisaatioSuhdeDTOV2>>() {
        }.getType();

        historia.setChildSuhteet(organisaatioSuhdeModelMapper.map(childSuhteet, organisaatioSuhdeSetType));
        historia.setParentSuhteet(organisaatioSuhdeModelMapper.map(parentSuhteet, organisaatioSuhdeSetType));

        // Haetaan organisaation liitokset
        Set<OrganisaatioSuhde> liitokset = organisaatio.getChildSuhteet(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.LIITOS);
        Set<OrganisaatioSuhde> liittynyt = new HashSet<>(organisaatio.getParentSuhteet(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.LIITOS));

        Type organisaatioLiitosSetType = new TypeToken<Set<OrganisaatioLiitosDTOV2>>() {
        }.getType();

        historia.setLiitokset(organisaatioLiitosModelMapper.map(liitokset, organisaatioLiitosSetType));
        historia.setLiittymiset(organisaatioLiitosModelMapper.map(liittynyt, organisaatioLiitosSetType));

        return historia;
    }

    // GET /organisaatio/v2/liitokset
    @Override
    public List<OrganisaatioLiitosDTOV2> haeLiitokset(LocalDate liitoksetAlkaen) {
        Date date = null;
        if (liitoksetAlkaen != null && java.sql.Date.valueOf(liitoksetAlkaen) != null) {
            date = java.sql.Date.valueOf(liitoksetAlkaen);
        }

        List<OrganisaatioSuhde> liitokset = organisaatioFindBusinessService.findLiitokset(date);

        Type organisaatioLiitosType = new TypeToken<List<OrganisaatioLiitosDTOV2>>() {
        }.getType();

        return organisaatioLiitosModelMapper.map(liitokset, organisaatioLiitosType);
    }

    // GET /organisaatio/v2/ryhmat
    @Override
    public List<OrganisaatioGroupDTOV2> groups(RyhmaCriteriaDtoV2 criteria) {
        long qstarted = System.currentTimeMillis();

        List<Organisaatio> entitys = organisaatioFindBusinessService.findGroups(conversionService.convert(criteria, RyhmaCriteriaDtoV3.class));

        logger.debug("Ryhmien haku {} ms", System.currentTimeMillis() - qstarted);
        long qstarted2 = System.currentTimeMillis();

        Type groupListType = new TypeToken<List<OrganisaatioGroupDTOV2>>() {
        }.getType();

        List<OrganisaatioGroupDTOV2> groupList = groupModelMapper.map(entitys, groupListType);

        logger.debug("Ryhmien convertointi {} ms --> yhteensä {} ms", System.currentTimeMillis() - qstarted2, System.currentTimeMillis() - qstarted);

        return groupList;
    }

    // GET /organisaatio/v2/{oid}/hakutoimisto
    @Override
    public HakutoimistoDTO hakutoimisto(String organisaatioOid) {

        try {
            permissionChecker.checkReadOrganisation(organisaatioOid);
        } catch (NotAuthorizedException nae) {
            logger.warn(NOT_AUTHORIZED_TO_READ_ORGANISATION, organisaatioOid);
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, nae);
        }

        try {
            return hakutoimistoService.hakutoimisto(organisaatioOid);
        } catch (OrganisaatioNotFoundException | HakutoimistoNotFoundException e) {
            logger.warn("Hakutoimiston haku organisaatiolle {} epäonnistui.", organisaatioOid);
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, e.getMessage()
            );
        }
    }

}

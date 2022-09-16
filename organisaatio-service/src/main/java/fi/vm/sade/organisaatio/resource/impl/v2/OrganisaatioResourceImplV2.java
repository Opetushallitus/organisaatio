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
import fi.vm.sade.organisaatio.api.DateParam;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioHakutulos;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.api.util.OrganisaatioPerustietoUtil;
import fi.vm.sade.organisaatio.auth.PermissionChecker;
import fi.vm.sade.organisaatio.business.Hakutoimisto;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.business.OrganisaatioFindBusinessService;
import fi.vm.sade.organisaatio.business.exception.HakutoimistoNotFoundException;
import fi.vm.sade.organisaatio.business.exception.NotAuthorizedException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioNotFoundException;
import fi.vm.sade.organisaatio.dto.OrganisaatioNimiDTO;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioLiitosModelMapper;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioModelMapper;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioNimiModelMapper;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioSuhdeModelMapper;
import fi.vm.sade.organisaatio.dto.mapping.v2.GroupModelMapperV2;
import fi.vm.sade.organisaatio.dto.v2.*;
import fi.vm.sade.organisaatio.model.*;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    private OrganisaatioRepository organisaatioRepository;

    @Autowired
    PermissionChecker permissionChecker;

    @Autowired
    private SearchCriteriaService searchCriteriaService;

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
        List<OrganisaatioNimiDTO> orgNimet = organisaatioNimiModelMapper.map(organisaatioBusinessService.getOrganisaatioNimet(oid), new TypeToken<List<OrganisaatioNimiDTO>>() {
        }.getType());
        Organisaatio org = organisaatioRepository.customFindByOid(oid);
        return getOrganisaatioNimiDTOS(orgNimet, org).stream()
                .sorted(Comparator.comparing(OrganisaatioNimiDTO::getAlkuPvm)).collect(Collectors.toList());
    }

    private List<OrganisaatioNimiDTO> getOrganisaatioNimiDTOS(List<OrganisaatioNimiDTO> orgNimet, Organisaatio org) {
        return org.getTyypit().contains(OrganisaatioTyyppi.TOIMIPISTE.koodiValue()) ?
                decoreateToimipisteNimet(orgNimet, getOppilaitosNameIntervals(org)) :
                orgNimet;

    }

    List<Map.Entry<Map.Entry<Date, Optional<Date>>, List<OrganisaatioNimiDTO>>> getOppilaitosNameIntervals(Organisaatio org) {
        return sanitizeParentSuhteet(org.getParentSuhteet(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.HISTORIA), org).stream()
                .map(parentSuhde -> {
                    List<OrganisaatioNimiDTO> parentNimet = organisaatioNimiModelMapper.map(organisaatioBusinessService.getOrganisaatioNimet(parentSuhde.getParent().getOid()), new TypeToken<List<OrganisaatioNimiDTO>>() {
                    }.getType());
                    List<OrganisaatioNimiDTO> relevantParentNimet = getRelevantParentNimet(org, parentNimet);
                    return Map.<Map.Entry<Date, Optional<Date>>, List<OrganisaatioNimiDTO>>entry(Map.entry(parentSuhde.getAlkuPvm(), Optional.ofNullable(parentSuhde.getLoppuPvm())), relevantParentNimet);
                })
                .collect(Collectors.toList());
    }

    private List<OrganisaatioSuhde> sanitizeParentSuhteet(List<OrganisaatioSuhde> parentSuhteet, Organisaatio org) {
        List<OrganisaatioSuhde> sanitizedParentSuhteet = parentSuhteet.stream()
                .sorted(Comparator.comparing(OrganisaatioSuhde::getAlkuPvm))
                .collect(Collectors.toList());
        sanitizedParentSuhteet.get(0).setAlkuPvm(org.getAlkuPvm());
        return sanitizedParentSuhteet;
    }

    private static List<OrganisaatioNimiDTO> getRelevantParentNimet(Organisaatio org, List<OrganisaatioNimiDTO> parentNimet) {
        List<OrganisaatioNimiDTO> relevantParentNimet = parentNimet.stream().filter(parentNimi -> !parentNimi.getAlkuPvm().before(org.getAlkuPvm())).collect(Collectors.toList());
        return relevantParentNimet.isEmpty() ? List.of(parentNimet.get(parentNimet.size() - 1)) : relevantParentNimet;
    }

    List<OrganisaatioNimiDTO> decoreateToimipisteNimet(List<OrganisaatioNimiDTO> toimipisteNimet, List<Map.Entry<Map.Entry<Date, Optional<Date>>, List<OrganisaatioNimiDTO>>> oppilaitosHistoryNimet) {
        List<OrganisaatioNimiDTO> result = new ArrayList<>();
        List<OrganisaatioNimiDTO> oppilaitosNimet = evaluateParentNameHistory(oppilaitosHistoryNimet);
        IntStream.range(0, toimipisteNimet.size()).forEach(toimipisteIndex -> {
            OrganisaatioNimiDTO toimipiste1 = toimipisteNimet.get(toimipisteIndex);
            Optional<OrganisaatioNimiDTO> toimipiste2 = toimipisteIndex + 1 < toimipisteNimet.size() ? Optional.of(toimipisteNimet.get(toimipisteIndex + 1)) : Optional.empty();
            IntStream.range(0, oppilaitosNimet.size()).forEach(oppilaitosIndex -> {
                        OrganisaatioNimiDTO oppilaitos1 = oppilaitosNimet.get(oppilaitosIndex);
                        Optional<OrganisaatioNimiDTO> oppilaitos2 = oppilaitosIndex + 1 < oppilaitosNimet.size() ? Optional.of(oppilaitosNimet.get(oppilaitosIndex + 1)) : Optional.empty();
                        boolean firstToimipiste = toimipisteIndex == 0;
                        boolean lastToimipiste = toimipiste2.isEmpty();
                        boolean firtsOppilaitos = oppilaitosIndex == 0;
                        boolean lastOppilaitos = oppilaitos2.isEmpty();
                        boolean toimipisteInOppilaitosRange = (firtsOppilaitos || toimipiste1.getAlkuPvm().compareTo(oppilaitos1.getAlkuPvm()) >= 0) && (lastOppilaitos || toimipiste1.getAlkuPvm().compareTo(oppilaitos2.get().getAlkuPvm()) < 0);
                        boolean oppilaitosWithinToimipisteet = (firstToimipiste || oppilaitos1.getAlkuPvm().compareTo(toimipiste1.getAlkuPvm()) >= 0) && (lastToimipiste || oppilaitos1.getAlkuPvm().compareTo(toimipiste2.get().getAlkuPvm()) < 0);
                        if (toimipisteInOppilaitosRange) {
                            result.add(oppilaitosToimipisteNimi(toimipiste1, oppilaitos1, toimipiste1.getAlkuPvm()));
                        } else if (oppilaitosWithinToimipisteet)
                            result.add(oppilaitosToimipisteNimi(toimipiste1, oppilaitos1, oppilaitos1.getAlkuPvm()));
                    }
            );
        });
        result.sort(Comparator.comparing(OrganisaatioNimiDTO::getAlkuPvm));
        return result;
    }

    List<OrganisaatioNimiDTO> evaluateParentNameHistory(List<Map.Entry<Map.Entry<Date, Optional<Date>>, List<OrganisaatioNimiDTO>>> oppilaitosHistoryNimet) {
        List<OrganisaatioNimiDTO> result = new ArrayList<>();
        IntStream.range(0, oppilaitosHistoryNimet.size()).forEach(index -> {
            Map.Entry<Map.Entry<Date, Optional<Date>>, List<OrganisaatioNimiDTO>> currentOppilaitosNimet = oppilaitosHistoryNimet.get(index);
            Date startOfParentRange = currentOppilaitosNimet.getKey().getKey();
            Optional<Date> endOfParentRange = currentOppilaitosNimet.getKey().getValue();
            List<OrganisaatioNimiDTO> currentNames = new ArrayList<>();
            IntStream.range(0, currentOppilaitosNimet.getValue().size()).forEach(index2 -> {
                OrganisaatioNimiDTO currentOppilaitosNimi = currentOppilaitosNimet.getValue().get(index2);
                boolean lastOppilaitosNimi = (index == oppilaitosHistoryNimet.size() - 1 && index2 == currentOppilaitosNimet.getValue().size() - 1);
                boolean nimiInRange = (index == 0 || lastOppilaitosNimi || currentOppilaitosNimi.getAlkuPvm().compareTo(startOfParentRange) >= 0) && (endOfParentRange.isEmpty() || currentOppilaitosNimi.getAlkuPvm().compareTo(endOfParentRange.get()) < 0);
                if (nimiInRange) {
                    currentNames.addAll(getNames(index, index2, currentOppilaitosNimet, startOfParentRange, currentOppilaitosNimi, currentNames.isEmpty()));
                }
            });
            result.addAll(currentNames);
        });
        result.sort(Comparator.comparing(OrganisaatioNimiDTO::getAlkuPvm));
        return result;
    }

    private List<OrganisaatioNimiDTO> getNames(int index, int index2, Map.Entry<Map.Entry<Date, Optional<Date>>, List<OrganisaatioNimiDTO>> currentOppilaitosNimet, Date startOfParentRange, OrganisaatioNimiDTO currentOppilaitosNimi, boolean evaluatingFirstRange) {
        List<OrganisaatioNimiDTO> currentNames = new ArrayList<>();
        if (index > 0 && index2 > 0 && evaluatingFirstRange && currentOppilaitosNimi.getAlkuPvm().compareTo(startOfParentRange) > 0) {
            //add previous name with start from start of range
            currentNames.add(copyNimi(currentOppilaitosNimet.getValue().get(index2 - 1), startOfParentRange));
        }
        if (currentOppilaitosNimi.getAlkuPvm().compareTo(startOfParentRange) < 0) {
            // if oppilaitos is from before the start of the range, the startdate should still be according to the range
            currentNames.add(copyNimi(currentOppilaitosNimi, startOfParentRange));
        } else {
            currentNames.add(currentOppilaitosNimi);
        }
        return currentNames;
    }

    OrganisaatioNimiDTO oppilaitosToimipisteNimi(OrganisaatioNimiDTO toimipiste, OrganisaatioNimiDTO oppilaitosNimi, Date alkuPvm) {
        OrganisaatioNimiDTO toimipisteNimi = copyNimi(toimipiste, alkuPvm);
        Map<String, String> nimi = toimipisteNimi.getNimi();
        nimi.keySet().forEach(kieli -> nimi.put(kieli,
                generateToimipisteNimi(oppilaitosNimi, nimi, kieli)));
        return toimipisteNimi;
    }

    private OrganisaatioNimiDTO copyNimi(OrganisaatioNimiDTO toimipiste, Date alkuPvm) {
        OrganisaatioNimiDTO toimipisteNimi = new OrganisaatioNimiDTO();
        Map<String, String> thisNimi = toimipiste.getNimi().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        toimipisteNimi.setVersion(toimipiste.getVersion());
        toimipisteNimi.setAlkuPvm(alkuPvm);
        toimipisteNimi.setNimi(thisNimi);
        return toimipisteNimi;
    }

    private String generateToimipisteNimi(OrganisaatioNimiDTO oppilaitosNimi, Map<String, String> thisNimi, String kieli) {
        String nimiString = thisNimi.get(kieli);
        String oppilaitosNimiString = oppilaitosNimi.getNimi().get(kieli) != null ? oppilaitosNimi.getNimi().get(kieli) : oppilaitosNimi.getNimi().get("fi");
        if (nimiString.equals(oppilaitosNimiString))
            return nimiString;
        else
            return String.format("%s, %s", oppilaitosNimiString, nimiString);
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
    public List<OrganisaatioRDTO> haeMuutetut(DateParam lastModifiedSince, boolean includeImage) {

        Preconditions.checkNotNull(lastModifiedSince);

        logger.debug("haeMuutetut: {}", lastModifiedSince);
        long qstarted = System.currentTimeMillis();

        List<Organisaatio> organisaatiot = organisaatioRepository.findModifiedSince(
                !permissionChecker.isReadAccessToAll(), lastModifiedSince.getValue());

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
    public String haeMuutettujenOid(DateParam lastModifiedSince) {

        Preconditions.checkNotNull(lastModifiedSince);
        logger.debug("haeMuutettujenOid: {}", lastModifiedSince);

        List<Organisaatio> organisaatiot = organisaatioRepository.findModifiedSince(
                !permissionChecker.isReadAccessToAll(),
                lastModifiedSince.getValue());

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
    public List<OrganisaatioLiitosDTOV2> haeLiitokset(DateParam liitoksetAlkaen) {
        Date date = null;
        if (liitoksetAlkaen != null && liitoksetAlkaen.getValue() != null) {
            date = liitoksetAlkaen.getValue();
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
            return hakutoimistoRec(organisaatioOid);
        } catch (OrganisaatioNotFoundException | HakutoimistoNotFoundException e) {
            logger.warn("Hakutoimiston haku organisaatiolle {} epäonnistui.", organisaatioOid);
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, e.getMessage()
            );
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

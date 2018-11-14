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
package fi.vm.sade.organisaatio.business.impl;

import com.google.common.base.Preconditions;
import fi.vm.sade.organisaatio.api.DateParam;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.business.OrganisaatioFindBusinessService;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAO;
import fi.vm.sade.organisaatio.dao.OrganisaatioSuhdeDAO;
import fi.vm.sade.organisaatio.dto.ChildOidsCriteria;
import fi.vm.sade.organisaatio.dto.mapping.RyhmaCriteriaDto;
import fi.vm.sade.organisaatio.dto.v3.OrganisaatioRDTOV3;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;
import fi.vm.sade.organisaatio.resource.OrganisaatioResourceException;
import fi.vm.sade.organisaatio.resource.dto.RyhmaCriteriaDtoV3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author simok
 */
@Transactional
@Service("organisaatioFindBusinessService")
public class OrganisaatioFindBusinessServiceImpl implements OrganisaatioFindBusinessService {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrganisaatioDAO organisaatioDAO;

    @Autowired
    private OrganisaatioSuhdeDAO organisaatioSuhdeDAO;

    @Autowired
    private ConversionService conversionService;

    @Value("${root.organisaatio.oid}")
    private String rootOid;

    @Override
    @Transactional(readOnly = true)
    public List<Organisaatio> findBySearchCriteria(
            List<String> kieliList,
            List<String> kuntaList,
            List<String> oppilaitostyyppiList,
            List<String> vuosiluokkaList,
            List<String> ytunnusList,
            List<String> oidList,
            int limit) {

        return organisaatioDAO.findBySearchCriteria(kieliList, kuntaList, oppilaitostyyppiList, vuosiluokkaList, ytunnusList, oidList, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Organisaatio> findGroups(RyhmaCriteriaDtoV3 criteria) {
        return findGroups(conversionService.convert(criteria, RyhmaCriteriaDto.class));
    }

    private List<Organisaatio> findGroups(RyhmaCriteriaDto criteria) {
        if (criteria.getAktiivinen() != null && criteria.getLakkautusPvm() == null) {
            criteria.setLakkautusPvm(LocalDate.now());
        }
        criteria.setParentOidPath("|" + rootOid + "|");
        criteria.setPoistettu(false);
        return organisaatioDAO.findGroups(criteria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganisaatioRDTOV3> findByOids(Collection<String> oids) {
        return organisaatioDAO.findByOids(oids);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganisaatioRDTOV4> findByOidsV4(Collection<String> oids) {
        Preconditions.checkNotNull(oids);
        Preconditions.checkArgument(!oids.isEmpty());
        Preconditions.checkArgument(oids.size() <= 1000);
        return organisaatioDAO.findByOids(oids, true).stream()
                .map(organisaatio -> this.conversionService.convert(organisaatio, OrganisaatioRDTOV4.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrganisaatioRDTOV4 findByIdV4(String id, boolean includeImage) {
        LOG.debug("/organisaatio/{} -- getOrganisaatioByOID()", id);

        Organisaatio o = this.findById(id);

        if (o == null) {
            LOG.info("Failed to find organisaatio by: " + id);
            throw new OrganisaatioResourceException(404, "organisaatio.exception.organisaatio.not.found");
        }

        // Jätetään kuva pois, jos sitä ei haluta
        if (o.getMetadata() != null) {
            o.getMetadata().setIncludeImage(includeImage);
        }

        OrganisaatioRDTOV4 result = conversionService.convert(o, OrganisaatioRDTOV4.class);

        LOG.debug("  result={}", result);
        return result;

    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganisaatioRDTOV4> findChildrenById(String id, boolean includeImage) {
        Preconditions.checkNotNull(id);
        Organisaatio parentOrg = this.findById(id);
        return parentOrg == null
                ? new ArrayList<>()
                : mapToOrganisaatioRdtoV4(parentOrg.getChildren(true), includeImage);
    }

    private List<OrganisaatioRDTOV4> mapToOrganisaatioRdtoV4(Collection<Organisaatio> children, boolean includeImage) {
        return children.stream()
                .map(child -> {
                    // Jätetään kuva pois, jos sitä ei haluta
                    if (child.getMetadata() != null) {
                        child.getMetadata().setIncludeImage(includeImage);
                    }
                    return conversionService.convert(child, OrganisaatioRDTOV4.class);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Organisaatio findById(String id) {
        Organisaatio o = organisaatioDAO.findByOid(id);
        if (o == null) {
            o = organisaatioDAO.findByYTunnus(id);
        }
        if (o == null) {
            o = organisaatioDAO.findByVirastoTunnus(id);
        }
        if (o == null) {
            o = organisaatioDAO.findByOppilaitoskoodi(id);
        }
        if (o == null) {
            o = organisaatioDAO.findByToimipistekoodi(id);
        }
        return o;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findOidsBy(String searchTerms, int count, int startIndex, OrganisaatioTyyppi type) {
        return organisaatioDAO.findOidsBy(false, count, startIndex, type);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganisaatioSuhde> findLiitokset(Date date) {
        return organisaatioSuhdeDAO.findLiitokset(date);
    }

    @Override
    public Collection<String> findChildOidsRecursive(ChildOidsCriteria criteria) {
        return organisaatioDAO.findChildOidsRecursive(criteria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganisaatioRDTOV4> haeMuutetut(DateParam lastModifiedSince, boolean includeImage) {
        Preconditions.checkNotNull(lastModifiedSince);

        LOG.debug("haeMuutetut: " + lastModifiedSince.toString());
        long qstarted = System.currentTimeMillis();

        List<Organisaatio> organisaatiot = organisaatioDAO.findModifiedSince(lastModifiedSince.getValue());

        LOG.debug("Muutettujen haku {} ms", System.currentTimeMillis() - qstarted);

        if (organisaatiot == null || organisaatiot.isEmpty()) {
            return Collections.emptyList();
        }
        return this.mapToOrganisaatioRdtoV4(organisaatiot, includeImage);
    }

}

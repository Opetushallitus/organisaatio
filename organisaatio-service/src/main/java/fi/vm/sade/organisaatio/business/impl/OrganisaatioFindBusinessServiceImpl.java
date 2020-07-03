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
import com.google.common.collect.Iterables;
import fi.vm.sade.organisaatio.api.DateParam;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.auth.PermissionChecker;
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
import fi.vm.sade.organisaatio.service.TimeService;
import fi.vm.sade.organisaatio.service.search.SearchConfig;
import fi.vm.sade.organisaatio.service.search.SearchCriteria;
import fi.vm.sade.organisaatio.service.util.OrganisaatioTyyppiUtil;
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

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 *
 * @author simok
 */
@Transactional
@Service("organisaatioFindBusinessService")
public class OrganisaatioFindBusinessServiceImpl implements OrganisaatioFindBusinessService {

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private static final int MAX_PARENT_OID_PATHS = 500;

    @Autowired
    private OrganisaatioDAO organisaatioDAO;

    @Autowired
    private OrganisaatioSuhdeDAO organisaatioSuhdeDAO;

    @Autowired
    private ConversionService conversionService;

    @Autowired
    private TimeService timeService;

    @Autowired
    private PermissionChecker permissionChecker;

    @Value("${root.organisaatio.oid}")
    private String rootOrganisaatioOid;

    @Override
    @Transactional(readOnly = true)
    public List<OrganisaatioPerustieto> findBy(SearchCriteria criteria, SearchConfig config) {
        // haetaan hakukriteerien mukaiset organisaatiot
        Date now = timeService.getNow();
        Set<Organisaatio> entities = new TreeSet<>((o1, o2) -> o1.getOid().compareTo(o2.getOid()));
        entities.addAll(organisaatioDAO.findBy(criteria, now));
        Set<String> oids = entities.stream().map(Organisaatio::getOid).collect(toSet());

        // haetaan ylä- ja aliorganisaatiot
        if (config.isParentsIncluded() || config.isChildrenIncluded()) {
            Set<String> parentOids = new HashSet<>();
            SortedSet<String> parentOidPaths = new TreeSet<>((path1, path2) -> path1.compareTo(path2));
            entities.forEach(entity -> {
                Optional.ofNullable(entity.getParentOidPath()).ifPresent(parentOidPath -> {
                    Arrays.stream(parentOidPath.split("\\|"))
                            .filter(oid -> !oid.isEmpty() && !oid.equals(rootOrganisaatioOid))
                            .forEach(parentOids::add);
                    parentOidPaths.add(parentOidPath + entity.getOid() + "|");
                });
            });
            parentOids.removeAll(oids);
            optimizeParentOidPaths(parentOidPaths);

            if (config.isParentsIncluded() && !parentOids.isEmpty()) {
                SearchCriteria parentsCriteria = constructRelativeCriteria(criteria);
                parentsCriteria.setOid(parentOids);
                entities.addAll(organisaatioDAO.findBy(parentsCriteria, now));
            }
            if (config.isChildrenIncluded() && !parentOidPaths.isEmpty()) {
                SearchCriteria childrenCriteria = constructRelativeCriteria(criteria);
                Iterables.partition(parentOidPaths, MAX_PARENT_OID_PATHS).forEach(optimizedParentOidPaths -> {
                    childrenCriteria.setParentOidPaths(optimizedParentOidPaths);
                    entities.addAll(organisaatioDAO.findBy(childrenCriteria, now));
                });
            }
        }

        // haetaan aliorganisaatioiden lukumäärät (myös hakukriteerien ulkopuolella olevat)
        Map<String, Long> childCount = config.isCountChildren() ? organisaatioDAO.countActiveChildrenByOid(now) : emptyMap();

        return entities.stream()
                .filter(entity -> !rootOrganisaatioOid.equals(entity.getOid()))
                .map(entity -> conversionService.convert(entity, OrganisaatioPerustieto.class))
                .map(dto -> {
                    dto.setAliOrganisaatioMaara(childCount.getOrDefault(dto.getOid(), 0L));
                    dto.setMatch(oids.contains(dto.getOid()));
                    return dto;
                })
                .collect(toList());
    }

    private static void optimizeParentOidPaths(SortedSet<String> parentOidPaths) {
        // optimoidaan parentOidPath: poistetaan organisaatiot joiden yläorganisaatio on myös mukana listassa
        Iterator<String> iterator = parentOidPaths.iterator();
        String prev = null;
        while (iterator.hasNext()) {
            String next = iterator.next();
            if (prev != null && next.startsWith(prev)) {
                iterator.remove();
            } else {
                prev = next;
            }
        }
    }

    private static SearchCriteria constructRelativeCriteria(SearchCriteria from) {
        SearchCriteria to = new SearchCriteria();
        to.setAktiiviset(from.getAktiiviset());
        to.setSuunnitellut(from.getSuunnitellut());
        to.setLakkautetut(from.getLakkautetut());
        to.setPoistettu(from.getPoistettu());
        to.setPiilotettu(from.getPiilotettu());
        to.setOrganisaatioTyyppi(from.getOrganisaatioTyyppi().stream()
                .flatMap(organisaatiotyyppi -> OrganisaatioTyyppiUtil.getOrgTypeLimit(organisaatiotyyppi).stream())
                .collect(toList()));
        to.setOidRestrictionList(from.getOidRestrictionList());
        return to;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Organisaatio> findBySearchCriteria (
            Set<String> kieliList,
            Set<String> kuntaList,
            Set<String> oppilaitostyyppiList,
            Set<String> vuosiluokkaList,
            Set<String> ytunnusList,
            Set<String> oidList,
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
        criteria.setParentOidPath("|" + rootOrganisaatioOid + "|");
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
        boolean excludePiilotettu = !permissionChecker.isReadAccessToAll();
        return organisaatioDAO.findByOids(oids, true, excludePiilotettu).stream()
                .map(this::markImagesNotIncluded)
                .map(organisaatio -> mapToOrganisaatioRdtoV4(organisaatio))
                .collect(Collectors.toList());
    }


    private Organisaatio markImagesNotIncluded(Organisaatio organisaatio) {
        Optional.ofNullable(organisaatio.getMetadata())
                .ifPresent(metadata -> metadata.setIncludeImage(false));
        return organisaatio;
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

        OrganisaatioRDTOV4 result = mapToOrganisaatioRdtoV4(o);

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
                return mapToOrganisaatioRdtoV4(child);
            })
            .collect(Collectors.toList());
    }

    private OrganisaatioRDTOV4 mapToOrganisaatioRdtoV4(Organisaatio organisaatio) {
        OrganisaatioRDTOV4 organisaatio_result = conversionService.convert(organisaatio, OrganisaatioRDTOV4.class);
        return organisaatio_result;
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
        return organisaatioSuhdeDAO.findLiitokset(permissionChecker.isReadAccessToAll() ? null : false, date);
    }

    @Override
    public Collection<String> findChildOidsRecursive(ChildOidsCriteria criteria) {
        return organisaatioDAO.findChildOidsRecursive(criteria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganisaatioRDTOV4> haeMuutetut(
            DateParam lastModifiedSince,
            boolean includeImage,
            List<OrganisaatioTyyppi> organizationTypes,
            boolean excludeDiscontinued) {
        Preconditions.checkNotNull(lastModifiedSince);

        LOG.debug("haeMuutetut: " + lastModifiedSince.toString());
        long qstarted = System.currentTimeMillis();

        List<Organisaatio> organisaatiot = organisaatioDAO.findModifiedSince(
                !permissionChecker.isReadAccessToAll(),
                lastModifiedSince.getValue(),
                organizationTypes,
                excludeDiscontinued);

        LOG.debug("Muutettujen haku {} ms", System.currentTimeMillis() - qstarted);

        if (organisaatiot == null || organisaatiot.isEmpty()) {
            return Collections.emptyList();
        }
        return this.mapToOrganisaatioRdtoV4(organisaatiot, includeImage);
    }

}

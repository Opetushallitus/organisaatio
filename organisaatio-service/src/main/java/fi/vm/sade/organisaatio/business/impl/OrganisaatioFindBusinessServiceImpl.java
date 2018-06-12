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

import com.google.common.collect.Iterables;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.business.OrganisaatioFindBusinessService;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAO;
import fi.vm.sade.organisaatio.dao.OrganisaatioSuhdeDAO;
import fi.vm.sade.organisaatio.dto.v3.OrganisaatioRDTOV3;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;
import fi.vm.sade.organisaatio.service.TimeService;
import fi.vm.sade.organisaatio.service.search.SearchConfig;
import fi.vm.sade.organisaatio.service.search.SearchCriteria;
import fi.vm.sade.organisaatio.service.util.OrganisaatioTyyppiUtil;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import static java.util.Collections.emptyMap;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;

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
        to.setOrganisaatioTyyppi(from.getOrganisaatioTyyppi().stream()
                .flatMap(organisaatiotyyppi -> OrganisaatioTyyppiUtil.getOrgTypeLimit(organisaatiotyyppi).stream())
                .collect(toList()));
        to.setOidRestrictionList(from.getOidRestrictionList());
        return to;
    }

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
    public List<Organisaatio> findGroups() {
        return organisaatioDAO.findGroups();
    }


    @Override
    @Transactional(readOnly = true)
    public List<OrganisaatioRDTOV3> findByOids(Collection<String> oids) {
        return organisaatioDAO.findByOids(oids);
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
}

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
package fi.vm.sade.organisaatio.ui.widgets.factory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioPerustietoType;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioSearchCriteriaDTO;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi.Teksti;

/**
 * Cache implementation for Organisaatio widget use.
 *
 * @author mlyly
 */
class OrganisaatioProxyCachingImpl implements OrganisaatioProxy {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioProxyCachingImpl.class);
    private static final int MaxResults = 500;


    // private Executor executor = Executors.newSingleThreadExecutor();

    private OrganisaatioService organisaatioService;
    private LoadingCache<OrganisaatioCacheKey, Collection<OrganisaatioDTO>> organisaatioCache;

    private long showCacheStatsWhenLessThatCurrentTime = -1L;

    OrganisaatioProxyCachingImpl(OrganisaatioService service) {
        organisaatioService = service;
        if (organisaatioService == null) {
            throw new RuntimeException("OrganisaatioProxyCachingImpl() - NULL OrganisaatioService");
        }

        // set up cache
        organisaatioCache = CacheBuilder
                .newBuilder()
                .recordStats()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                // .refreshAfterWrite(5, TimeUnit.MINUTES)
                .build(new CacheLoader<OrganisaatioCacheKey, Collection<OrganisaatioDTO>>() {
            @Override
            public Collection<OrganisaatioDTO> load(OrganisaatioCacheKey key) throws Exception {
                Collection<OrganisaatioDTO> result = new ArrayList<OrganisaatioDTO>();

                if (key.getOid() != null) {
                    OrganisaatioDTO dto = organisaatioService.findByOid(key.getOid());
                    result.add(dto);
                } else if (key.getOidList() != null) {
                    /*OrganisaatioSearchCriteriaDTO criteria = new OrganisaatioSearchCriteriaDTO();
                    criteria.setMaxResults(MaxResults);
                    criteria.getOidResctrictionList().addAll(key.getOidList());*/ 
                    result = organisaatioService.findByOidList(key.getOidList(), key.getOidList().size());//convertToDTOs(organisaatioService.searchBasicOrganisaatios(criteria));//
                } else if (key.getCriteria() != null) {
                    // TODO may be inefficient, multiple search results in cache - maybe cache only oid list and convert to actual result list later?
                    result = organisaatioService.searchOrganisaatios(key.getCriteria());
                }

                LOG.debug("load(key={}) --> result.size={}", key, result != null ? result.size() : -1);

                return result;
            }

        });
    }
    
    private List<OrganisaatioDTO> convertToDTOs(List<OrganisaatioPerustietoType> perusOrgs) {
        List<OrganisaatioDTO> resultList = new ArrayList<OrganisaatioDTO>();
        if (perusOrgs != null) {
            for (OrganisaatioPerustietoType curOrgPerus : perusOrgs) {
                resultList.add(convertOrganisaatioToDTO(curOrgPerus));
            }
        }
        return resultList;
    }
    
    private OrganisaatioDTO convertOrganisaatioToDTO(OrganisaatioPerustietoType curOrgPerus) {
        OrganisaatioDTO curOrg = new OrganisaatioDTO();
        curOrg.setOid(curOrgPerus.getOid());
        MonikielinenTekstiTyyppi nimi = new MonikielinenTekstiTyyppi();
        Teksti nimiFi = new Teksti();
        nimiFi.setKieliKoodi("fi");
        nimiFi.setValue(curOrgPerus.getNimiFi());
        nimi.getTeksti().add(nimiFi);
        Teksti nimiSv = new Teksti();
        nimiSv.setKieliKoodi("sv");
        nimiSv.setValue(curOrgPerus.getNimiSv());
        nimi.getTeksti().add(nimiSv);
        Teksti nimiEn = new Teksti();
        nimiEn.setKieliKoodi("en");
        nimiEn.setValue(curOrgPerus.getNimiFi());
        nimi.getTeksti().add(nimiEn);
        curOrg.setNimi(nimi);
        curOrg.setParentOid(curOrgPerus.getParentOid());
        curOrg.setOppilaitosKoodi(curOrgPerus.getOppilaitosKoodi());
        curOrg.setYtunnus(curOrgPerus.getYtunnus());
        curOrg.getTyypit().addAll(curOrgPerus.getTyypit());
        return curOrg;
    }

    @Override
    public Collection<OrganisaatioDTO> findByParentOids(List<String> oids) {
        try {
            LOG.debug("findByParentOids({})", oids);
            showCacheStatsIfNecessary();
            return organisaatioCache.get(new OrganisaatioCacheKey(null, oids, null));
        } catch (ExecutionException e) {
            throw new RuntimeException("Can't get organisaatios for oid list: " + oids);
        }
    }

    @Override
    public Collection<OrganisaatioDTO> find(OrganisaatioSearchCriteriaDTO dto) {
        try {
            LOG.debug("find({})", dto);
            showCacheStatsIfNecessary();
            Collection<OrganisaatioDTO> result = organisaatioCache.get(new OrganisaatioCacheKey(null, null, dto));
            return result;
        } catch (ExecutionException e) {
            throw new RuntimeException("Can't get organisaatios for criteria: " + dto);
        }
    }

    @Override
    public OrganisaatioDTO findByOid(String oid) {
        try {
            LOG.debug("findByOid({})", oid);
            showCacheStatsIfNecessary();
            Collection<OrganisaatioDTO> result = organisaatioCache.get(new OrganisaatioCacheKey(oid, null, null));
            if (!result.isEmpty()) {
                return result.iterator().next();
            } else {
                return null;
            }
        } catch (ExecutionException e) {
            throw new RuntimeException("Can't get organisaatios for oid: " + oid);
        }
    }

    private void showCacheStatsIfNecessary() {
        if (showCacheStatsWhenLessThatCurrentTime < System.currentTimeMillis()) {
            // Next time show after one minute
            showCacheStatsWhenLessThatCurrentTime = System.currentTimeMillis() + 60000L;

            LOG.info("CACHE STATS: {}", organisaatioCache.stats().toString());
        }
    }

    /**
     * Cache key used to cache organisations with different search operations.
     */
    private static class OrganisaatioCacheKey implements Serializable {

        private String oid;
        private List<String> oidList;
        private OrganisaatioSearchCriteriaDTO criteria;

        public OrganisaatioCacheKey(String oid, List<String> oidList, OrganisaatioSearchCriteriaDTO criteria) {
            this.oid = oid;
            this.oidList = oidList;
            this.criteria = criteria;
        }

        public String getOid() {
            return oid;
        }

        public OrganisaatioSearchCriteriaDTO getCriteria() {
            return criteria;
        }

        public List<String> getOidList() {
            return oidList;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("OrganisaatioCacheKey[");
            sb.append("oid=");
            sb.append(oid);
            sb.append(", oidList=");
            sb.append(oidList);

            sb.append(", criteria=[");
            if (criteria != null) {
                sb.append("firstResult=");
                sb.append(criteria.getFirstResult());
                sb.append(", kunta=");
                sb.append(criteria.getKunta());
                sb.append(", maxResults=");
                sb.append(criteria.getMaxResults());
                sb.append(", oidRestrictionList=");
                sb.append(criteria.getOidResctrictionList());
                sb.append(", oppilaitosTyyppi=");
                sb.append(criteria.getOppilaitosTyyppi());
                sb.append(", organisaatioDomainNimi=");
                sb.append(criteria.getOrganisaatioDomainNimi());
                sb.append(", organisaatioTyyppi=");
                sb.append(criteria.getOrganisaatioTyyppi());
                sb.append(", searchStr=");
                sb.append(criteria.getSearchStr());
            }
            sb.append("]]");

            return sb.toString();
        }

        @Override
        public boolean equals(Object obj) {
            boolean result = false;

            if (obj == null) {
                result = false;
            } else if (obj == this) {
                result = true;
            } else if (obj.getClass() != getClass()) {
                result = false;
            } else {
                OrganisaatioCacheKey k = (OrganisaatioCacheKey) obj;

                // TODO sorry about this... it's not exactly what I wanted, but for some reason the EqualsBuilder fails... string comparison ftw!
                String thisStr = this.toString();
                String otherStr = k.toString();

                result = thisStr.equals(otherStr);

//                EqualsBuilder eb = new EqualsBuilder();
//                eb.appendSuper(super.equals(obj));
//                eb.append(oid, k.oid);
//                //                eb.append(oidList, k.oidList);
//
//                if (criteria != null) {
//                    eb.append(criteria.getFirstResult(), k.criteria.getFirstResult());
//                    eb.append(criteria.getKunta(), k.criteria.getKunta());
//                    eb.append(criteria.getMaxResults(), k.criteria.getMaxResults());
//                    //                    eb.append(criteria.getOidResctrictionList(), k.criteria.getOidResctrictionList());
//                    eb.append(criteria.getOppilaitosTyyppi(), k.criteria.getOppilaitosTyyppi());
//                    eb.append(criteria.getOrganisaatioDomainNimi(), k.criteria.getOrganisaatioDomainNimi());
//                    eb.append(criteria.getOrganisaatioTyyppi(), k.criteria.getOrganisaatioTyyppi());
//                    eb.append(criteria.getSearchStr(), k.criteria.getSearchStr());
//                }
//
//                result = eb.isEquals();
            }

            LOG.debug("equals == {}", result);
            return result;
        }

        @Override
        public int hashCode() {
            HashCodeBuilder eb = new HashCodeBuilder(5, 41);

            if (oid != null) {
                eb.append(oid);
            }

            if (oidList != null) {
                eb.append(oidList);
            }

            if (criteria != null) {
                eb.append(criteria.getFirstResult());
                eb.append(criteria.getKunta());
                eb.append(criteria.getMaxResults());
                eb.append(criteria.getOidResctrictionList());
                eb.append(criteria.getOppilaitosTyyppi());
                eb.append(criteria.getOrganisaatioDomainNimi());
                eb.append(criteria.getOrganisaatioTyyppi());
                eb.append(criteria.getSearchStr());
            }

            int hash = eb.toHashCode();
            LOG.debug("hashcode == {}", hash);

            return hash;
        }
    }
}

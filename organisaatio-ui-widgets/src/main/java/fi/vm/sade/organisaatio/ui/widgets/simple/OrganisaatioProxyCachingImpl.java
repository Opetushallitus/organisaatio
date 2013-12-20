/*
 * Copyright
 * *
 *  Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  European Union Public Licence for more details.
 *
 */

package fi.vm.sade.organisaatio.ui.widgets.simple;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.organisaatio.api.model.types.OrganizationStructureType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.List;
import java.util.concurrent.*;

public class OrganisaatioProxyCachingImpl implements OrganisaatioProxy {

    private Executor executor = Executors.newSingleThreadExecutor();

    private LoadingCache<OrgCacheKey, List<OrganizationStructureType>> orgCache;
    private OrganisaatioService organisaatioService;

    public OrganisaatioProxyCachingImpl(final OrganisaatioService organisaatioService) {

        this.organisaatioService = organisaatioService;

        // Set up caches.

        orgCache = CacheBuilder
                .newBuilder()
                .recordStats()
                .refreshAfterWrite(10, TimeUnit.MINUTES)
                .expireAfterWrite(20, TimeUnit.MINUTES)
                .build(new CacheLoader<OrgCacheKey, List <OrganizationStructureType>>() {

                    @Override
                    public List<OrganizationStructureType> load(OrgCacheKey oids) throws Exception {
                        return fetchOrganisaatios(oids.getOids());
                    }
                });
    }

    private List<OrganizationStructureType> fetchOrganisaatios(List<String> oids) {
        return organisaatioService.getOrganizationStructure(oids);
    }

    @Override
    public List<OrganizationStructureType> getOrganisaatios(List<String> oids) {
        try {
            return orgCache.get(new OrgCacheKey(oids));
        } catch (Exception e) {
            throw new RuntimeException("Can't get organisation structure", e);
        }
    }

    /**
     * Key class for koodi cache.
     */
    static class OrgCacheKey {

        private List<String> oids;

        public OrgCacheKey(List<String> oids) {
            this.oids = oids;
        }

        List<String> getOids() {
            return oids;
        }

        void setOids(List<String> oids) {
            this.oids = oids;
        }

        @Override
        public boolean equals(Object obj) {

            if (obj == null) {
                return false;
            } else if (obj == this) {
                return true;
            } else if (obj.getClass() != getClass()) {
                return false;
            } else {
                OrgCacheKey k = (OrgCacheKey) obj;
                return new EqualsBuilder()
                        .append(oids, k.oids)
                        .isEquals();
            }
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(5, 41)
                    .append(oids)
                    .toHashCode();
        }
    }
}

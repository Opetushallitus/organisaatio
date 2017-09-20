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
package fi.vm.sade.organisaatio.service.search;

import java.util.*;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;

public class OrganisaatioSearchService extends SolrOrgFields {

    @Value("${root.organisaatio.oid}")
    private String rootOrganisaatioOid;

    private final SolrServer solr;
    private final Logger LOG = LoggerFactory.getLogger(OrganisaatioSearchService.class);
    private Map<String, Set<String>> orgTypeLimit = Maps.newHashMap();

    @Autowired
    public OrganisaatioSearchService(SolrServerFactory factory) {
        this.solr = factory.getSolrServer();
        orgTypeLimit.put(OrganisaatioTyyppi.KOULUTUSTOIMIJA.value(), Sets.newHashSet(OrganisaatioTyyppi.KOULUTUSTOIMIJA.value(),
                OrganisaatioTyyppi.OPPILAITOS.value(), OrganisaatioTyyppi.TOIMIPISTE.value(), OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE.value()));
        orgTypeLimit.put(OrganisaatioTyyppi.OPPILAITOS.value(), Sets.newHashSet(OrganisaatioTyyppi.OPPILAITOS.value(), OrganisaatioTyyppi.TOIMIPISTE.value(),
                OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE.value()));
        orgTypeLimit.put(OrganisaatioTyyppi.TOIMIPISTE.value(),
                Sets.newHashSet(OrganisaatioTyyppi.TOIMIPISTE.value(), OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE.value()));
        orgTypeLimit.put(OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE.value(),
                Sets.newHashSet(OrganisaatioTyyppi.TOIMIPISTE.value(), OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE.value()));
        orgTypeLimit.put(OrganisaatioTyyppi.MUU_ORGANISAATIO.value(), Sets.newHashSet("\"" + OrganisaatioTyyppi.MUU_ORGANISAATIO.value() + "\""));
        orgTypeLimit.put(OrganisaatioTyyppi.TYOELAMAJARJESTO.value(), Sets.newHashSet(OrganisaatioTyyppi.TYOELAMAJARJESTO.value()));
    }

    public List<OrganisaatioPerustieto> searchExact(final SearchCriteria searchCriteria) {
        long time = System.currentTimeMillis();
        final List<String> kunta = searchCriteria.getKunta();
        final List<String> restrictionList = searchCriteria.getOidRestrictionList();
        final String organisaatioTyyppi = searchCriteria.getOrganisaatioTyyppi();
        final List<String> kieli = searchCriteria.getKieli();
        String searchStr = searchCriteria.getSearchStr();
        String oid = searchCriteria.getOid();

        SolrQuery q = createOrgQuery(searchCriteria, kunta, restrictionList, organisaatioTyyppi, kieli, searchStr, oid);

        // max rows to return
        q.setRows(10000);

        try {
            QueryResponse response = solr.query(q, METHOD.POST);

            final SolrDocumentToOrganisaatioPerustietoTypeFunction converter =
                    new SolrDocumentToOrganisaatioPerustietoTypeFunction(null);

            final List<OrganisaatioPerustieto> result =
                    Lists.newArrayList(Lists.transform(response.getResults(), converter));

            LOG.debug("Total time :{} ms. Results :{}",
                    (System.currentTimeMillis() - time),
                    response.getResults().getNumFound());
            return result;
        } catch (SolrServerException e) {
            throw new RuntimeException(e);
        }

    }

    public List<OrganisaatioPerustieto> searchHierarchy(final SearchCriteria searchCriteria) {
        long time = System.currentTimeMillis();
        final List<String> kunta = searchCriteria.getKunta();
        final List<String> restrictionList = searchCriteria.getOidRestrictionList();
        final String organisaatioTyyppi    = searchCriteria.getOrganisaatioTyyppi();
        final List<String> kieli    = searchCriteria.getKieli();
        String searchStr = searchCriteria.getSearchStr();
        String oid = searchCriteria.getOid();

        SolrQuery q = createOrgQuery(searchCriteria, kunta, restrictionList, organisaatioTyyppi, kieli, searchStr, oid);

        q.set("fl", OID, PATH);
        // max rows to return
        q.setRows(10000);
        try {
            QueryResponse response = solr.query(q, METHOD.POST);

            LOG.debug("Sending query: " + q.getQuery() + ", filters: " + Joiner.on(" ").join(q.getFilterQueries()));
            LOG.debug("Search matched {} results, fetching docs...", response
                    .getResults().getNumFound());

            if (response.getResults().getNumFound() == 0) {
                // short circuit no results here
                return Lists.newArrayList();
            }
            Set<String> oids = Sets.newHashSet();
            Set<String> paths = Sets.newHashSet();
            for (SolrDocument doc : response.getResults()) {
                if (!rootOrganisaatioOid.equals(doc.getFieldValue(OID))) {
                    paths.add((String) doc.getFieldValue(OID));
                }

                if (!searchCriteria.getSkipParents()) {
                    for (Object path : doc.getFieldValues(PATH)) {
                        if (!rootOrganisaatioOid.equals(path)) {
                            oids.add((String) path);
                        }
                    }
                }
                oids.add((String) doc.getFirstValue(OID));
            }


            // get the actual docs
            q = new SolrQuery("*:*");
            q.setFields("*");
            addDateFilters(searchCriteria, q);

            // filter out oph (TODO do not index oph)
            q.addFilterQuery(String.format("-%s:%s", OID, rootOrganisaatioOid));

            // filter out types in upper hierarchy
            if (organisaatioTyyppi != null && organisaatioTyyppi.length() > 0) {
                final Set<String> limitToTypes = orgTypeLimit
                        .get(organisaatioTyyppi);
                q.addFilterQuery(String.format("%s:(%s)", ORGANISAATIOTYYPPI, Joiner.on(" ")
                        .join(limitToTypes)));
            }

            // restrictions
            if (restrictionList.size() > 0) {
                // filter based on restriction list
                addFilterQuery(q, "%s:(%s)", PATH, restrictionList, true);
            }

            String query = String.format("%s:(%s)", OID, Joiner.on(" ").join(escapeAll(oids)));
            if (paths.size() > 0) {
                query = query
                        + String.format(" %s:(%s)", PATH,
                                Joiner.on(" ").join(escapeAll(paths)));
            }
            q.setQuery(query);
            q.setRows(20000);

            response = solr.query(q, METHOD.POST);

            LOG.debug("Search time :{} ms.", (System.currentTimeMillis() - time));

            final SolrDocumentToOrganisaatioPerustietoTypeFunction converter =
                    new SolrDocumentToOrganisaatioPerustietoTypeFunction(oids);

            final List<OrganisaatioPerustieto> result =
                    Lists.newArrayList(Lists.transform(response.getResults(), converter));

            LOG.debug("Total time :{} ms.", (System.currentTimeMillis() - time));
            return result;
        } catch (SolrServerException e) {
            LOG.error("Error executing search, q={}", q.getQuery());
            throw new RuntimeException(e);
        }
    }

    private SolrQuery createOrgQuery(
            final SearchCriteria searchCriteria,
            final List<String> kunta, final List<String> restrictionList,
            final String organisaatioTyyppi, final List<String> kieli, String searchStr,
            String oid) {
        SolrQuery q = new SolrQuery("*:*");
        final List<String> queryParts = Lists.newArrayList();

        if (oid != null && !oid.isEmpty()) {
            q.addFilterQuery(String.format("%s:%s", OID, escape(oid)));
        } else if (searchStr != null && searchStr.length() > 0) {
            // nimi
            searchStr = escape(searchStr);
            // nimi search
            queryParts.clear();
            addQuery(searchStr, queryParts, "%s:*%s*", NIMISEARCH, searchStr);
            q.addFilterQuery(Joiner.on(" ").join(queryParts));

        }

        addDateFilters(searchCriteria, q);

        removeEmptyEntries(searchCriteria.getOppilaitosTyyppi());
        if (searchCriteria.getOppilaitosTyyppi() != null 
                && searchCriteria.getOppilaitosTyyppi().size() > 0) {
            // filter based on oppilaitosTyyppi list
            addFilterQuery(q, "%s:(%s)", OPPILAITOSTYYPPI, searchCriteria.getOppilaitosTyyppi(), true);
        }

        removeEmptyEntries(kunta);
        // kunta
        if (kunta != null
                && kunta.size() > 0) {
            // filter based on kunta list
            addFilterQuery(q, "+%s:(%s)", KUNTA, kunta);
        }
        // organisaatiotyyppi
        queryParts.clear();
        //No escaping is needed with term query parser - handling of search terms with white spaces will break, if escaping is added
        addQuery(organisaatioTyyppi, queryParts, "{!term f=%s}%s", ORGANISAATIOTYYPPI, organisaatioTyyppi);
        if (queryParts.size() > 0) {
            q.addFilterQuery(Joiner.on(" ").join(queryParts));
        }

        removeEmptyEntries(kieli);
        // kieli
        if (kieli != null
                && kieli.size() > 0) {
            // filter based on kieli list
            addFilterQuery(q, "%s:(%s)", KIELI, kieli);
        }

        removeEmptyEntries(restrictionList);
        if (restrictionList.size() > 0) {
            // filter based on restriction list
            addFilterQuery(q, "%s:(%s)", PATH, restrictionList, true);
        }
        // also filter out oph (TODO do not index oph)
        q.addFilterQuery(String.format("-%s:%s", OID, escape(rootOrganisaatioOid)));
        return q;
    }

    private void removeEmptyEntries(List<String> queryParams) {
        if(null != queryParams) {
            queryParams.removeAll(Arrays.asList(null, ""));
        }
    }

    private void addFilterQuery(SolrQuery query, String template, String paramName, List<String> queryParams) {
        addFilterQuery(query, template, paramName, queryParams, false);
    }

    private void addFilterQuery(SolrQuery query, String template, String paramName, List<String> queryParams, boolean allowWildcards) {
        query.addFilterQuery(String.format(template, paramName, Joiner.on(" ")
                .join(escapeAll(queryParams, allowWildcards))));
    }

    private Set<String> escapeAll(Collection<String> queryParams) {
        return escapeAll(queryParams, false);
    }

    private Set<String> escapeAll(Collection<String> queryParams, boolean allowWildcards) {
        Set<String> escaped = new HashSet<>(queryParams.size());
        for (String param : queryParams) {
            String escapedParam = allowWildcards ? escapeButAllowWildcard(param) : escape(param);
            escaped.add(escapedParam);
        }
        return escaped;
    }

    private String escapeButAllowWildcard(String searchStr) {
        searchStr = escape(searchStr);
        return searchStr.replace("\\*", "*");
    }

    private String escape(String searchStr) {
        searchStr = null == searchStr ? searchStr : ClientUtils.escapeQueryChars(searchStr);
        return searchStr;
    }

    private void addDateFilters(final SearchCriteria searchCriteria, SolrQuery q) {
        // Ei aktiivisia, suunniteltuja eikä lakkautettuja - tätä ei pitäisi tapahtua
        if (!searchCriteria.getAktiiviset() && !searchCriteria.getSuunnitellut() &&
                !searchCriteria.getLakkautetut()) {
            // Filtteröidään pois aktiiviset, suunnitellut, lakkautetut
            q.addFilterQuery(String.format("-%s:[%s TO %s]", ALKUPVM, "*", "NOW"));
            q.addFilterQuery(String.format("-%s:[%s TO %s]", ALKUPVM, "NOW", "*"));
            q.addFilterQuery(String.format("-%s:[%s TO %s]", LAKKAUTUSPVM, "*", "NOW"));
            return;
        }

        // Aktiiviset, Suunnitellut, Lakkautetut
        if (searchCriteria.getAktiiviset() && searchCriteria.getSuunnitellut() &&
                searchCriteria.getLakkautetut()) {
            // Ei päivämääräfiltteröintiä
            return;
        }

        // Suunnitellut, Lakkautetut
        if (!searchCriteria.getAktiiviset() && searchCriteria.getSuunnitellut() &&
                searchCriteria.getLakkautetut()) {
            // Alkupvm tulevaisuudessa tai lakkautuspvm menneisyydessä
            q.addFilterQuery(String.format("%s:[%s TO %s] || %s:[%s TO %s]", ALKUPVM, "NOW", "*", LAKKAUTUSPVM, "*", "NOW"));
            return;
        }

        // Lakkautetut
        if (!searchCriteria.getAktiiviset() && !searchCriteria.getSuunnitellut() &&
                searchCriteria.getLakkautetut()) {
            // Haetaan mukaan kaikki lakkautetut - lakkautuspäivämäärä menneisyydessä
            q.addFilterQuery(String.format("%s:[%s TO %s]", LAKKAUTUSPVM, "*", "NOW"));
            return;
        }

        // Alkupäivämäärän käsittely -otetaanko mukaan suunnitellut vai filtteröidäänkö ne ulos
        if (searchCriteria.getSuunnitellut() && !searchCriteria.getAktiiviset()) {
            // Filtteröidään pois aktiiviset - joiden alkupvm menneisyydessä
            q.addFilterQuery(String.format("-%s:[%s TO %s]", ALKUPVM, "*", "NOW"));
        }
        else if (!searchCriteria.getSuunnitellut() && searchCriteria.getAktiiviset()) {
            // Filtteröidään pois suunnitellut - joiden alkupvm tulevaisuudessa
            q.addFilterQuery(String.format("-%s:[%s TO %s]", ALKUPVM, "NOW", "*"));
        }

        // Loppupäivämäärän käsittely - otetaanko mukaan lakkautetut vai filtteröidäänkö ne ulos
        if (!searchCriteria.getLakkautetut()) {
            // Filteröidään pois lakkautetut.
            q.addFilterQuery(String.format("-%s:[%s TO %s]", LAKKAUTUSPVM, "*", "NOW"));
        }
    }

    private void addQuery(final String param, final List<String> queryParts,
            String template, Object... params) {
        if (param != null) {
            queryParts.add(String.format(template, params));
        }
    }

    /**
     * Search Organisations by oid
     *
     * @param organisationOids
     * @return
     */
    public List<OrganisaatioPerustieto> findByOidSet(Set<String> organisationOids) {

        if (organisationOids.isEmpty()) {
            return Collections.emptyList();
        }

        SolrQuery q = new SolrQuery(String.format(SolrOrgFields.OID + ":(%s)", Joiner.on(" ").join(escapeAll(organisationOids))));
        q.setRows(organisationOids.size());

        final SolrDocumentToOrganisaatioPerustietoTypeFunction converter = new SolrDocumentToOrganisaatioPerustietoTypeFunction(null);

        try {
            List<OrganisaatioPerustieto> result = Lists.newArrayList(Lists.transform(solr.query(q, METHOD.POST).getResults(),
                    converter));
            return result;
        } catch (SolrServerException e) {
            throw new RuntimeException(e);
        }

    }

    public List<String> findParentOids(String organisationOid) {
        final SolrQuery q = new SolrQuery(String.format(SolrOrgFields.OID + ":%s", escape(organisationOid)));
        q.setFields(SolrOrgFields.PATH);
        final List<String> oids = Lists.newArrayList();

        SolrDocumentList docList;
        try {
            docList = solr.query(q).getResults();
            if (docList.getNumFound() == 1) {
                SolrDocument doc = docList.get(0);
                for (Object field : doc.getFieldValues(SolrOrgFields.PATH)) {
                    if (!rootOrganisaatioOid.equals(field)) {
                        oids.add((String) field);
                    }
                }
            }

        } catch (SolrServerException e) {
            throw new RuntimeException(e);
        }

        return oids;
    }

}

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

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import fi.vm.sade.organisaatio.api.search.OrganisaatioSearchCriteria;

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
                OrganisaatioTyyppi.OPPILAITOS.value(), OrganisaatioTyyppi.OPETUSPISTE.value(), OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE.value()));
        orgTypeLimit.put(OrganisaatioTyyppi.OPPILAITOS.value(), Sets.newHashSet(OrganisaatioTyyppi.OPPILAITOS.value(), OrganisaatioTyyppi.OPETUSPISTE.value(),
                OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE.value()));
        orgTypeLimit.put(OrganisaatioTyyppi.OPETUSPISTE.value(),
                Sets.newHashSet(OrganisaatioTyyppi.OPETUSPISTE.value(), OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE.value()));
        orgTypeLimit.put(OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE.value(),
                Sets.newHashSet(OrganisaatioTyyppi.OPETUSPISTE.value(), OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE.value()));
        orgTypeLimit.put(OrganisaatioTyyppi.MUU_ORGANISAATIO.value(), Sets.newHashSet("\"" + OrganisaatioTyyppi.MUU_ORGANISAATIO.value() + "\""));
    }

    public List<OrganisaatioPerustieto> searchBasicOrganisaatiosExact(final OrganisaatioSearchCriteria organisaatioSearchCriteria) {

        final String kunta = organisaatioSearchCriteria.getKunta();
        final List<String> restrictionList = organisaatioSearchCriteria
                .getOidRestrictionList();
        final String organisaatioTyyppi = organisaatioSearchCriteria
                .getOrganisaatioTyyppi();
        String searchStr = organisaatioSearchCriteria.getSearchStr();

        SolrQuery q = createOrgQuery(organisaatioSearchCriteria, kunta,
                restrictionList, organisaatioTyyppi, searchStr);

        q.setRows(10000);

        final SolrDocumentToOrganisaatioPerustietoTypeFunction converter = new SolrDocumentToOrganisaatioPerustietoTypeFunction(null);

        try {
            List<OrganisaatioPerustieto> result = Lists.newArrayList(Lists.transform(solr.query(q, METHOD.POST).getResults(),
                    converter));
            return result;
        } catch (SolrServerException e) {
            throw new RuntimeException(e);
        }

    }

    public List<OrganisaatioPerustieto> searchBasicOrganisaatios(
            final OrganisaatioSearchCriteria organisaatioSearchCriteria) {
        long time = System.currentTimeMillis();
        final String kunta = organisaatioSearchCriteria.getKunta();
        final List<String> restrictionList = organisaatioSearchCriteria
                .getOidRestrictionList();
        final String organisaatioTyyppi = organisaatioSearchCriteria
                .getOrganisaatioTyyppi();
        String searchStr = organisaatioSearchCriteria.getSearchStr();

        SolrQuery q = createOrgQuery(organisaatioSearchCriteria, kunta,
                restrictionList, organisaatioTyyppi, searchStr);

        q.set("fl", OID, PATH);
        // max rows to return
        q.setRows(10000);
        try {
            QueryResponse response = solr.query(q, METHOD.POST);

            LOG.debug("Sending query: " + q.getQuery());
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

                if (!organisaatioSearchCriteria.getSkipParents()) {
                    for (Object path : doc.getFieldValues(PATH)) {
                        if (!rootOrganisaatioOid.equals(path)) {
                            oids.add((String) path);
                        }
                    }
                }
                oids.add((String) doc.getFirstValue(OID));
            }

            // get the actual docs
            q = new SolrQuery();
            q.setFields("*");
            addDateFilters(organisaatioSearchCriteria, q);

            // filter out oph (TODO do not index oph)
            q.addFilterQuery(String.format("-%s:%s", OID, rootOrganisaatioOid));

            // filter out types in upper hierarchy
            if (organisaatioTyyppi != null) {
                final Set<String> limitToTypes = orgTypeLimit
                        .get(organisaatioTyyppi);
                q.addFilterQuery(String.format("%s:(%s)", ORGANISAATIOTYYPPI,
                        Joiner.on(" ").join(limitToTypes)));
            }

            // restrictions
            if (restrictionList.size() > 0) {
                // filter based on restriction list
                q.addFilterQuery(String.format("%s:(%s)", PATH, Joiner.on(" ")
                        .join(restrictionList)));
            }

            String query = null;
            query = String.format("%s:(%s)", OID, Joiner.on(" ").join(oids));
            if (paths.size() > 0) {
                query = query
                        + String.format(" %s:(%s)", PATH,
                                Joiner.on(" ").join(paths));
            }
            q.setQuery(query);
            q.setRows(10000);
            response = solr.query(q, METHOD.POST);

            LOG.debug("Search time :{} ms.", (System.currentTimeMillis() - time));

            final SolrDocumentToOrganisaatioPerustietoTypeFunction converter = new SolrDocumentToOrganisaatioPerustietoTypeFunction(oids);

            final List<OrganisaatioPerustieto> tempResult = Lists.newArrayList(Lists.transform(response.getResults(),
                    converter));

            // Filteröidään vanhentuneet organisaatiot pois
            System.out.println("===");
            System.out.println("Filtering for query: " + q.getQuery() + " " + Arrays.toString(q.getFilterQueries()));
            System.out.println("Vain  aktiiviset:" + organisaatioSearchCriteria.isVainAktiiviset());
            System.out.println("Vain lakkautetut:" + organisaatioSearchCriteria.isVainLakkautetut());
            final List<OrganisaatioPerustieto> result = Lists.newArrayList();
            Date now = new Date();
            System.out.println("Now: " + now);
            if (!organisaatioSearchCriteria.isVainLakkautetut() && !organisaatioSearchCriteria.isVainAktiiviset()) {
                System.out.println("1");
                for (OrganisaatioPerustieto op : tempResult) {
                    if (op.getLakkautusPvm() == null || op.getLakkautusPvm().after(now)) {
                        result.add(op);
                    }
                }
            } else if (organisaatioSearchCriteria.isVainLakkautetut()) {
                System.out.println("2");
                for (OrganisaatioPerustieto op : tempResult) {
                    if (op.getLakkautusPvm() != null && op.getLakkautusPvm().before(now)) {
                        result.add(op);
                    }
                }
            } else if (organisaatioSearchCriteria.isVainAktiiviset()) {
                System.out.println("3");
                for (OrganisaatioPerustieto op : tempResult) {
                    if ((op.getLakkautusPvm() == null || op.getLakkautusPvm().after(now))
                            && ((op.getAlkuPvm() == null) || op.getAlkuPvm().before(now))) { // TODO: Päteekö null alku?
                        result.add(op);
                    } else if ((op.getAlkuPvm() != null) && op.getAlkuPvm().after(now))
                        System.out.println("Filtered " + op.getNimi("fi") + op.getAlkuPvm() + op.getLakkautusPvm());
                }
            }
            System.out.println("===");

            LOG.debug("Total time :{} ms.", (System.currentTimeMillis() - time));
            return result;
        } catch (SolrServerException e) {
            LOG.error("Error executing search, q={}", q.getQuery());
            throw new RuntimeException(e);
        }
    }

    private SolrQuery createOrgQuery(
            final OrganisaatioSearchCriteria organisaatioSearchCriteria,
            final String kunta, final List<String> restrictionList,
            final String organisaatioTyyppi, String searchStr) {
        SolrQuery q = new SolrQuery("*:*");
        final List<String> queryParts = Lists.newArrayList();

        // nimi
        if (searchStr != null && searchStr.length() > 0) {
            searchStr = escape(searchStr);
            // nimi search
            queryParts.clear();
            addQuery(searchStr, queryParts, "%s:*%s*", NIMISEARCH, searchStr);
            q.addFilterQuery(Joiner.on(" ").join(queryParts));

        }

        addDateFilters(organisaatioSearchCriteria, q);

        if (organisaatioSearchCriteria.getOppilaitosTyyppi() != null
                && organisaatioSearchCriteria.getOppilaitosTyyppi().length() > 0) {
            queryParts.clear();
            addQuery(organisaatioSearchCriteria.getOppilaitosTyyppi(),
                    queryParts, "%s:%s", OPPILAITOSTYYPPI,
                    organisaatioSearchCriteria.getOppilaitosTyyppi());
            q.addFilterQuery(Joiner.on(" ").join(queryParts));
        }

        // kunta
        queryParts.clear();
        addQuery(kunta, queryParts, "+%s:*%s*", KUNTA, kunta);
        if (queryParts.size() > 0) {
            q.addFilterQuery(Joiner.on(" ").join(queryParts));
        }
        // organisaatiotyyppi
        queryParts.clear();
        addQuery(organisaatioTyyppi, queryParts, "{!term f=%s}%s", ORGANISAATIOTYYPPI,
                organisaatioTyyppi);
        if (queryParts.size() > 0) {
            q.addFilterQuery(Joiner.on(" ").join(queryParts));
        }

        if (restrictionList.size() > 0) {
            // filter based on restriction list
            q.addFilterQuery(String.format("%s:(%s)", PATH, Joiner.on(" ")
                    .join(restrictionList), OID));
        }
        // also filter out oph (TODO do not index oph)
        q.addFilterQuery(String.format("-%s:%s", OID, rootOrganisaatioOid));
        return q;
    }

    private String escape(String searchStr) {
        searchStr = ClientUtils.escapeQueryChars(searchStr);
        return searchStr;
    }

    private void addDateFilters(
            final OrganisaatioSearchCriteria organisaatioSearchCriteria,
            SolrQuery q) {

        // Tehdään date filtteröinti jälkikäsittelynä

        /*
         * if (!organisaatioSearchCriteria.isVainLakkautetut() && !organisaatioSearchCriteria.isVainAktiiviset()) {
         * 
         * // Oletuksena nykyiset ja suunnitellut = // lakkautuspvm > NOW OR lakkautuspvm=tyhjä // -(lakkautuspvm < NOW OR lakkautuspvm != tyhjä)
         * 
         * // Filteröidään lakkautetut pois
         * 
         * // Jos lakkautus on tyhjä, tämä filtteröi sen pois // q.addFilterQuery(String.format("-%s:[%s TO %s]", LAKKAUTUSPVM, "*", "NOW"));
         * 
         * // Tämä ottaa aina mukaan myös KAIKKI tyhjäkenttäiset ja jättää huomiotta aiemman nimihaun //
         * q.addFilterQuery(String.format("-(-%s:[* TO *] OR -%s:[NOW TO *])", LAKKAUTUSPVM, LAKKAUTUSPVM));
         * 
         * String searchString = String.format("-(-%s:[* TO *] OR -%s:[NOW TO *])", LAKKAUTUSPVM, LAKKAUTUSPVM); q.addFilterQuery(searchString);
         * 
         * // -field:[* TO *] finds all documents without a value for field // field:[100 TO *] finds all field values greater than or equal to 100
         * 
         * } else if (organisaatioSearchCriteria.isVainLakkautetut()) {
         * 
         * // vain lakkautetut System.out.println(String.format("-%s:[%s TO %s]", LAKKAUTUSPVM, "NOW", "*")); q.addFilterQuery(String.format("-%s:[%s TO %s]",
         * LAKKAUTUSPVM, "NOW", "*"));
         * 
         * } else if (organisaatioSearchCriteria.isVainAktiiviset()) {
         * 
         * // vain aktiiviset // Filteröidään suunnitellut pois (alkupvm tulevaisuudessa) q.addFilterQuery(String.format("-%s:[%s TO %s]", ALKUPVM, "NOW",
         * "*")); System.out.println(String.format("-%s:[%s TO %s]", ALKUPVM, "NOW", "*")); // Filteröidään lakkautetut pois (lakkautuspvm menneisyydessä))
         * q.addFilterQuery(String.format("-%s:[%s TO %s]", LAKKAUTUSPVM, "*", "NOW")); System.out.println(String.format("-%s:[%s TO %s]", LAKKAUTUSPVM, "*",
         * "NOW")); } // TODO: poista kommentit
         */
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
    public List<OrganisaatioPerustieto> findByOidSet(
            Set<String> organisationOids) {
        if (organisationOids.size() == 0) {
            return Collections.EMPTY_LIST;
        }

        SolrQuery q = new SolrQuery(String.format(SolrOrgFields.OID + ":(%s)", Joiner.on(" ").join(organisationOids)));
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

    public List<String> findParentOids(
            String organisationOid) {
        final SolrQuery q = new SolrQuery(String.format(SolrOrgFields.OID + ":%s", organisationOid));
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

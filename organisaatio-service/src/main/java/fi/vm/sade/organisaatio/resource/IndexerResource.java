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
package fi.vm.sade.organisaatio.resource;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAO;
import fi.vm.sade.organisaatio.dao.OrganisaatioNimiDAO;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.service.search.SolrServerFactory;
import fi.vm.sade.organisaatio.service.util.OrganisaatioToSolrInputDocumentUtil;
import fi.vm.sade.organisaatio.service.util.OrganisaatioUtil;
import java.io.IOException;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Organisaatioiden indeksointi solriin
 */

@Path("/indexer")
@Api(value = "/indexer", description = "Organisaatioiden indeksointi solriin")
@Component
public class IndexerResource {

    private static final Logger LOG = LoggerFactory.getLogger(IndexerResource.class);

    @Autowired(required = true)
    private OrganisaatioDAO organisaatioDAO;

    @Autowired
    protected OrganisaatioNimiDAO organisaatioNimiDAO;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private final SolrServer solr;

    // default constructor for CGLIB proxying
    // default constructor is called first and then public constructor is called with autowired dependencies
    // required to use the @PreAuthorize annotation on methods of this class
    public IndexerResource() {
        solr = null;
    }

    @Autowired
    public IndexerResource(SolrServerFactory factory) {
        this.solr = factory.getSolrServer();
    }

    /**
     * Indeksoi organisaatiot tietokannasta uudelleen Solriin.
     *
     * @param clean Tyhjennetäänkö indeksi ensin
     * @return
     */
    @GET
    @Path("/start")
    @ApiOperation(value = "Indeksoi organisaatiot tietokannasta uudelleen Solriin. Vain kehityskäyttöön.",
            notes = "Indeksoi organiasaatiot tietokannasta uudelleen Solriin. Vain kehityskäyttöön.",
            response = String.class)
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    @Produces(MediaType.TEXT_PLAIN)
    public String reBuildIndex(@QueryParam("clean") final boolean clean) {
        Preconditions.checkNotNull(organisaatioDAO, "need dao!");
        Preconditions.checkNotNull(transactionManager, "need TM!");

        // sigh... annotations, for some reason, did not work
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        int count = tt.execute(new TransactionCallback<Integer>() {
            @Override
            public Integer doInTransaction(TransactionStatus arg0) {

                List<Organisaatio> organisaatiot = organisaatioDAO.findAll();
                try {
                    if (clean) {
                        solr.deleteByQuery("*:*");
                    }
                } catch (SolrServerException | IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                index(organisaatiot);
                return organisaatiot.size();
            }
        });

        return Integer.toString(count);
    }

    public void delete(String organisaatioOid) {
        final List<String> organisaatioOids = Lists.newArrayList();

        organisaatioOids.add(organisaatioOid);

        deleteDocs(organisaatioOids);
    }


    public void delete(List<String> organisaatioOids) {
        deleteDocs(organisaatioOids);
    }

    public void index(Organisaatio organisaatio) {
        List<Organisaatio> organisaatiot = Lists.newArrayList();

        organisaatiot.add(organisaatio);

        index(organisaatiot);
    }

    public void index(List<Organisaatio> organisaatiot) {
        final List<SolrInputDocument> docs = Lists.newArrayList();
        final List<String> delete = Lists.newArrayList();

        for (Organisaatio org : organisaatiot) {
            // Ei indeksoida ryhmiä
            if (OrganisaatioUtil.isRyhma(org)) {
                continue;
            }
            if (org.isOrganisaatioPoistettu()) {
                delete.add(org.getOid());
            } else {
                docs.add(OrganisaatioToSolrInputDocumentUtil.apply(org));
            }
        }
        if (docs.size() > 0) {
            try {
                LOG.info("Indexing {} docs.", docs.size());
                LOG.info("Indexing following organisations {}.", docs.toString());
                solr.add(docs);
                LOG.info("Committing changes to index.", docs.size());
                solr.commit(true, true, false);
                LOG.info("Done.");
            } catch (SolrServerException | IOException e) {
                LOG.error("Indexing failed", e);
            }
        }
        deleteDocs(delete);
    }

    private void deleteDocs(final List<String> delete) {
        if (delete.size() > 0) {
            try {
                LOG.info("Deleting {} docs.", delete.size());
                solr.deleteById(delete);
                solr.commit(true, true, false);
                LOG.info("Committing changes to index.");
            } catch (SolrServerException | IOException e) {
                LOG.error("Deleting failed", e);
            }
        }
    }
}

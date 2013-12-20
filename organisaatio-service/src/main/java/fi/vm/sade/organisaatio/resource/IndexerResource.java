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

import java.io.IOException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import fi.vm.sade.organisaatio.dao.OrganisaatioDAOImpl;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.service.search.OrganisaatioToSolrInputDocumentFunction;
import fi.vm.sade.organisaatio.service.search.SolrServerFactory;

@Path("/indexer")
@Component
@Api(value = "/indexer", description = "Indeksoijan operaatiot")
public class IndexerResource {

    Logger logger = LoggerFactory.getLogger(IndexerResource.class);

    @Autowired(required = true)
    private OrganisaatioDAOImpl organisaatioDAOImpl;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private final SolrServer solr;

    final OrganisaatioToSolrInputDocumentFunction converter = new OrganisaatioToSolrInputDocumentFunction();

    @Autowired
    public IndexerResource(SolrServerFactory factory) {
        this.solr = factory.getSolrServer();
    }

    @GET
    @Path("/start")
    @Produces("text/plain")
    @ApiOperation(value = "Rakentaa indeksin uudelleen", notes = "Operaatio rakentaa indeksin uudelleen.", response = String.class)
    public String reBuildIndex(@ApiParam(value = "Tyhjennetäänkö indeksi ensin", required = true) @QueryParam("clean") final boolean clean) {
        Preconditions.checkNotNull(organisaatioDAOImpl, "need dao!");
        Preconditions.checkNotNull(transactionManager, "need TM!");

        // sigh... annotations, for some reason, did not work
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        int count = tt.execute(new TransactionCallback<Integer>() {
            @Override
            public Integer doInTransaction(TransactionStatus arg0) {

                List<Organisaatio> organisaatiot = organisaatioDAOImpl.findAll();
                try {
                    if (clean) {
                        solr.deleteByQuery("*:*");
                    }
                } catch (SolrServerException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                index(organisaatiot);
                return organisaatiot.size();
            }
        });

        return Integer.toString(count);
    }

    public void delete(List<String> organisaatioOids) {
        deleteDocs(organisaatioOids);
    }

    public void index(List<Organisaatio> organisaatiot) {
        final List<SolrInputDocument> docs = Lists.newArrayList();
        final List<String> delete = Lists.newArrayList();

        for (Organisaatio org : organisaatiot) {
            if (org.isOrganisaatioPoistettu()) {
                delete.add(org.getOid());
            } else {
                docs.add(converter.apply(org));
            }
        }
        if (docs.size() > 0) {
            try {
                logger.info("Indexing {} docs.", docs.size());
                solr.add(docs);
                logger.info("Committing changes to index.", docs.size());
                solr.commit(true, true, false);
                logger.info("Done.");
            } catch (SolrServerException e) {
                logger.error("Indexing failed", e);
            } catch (IOException e) {
                logger.error("Indexing failed", e);
            }
        }
        deleteDocs(delete);
    }

    private void deleteDocs(final List<String> delete) {
        if (delete.size() > 0) {
            try {
                logger.info("Deleting {} docs.", delete.size());
                solr.deleteById(delete);
                solr.commit(true, true, false);
                logger.info("Committing changes to index.");
            } catch (SolrServerException e) {
                logger.error("Deleting failed", e);
            } catch (IOException e) {
                logger.error("Deleting failed", e);
            }
        }
    }
}

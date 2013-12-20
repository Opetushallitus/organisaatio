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
package fi.vm.sade.organisaatio.service.search.it;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import fi.vm.sade.organisaatio.service.search.SolrServerFactory;

@Component
@Profile("embedded-solr")
public class EmbeddedSolrServerFactory extends SolrServerFactory {

    EmbeddedSolrServer server = null;

    // @Value("${foo}")
    private String SolrHome;

    // @Value("${foo}")
    private String SolrData;

    public SolrServer getSolrServer() {
        if (server == null) {
            System.setProperty("solr.solr.home", "src/main/resources/solr");
            System.setProperty("solr.data.dir", "target/solr-data");
            CoreContainer.Initializer initializer = new CoreContainer.Initializer();
            CoreContainer coreContainer = initializer.initialize();
            server = new EmbeddedSolrServer(coreContainer, "organisaatiot");
            System.clearProperty("solr.solr.home");
            System.clearProperty("solr.data.dir");
        }
        return server;
    }
}
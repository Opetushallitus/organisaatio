package fi.vm.sade.organisaatio.service.search;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.BinaryResponseParser;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

@Component
@Profile(value = {"default","solr"})
public class SolrServerFactory implements InitializingBean {

    @Value("${organisaatio.solr.url:organisaatio.solr.url_is_not_set}")
    protected String solrUrl;

    public SolrServer getSolrServer() {
        HttpSolrServer server = new HttpSolrServer(solrUrl);
        server.setRequestWriter(new BinaryRequestWriter());
        server.setParser(new BinaryResponseParser());
        return server;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Preconditions.checkNotNull(solrUrl,
                "Solr url not specified, application will not work!");
    }

}

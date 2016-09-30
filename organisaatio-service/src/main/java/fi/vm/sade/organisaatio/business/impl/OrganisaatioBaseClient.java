package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.config.UrlConfiguration;
import fi.vm.sade.organisaatio.service.filters.IDContextMessageHelper;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;

public abstract class OrganisaatioBaseClient {
    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    protected String ticket;

    private boolean reauthorize;

    @Autowired
    private UrlConfiguration urlConfiguration;

    protected abstract void authorize() throws Exception;

    /**
     * Configure authorization
     *
     * @param reauthorize If true, new ticket will be used for each request
     */
    public void setReauthorize(boolean reauthorize) {
        if (this.reauthorize != reauthorize) {
            this.ticket = null;
        }
        this.reauthorize = reauthorize;
    }

    protected void authorize(String serviceUrl, String clientUsername, String clientPassword)
            throws Exception {
        if (ticket != null && !reauthorize) {
            LOG.debug("Using existing ticket.");
            return;
        }
        if (clientPassword.isEmpty() || clientUsername.isEmpty()) {
            String err = "Failed to authorize for service because of missing username/password. Please set " +
                    "organisaatio.service.username.to.*" + " and " +
                    "organisaatio.service.password.to.* properties properly.";
            LOG.error(err);
            throw new Exception(err);
        } else {
            String serviceAccessUrl = urlConfiguration.getProperty("organisaatio-service.service-access.ticket");
            ArrayList<NameValuePair> postParameters = new ArrayList<>();
            HttpPost post = new HttpPost(serviceAccessUrl);
            post.addHeader("ID", IDContextMessageHelper.getIDChain());
            post.addHeader("clientSubSystemCode", IDContextMessageHelper.getClientSubSystemCode());
            postParameters.add(new BasicNameValuePair("client_id", clientUsername));
            postParameters.add(new BasicNameValuePair("client_secret", clientPassword));
            postParameters.add(new BasicNameValuePair("service_url", serviceUrl));
            try {
                post.setEntity(new UrlEncodedFormEntity(postParameters));
                HttpClient client = HttpClientBuilder.create().build();
                HttpResponse resp = client.execute(post);
                Header header = resp.getFirstHeader("ID");
                if(header != null) {
                    IDContextMessageHelper.setReceivedIDChain(header.getValue());
                }
                ticket = EntityUtils.toString(resp.getEntity()).trim();
                if (resp.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED || ticket.isEmpty()) {
                    LOG.error("Failed to get ticket from :" + serviceAccessUrl + ". Response code:"
                            + resp.getStatusLine().getStatusCode() + ", text:" + ticket);
                    throw new Exception("Invalid service-access response: " + resp.getStatusLine().getStatusCode());
                }
                LOG.debug("Got ticket: " + ticket);
            } catch (IOException e) {
                LOG.error("Failed to get ticket: " + e.getMessage());
                throw new Exception(e.getMessage());
            }
        }
    }
}

/*
 * Copyright (c) 2014 The Finnish Board of Education - Opetushallitus
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

import fi.vm.sade.organisaatio.business.exception.NotAuthorizedException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioKoodistoException;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Koodisto-servicen REST operaatiot ja autentikointi
 */
@Component
public class OrganisaatioKoodistoClient {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Value("${organisaatio-service.koodisto-service.rest.url}")
    private String koodistoServiceUrl;

    @Value("${organisaatio-service.service-access.url}")
    private String serviceAccessUrl;

    @Value("${organisaatio.service.username.to.koodisto}")
    private String clientUsername;

    @Value("${organisaatio.service.password.to.koodisto}")
    private String clientPassword;

    private String ticket;

    public OrganisaatioKoodistoClient() {
    }

    private void authorize() throws OrganisaatioKoodistoException {
        if (clientPassword.isEmpty() || clientUsername.isEmpty()) {
            String err = "Failed to authorize for koodisto-service because of missing username/password. Please set " +
                    "organisaatio.service.username.to.koodisto" + " and " +
                    "organisaatio.service.password.to.koodisto properties properly.";
            LOG.error(err);
            throw new OrganisaatioKoodistoException(err);
        } else {
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            HttpPost post = new HttpPost(serviceAccessUrl + "/accessTicket");
            postParameters.add(new BasicNameValuePair("client_id", clientUsername));
            postParameters.add(new BasicNameValuePair("client_secret", clientPassword));
            postParameters.add(new BasicNameValuePair("service_url", koodistoServiceUrl));
            try {
                post.setEntity(new UrlEncodedFormEntity(postParameters));
                HttpClient client = new DefaultHttpClient();
                HttpResponse resp = client.execute(post);
                ticket = EntityUtils.toString(resp.getEntity()).trim();
                if (resp.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED || ticket.isEmpty()) {
                    LOG.error("Failed to get ticket from :" + serviceAccessUrl + ". Response code:"
                            + resp.getStatusLine().getStatusCode() + ", text:" + ticket);
                    throw new OrganisaatioKoodistoException("Invalid service-access response: " + resp.getStatusLine().getStatusCode());
                }
                LOG.debug("Got ticket: " + ticket);
            } catch (IOException e) {
                LOG.error("Failed to get ticket: " + e.getMessage());
                throw new OrganisaatioKoodistoException(e.getMessage());
            }
        }
    }

    /**
     * Hae koodi URIn mukaan
     *
     * @param uri koodiston uri, esim.
     * "/rest/json/opetuspisteet/koodi/opetuspisteet_0106705"
     * @return koodi json-muodossa, tai null jos koodia ei löydy
     * @throws NotAuthorizedException Autorisointi epäonnistui
     * @throws OrganisaatioKoodistoException Koodistopalvelupyyntö epäonnistui
     */
    public String get(String uri) throws OrganisaatioKoodistoException {
        LOG.debug("GET " + koodistoServiceUrl + uri);
        authorize();
        String json = null;
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(koodistoServiceUrl + uri);
        get.addHeader("CasSecurityTicket", ticket);
        try {
            HttpResponse resp = client.execute(get);
            json = EntityUtils.toString(resp.getEntity(), "UTF-8");
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                // 500 => Koodia ei löydy
                LOG.debug("Code not found");
                return null;
            }
            if (resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                String err = "Invalid status code " + resp.getStatusLine().getStatusCode() + " from GET " + koodistoServiceUrl + uri;
                LOG.error(err);
                throw new OrganisaatioKoodistoException(err);
            }
        } catch (IOException e) {
            String err = "Failed to GET " + koodistoServiceUrl + uri + ": " + e.getMessage();
            LOG.error(err);
            throw new OrganisaatioKoodistoException(err);
        }
        return json;
    }

    /**
     * Päivittää koodin koodistoon
     *
     * @param json Päivitettävä koodi json-muodossa
     * @throws NotAuthorizedException Autorisointi epäonnistui
     * @throws OrganisaatioKoodistoException Koodistopalvelupyyntö epäonnistui
     */
    public void put(String json) throws OrganisaatioKoodistoException {
        HttpContext localContext = new BasicHttpContext();
        String uri = "/rest/codeelement";
        LOG.debug("PUT " + koodistoServiceUrl + uri);
        LOG.debug("PUT data=" + json);
        authorize();
        HttpClient client = new DefaultHttpClient();
        HttpPut put = new HttpPut(koodistoServiceUrl + uri);
        put.addHeader("CasSecurityTicket", ticket);
        put.addHeader("Content-Type", "application/json; charset=UTF-8");
        try {
            LOG.debug("NOW json   =" + json);
            put.setEntity(new StringEntity(json, "UTF-8"));
            HttpResponse resp = client.execute(put, localContext);
            if (resp.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
                String err = "Invalid status code " + resp.getStatusLine().getStatusCode() + " from PUT " + koodistoServiceUrl + uri;
                LOG.error(err);
                LOG.debug("Response body: " + EntityUtils.toString(resp.getEntity()));
                throw new OrganisaatioKoodistoException(err);
            } else {
                LOG.info("Code " + koodistoServiceUrl + uri + " succesfully updated: return code " + resp.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            String err = "Failed to PUT " + koodistoServiceUrl + uri + ": " + e.getMessage();
            LOG.error(err);
            throw new OrganisaatioKoodistoException(err);
        }
    }

    /**
     * Lisää koodin koodistoon
     *
     * @param json Lisättävä koodi json-muodossa
     * @param uri Lisättävän koodin uri, esim. 'opetuspisteet'
     * @throws NotAuthorizedException Autorisointi epäonnistui
     * @throws OrganisaatioKoodistoException Koodistopalvelupyyntö epäonnistui
     */
    public void post(String json, String uri) throws OrganisaatioKoodistoException {
        // TODO
        String path = "/rest/codeelement/" + uri;
        LOG.debug("POST " + koodistoServiceUrl + path);
        LOG.debug("POST data=" + json);
        authorize();
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(koodistoServiceUrl + path);
        post.addHeader("CasSecurityTicket", ticket);
        post.addHeader("Content-Type", "application/json; charset=UTF-8");
        try {
            post.setEntity(new StringEntity(json, "UTF-8"));
            HttpResponse resp = client.execute(post);
            if (resp.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
                String err = "Invalid status code " + resp.getStatusLine().getStatusCode() + " from POST " + koodistoServiceUrl + path;
                LOG.error(err);
                LOG.debug("Response body: " + EntityUtils.toString(resp.getEntity()));
                throw new OrganisaatioKoodistoException(err);
            } else {
                LOG.info("Code " + koodistoServiceUrl + path + " succesfully updated: return code " + resp.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            String err = "Failed to POST " + koodistoServiceUrl + path + ": " + e.getMessage();
            LOG.error(err);
            throw new OrganisaatioKoodistoException(err);
        }
    }
}

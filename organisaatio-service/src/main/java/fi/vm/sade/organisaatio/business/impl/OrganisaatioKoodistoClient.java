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
import fi.vm.sade.organisaatio.config.UrlConfiguration;
import fi.vm.sade.organisaatio.service.filters.IDContextMessageHelper;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

/**
 * Koodisto-servicen REST operaatiot ja autentikointi
 */
@Component
public class OrganisaatioKoodistoClient extends OrganisaatioBaseClient {

    @Value("${organisaatio.service.username.to.koodisto}")
    private String koodistoClientUsername;

    @Value("${organisaatio.service.password.to.koodisto}")
    private String koodistoClientPassword;

    @Autowired
    private UrlConfiguration urlConfiguration;

    protected void authorize(final String csrfCookie) throws OrganisaatioKoodistoException {
        try {
            String koodistoServiceUrl = urlConfiguration.getProperty("organisaatio-service.koodisto-service.rest.url");
            LOG.info("authorize url " + koodistoServiceUrl);
            authorize(koodistoServiceUrl, koodistoClientUsername, koodistoClientPassword, csrfCookie);
        } catch (Exception e) {
            throw new OrganisaatioKoodistoException(e.getMessage());
        }
    }


    private String createKoodistoServiceParameters() {
        // Estetään cachen käyttö
        return "?noCache=" + new Date().getTime();
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
        String koodistoServiceUrl = urlConfiguration.getProperty("organisaatio-service.koodisto-service.rest.url");
        String url = koodistoServiceUrl + uri + createKoodistoServiceParameters();
        LOG.debug("GET " + url);
        LOG.info("GET " + url);
        String json = null;
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(url);
        get.addHeader("ID", IDContextMessageHelper.getIDChain());
        get.addHeader("clientSubSystemCode", IDContextMessageHelper.getClientSubSystemCode());
        try {
            HttpResponse resp = client.execute(get);
            Header header = resp.getFirstHeader("ID");
            if(header != null) {
                IDContextMessageHelper.setReceivedIDChain(header.getValue());
            }
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
            String err = "Failed to GET " + url + ": " + e.getMessage();
            LOG.error(err);
            throw new OrganisaatioKoodistoException(err);
        }
        return json;
    }

    /**
     * Päivittää koodin koodistoon
     *
     * @param json Päivitettävä koodi json-muodossa
     * @param csrfCookie CSRF-keksin arvo
     * @throws NotAuthorizedException Autorisointi epäonnistui
     * @throws OrganisaatioKoodistoException Koodistopalvelupyyntö epäonnistui
     */
    public void put(String json, final String csrfCookie) throws OrganisaatioKoodistoException {
        HttpContext localContext = new BasicHttpContext();
        String uri = urlConfiguration.getProperty("organisaatio-service.koodisto-service.rest.codeelement", "save");

        LOG.debug("PUT " + uri);
        LOG.info("PUT " + uri);
        LOG.debug("PUT data=" + json);
        authorize(csrfCookie);
        HttpClient client = HttpClientBuilder.create().build();
        HttpPut put = new HttpPut(uri);
        put.addHeader("ID", IDContextMessageHelper.getIDChain());
        put.addHeader("clientSubSystemCode", IDContextMessageHelper.getClientSubSystemCode());
        put.addHeader("CasSecurityTicket", ticket);
        put.addHeader("Content-Type", "application/json; charset=UTF-8");
        try {
            LOG.debug("NOW json   =" + json);
            put.setEntity(new StringEntity(json, "UTF-8"));
            HttpResponse resp = client.execute(put, localContext);
            Header header = resp.getFirstHeader("ID");
            if(header != null) {
                IDContextMessageHelper.setReceivedIDChain(header.getValue());
            }
            if (resp.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
                String err = "Invalid status code " + resp.getStatusLine().getStatusCode() + " from PUT " + uri;
                LOG.error(err);
                LOG.debug("Response body: " + EntityUtils.toString(resp.getEntity()));
                throw new OrganisaatioKoodistoException(err);
            } else {
                LOG.info("Code " + uri + " succesfully updated: return code " + resp.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            String err = "Failed to PUT " + uri + ": " + e.getMessage();
            LOG.error(err);
            throw new OrganisaatioKoodistoException(err);
        }
    }

    /**
     * Lisää koodin koodistoon
     *
     * @param json Lisättävä koodi json-muodossa
     * @param uri Lisättävän koodin uri, esim. 'opetuspisteet'
     * @param csrfCookie CSRF-keksin arvo
     * @throws NotAuthorizedException Autorisointi epäonnistui
     * @throws OrganisaatioKoodistoException Koodistopalvelupyyntö epäonnistui
     */
    public void post(String json, String uri, final String csrfCookie) throws OrganisaatioKoodistoException {
        String url = urlConfiguration.getProperty("organisaatio-service.koodisto-service.rest.codeelement", uri);

        LOG.debug("POST " + url);
        LOG.info("POST " + url);
        LOG.debug("POST data=" + json);
        authorize(csrfCookie);
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);
        post.addHeader("ID", IDContextMessageHelper.getIDChain());
        post.addHeader("clientSubSystemCode", IDContextMessageHelper.getClientSubSystemCode());
        post.addHeader("CasSecurityTicket", ticket);
        post.addHeader("Content-Type", "application/json; charset=UTF-8");
        try {
            post.setEntity(new StringEntity(json, "UTF-8"));
            HttpResponse resp = client.execute(post);
            Header header = resp.getFirstHeader("ID");
            if(header != null) {
                IDContextMessageHelper.setReceivedIDChain(header.getValue());
            }
            if (resp.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
                String err = "Invalid status code " + resp.getStatusLine().getStatusCode() + " from POST " + url;
                LOG.error(err);
                LOG.debug("Response body: " + EntityUtils.toString(resp.getEntity()));
                throw new OrganisaatioKoodistoException(err);
            } else {
                LOG.info("Code " + url + " succesfully updated: return code " + resp.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            String err = "Failed to POST " + url + ": " + e.getMessage();
            LOG.error(err);
            throw new OrganisaatioKoodistoException(err);
        }
    }
}

/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
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

import java.io.IOException;
import java.lang.Exception;

import fi.vm.sade.organisaatio.business.exception.OrganisaatioViestintaException;
import fi.vm.sade.organisaatio.service.filters.IDContextMessageHelper;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class OrganisaatioViestintaClient extends OrganisaatioBaseClient {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Value("${organisaatio-service.ryhmasahkoposti-service.rest.url}")
    protected String viestintaServiceUrl;

    @Value("${organisaatio.service.username.to.viestinta}")
    private String viestintaClientUsername;

    @Value("${organisaatio.service.password.to.viestinta}")
    private String viestintaClientPassword;

    @PostConstruct
    public void init() {
        setUp(viestintaServiceUrl, viestintaClientUsername, viestintaClientPassword);
    }

    @Override
    protected void authorize() throws OrganisaatioViestintaException {
        try {
            authorize(viestintaServiceUrl, viestintaClientUsername, viestintaClientPassword);
        } catch (Exception e) {
            throw new OrganisaatioViestintaException(e.getMessage());
        }
    }

    public void post(String json, String uri) throws OrganisaatioViestintaException {
        String path = "/email" + uri;
        LOG.debug("POST " + viestintaServiceUrl + path);
        LOG.debug("POST data=" + json);
        authorize();
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(viestintaServiceUrl + path);
        post.addHeader("ID", IDContextMessageHelper.getIDChain());
        post.addHeader("clientSubSystemCode", IDContextMessageHelper.getClientSubSystemCode());
        post.addHeader("CasSecurityTicket", ticket);
        post.addHeader("Content-Type", "application/json; charset=UTF-8");
        try {
            post.setEntity(new StringEntity(json, "UTF-8"));
            HttpResponse resp = client.execute(post);
            Header idHeader = resp.getFirstHeader("ID");
            if(idHeader != null) {
                IDContextMessageHelper.setReceivedIDChain(idHeader.getValue());
            }
            if (resp.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
                String err = "Invalid status code " + resp.getStatusLine().getStatusCode() + " from POST "
                        + viestintaServiceUrl + path;
                LOG.error(err);
                LOG.debug("Response body: " + EntityUtils.toString(resp.getEntity()));
                throw new OrganisaatioViestintaException(err);
            } else {
                LOG.info("Code " + viestintaServiceUrl + path + " succesfully posted: return code "
                        + resp.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            String err = "Failed to POST " + viestintaServiceUrl + path + ": " + e.getMessage();
            LOG.error(err);
            throw new OrganisaatioViestintaException(err);
        }
    }
}

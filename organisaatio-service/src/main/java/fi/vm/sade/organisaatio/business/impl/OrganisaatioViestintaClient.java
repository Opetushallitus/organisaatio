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
import fi.vm.sade.organisaatio.config.UrlConfiguration;
import fi.vm.sade.organisaatio.service.filters.IDContextMessageHelper;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static fi.vm.sade.organisaatio.service.filters.IDContextMessageHelper.CSRF_HEADER_NAME;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import static java.util.stream.Collectors.joining;


@Component
public class OrganisaatioViestintaClient extends OrganisaatioBaseClient {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private UrlConfiguration urlConfiguration;

    @Value("${organisaatio.service.username.to.viestinta}")
    private String viestintaClientUsername;

    @Value("${organisaatio.service.password.to.viestinta}")
    private String viestintaClientPassword;

    protected void authorize() throws OrganisaatioViestintaException {
        try {
            String viestintaServiceUrl = urlConfiguration.getProperty("organisaatio-service.ryhmasahkoposti-service.rest.url");
            authorize(viestintaServiceUrl, viestintaClientUsername, viestintaClientPassword);
        } catch (Exception e) {
            throw new OrganisaatioViestintaException(e.getMessage());
        }
    }

    public String post(String json, String uri) throws OrganisaatioViestintaException {
        return post(json, uri, true);
    }

    public String post(String json, String uri, boolean sanitize) {
        String viestintaServiceUrl = urlConfiguration.getProperty("organisaatio-service.ryhmasahkoposti-service.rest.mail", uri, sanitize);
        LOG.debug("POST " + viestintaServiceUrl );
        LOG.debug("POST data=" + json);
        authorize();

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(viestintaServiceUrl );
        post.addHeader("ID", IDContextMessageHelper.getIDChain());
        post.addHeader("clientSubSystemCode", IDContextMessageHelper.getClientSubSystemCode());
        post.addHeader(CSRF_HEADER_NAME, IDContextMessageHelper.getCsrfHeader());
        post.addHeader("CasSecurityTicket", ticket);
        post.addHeader("Content-Type", "application/json; charset=UTF-8");
        try {
            post.setEntity(new StringEntity(json, "UTF-8"));
            HttpResponse resp = client.execute(post);
            Header idHeader = resp.getFirstHeader("ID");
            if (idHeader != null) {
                IDContextMessageHelper.setReceivedIDChain(idHeader.getValue());
            }
            Header csrfHeader = resp.getFirstHeader(CSRF_HEADER_NAME);
            if (csrfHeader != null) {
                IDContextMessageHelper.setCsrfHeader(csrfHeader.getValue());
            }
            if (resp.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
                String err = "Invalid status code " + resp.getStatusLine().getStatusCode() + " from POST "
                        + viestintaServiceUrl;
                LOG.error(err);
                LOG.debug("Response body: " + EntityUtils.toString(resp.getEntity()));
                throw new OrganisaatioViestintaException(err);
            } else {
                LOG.info("Code " + viestintaServiceUrl  + " succesfully posted: return code "
                        + resp.getStatusLine().getStatusCode());
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()))) {
                    return reader.lines().collect(joining("\n"));
                }
            }
        } catch (IOException e) {
            String err = "Failed to POST " + viestintaServiceUrl  + ": " + e.getMessage();
            LOG.error(err);
            throw new OrganisaatioViestintaException(err);
        }
    }
}

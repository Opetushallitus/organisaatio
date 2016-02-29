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

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import fi.vm.sade.generic.rest.CachingRestClient;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import fi.vm.sade.organisaatio.service.filters.IDContextMessageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Käyttää CachingRestClientia.
 *
 * @author simok
 */
@Component
public class OrganisaatioRestToStream {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    // NOTE! cachingRestClient is static because we need application-scoped rest cache for organisaatio-service
    private static final CachingRestClient cachingRestClient = new CachingRestClient();

    public JsonElement getInputStreamFromUri(String uri) {
        JsonElement json;

        try {
            long t0 = System.currentTimeMillis();
            // Set "caller-id" (clientSubSystemCode) and ID chain to be sent on header.
            cachingRestClient.setCallerId(IDContextMessageHelper.getClientSubSystemCode());
            cachingRestClient.setID(IDContextMessageHelper.getIDChain());
            InputStream inputStream = cachingRestClient.get(uri);
            // Get the ID chain from received message and set it to cxf exchange.
            Reader reader = new InputStreamReader(inputStream);
            json = new JsonParser().parse(reader);
            JsonElement ID = json.getAsJsonObject().get("ID");
            if(ID != null) {
                IDContextMessageHelper.setReceivedIDChain(ID.getAsString());
            }
            LOG.debug("rest get done, uri: {}, took: {} ms, cacheStatus: {}",
                    new Object[] {uri, (System.currentTimeMillis() - t0), cachingRestClient.getCacheStatus()});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return json;
    }
 }

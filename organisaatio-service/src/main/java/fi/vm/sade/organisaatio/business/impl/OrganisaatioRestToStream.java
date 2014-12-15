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

import fi.vm.sade.generic.rest.CachingRestClient;
import java.io.IOException;
import java.io.InputStream;
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

    public InputStream getInputStreamFromUri(String uri) {
        InputStream inputStream;

        try {
            long t0 = System.currentTimeMillis();
            inputStream = cachingRestClient.get(uri);
            LOG.debug("rest get done, uri: {}, took: {} ms, cacheStatus: {}",
                    new Object[] {uri, (System.currentTimeMillis() - t0), cachingRestClient.getCacheStatus()});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return inputStream;
    }
 }

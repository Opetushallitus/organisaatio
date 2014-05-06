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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.vm.sade.generic.common.EnhancedProperties;
import fi.vm.sade.generic.rest.CachingRestClient;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioTarjontaException;
import fi.vm.sade.tarjonta.service.resources.v1.dto.HakutuloksetV1RDTO;
import fi.vm.sade.tarjonta.service.resources.v1.dto.KoulutusHakutulosV1RDTO;
import fi.vm.sade.tarjonta.service.resources.v1.dto.ResultV1RDTO;
import fi.vm.sade.tarjonta.service.resources.v1.dto.ResultV1RDTO.ResultStatus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author simok
 */
public class OrganisaatioKoulutukset {
    
    private final Logger LOG = LoggerFactory.getLogger(getClass());
    
    // NOTE! cachingRestClient is static because we need application-scoped rest cache for organisaatio-service
    private static final CachingRestClient cachingRestClient = new CachingRestClient();
    private String tarjontaServiceWebappUrl;
    
    public OrganisaatioKoulutukset(String tarjontaServiceWebappUrl) {
        this.tarjontaServiceWebappUrl = tarjontaServiceWebappUrl;
    }
    
    public OrganisaatioKoulutukset() {
        // read tarjonta-service url from common.properties
        FileInputStream fis = null;
        try {
            Properties props = new EnhancedProperties();
            fis = new FileInputStream(new File(System.getProperty("user.home"), "oph-configuration/common.properties"));
            props.load(fis);
            this.tarjontaServiceWebappUrl = props.getProperty("cas.service.tarjonta-service");
        } catch (IOException e) {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ioe) {
                }
            }
            throw new RuntimeException("failed to read common.properties", e);
        }
    }
    
    private <T> T get(String uri, Class<? extends T> resultClass) {
        try {
            long t0 = System.currentTimeMillis();
            T result = cachingRestClient.get(tarjontaServiceWebappUrl + "/rest/v1" + uri, resultClass);
            LOG.debug("tarjonta rest get done, uri: {}, took: {} ms, cacheStatus: {} result: {}", 
                    new Object[] { uri, (System.currentTimeMillis() - t0), cachingRestClient.getCacheStatus(), result });
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private String buildSearchKoulutusUri(String oid) {
        return "/koulutus/search?" + "organisationOid=" + oid;
    }
    
    public boolean haeKoulutukset(String oid) {
        LOG.debug("Haetaan koulutuksia oidille: " + oid);
        
        ResultV1RDTO<HakutuloksetV1RDTO<KoulutusHakutulosV1RDTO>> hakuTulokset;

        ResultV1RDTO<HakutuloksetV1RDTO<KoulutusHakutulosV1RDTO>> classInstance = new ResultV1RDTO<HakutuloksetV1RDTO<KoulutusHakutulosV1RDTO>>();
                
        hakuTulokset = get(buildSearchKoulutusUri(oid), classInstance.getClass());
        
        // Tarkistetaan hakutulokset
        if (hakuTulokset.getStatus() != ResultStatus.OK) {
            LOG.warn("Search failed for koulutus with organization oid: " + oid + " status: " + hakuTulokset.getStatus());
            throw new OrganisaatioTarjontaException();
        }
        
        return true;
    }
}

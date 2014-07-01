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

import fi.vm.sade.generic.common.EnhancedProperties;
import fi.vm.sade.generic.rest.CachingRestClient;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioTarjontaException;
import fi.vm.sade.tarjonta.service.resources.v1.dto.HakutuloksetV1RDTO;
import fi.vm.sade.tarjonta.service.resources.v1.dto.KoulutusHakutulosV1RDTO;
import fi.vm.sade.tarjonta.service.resources.v1.dto.ResultV1RDTO;
import fi.vm.sade.tarjonta.service.resources.v1.dto.ResultV1RDTO.ResultStatus;
import fi.vm.sade.tarjonta.service.resources.v1.dto.TarjoajaHakutulosV1RDTO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private List<KoulutusHakutulosV1RDTO> haeKoulutukset(String oid) {
        LOG.debug("Haetaan koulutuksia oidille: " + oid);

        ResultV1RDTO<HakutuloksetV1RDTO<KoulutusHakutulosV1RDTO>> hakuTulokset;
        ResultV1RDTO<HakutuloksetV1RDTO<KoulutusHakutulosV1RDTO>> classInstance = new ResultV1RDTO<HakutuloksetV1RDTO<KoulutusHakutulosV1RDTO>>();


        hakuTulokset = get(buildSearchKoulutusUri(oid), classInstance.getClass());

        // Tarkistetaan hakutulokset
        if (hakuTulokset.getStatus() != ResultStatus.OK) {
            LOG.warn("Search failed for koulutus with organization oid: " + oid + " status: " + hakuTulokset.getStatus());
            throw new OrganisaatioTarjontaException();
        }

        LOG.debug("Hakutulokset: "+ hakuTulokset.toString());

        if (hakuTulokset.getResult() == null) {
            LOG.debug("Hakutulosten result NULL!");
            return Collections.EMPTY_LIST;
        }

        List<TarjoajaHakutulosV1RDTO<KoulutusHakutulosV1RDTO>> tarjoajienKoulutukset = hakuTulokset.getResult().getTulokset();
        List<KoulutusHakutulosV1RDTO> koulutukset = new ArrayList<KoulutusHakutulosV1RDTO>();

        // Ei löytynyt koulutuksia
        if (hakuTulokset.getResult().getTuloksia() == 0 || tarjoajienKoulutukset == null) {
            LOG.debug("Ei koulutuksia organisaatiolle: " + oid);
            return Collections.EMPTY_LIST;
        }

        // Kerätään tuloksista annetun oid:n määrittämän tarjoajan koulutukset
        for (TarjoajaHakutulosV1RDTO<KoulutusHakutulosV1RDTO> tarjoajanKoulutukset: tarjoajienKoulutukset) {
            // Ei pitäisi olla muita tarjoajia listalla, mutta tarkistetaan silti
            if (tarjoajanKoulutukset.getOid().equals(oid)) {
		koulutukset.addAll(tarjoajanKoulutukset.getTulokset());
            }
	}

        return koulutukset;
    }

    public boolean alkaviaKoulutuksia(String oid, Date after) {
        List<KoulutusHakutulosV1RDTO> koulutukset  = haeKoulutukset(oid);

        // Ei alkavia koulutuksia
        if (koulutukset.isEmpty()) {
            return false;
        }

        // Tarkistetaan onko alkavia koulutuksia annetun päivämäärän jälkeen
        for (KoulutusHakutulosV1RDTO koulutus : koulutukset) {
            if (koulutus.getKoulutuksenAlkamisPvmMax().after(after)) {
                LOG.debug("After: " + koulutus.getNimi().get("fi"));
		return true;
            }
            else {
                LOG.debug("Before: " + koulutus.getNimi().get("fi"));
            }
	}

        // Ei alkavia koulutuksia annetun päivämäärän jälkeen
        return false;
    }
 }

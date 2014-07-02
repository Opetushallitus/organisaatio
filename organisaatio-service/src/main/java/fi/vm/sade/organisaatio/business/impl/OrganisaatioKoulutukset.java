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

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import fi.vm.sade.generic.rest.CachingRestClient;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioTarjontaException;
import fi.vm.sade.tarjonta.service.resources.v1.dto.KoulutusHakutulosV1RDTO;
import fi.vm.sade.tarjonta.service.resources.v1.dto.ResultV1RDTO.ResultStatus;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author simok
 */
@Component
public class OrganisaatioKoulutukset {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    // NOTE! cachingRestClient is static because we need application-scoped rest cache for organisaatio-service
    private static final CachingRestClient cachingRestClient = new CachingRestClient();

    @Value("${organisaatio-service.tarjonta-service.rest.url}")
    private String tarjontaServiceWebappUrl;

    private Gson gson;

    public OrganisaatioKoulutukset() {
        initGson();
    }

    private void initGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();

        // Jätetään oid pois sillä KoulutusHakutulosV1RDTO sisältää kaksi oid kenttää
        gsonBuilder.setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                return "oid".equals(fieldAttributes.getName());
            }
            @Override
            public boolean shouldSkipClass(Class<?> arg0) {
                return false;
            }
        });

        // Rekisteröidään adapteri, jolla hoidetaan date tyyppi long arvona
        gsonBuilder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            @Override
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return new Date(json.getAsJsonPrimitive().getAsLong());
            }
        });

        gson = gsonBuilder.create();
    }

    private String buildSearchKoulutusUri(String oid) {
        return "/koulutus/search?" + "organisationOid=" + oid;
    }

    private List<KoulutusHakutulosV1RDTO> getOrganisaatioKoulutukset(JsonElement organisaatioTulos) {
        List<KoulutusHakutulosV1RDTO> koulutukset = new ArrayList<KoulutusHakutulosV1RDTO>();

        JsonElement koulutusTulokset = organisaatioTulos.getAsJsonObject().get("tulokset");

        // Tarkistetaan, että tuloksia löytyy!
        if (koulutusTulokset.isJsonNull()) {
            LOG.warn("Search failed for koulutus! --> koulutus tulokset == NULL");
            return Collections.EMPTY_LIST;
        }

        // Käydään läpi koulutukset ja deserialisoidaan
        JsonArray koulutusTuloksetArray = koulutusTulokset.getAsJsonArray();
        for (JsonElement koulutusTulos : koulutusTuloksetArray){
            KoulutusHakutulosV1RDTO hakuTulos = gson.fromJson(koulutusTulos, KoulutusHakutulosV1RDTO.class);

            koulutukset.add(hakuTulos);
        }

        return koulutukset;
    }

    private List<KoulutusHakutulosV1RDTO> haeKoulutukset(String oid) {
        List<KoulutusHakutulosV1RDTO> koulutukset = new ArrayList<KoulutusHakutulosV1RDTO>();
        InputStream jsonStream;

        try {
            long t0 = System.currentTimeMillis();
            jsonStream = cachingRestClient.get(tarjontaServiceWebappUrl + "/v1" + buildSearchKoulutusUri(oid));
            LOG.debug("tarjonta rest get done, uri: {}, took: {} ms, cacheStatus: {}",
                    new Object[] {buildSearchKoulutusUri(oid), (System.currentTimeMillis() - t0), cachingRestClient.getCacheStatus()});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Reader reader = new InputStreamReader(jsonStream);
        JsonElement json = new JsonParser().parse(reader);

        // Tarkistetaan hakutulokset
        if (json.getAsJsonObject().get("status").isJsonNull()) {
            LOG.warn("Search failed for koulutus with organization oid: " + oid);
            throw new OrganisaatioTarjontaException();
        }
        ResultStatus status = gson.fromJson(json.getAsJsonObject().get("status"), new TypeToken<ResultStatus>(){}.getType());
        if (status != ResultStatus.OK) {
            LOG.warn("Search failed for koulutus with organization oid: " + oid + " status: " + status);
            throw new OrganisaatioTarjontaException();
        }

        // Otetaan ensimmäinen taso "result"
        JsonElement result = json.getAsJsonObject().get("result");
        if (result.isJsonNull()) {
            LOG.warn("Search failed for koulutus with organization oid: " + oid + " --> result == NULL");
            return Collections.EMPTY_LIST;
        }

        // Otetaan organisaatiolista (sisältää listata organisaatioiden hakutuloksista)
        JsonElement organisaatioTulokset = result.getAsJsonObject().get("tulokset");
        if (organisaatioTulokset.isJsonNull()) {
            LOG.warn("Search failed for koulutus with organization oid: " + oid + " --> tulokset == NULL");
            return Collections.EMPTY_LIST;
        }

        // Käydään läpi organisaatioiden koulutukset ja lisätään listalle
        JsonArray organisaatioTuloksetArray = organisaatioTulokset.getAsJsonArray();
        for (JsonElement organisaatioTulos : organisaatioTuloksetArray){
            koulutukset.addAll(getOrganisaatioKoulutukset(organisaatioTulos));
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
		return true;
            }
	}

        // Ei alkavia koulutuksia annetun päivämäärän jälkeen
        return false;
    }
 }

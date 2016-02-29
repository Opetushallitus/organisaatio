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
import fi.vm.sade.organisaatio.business.exception.OrganisaatioTarjontaException;
import fi.vm.sade.tarjonta.service.resources.v1.dto.HakukohdeHakutulosV1RDTO;
import fi.vm.sade.tarjonta.service.resources.v1.dto.KoulutusHakutulosV1RDTO;
import fi.vm.sade.tarjonta.service.resources.v1.dto.ResultV1RDTO.ResultStatus;
import fi.vm.sade.tarjonta.shared.types.TarjontaTila;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.*;

import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Käyttää tarjonta palvelua selvittääkseen organisaation tulevia koulutuksia
 * ja hakukohteita.
 *
 * @author simok
 */
@Component
public class OrganisaatioTarjonta {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrganisaatioRestToStream restToStream;

    @Value("${organisaatio-service.tarjonta-service.rest.url}")
    private String tarjontaServiceWebappUrl;

    private Gson gson;

    public OrganisaatioTarjonta() {
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
        List<KoulutusHakutulosV1RDTO> koulutukset = new ArrayList<>();

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
        List<KoulutusHakutulosV1RDTO> koulutukset = new ArrayList<>();
        JsonElement json;

        String url = tarjontaServiceWebappUrl + "/v1" + buildSearchKoulutusUri(oid);

        try {
            json = restToStream.getInputStreamFromUri(url);
        }
        catch (RuntimeException e) {
            LOG.warn("Search failed for koulutus with organization oid: " + oid);
            throw new OrganisaatioTarjontaException();
        }

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

    /**
     * Tarkistaa onko annetulla organisaatiolla alkavia koulutuksia tämän
     * päivämäärän jälkeen.
     * HUOM! Vain "KOPIOITU", "VALMIS", "LUONNOS" ja "JULKAISTU" tilaiset
     * alkavat koulutukset huomioidaan.
     *
     * @param oid
     * @return Boolean, joka kertoo onko alkavia koulutuksia vai ei.
     */
    public boolean alkaviaKoulutuksia(String oid) {
        return alkaviaKoulutuksia(oid, new Date());
    }

    /**
     * Tarkistaa onko annetulla organisaatiolla alkavia koulutuksia annetun
     * päivämäärän jälkeen.
     * HUOM! Vain "KOPIOITU", "VALMIS", "LUONNOS" ja "JULKAISTU" tilaiset
     * alkavat koulutukset huomioidaan.
     *
     * @param oid
     * @param after
     * @return Boolean, joka kertoo onko alkavia koulutuksia vai ei.
     */
    public boolean alkaviaKoulutuksia(String oid, Date after) {
        List<KoulutusHakutulosV1RDTO> koulutukset  = haeKoulutukset(oid);

        // Ei alkavia koulutuksia tai after == null
        if (koulutukset.isEmpty() || after == null) {
            return false;
        }

        // Tarkistetaan onko alkavia koulutuksia annetun päivämäärän jälkeen
        for (KoulutusHakutulosV1RDTO koulutus : koulutukset) {
            Date koulutuksenAlkamisPvmMax = koulutus.getKoulutuksenAlkamisPvmMax();

            if (koulutuksenAlkamisPvmMax == null) {
                LOG.info("Missing 'kausi' (koulutuksenAlkamisPvmMax == null) for koulutus: " + koulutus.getOid());
                Integer vuosi = koulutus.getVuosi();

                if(vuosi == null) {
                    // Koulutukselta puuttuu kausi ja vuosi --> "valmistava hakukohde"
                    // eli se on sidottu johonkin toiseen koulutukseen. Ei estä esim.
                    // organisaation lakkauttamista.
                    LOG.info("Missing 'kausi' and 'vuosi' for koulutus: " + koulutus.getOid());
                    continue;
                } else {
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.YEAR, vuosi + 1);
                    cal.set(Calendar.MONTH, Calendar.JANUARY);
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);

                    cal.add(Calendar.MILLISECOND, -1);

                    koulutuksenAlkamisPvmMax = cal.getTime();
                    LOG.debug("oli vuosi, käytetään aikaa " + koulutuksenAlkamisPvmMax);
                }
            }

            if (koulutuksenAlkamisPvmMax.after(after) &&
                    (koulutus.getTila() == TarjontaTila.JULKAISTU ||
                    koulutus.getTila() == TarjontaTila.KOPIOITU ||
                    koulutus.getTila() == TarjontaTila.VALMIS ||
                    koulutus.getTila() == TarjontaTila.LUONNOS)) {
                return true;
            }
        }

        // Ei alkavia koulutuksia annetun päivämäärän jälkeen
        return false;
    }

    /**
     * Tarkistaa onko annettuun rymään liitetty hakukohteita.
     *
     * @param ryhmaOid
     * @return Boolean, joka kertoo onko rygmälle hakukohteita vai ei.
     */
    public boolean hakukohteita(String ryhmaOid) {
        List<HakukohdeHakutulosV1RDTO> hakukohteet  = haeHakukohteet(ryhmaOid);

        // Ei hakukohteita
        if (hakukohteet.isEmpty()) {
            return false;
        }

        // Ryhmään on liitetty hakukohteita
        return true;
    }

    private String buildSearchHakukohteetUri(String ryhmaOid) {
        return "/hakukohde/search?" + "organisaatioRyhmaOid=" + ryhmaOid;
    }

    private List<HakukohdeHakutulosV1RDTO> haeHakukohteet(String ryhmaOid) {
        List<HakukohdeHakutulosV1RDTO> hakukohteet = new ArrayList<>();
        JsonElement json;

        String url = tarjontaServiceWebappUrl + "/v1" + buildSearchHakukohteetUri(ryhmaOid);

        try {
            json = restToStream.getInputStreamFromUri(url);
        }
        catch (RuntimeException e) {
            LOG.warn("Search failed for hakukohteet with group oid: " + ryhmaOid);
            throw new OrganisaatioTarjontaException();
        }

        // Tarkistetaan hakutulokset
        if (json.getAsJsonObject().get("status").isJsonNull()) {
            LOG.warn("Search failed for hakukohteet with organization oid: " + ryhmaOid);
            throw new OrganisaatioTarjontaException();
        }
        ResultStatus status = gson.fromJson(json.getAsJsonObject().get("status"), new TypeToken<ResultStatus>(){}.getType());
        if (status != ResultStatus.OK) {
            LOG.warn("Search failed for hakukohteet with organization oid: " + ryhmaOid + " status: " + status);
            throw new OrganisaatioTarjontaException();
        }

        // Otetaan ensimmäinen taso "result"
        JsonElement result = json.getAsJsonObject().get("result");
        if (result.isJsonNull()) {
            LOG.warn("Search failed for hakukohteet with organization oid: " + ryhmaOid + " --> result == NULL");
            return Collections.EMPTY_LIST;
        }

        // Otetaan organisaatiolista (sisältää listan organisaatioiden hakukohteista)
        JsonElement organisaatioTulokset = result.getAsJsonObject().get("tulokset");
        if (organisaatioTulokset.isJsonNull()) {
            LOG.warn("Search failed for hakukohteet with organization oid: " + ryhmaOid + " --> tulokset == NULL");
            return Collections.EMPTY_LIST;
        }

        // Käydään läpi organisaatioiden hakukohteet ja lisätään listalle
        JsonArray organisaatioTuloksetArray = organisaatioTulokset.getAsJsonArray();
        for (JsonElement organisaatioTulos : organisaatioTuloksetArray){
            hakukohteet.addAll(getOrganisaatioHakukohteet(organisaatioTulos));
        }

        return hakukohteet;
    }

    private List<HakukohdeHakutulosV1RDTO> getOrganisaatioHakukohteet(JsonElement organisaatioTulos) {
        List<HakukohdeHakutulosV1RDTO> hakukohteet = new ArrayList<>();

        JsonElement hakukohdeTulokset = organisaatioTulos.getAsJsonObject().get("tulokset");

        // Tarkistetaan, että tuloksia löytyy!
        if (hakukohdeTulokset.isJsonNull()) {
            LOG.warn("Search failed for hakukohde! --> hakukohde tulokset == NULL");
            return Collections.EMPTY_LIST;
        }

        // Käydään läpi hakukohteet ja deserialisoidaan
        JsonArray hakukohdeTuloksetArray = hakukohdeTulokset.getAsJsonArray();
        for (JsonElement hakukohdeTulos : hakukohdeTuloksetArray){
            HakukohdeHakutulosV1RDTO hakuTulos = gson.fromJson(hakukohdeTulos, HakukohdeHakutulosV1RDTO.class);

            hakukohteet.add(hakuTulos);
        }

        return hakukohteet;
    }
}

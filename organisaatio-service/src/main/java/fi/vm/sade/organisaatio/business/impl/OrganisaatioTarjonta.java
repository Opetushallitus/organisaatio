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

import com.google.gson.*;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioTarjontaException;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.tarjonta.service.resources.v1.dto.HakukohdeHakutulosV1RDTO;
import fi.vm.sade.tarjonta.service.resources.v1.dto.KoulutusHakutulosV1RDTO;
import fi.vm.sade.tarjonta.service.resources.v1.dto.ResultV1RDTO.ResultStatus;
import fi.vm.sade.tarjonta.shared.types.TarjontaTila;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Käyttää tarjonta palvelua selvittääkseen organisaation tulevia koulutuksia
 * ja hakukohteita.
 *
 * @author simok
 */
@Component
public class OrganisaatioTarjonta {
    private static final String TULOKSET = "tulokset";
    private static final String STATUS = "status";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrganisaatioRestToStream restToStream;

    @Autowired
    private OphProperties properties;

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
        gsonBuilder.registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> new Date(json.getAsJsonPrimitive().getAsLong()));

        gson = gsonBuilder.create();
    }

    private List<KoulutusHakutulosV1RDTO> getOrganisaatioKoulutukset(JsonElement organisaatioTulos) {
        List<KoulutusHakutulosV1RDTO> koulutukset = new ArrayList<>();

        JsonElement koulutusTulokset = organisaatioTulos.getAsJsonObject().get(TULOKSET);

        // Tarkistetaan, että tuloksia löytyy!
        if (koulutusTulokset.isJsonNull()) {
            logger.warn("Search failed for koulutus! --> koulutus tulokset == NULL");
            return Collections.emptyList();
        }

        // Käydään läpi koulutukset ja deserialisoidaan
        JsonArray koulutusTuloksetArray = koulutusTulokset.getAsJsonArray();
        for (JsonElement koulutusTulos : koulutusTuloksetArray) {
            KoulutusHakutulosV1RDTO hakuTulos = gson.fromJson(koulutusTulos, KoulutusHakutulosV1RDTO.class);

            koulutukset.add(hakuTulos);
        }

        return koulutukset;
    }

    private List<KoulutusHakutulosV1RDTO> haeKoulutukset(String oid) {
        List<KoulutusHakutulosV1RDTO> koulutukset = new ArrayList<>();
        JsonElement json;
        String tarjontaServiceWebappUrl = properties.getProperty("organisaatio-service.tarjonta-service.rest.tarjonta.haku", "koulutus");
        String url = tarjontaServiceWebappUrl + "?organisationOid=" + oid;

        try {
            json = restToStream.getInputStreamFromUri(url);
        } catch (RuntimeException e) {
            logger.warn("Search failed for koulutus with organization oid: {}", oid);
            throw new OrganisaatioTarjontaException();
        }

        // Tarkistetaan hakutulokset
        if (json.getAsJsonObject().get(STATUS).isJsonNull()) {
            logger.warn("Search failed for koulutus with organization oid: {}", oid);
            throw new OrganisaatioTarjontaException();
        }
        ResultStatus status = gson.fromJson(json.getAsJsonObject().get(STATUS), new TypeToken<ResultStatus>() {
        }.getType());
        if (status != ResultStatus.OK) {
            logger.warn("Search failed for koulutus with organization oid: {}  status: {}", oid, status);
            throw new OrganisaatioTarjontaException();
        }

        // Otetaan ensimmäinen taso "result"
        JsonElement result = json.getAsJsonObject().get("result");
        if (result.isJsonNull()) {
            logger.warn("Search failed for koulutus with organization oid: {} --> result == NULL",oid);
            return Collections.emptyList();
        }

        // Otetaan organisaatiolista (sisältää listata organisaatioiden hakutuloksista)
        JsonElement organisaatioTulokset = result.getAsJsonObject().get(TULOKSET);
        if (organisaatioTulokset.isJsonNull()) {
            logger.warn("Search failed for koulutus with organization oid: {} --> tulokset == NULL",oid);
            return Collections.emptyList();
        }

        // Käydään läpi organisaatioiden koulutukset ja lisätään listalle
        JsonArray organisaatioTuloksetArray = organisaatioTulokset.getAsJsonArray();
        for (JsonElement organisaatioTulos : organisaatioTuloksetArray) {
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
     * @param oid organisaation oid
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
     * @param oid organiaation oid
     * @param after pöivömöörön jölkeen
     * @return Boolean, joka kertoo onko alkavia koulutuksia vai ei.
     */
    public boolean alkaviaKoulutuksia(String oid, Date after) {
        List<KoulutusHakutulosV1RDTO> koulutukset = haeKoulutukset(oid);

        // Ei alkavia koulutuksia tai after == null
        if (koulutukset.isEmpty() || after == null) {
            return false;
        }

        // Tarkistetaan onko alkavia koulutuksia annetun päivämäärän jälkeen
        for (KoulutusHakutulosV1RDTO koulutus : koulutukset) {
            Date koulutuksenAlkamisPvmMax = koulutus.getKoulutuksenAlkamisPvmMax();

            if (koulutuksenAlkamisPvmMax == null) {
                logger.info("Missing 'kausi' (koulutuksenAlkamisPvmMax == null) for koulutus: {}", koulutus.getOid());
                Integer vuosi = koulutus.getVuosi();

                if (vuosi == null) {
                    // Koulutukselta puuttuu kausi ja vuosi --> "valmistava hakukohde"
                    // eli se on sidottu johonkin toiseen koulutukseen. Ei estä esim.
                    // organisaation lakkauttamista.
                    logger.info("Missing 'kausi' and 'vuosi' for koulutus: {}", koulutus.getOid());
                    continue;
                } else {
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.YEAR, vuosi);
                    cal.set(Calendar.MONTH, Calendar.DECEMBER);
                    cal.set(Calendar.DAY_OF_MONTH, 31);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);

                    koulutuksenAlkamisPvmMax = cal.getTime();
                    logger.debug("oli vuosi, käytetään aikaa {}", koulutuksenAlkamisPvmMax);
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
     * @param ryhmaOid ryhmän oid
     * @return Boolean, joka kertoo onko rygmälle hakukohteita vai ei.
     */
    public boolean hakukohteita(String ryhmaOid) {
        List<HakukohdeHakutulosV1RDTO> hakukohteet = haeHakukohteet(ryhmaOid);

        // Ei hakukohteita
        return !hakukohteet.isEmpty();

        // Ryhmään on liitetty hakukohteita
    }

    private List<HakukohdeHakutulosV1RDTO> haeHakukohteet(String ryhmaOid) {
        List<HakukohdeHakutulosV1RDTO> hakukohteet = new ArrayList<>();
        JsonElement json;

        String tarjontaServiceWebappUrl = properties.getProperty("organisaatio-service.tarjonta-service.rest.tarjonta.haku", "hakukohde");
        String url = tarjontaServiceWebappUrl + "?organisaatioRyhmaOid=" + ryhmaOid;

        try {
            json = restToStream.getInputStreamFromUri(url);
        } catch (RuntimeException e) {
            logger.warn("Search failed for hakukohteet with group oid: {}", ryhmaOid);
            throw new OrganisaatioTarjontaException();
        }

        // Tarkistetaan hakutulokset
        if (json.getAsJsonObject().get(STATUS).isJsonNull()) {
            logger.warn("Search failed for hakukohteet with organization oid: {}", ryhmaOid);
            throw new OrganisaatioTarjontaException();
        }
        ResultStatus status = gson.fromJson(json.getAsJsonObject().get(STATUS), new TypeToken<ResultStatus>() {
        }.getType());
        if (status != ResultStatus.OK) {
            logger.warn("Search failed for hakukohteet with organization oid: {} status: {}",ryhmaOid, status);
            throw new OrganisaatioTarjontaException();
        }

        // Otetaan ensimmäinen taso "result"
        JsonElement result = json.getAsJsonObject().get("result");
        if (result.isJsonNull()) {
            logger.warn("Search failed for hakukohteet with organization oid: {} --> result == NULL",ryhmaOid);
            return Collections.emptyList();
        }

        // Otetaan organisaatiolista (sisältää listan organisaatioiden hakukohteista)
        JsonElement organisaatioTulokset = result.getAsJsonObject().get(TULOKSET);
        if (organisaatioTulokset.isJsonNull()) {
            logger.warn("Search failed for hakukohteet with organization oid: {} --> tulokset == NULL",ryhmaOid);
            return Collections.emptyList();
        }

        // Käydään läpi organisaatioiden hakukohteet ja lisätään listalle
        JsonArray organisaatioTuloksetArray = organisaatioTulokset.getAsJsonArray();
        for (JsonElement organisaatioTulos : organisaatioTuloksetArray) {
            hakukohteet.addAll(getOrganisaatioHakukohteet(organisaatioTulos));
        }

        return hakukohteet;
    }

    private List<HakukohdeHakutulosV1RDTO> getOrganisaatioHakukohteet(JsonElement organisaatioTulos) {
        List<HakukohdeHakutulosV1RDTO> hakukohteet = new ArrayList<>();

        JsonElement hakukohdeTulokset = organisaatioTulos.getAsJsonObject().get(TULOKSET);

        // Tarkistetaan, että tuloksia löytyy!
        if (hakukohdeTulokset.isJsonNull()) {
            logger.warn("Search failed for hakukohde! --> hakukohde tulokset == NULL");
            return Collections.emptyList();
        }

        // Käydään läpi hakukohteet ja deserialisoidaan
        JsonArray hakukohdeTuloksetArray = hakukohdeTulokset.getAsJsonArray();
        for (JsonElement hakukohdeTulos : hakukohdeTuloksetArray) {
            HakukohdeHakutulosV1RDTO hakuTulos = gson.fromJson(hakukohdeTulos, HakukohdeHakutulosV1RDTO.class);

            hakukohteet.add(hakuTulos);
        }

        return hakukohteet;
    }
}

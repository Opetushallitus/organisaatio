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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.model.Organisaatio;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Käyttää koodistopalvelua selvittääkseen organisaation yhteishaun koulukoodin.
 *
 * @author simok
 */
@Component
public class OrganisaatioYHKoulukoodi {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrganisaatioRestToStream restToStream;

    @Value("${root.organisaatio.oid}")
    private String rootOrganisaatioOid;

    @Value("${organisaatio-service.koodisto-service.rest.url}")
    private String koodistoServiceUrl;

    private final Gson gson;

    /**
     * Luo instanssin ja alustaa gson:in
     */
    public OrganisaatioYHKoulukoodi() {
        gson = new GsonBuilder().create();
    }

    /*
     * method that searches for the yhkoodi of the organisaatio. If such is found is updated
     * to the yhteyishanKoulukoodi field of organisaatio.
     */
    public String getYhKoulukoodi(Organisaatio org) {
        String yhKoodi = null;
        String olkoodi = null;
        Organisaatio parentOl = getParentOl(org);

        if (org.getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS.value())) {
            olkoodi = org.getOppilaitosKoodi();
        } else if (parentOl != null) {
            olkoodi = parentOl.getOppilaitosKoodi();
        }
        if (!isEmpty(olkoodi)) {
            yhKoodi = getYhkoodi(olkoodi, this.getOpPisteenJarjNro(org));
        }
        return yhKoodi;
    }

    /*
     * Method that seeks the nearest oppilaitos ancestor of organisaatio o.
     */
    private Organisaatio getParentOl(Organisaatio o) {
        Organisaatio parentOl = o.getParent();
        if (parentOl == null || parentOl.getOid().equals(this.rootOrganisaatioOid)) {
            return null;
        }
        if (parentOl.getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS.value())) {
            return parentOl;
        }
        return getParentOl(parentOl);
    }


    private boolean isEmpty(String val) {
        return val == null || val.isEmpty();
    }

    private String getOpPisteenJarjNro(Organisaatio orgE) {
        String opPisteenJarjNro = "";
        if (orgE.getOpetuspisteenJarjNro() != null) {
            opPisteenJarjNro = orgE.getOpetuspisteenJarjNro();
        }
        return opPisteenJarjNro;
    }

    private String buildOpetuspisteUri(String olkoodi, String opJnro) {
        // TUOLLAISESTA LÄHDETÄÄN
        // https://itest-virkailija.oph.ware.fi/koodisto-service/rest/codeelement/opetuspisteet_0131001/1
        return "/rest/codeelement/opetuspisteet_" + olkoodi + opJnro + "/1";
    }

    /**
     * Haetaan organisaation yhkoodi koodistosta.
     * Organisaatio esitetään oppilaitoskoodistossa oppilaitoskoodilla ja oppilaitoksen järjestysnumerolla.
     */
    private String getYhkoodi(String olkoodi, String opJnro) {
        String yhkoodi = null;
        InputStream jsonStream;

        LOG.debug("Haetaan yhteishaun koulukoodia oppilaitokselle: " + olkoodi + " numerolla: " + opJnro);

        String url = koodistoServiceUrl + buildOpetuspisteUri(olkoodi, opJnro);

        try {
            jsonStream = restToStream.getInputStreamFromUri(url);
        }
        catch (RuntimeException e) {
            LOG.warn("Failed to get Yhkoodi for olkoodi: " + olkoodi + " opJnro: " + opJnro);
            return null;
        }

        Reader reader = new InputStreamReader(jsonStream);
        JsonElement json = new JsonParser().parse(reader);

        // Otetaan sisältyvät koodit
        JsonElement includesCodeElements = json.getAsJsonObject().get("includesCodeElements");
        if (includesCodeElements.isJsonNull()) {
            LOG.warn("Failed to get Yhkoodi for olkoodi: " + olkoodi + " opJnro: " + opJnro +
                    " --> includesCodeElements == NULL");
            return null;
        }

        // Käydään läpi within koodit ja jos niissä on
        JsonArray includesCodeElementsArray = includesCodeElements.getAsJsonArray();
        for (JsonElement includedCode : includesCodeElementsArray) {
            yhkoodi = getYhkoodi(includedCode);
            if (yhkoodi != null) {
                return yhkoodi;
            }
        }

        LOG.warn("Failed to get Yhkoodi for olkoodi: " + olkoodi + " opJnro: " + opJnro +
                " --> yhteishaunkoulukoodi not in includesCodeElements");
        return null;
    }

    private String getYhkoodi(JsonElement includedInOpetuspiste) {
        String yhkoodi = null;
        OrganisaatioKoodistoKoodiCodeElements codeElements;

        codeElements = gson.fromJson(includedInOpetuspiste, OrganisaatioKoodistoKoodiCodeElements.class);

        if (codeElements.getCodeElementUri().startsWith("yhteishaunkoulukoodi_")) {
           yhkoodi = codeElements.getCodeElementValue();
        }

        return yhkoodi;
    }

}

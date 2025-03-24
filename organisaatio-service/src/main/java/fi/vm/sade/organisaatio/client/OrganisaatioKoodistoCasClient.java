/*
 * Copyright (c) 2014 The Finnish Board of Education - Opetushallitus
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
package fi.vm.sade.organisaatio.client;

import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpEntity;
import fi.vm.sade.javautils.http.OphHttpRequest;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioKoodistoException;
import fi.vm.sade.properties.OphProperties;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Supplier;

import static fi.vm.sade.organisaatio.config.HttpClientConfiguration.HTTP_CLIENT_KOODISTO;
import static java.util.function.Function.identity;

/**
 * Koodisto-servicen REST operaatiot ja autentikointi
 */
@Component
@ConditionalOnProperty(name = "organisaatio.koodisto-oauth2-client.enabled", havingValue = "false", matchIfMissing = true)
public class OrganisaatioKoodistoCasClient extends CustomClient implements OrganisaatioKoodistoClient {

    public OrganisaatioKoodistoCasClient(@Qualifier(HTTP_CLIENT_KOODISTO) OphHttpClient httpClient, OphProperties properties) {
        super(httpClient, properties);
    }

    /**
     * Hae koodi URIn mukaan
     *
     * @param uri kononainen koodiston uri, käytä urlpropertiesseja generointiin esim.
     *            "/koodisto-service/rest/json/opetuspisteet/koodi/opetuspisteet_0106705"
     * @return koodi json-muodossa, tai null jos koodia ei löydy
     * @throws OrganisaatioKoodistoException Koodistopalvelupyyntö epäonnistui
     */
    @Override
    public String get(String uri) throws OrganisaatioKoodistoException {
        return wrapException(() -> httpClient.<String>execute(OphHttpRequest.Builder.get(uri).build())
                .handleErrorStatus(204).with(json -> { throw new ClientException(String.format("Osoite %s palautti 204", uri)); }) // ilman tätä 204 => Optional#empty
                .handleErrorStatus(500).with(json -> Optional.empty()) // 500 => Koodia ei löydy
                .expectedStatus(200)
                .mapWith(identity())
                .orElse(null)); // 404 => Koodia ei löydy
    }

    /**
     * Päivittää koodin koodistoon
     *
     * @param json Päivitettävä koodi json-muodossa
     * @throws OrganisaatioKoodistoException Koodistopalvelupyyntö epäonnistui
     */
    @Override
    public void put(String json) throws OrganisaatioKoodistoException {
        String uri = properties.getProperty("organisaatio-service.koodisto-service.rest.codeelement", "save");
        OphHttpRequest request = OphHttpRequest.Builder.put(uri)
                .setEntity(new OphHttpEntity.Builder()
                        .content(json)
                        .contentType(ContentType.APPLICATION_JSON)
                        .build())
                .build();
        wrapException(() -> httpClient.<String>execute(request)
                .expectedStatus(200)
                .mapWith(identity())
                .orElseThrow(() -> new ClientException(String.format("Osoite %s palautti 204 tai 404", uri))));
    }

    /**
     * Lisää koodin koodistoon
     *
     * @param json Lisättävä koodi json-muodossa
     * @param uri  Lisättävän koodin uri, esim. 'opetuspisteet'
     * @throws OrganisaatioKoodistoException Koodistopalvelupyyntö epäonnistui
     */
    @Override
    public void post(String json, String uri) throws OrganisaatioKoodistoException {
        String url = properties.getProperty("organisaatio-service.koodisto-service.rest.codeelement", uri);
        OphHttpRequest request = OphHttpRequest.Builder.post(url)
                .setEntity(new OphHttpEntity.Builder()
                        .content(json)
                        .contentType(ContentType.APPLICATION_JSON)
                        .build())
                .build();
        wrapException(() -> httpClient.<String>execute(request)
                .expectedStatus(201)
                .mapWith(identity())
                .orElseThrow(() -> new ClientException(String.format("Osoite %s palautti 204 tai 404", uri))));
    }
    <T> T wrapException(Supplier<T> action) {
        try {
            return action.get();
        } catch (Exception e) {
            OrganisaatioKoodistoException ex = new OrganisaatioKoodistoException(e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }
}

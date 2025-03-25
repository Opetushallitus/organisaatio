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

import fi.vm.sade.organisaatio.business.exception.OrganisaatioKoodistoException;
import fi.vm.sade.properties.OphProperties;
import lombok.RequiredArgsConstructor;
import org.apache.http.entity.ContentType;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpRequest;

@Component
@RequiredArgsConstructor
public class OrganisaatioKoodistoClient {
    private final OtuvaOauth2Client httpClient;
    private final OphProperties properties;

    public String get(String uri) throws OrganisaatioKoodistoException {
        try {
            var request = HttpRequest.newBuilder().uri(new URI(uri)).GET();
            var response = httpClient.executeRequest(request);
            if (response.statusCode() == 200) {
                return response.body();
            } else if (response.statusCode() == 204) {
                throw new ClientException(String.format("Osoite %s palautti 204", response.request().uri()));
            }
        } catch  (Exception e) {
            OrganisaatioKoodistoException ex = new OrganisaatioKoodistoException(e.getMessage());
            ex.initCause(e);
            throw ex;
        }
        return null;
    }

    public void put(String json) throws OrganisaatioKoodistoException {
        try {
            var uri = properties.getProperty("organisaatio-service.koodisto-service.rest.codeelement", "save");
            var request = HttpRequest.newBuilder()
                    .uri(new URI(uri))
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .header("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
            var response = httpClient.executeRequest(request);
            if (response.statusCode() != 200) {
                throw new ClientException(String.format("Osoite %s palautti 204 tai 404", response.request().uri()));
            }
        } catch  (Exception e) {
            OrganisaatioKoodistoException ex = new OrganisaatioKoodistoException(e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    public void post(String json, String uri) throws OrganisaatioKoodistoException {
        try {
            var request = HttpRequest.newBuilder()
                    .uri(new URI(properties.getProperty("organisaatio-service.koodisto-service.rest.codeelement", uri)))
                    .header("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
                    .POST(HttpRequest.BodyPublishers.ofString(json));
            var response = httpClient.executeRequest(request);
            if (response.statusCode() != 201) {
                throw new ClientException(String.format("Osoite %s palautti 204 tai 404", response.request().uri()));
            }
        } catch  (Exception e) {
            OrganisaatioKoodistoException ex = new OrganisaatioKoodistoException(e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }
}

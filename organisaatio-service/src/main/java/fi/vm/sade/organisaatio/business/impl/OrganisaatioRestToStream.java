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

import fi.vm.sade.organisaatio.client.OtuvaOauth2Client;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.net.http.HttpRequest;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrganisaatioRestToStream {
    private final OtuvaOauth2Client httpClient;

    public JsonElement getInputStreamFromUri(String uri) {
        try {
            var request = HttpRequest.newBuilder().uri(new URI(uri)).GET();
            var response = httpClient.executeRequest(request);
            if (response.statusCode() == 200) {
                return JsonParser.parseString(response.body());
            } else {
                throw new RuntimeException(String.format("Osoite %s palautti 204 tai 404", uri));
            }
        } catch  (Exception e) {
            throw new RuntimeException(e);
        }
    }

 }

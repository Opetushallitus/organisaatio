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

package fi.vm.sade.organisaatio.client;

import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpEntity;
import fi.vm.sade.javautils.http.OphHttpRequest;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioViestintaException;
import fi.vm.sade.properties.OphProperties;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static fi.vm.sade.organisaatio.config.HttpClientConfiguration.HTTP_CLIENT_VIESTINTA;
import static java.util.function.Function.identity;


@Component
public class OrganisaatioViestintaClient extends CustomClient {

    public OrganisaatioViestintaClient(@Qualifier(HTTP_CLIENT_VIESTINTA) OphHttpClient httpClient, OphProperties properties) {
        super(httpClient, properties);
    }

    public void sendEmail(String json) throws OrganisaatioViestintaException {
        sendEmail(json, true);
    }

    public String sendEmail(String json, boolean sanitize) {
        String viestintaServiceUrl = properties.getProperty("organisaatio-service.ryhmasahkoposti-service.rest.mail", sanitize);

        OphHttpRequest request = OphHttpRequest.Builder.post(viestintaServiceUrl)
                .setEntity(new OphHttpEntity.Builder()
                        .content(json)
                        .contentType(ContentType.APPLICATION_JSON)
                        .build())
                .build();
        return wrapException(() -> httpClient.<String>execute(request)
                .expectedStatus(200)
                .mapWith(identity())
                .orElseThrow(() -> new ClientException(String.format("Osoite %s palautti 204 tai 404", viestintaServiceUrl))));
    }
    <T> T wrapException(Supplier<T> action) {
        try {
            return action.get();
        } catch (Exception e) {
            OrganisaatioViestintaException ex = new OrganisaatioViestintaException(e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }
}

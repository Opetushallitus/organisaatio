/*
 * Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
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
 * European Union Public Licence for more details.
 */

package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.generic.service.exception.SadeBusinessException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 *
 */
public class OrganisaatioResourceException extends WebApplicationException {

    public static class ErrorMessage {

        private final String errorMessage;
        private final String errorKey;

        public ErrorMessage(String errorMessage, String errorKey) {
            this.errorMessage = errorMessage;
            this.errorKey = errorKey;
        }

        public ErrorMessage(SadeBusinessException sbe) {
            this.errorMessage = sbe.getMessage();
            this.errorKey = sbe.getErrorKey();
        }

        public ErrorMessage(String errorMessage) {
            this(errorMessage, "");
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String getErrorKey() {
            return errorKey;
        }

    }

    public OrganisaatioResourceException(int status, Object message) {
        super(Response.status(status).entity(message).build());
    }

    public OrganisaatioResourceException(int status, String message) {
        this(status, new ErrorMessage(message));
    }

    public OrganisaatioResourceException(int status, String message, String key) {
        this(status, new ErrorMessage(message, key));
    }

    public OrganisaatioResourceException(Status status, String message) {
        this(status.getStatusCode(), new ErrorMessage(message));
    }

    public OrganisaatioResourceException(Status status, String message, String key) {
        this(status.getStatusCode(), new ErrorMessage(message, key));
    }

    public OrganisaatioResourceException(SadeBusinessException sbe) {
        this(500, new ErrorMessage(sbe));
    }

    public OrganisaatioResourceException(Status status, SadeBusinessException exception) {
        this(status.getStatusCode(), new ErrorMessage(exception));
    }

}

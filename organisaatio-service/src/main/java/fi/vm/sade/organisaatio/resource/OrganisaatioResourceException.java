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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 *
 */
public class OrganisaatioResourceException extends ApiException {

    public OrganisaatioResourceException(ErrorMessage errorMessage, HttpStatus status) {
        super(new ResponseEntity<>(errorMessage, status));
    }

    public OrganisaatioResourceException(int status, ErrorMessage message) {
        this(message, HttpStatus.valueOf(status));
    }

    public OrganisaatioResourceException(int status, String message) {
        this(status, new ErrorMessage(message));
    }

    public OrganisaatioResourceException(int status, String message, String key) {
        this(status, new ErrorMessage(message, key));
    }

    public OrganisaatioResourceException(HttpStatus status, String message) {
        this(new ErrorMessage(message), status);
    }

    public OrganisaatioResourceException(HttpStatus status, String message, String key) {
        this(new ErrorMessage(message, key), status);
    }

    public OrganisaatioResourceException(SadeBusinessException sbe) {
        this(new ErrorMessage(sbe), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public OrganisaatioResourceException(HttpStatus status, SadeBusinessException exception) {
        this(new ErrorMessage(exception), status);
    }

}

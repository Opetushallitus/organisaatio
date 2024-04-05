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

import fi.vm.sade.organisaatio.SadeBusinessException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 *
 */
@RequiredArgsConstructor
@Getter
public class OrganisaatioResourceException extends RuntimeException {

    private final HttpStatus status;
    private final String errorMessage;
    private final String errorKey;

    public OrganisaatioResourceException(SadeBusinessException sbe) {
        this(HttpStatus.INTERNAL_SERVER_ERROR, sbe.getMessage(), sbe.getErrorKey());
    }

    public OrganisaatioResourceException(HttpStatus status, SadeBusinessException sbe) {
        this(status, sbe.getMessage(), sbe.getErrorKey());
    }

    public OrganisaatioResourceException(HttpStatus status, String message) {
        this(status, message, message);
    }

}

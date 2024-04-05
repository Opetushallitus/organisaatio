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

package fi.vm.sade.organisaatio.business.exception;

import fi.vm.sade.organisaatio.SadeBusinessException;

/**
 *
 */
public abstract class OrganisaatioBusinessException extends SadeBusinessException {

    public OrganisaatioBusinessException() {
    }

    public OrganisaatioBusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrganisaatioBusinessException(String message) {
        super(message);
    }

    public OrganisaatioBusinessException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getErrorKey() {
        return getMessage();
    }

    @Override
    public String toString() {
        return super.toString() + " [" + getErrorKey() + "]";
    }

}

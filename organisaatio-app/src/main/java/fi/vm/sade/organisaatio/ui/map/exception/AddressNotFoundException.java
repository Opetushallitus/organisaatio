/*
 *
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

package fi.vm.sade.organisaatio.ui.map.exception;

/**
 *
 * @author Tuomas Katva
 *
 * This exception is thrown if given address cannot be resolved to coordinates
 *
 */
public class AddressNotFoundException extends Exception  {
    private String messageCode = null;
    private String message = null;

    public AddressNotFoundException(String msg) {
        message = msg;
    }

    @Override
    public String toString() {
        if (messageCode == null) {
            return message;
        } else {
            return messageCode + " " + message;
        }
    }

    @Override
    public String getMessage() {
        return message;
    }

}

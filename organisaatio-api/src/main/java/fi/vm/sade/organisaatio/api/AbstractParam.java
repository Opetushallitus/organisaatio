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

package fi.vm.sade.organisaatio.api;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public abstract class AbstractParam<V> {
    private final V value;
    private final String originalParam;

    public AbstractParam(String param) throws WebApplicationException {
        this.originalParam = param;
        try {
            this.value = parse(param);
        } catch (Throwable e) {
            throw new WebApplicationException(onError(param, e));
        }
    }

    public V getValue() {
        return value;
    }

    public String getOriginalParam() {
        return originalParam;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    protected abstract V parse(String param) throws Throwable;

    protected Response onError(String param, Throwable e) {
        return Response
                .status(Status.BAD_REQUEST)
                .entity(getErrorMessage(param, e))
                .build();
    }

    protected String getErrorMessage(String param, Throwable e) {
        return "Invalid parameter: " + param + " (" + e.getMessage() + ")";
    }
}

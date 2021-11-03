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

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * Selainkäyttöliittymän sessionhallinta
 */
@Component("sessionResource")
@Path("/session")
@Hidden
public class SessionResource {

    /**
     * Palauttaa session erääntymisen aikarajan sekunteina.
     *
     * @param req HTTP kutsu, jossa on session id
     * @return session erääntymisen aikaraja sekunteina
     */
    @GET
    @Path("/maxinactiveinterval")
    @PreAuthorize("isAuthenticated()")
    @Produces(MediaType.TEXT_PLAIN)
    public String maxInactiveInterval(@Context HttpServletRequest req) {
        return Integer.toString(req.getSession().getMaxInactiveInterval());
    }
}

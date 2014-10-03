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

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 *
 * @author simok
 */
@Path("/dev")
@Component
@Api(value = "/dev", description = "Development operaatiot")
public class OrganisaatioDevResource {

    @GET
    @Path("/myroles")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    @ApiOperation(
            value = "Hakee autentikoituneen käyttäjän roolit",
            notes = "Operaatio palauttaa samat kuin /cas/myroles. HUOM! Testikäyttöön tarkoitettu.",
            response = String.class)
    public String getRoles() {
        StringBuilder ret = new StringBuilder("[");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        ret.append("\"");
        ret.append(auth.getName());
        ret.append("\",");

        for (GrantedAuthority ga : auth.getAuthorities()) {
            ret.append("\"");
            ret.append(ga.getAuthority().replace("ROLE_", ""));
            ret.append("\",");
        }
        ret.setCharAt(ret.length() - 1, ']');
        return ret.toString();
    }

}

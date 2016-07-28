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

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.business.OrganisaatioYtjService;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.YtjPaivitysLoki;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Development operaatiot
 *
 * @author simok
 */
@Path("/dev")
@Api(value = "/dev", description = "Development operaatiot")
@Component
@Transactional(readOnly = true)
public class OrganisaatioDevResource {

    @Autowired
    OrganisaatioYtjService organisaatioYtjService;

    @Autowired
    ConversionService conversionService;

    /**
     * Hakee autentikoituneen käyttäjän roolit
     * @return Operaatio palauttaa samat kuin /cas/myroles. HUOM! Testikäyttöön tarkoitettu.
     */
    @GET
    @Path("/myroles")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Hakee autentikoituneen käyttäjän roolit. Tarkoitettu vain kehityskäyttöön.",
            notes = "Hakee autentikoituneen käyttäjän roolit. Tarkoitettu vain kehityskäyttöön.",
            response = String.class)
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
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

    @GET
    @Path("/ytjbatchupdate")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Hakee autentikoituneen käyttäjän roolit. Tarkoitettu vain kehityskäyttöön.",
            notes = "Hakee autentikoituneen käyttäjän roolit. Palauttaa montako organisaatiota päivitettiin. Tarkoitettu vain kehityskäyttöön.",
            response = YtjPaivitysLoki.class,
            responseContainer = "List")
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public YtjPaivitysLoki updateYtj(@DefaultValue("false") @QueryParam("forceUpdate") final boolean forceUpdate) {
        return organisaatioYtjService.updateYTJData(forceUpdate);
    }

}

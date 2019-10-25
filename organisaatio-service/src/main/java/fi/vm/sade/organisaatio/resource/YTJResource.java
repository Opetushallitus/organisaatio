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

import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.rajapinnat.ytj.api.YTJDTO;
import fi.vm.sade.rajapinnat.ytj.api.YTJKieli;
import fi.vm.sade.rajapinnat.ytj.api.YTJService;
import fi.vm.sade.rajapinnat.ytj.api.exception.YtjConnectionException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/ytj")
@Component("ytjResource")
@Transactional(readOnly = true)
@Api(value = "/ytj", description = "YTJ hakuoperaatiot")
public class YTJResource {

    private static final Logger LOG = LoggerFactory.getLogger(YTJResource.class);
        
    @Autowired(required = true)
    private YTJService ytjService;

    @Autowired
    private ConversionService conversionService;
    
    /**
     * YTJ DTO as JSON.
     * 
     * @param ytunnus
     * @return
     */
    @GET
    @Path("{ytunnus}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Näyttää yhden yrityksen tiedot", notes = "Operaatio näyttää yhden yrityksen tiedot annetulla Y tunnuksella.", response = YTJDTO.class)
    public YTJDTO findByYTunnus(@ApiParam(value = "Y Tunnus", required = true) @PathParam("ytunnus") String ytunnus) {
        YTJDTO ytj = new YTJDTO();
        try {
            ytj = ytjService.findByYTunnus(ytunnus.trim(), YTJKieli.FI);
        } 
        catch (YtjConnectionException ex) {
            ex.printStackTrace();
            LOG.error("YtjConnectionException : " + ex.toString());
            
            throw new OrganisaatioResourceException(Response.Status.INTERNAL_SERVER_ERROR, ex.toString());
        }
        
        return ytj; 
    }

    @GET
    @Path("{ytunnus}/v4")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Näyttää yhden yrityksen tiedot", notes = "Operaatio näyttää yhden yrityksen tiedot annetulla Y tunnuksella.", response = OrganisaatioRDTOV4.class)
    public OrganisaatioRDTOV4 findByYTunnusV4(@ApiParam(value = "Y Tunnus", required = true) @PathParam("ytunnus") String ytunnus) {
        YTJDTO ytjdto = findByYTunnus(ytunnus);
        if (ytjdto.getYtunnus() == null) {
            throw new OrganisaatioResourceException(Response.Status.NOT_FOUND, "organisaatio.exception.organisaatio.not.found");
        }
        Organisaatio organisaatio = conversionService.convert(ytjdto, Organisaatio.class);
        return conversionService.convert(organisaatio, OrganisaatioRDTOV4.class);
    }

    @GET
    @Path("/hae")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Hakee yritysten tiedot nimen perusteella", notes = "Operaatio palauttaa listan yritysten tiedoista, joiden nimessä esiintyy annettu nimi.")
    public List<YTJDTO> findByYNimi(@ApiParam(value = "nimi", required = true) @QueryParam("nimi") String nimi) {
        List<YTJDTO> ytjList = new ArrayList<YTJDTO>();
        if (nimi != null && nimi.length() > 0) {
            try {
                ytjList = ytjService.findByYNimi(nimi.trim(), true, YTJKieli.FI);
            } catch (YtjConnectionException ex) {
                ex.printStackTrace();
                LOG.warn("YtjConnectionException : " + ex.toString());

                throw new OrganisaatioResourceException(Response.Status.INTERNAL_SERVER_ERROR, ex.toString());
            }
        }
        return ytjList;
    }

    // Api for batch searches by y-tunnuses
    @GET
    @Path("/massahaku/{ytunnukset}")
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Hakee maksimissaan 1000:n yrityksen tiedot", notes = "Operaatio palauttaa listan yritysten tiedoista, joiden y-tunnukset on annettu")
    public List<YTJDTO> findByYTunnusBatch(@ApiParam(value = "Y-tunnukset", required = true)
                                               @PathParam("ytunnukset") List<String> ytunnuses) {
        return doYtjMassSearch(ytunnuses);
    }

    public List<YTJDTO> doYtjMassSearch(List<String> ytunnuses) {
        List<YTJDTO> ytjListResult;
        try {
            ytjListResult = ytjService.findByYTunnusBatch(ytunnuses, YTJKieli.FI);
        }
        catch (YtjConnectionException ex) {
            ex.printStackTrace();
            LOG.error("YtjConnectionException : " + ex.toString());

            throw new OrganisaatioResourceException(Response.Status.INTERNAL_SERVER_ERROR, ex.toString());
        }
        return ytjListResult;
    }
}

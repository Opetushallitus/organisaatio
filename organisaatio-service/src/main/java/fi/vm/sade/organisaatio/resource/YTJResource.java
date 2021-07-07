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
import fi.vm.sade.organisaatio.ytj.api.YTJDTO;
import fi.vm.sade.organisaatio.ytj.api.YTJKieli;
import fi.vm.sade.organisaatio.ytj.api.YTJService;
import fi.vm.sade.organisaatio.ytj.api.exception.YtjConnectionException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/ytj")
@Api(value = "/ytj")
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
    @GetMapping(path= "/{ytunnus}", produces = MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Näyttää yhden yrityksen tiedot", notes = "Operaatio näyttää yhden yrityksen tiedot annetulla Y tunnuksella.", response = YTJDTO.class)
    public YTJDTO findByYTunnus(@ApiParam(value = "Y Tunnus", required = true) @PathVariable String ytunnus) {
        YTJDTO ytj = new YTJDTO();
        try {
            ytj = ytjService.findByYTunnus(ytunnus.trim(), YTJKieli.FI);
        } 
        catch (YtjConnectionException ex) {
            ex.printStackTrace();
            LOG.error("YtjConnectionException : " + ex.toString());
            
            throw new OrganisaatioResourceException(HttpStatus.INTERNAL_SERVER_ERROR, ex.toString());
        }
        
        return ytj; 
    }

    @GetMapping(path= "/{ytunnus}/v4", produces = MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Näyttää yhden yrityksen tiedot", notes = "Operaatio näyttää yhden yrityksen tiedot annetulla Y tunnuksella.", response = OrganisaatioRDTOV4.class)
    public OrganisaatioRDTOV4 findByYTunnusV4(@ApiParam(value = "Y Tunnus", required = true) @PathVariable String ytunnus) {
        return conversionService.convert(getOrganisaatioByYTunnus(ytunnus), OrganisaatioRDTOV4.class);
    }

    private Organisaatio getOrganisaatioByYTunnus(String ytunnus) {
        YTJDTO ytjdto = findByYTunnus(ytunnus);
        if (ytjdto.getYtunnus() == null) {
            throw new OrganisaatioResourceException(HttpStatus.NOT_FOUND, "organisaatio.exception.organisaatio.not.found");
        }
        return conversionService.convert(ytjdto, Organisaatio.class);
    }

    @GetMapping(path= "/hae", produces = MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Hakee yritysten tiedot nimen perusteella", notes = "Operaatio palauttaa listan yritysten tiedoista, joiden nimessä esiintyy annettu nimi.")
    public List<YTJDTO> findByYNimi(@ApiParam(value = "nimi", required = true) @RequestParam(required = true) String nimi) {
        List<YTJDTO> ytjList = new ArrayList<YTJDTO>();
        if (nimi != null && nimi.length() > 0) {
            try {
                ytjList = ytjService.findByYNimi(nimi.trim(), true, YTJKieli.FI);
            } catch (YtjConnectionException ex) {
                ex.printStackTrace();
                LOG.warn("YtjConnectionException : " + ex.toString());

                throw new OrganisaatioResourceException(HttpStatus.INTERNAL_SERVER_ERROR, ex.toString());
            }
        }
        return ytjList;
    }

    // Api for batch searches by y-tunnuses

    @GetMapping(path= "/massahaku/{ytunnukset}", produces = MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    @ApiOperation(value = "Hakee maksimissaan 1000:n yrityksen tiedot", notes = "Operaatio palauttaa listan yritysten tiedoista, joiden y-tunnukset on annettu")
    public List<YTJDTO> findByYTunnusBatch(@ApiParam(value = "Y-tunnukset", required = true)
                                               @PathVariable List<String> ytunnuses) {
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

            throw new OrganisaatioResourceException(HttpStatus.INTERNAL_SERVER_ERROR, ex.toString());
        }
        return ytjListResult;
    }
}

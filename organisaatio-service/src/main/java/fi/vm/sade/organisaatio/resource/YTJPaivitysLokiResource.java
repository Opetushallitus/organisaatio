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

import fi.vm.sade.organisaatio.repository.YtjPaivitysLokiRepository;
import fi.vm.sade.organisaatio.model.YtjPaivitysLoki;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Path("/ytjpaivitysloki")
@Component("ytjPaivitysResource")
@Api(value = "/ytjpaivitysloki", description = "YTJ massapäivityksen status")
public class YTJPaivitysLokiResource {

    private static final Logger LOG = LoggerFactory.getLogger(YTJResource.class);

    @Autowired
    private YtjPaivitysLokiRepository ytjPaivitysLokiRepository;

    @GET
    @Path("/aikavali")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Hakee annetulta aikaväliltä", notes = "Operaatio palauttaa päivityksen statuksen ja virhelistan annetulle aikaväliltä (syötteet millisekunteja).")
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public List<YtjPaivitysLoki> findByDateRange(@ApiParam(value = "alkupvm", required = true) @QueryParam("alkupvm") long alkupvm,
                                                 @ApiParam(value = "loppupvm", required = true) @QueryParam("loppupvm") long loppupvm) {
        List<YtjPaivitysLoki> ytjLoki = new ArrayList<>();
        Date alkupvmDate = new Date(alkupvm);
        Date loppupvmDate = new Date(loppupvm);
        if (alkupvm != 0 && loppupvm != 0) {
            ytjLoki = ytjPaivitysLokiRepository.findByDateRange(alkupvmDate, loppupvmDate);
        }
        return ytjLoki;
    }

    @GET
    @Path("/uusimmat")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Hakee viimeisimmät", notes = "Operaatio palauttaa viimeisimpien päivityksen statuksen ja virheet.")
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public List<YtjPaivitysLoki> findByDateRange(@ApiParam(value = "limit", required = true) @QueryParam("limit") int limit) {
        List<YtjPaivitysLoki> ytjLoki = new ArrayList<YtjPaivitysLoki>();
        if (limit > 0) {
            ytjLoki = ytjPaivitysLokiRepository.findLatest(limit);
        }
        return ytjLoki;
    }
}

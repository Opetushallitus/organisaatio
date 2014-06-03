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

package fi.vm.sade.organisaatio.resource.v2;

import fi.vm.sade.organisaatio.dto.v2.YhteystiedotSearchCriteriaDTOV2;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioYhteystiedotDTOV2;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioSearchCriteriaDTOV2;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioPaivittajaDTOV2;

import fi.vm.sade.organisaatio.api.search.OrganisaatioHakutulos;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import java.util.List;
import javax.ws.rs.Consumes;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;


/**
 * V2 REST services for Organisaatio.
 *
 * @author simok
 */
@Path("/organisaatio/v2")
@Api(value = "/organisaatio/v2", description = "Organisaation operaatiot")
public interface OrganisaatioResourceV2 {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/hello")
    @ApiOperation(value = "Testi", notes = "Testioperaatio", response = String.class)
    public String hello();

    @GET
    @Path("/hae")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Hakee organisaatiot annetuilla hakuehdoilla",
            notes = "Operaatio näyttää listan organisaatioita, jotka vastaavat annettuja hakuehtoja.", response = OrganisaatioHakutulos.class)
    public OrganisaatioHakutulos searchOrganisaatioRakenne(@QueryParam("") @ApiParam(value = "hakuehdot", required = true)
            OrganisaatioSearchCriteriaDTOV2 hakuEhdot);

    /**
     *
     * @param hakuEhdot
     * @return
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/yhteystiedot/hae")
    @ApiOperation(value = "Hakee organisaatioita annetuilla hakukriteereillä ja palauttaa yhteystiedot",
            notes = "Operaatio palauttaa hakukriteerit täyttävien organisaatioiden yhteystiedot.")
    public List<OrganisaatioYhteystiedotDTOV2> searchOrganisaatioYhteystiedot(YhteystiedotSearchCriteriaDTOV2 hakuEhdot);

    @GET
    @Path("/{oid}/paivittaja")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Hakee aktiivisen organisaation päivittäjän",
            notes = "Operaatio palauttaa viimeisimmän päivittäjän.", response = OrganisaatioPaivittajaDTOV2.class)
    public OrganisaatioPaivittajaDTOV2 getOrganisaatioPaivittaja(@ApiParam(value = "Organisaation oid", required = true) @PathParam("oid") String oid) throws Exception;
}

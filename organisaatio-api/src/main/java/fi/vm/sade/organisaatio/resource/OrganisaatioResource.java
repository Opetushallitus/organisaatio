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
package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.api.search.OrganisaatioHakutulos;
import fi.vm.sade.organisaatio.api.search.OrganisaatioSearchCriteria;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;

import java.util.Date;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import fi.vm.sade.organisaatio.resource.dto.YhteystietojenTyyppiRDTO;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;

/**
 * REST services for Organisaatio.
 *
 * @author mlyly
 */
@Path("/organisaatio")
@Api(value = "/organisaatio", description = "Organisaation operaatiot")
public interface OrganisaatioResource {

    public String OID_SEPARATOR = "/";

    @GET
    @Path("/hae")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Hakee organisaatiot annetuilla hakuehdoilla", notes = "Operaatio näyttää listan organisaatioita, jotka vastaavat annettuja hakuehtoja.", response = OrganisaatioHakutulos.class)
    public OrganisaatioHakutulos searchBasic(@QueryParam("") @ApiParam(value = "hakuehdot", required = true) OrganisaatioSearchCriteria q);

    /**
     * NOTE: USED BY SECURITY FRAMEWORK - DON'T CHANGE
     *
     * Find oids of organisaatio's parents, result oids start from root, ends to
     * given oid itself, and are separated by '/'.
     *
     * @param oid
     * @return oid/path/form/root
     * @throws Exception
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{oid}/parentoids")
    @ApiOperation(value = "Etsii organisaation vanhempien oid:t", notes = "Operaatio palauttaa organisaation vanhempien oid:t "
            + "alkaen juuresta päättyen annetun organisaation oid:hen. " + "Id:t on eroteltu kautta-merkillä ('/').")
    public String parentoids(@ApiParam(value = "Organisaation oid", required = true) @PathParam("oid") String oid) throws Exception;

    /**
     * Get list of all child oids for the organisaatio.
     *
     * @param oid
     * @return List of child oids
     * @throws Exception
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{oid}/childoids")
    @ApiOperation(value = "Etsii organisaation alla olevien organisaatioiden oid:t", notes = "Operaatio palauttaa organisaation alla olevien organisaatioiden oid:t.", response = String.class)
    public String childoids(@ApiParam(value = "Organisaation oid", required = true) @PathParam("oid") String oid) throws Exception;

    /**
     * Get list of all children for the organisaatio.
     *
     * @param oid
     * @return List of children
     * @throws Exception
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{oid}/children")
    @ApiOperation(value = "Etsii organisaation alla olevat organisaatiot", notes = "Operaatio palauttaa organisaation alla olevat organisaatiot.")
    public List<OrganisaatioRDTO> children(@ApiParam(value = "Organisaation oid", required = true) @PathParam("oid") String oid) throws Exception;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/hello")
    @ApiOperation(value = "Testi", notes = "Testioperaatio", response = String.class)
    public String hello();

    /**
     * Get list of Organisaatio oids mathching the query.
     *
     * Search terms:
     * <ul>
     * <li>searchTerms=type=KOULUTUSTOIMIJA / OPPILAITOS / TOIMIPISTE ==
     * OrganisaatioTyyppi.name()</li>
     * </ul>
     *
     * @param searchTerms
     * @param count
     * @param startIndex
     * @param lastModifiedBefore
     * @param lastModifiedSince
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Hakee organisaatioiden oid:t annetuilla hakuehdoilla", notes = "Operaatio palauttaa listan organisaatioiden oid:tä annetuilla hakuehdoilla.")
    public List<String> search(@ApiParam(value = "Hakutermit", required = true) @QueryParam("searchTerms") String searchTerms,
            @ApiParam(value = "Tulosjoukon koko", required = true) @QueryParam("count") int count,
            @ApiParam(value = "Ensimmäisen hakutuloksen indeksi", required = true) @QueryParam("startIndex") int startIndex,
            @ApiParam(value = "Muokattu ennen", required = true) @QueryParam("lastModifiedBefore") Date lastModifiedBefore,
            @ApiParam(value = "Muokattu jälkeen", required = true) @QueryParam("lastModifiedSince") Date lastModifiedSince);

    /**
     * Organisaatio DTO as JSON.
     *
     * @param oid
     *            OID or Y-TUNNUS or VIRASTOTUNNUS or OPETUSPISTEKOODI or
     *            TOIMIPISTEKOODI
     * @return
     */
    @GET
    @Path("/{oid}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Näyttää yhden organisaation oid:llä", notes = "Operaatio näyttää yhden organisaation tiedot annetulla oid:llä.", response = OrganisaatioRDTO.class)
    public OrganisaatioRDTO getOrganisaatioByOID(@ApiParam(value = "Organisaation oid", required = true) @PathParam("oid") String oid);

    @POST
    @Path("/{oid}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Päivittää organisaation oid:llä", notes = "Operaatio päivittää organisaation tiedot annetulla oid:llä.", response = OrganisaatioRDTO.class)
    public OrganisaatioRDTO updateOrganisaatio(@PathParam("oid") String oid, OrganisaatioRDTO ordto);

    @DELETE
    @Path("/{oid}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Poistaa organisaation oid:llä", notes = "Operaatio poistaa organisaation annetulla oid:llä.", response = String.class)
    public String deleteOrganisaatio(@PathParam("oid") String oid);

    @PUT
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Luo uuden organisaation", notes = "Operaatio luo uuden organisaation annetusta JSON:sta.", response = OrganisaatioRDTO.class)
    public OrganisaatioRDTO newOrganisaatio(OrganisaatioRDTO ordto);

    @GET
    @Path("/myroles")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Palauttaa autentikoituneen käyttäjän roolit", notes = "Pitäisi palauttaa testikäyttöön samat kuin /cas/myroles.", response = String.class)
    public String getRoles();

    @GET
    @Path("/yhteystietometadata")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Palauttaa sallitut yhteystietotyypit", notes = "Palauttaa sallitut yhteystietotyypit", response = List.class)
    public List<YhteystietojenTyyppiRDTO> getYhteystietoMetadata(@QueryParam("organisaatioTyyppi") List<String> organisaatioTyyppi);

    @GET
    @Path("/auth")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Testaa autentikoituneen käyttäjän", notes = "Tätä voi kutsua ennen ensimmäistä autenttikoitua POST-kutsua, ettei tule CAS + autentikoitu POST redirection ongelma.", response = String.class)
    public String authHello();

}

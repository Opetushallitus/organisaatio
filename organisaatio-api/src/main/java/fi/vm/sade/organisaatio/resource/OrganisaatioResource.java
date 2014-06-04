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
 * Organisaation REST palvelut (Versio 1).
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
    @ApiOperation(
            value = "Hakee organisaatiot puurakenteena annetuilla hakuehdoilla",
            notes = "Operaatio palauttaa hakuehtoja vastaavat organisaatiot puurakenteena. "
                    + "Hakuehtojen osuessa hierarkiassa alemman tason organisaatioon, "
                    + "palautetaan puurakenne juureen asti (ellei hakuehdot sitä estä).",
            response = OrganisaatioHakutulos.class)
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
    @ApiOperation(
            value = "Hakee organisaation vanhempien oid:t",
            notes = "Operaatio palauttaa organisaation vanhempien oid:t "
                    + "alkaen juuresta päättyen annetun organisaation oid:hen. "
                    + "Id:t on eroteltu kautta-merkillä ('/').",
            response = String.class)
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
    @ApiOperation(
            value = "Hakee organisaation alla olevien organisaatioiden oid:t",
            notes = "Operaatio palauttaa organisaation alla olevien organisaatioiden oid:t.",
            response = String.class)
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
    @ApiOperation(
            value = "Hakee organisaation alla olevat organisaatiot",
            notes = "Operaatio palauttaa organisaation alla olevat organisaatiot.",
            response = OrganisaatioRDTO.class,
            responseContainer = "List")
    public List<OrganisaatioRDTO> children(@ApiParam(value = "Organisaation oid", required = true) @PathParam("oid") String oid) throws Exception;

    /**
     * Get list of all groups for the organisaatio.
     *
     * @param oid
     * @return List of groups
     * @throws Exception
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{oid}/ryhmat")
    @ApiOperation(
            value = "Hakee organisaation alla olevat ryhmät",
            notes = "Operaatio palauttaa organisaation alla olevat ryhmät.",
            response = OrganisaatioRDTO.class,
            responseContainer = "List")
    public List<OrganisaatioRDTO> groups(@ApiParam(value = "Organisaation oid", required = true) @PathParam("oid") String oid) throws Exception;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/hello")
    @ApiOperation(
            value = "Testioperaatio, jolla voi kokeilla onko organisaatiopalvelu pystyssä.",
            notes = "Operaatio vastaa tervehdykseen ja palauttaa palvelun aikaleiman.",
            response = String.class)
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
    @ApiOperation(
            value = "Hakee organisaatioiden oid:t annetuilla hakuehdoilla",
            notes = "Operaatio palauttaa listan organisaatioiden oid:tä annetuilla hakuehdoilla.",
            response = String.class)
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
    @ApiOperation(
            value = "Hakee yhden organisaation annetulla oid:lla",
            notes = "Operaatio palauttaa oid:n määrittämän organisaation tiedot.",
            response = OrganisaatioRDTO.class)
    public OrganisaatioRDTO getOrganisaatioByOID(@ApiParam(value = "Organisaation oid", required = true) @PathParam("oid") String oid);

    @POST
    @Path("/{oid}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Päivittää oid:n määrittämän organisaation tiedot",
            notes = "Operaatio päivittää oid:n määrittämän organisaation tiedot.",
            response = OrganisaatioRDTO.class)
    public OrganisaatioRDTO updateOrganisaatio(@PathParam("oid") String oid, OrganisaatioRDTO ordto);

    @DELETE
    @Path("/{oid}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(
            value = "Poistaa oid:n määrittämän organisaation",
            notes = "Operaatio poistaa organisaation annetulla oid:llä.",
            response = String.class)
    public String deleteOrganisaatio(@PathParam("oid") String oid);

    @PUT
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Luo uuden organisaation",
            notes = "Operaatio luo uuden organisaation annetusta JSON:sta.",
            response = OrganisaatioRDTO.class)
    public OrganisaatioRDTO newOrganisaatio(OrganisaatioRDTO ordto);

    @GET
    @Path("/myroles")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(
            value = "Hakee autentikoituneen käyttäjän roolit",
            notes = "Operaatio palauttaa samat kuin /cas/myroles. HUOM! Testikäyttöön tarkoitettu.",
            response = String.class)
    public String getRoles();

    @GET
    @Path("/yhteystietometadata")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(
            value = "Hakee sallitut yhteystietotyypit",
            notes = "Operaatio palauttaa annetuille organisaatiotyypeille sallitut yhteystietotyypit.",
            response = YhteystietojenTyyppiRDTO.class,
            responseContainer = "List")
    public List<YhteystietojenTyyppiRDTO> getYhteystietoMetadata(@QueryParam("organisaatioTyyppi") List<String> organisaatioTyyppi);

    @GET
    @Path("/auth")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(
            value = "Testaa autentikoituneen käyttäjän",
            notes = "Operaatiota käytetään ennen ensimmäistä autenttikoitua POST-kutsua. "
                    + "Näin vältetään CAS + autentikoitu POST redirection ongelma.",
            response = String.class)
    public String authHello();

}

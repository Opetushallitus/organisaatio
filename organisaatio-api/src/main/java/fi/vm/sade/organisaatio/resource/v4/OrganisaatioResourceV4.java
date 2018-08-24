package fi.vm.sade.organisaatio.resource.v4;

import fi.vm.sade.organisaatio.api.DateParam;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.dto.v4.ResultRDTOV4;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * V4 REST services for Organisaatio.
 *
 * Changes to V3:
 * <ul>
 * <li>organisaatiotyypit as codeelement values</li>
 * </ul>
 */
@Path("/organisaatio/v4")
@Api(value = "/organisaatio/v4", description = "Organisaation operaatiot (rajapintaversio 4)")
public interface OrganisaatioResourceV4 {

    @POST
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/findbyoids")
    @ApiOperation(
            value = "Hakee monta ei-poistettua organisaatiota kerralla syötetyille OIDeille (maksimissaan 1000)",
            response = OrganisaatioRDTOV4.class,
            responseContainer = "List")
    List<OrganisaatioRDTOV4> findByOids(@ApiParam(value = "JSON-taulukko organisaatio OIDeja: [\"oid1\", \"oid2\", ...]",
            required = true) List<String> oids);

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{oid}/children")
    @ApiOperation(
            value = "Hakee organisaation alla olevat organisaatiot",
            notes = "Operaatio palauttaa organisaation alla olevat organisaatiot.",
            response = OrganisaatioRDTOV4.class,
            responseContainer = "List")
    List<OrganisaatioRDTOV4> children(
            @ApiParam(value = "Organisaation oid", required = true) @PathParam("oid") String oid,
            @ApiParam(value = "Palaulautetaanko vastauksen mukana mahdollinen organisaation kuva (voi olla iso).",
                    required = false, defaultValue = "false") @DefaultValue("false") @QueryParam("includeImage") boolean includeImage) throws Exception;


    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(
            value = "Hakee yhden organisaation annetulla id:llä (id voi olla oid, y-tunnus, virastotunnus, oppilaitoskoodi tai toimipistekoodi).",
            notes = "Operaatio palauttaa id:n määrittämän organisaation tiedot.",
            response = OrganisaatioRDTOV4.class)
    OrganisaatioRDTOV4 getOrganisaatioByOID(
            @ApiParam(value = "Organisaation oid, y-tunnus, virastotunnus, oppilaitoskoodi tai toimipistekoodi.",
                    required = true) @PathParam("id") String oid,
            @ApiParam(value = "Palaulautetaanko vastauksen mukana mahdollinen organisaation kuva (voi olla iso).",
                    required = false, defaultValue = "false") @DefaultValue("false") @QueryParam("includeImage") boolean includeImage);

    @PUT
    @Path("/{oid}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiImplicitParams(
            @ApiImplicitParam(dataType = "java.io.File", name = "organisaatio",
                    value = "Organisaation tiedot json muodossa", paramType = "body"))
    @ApiOperation(
            value = "Päivittää oid:n määrittämän organisaation tiedot",
            notes = "Operaatio päivittää oid:n määrittämän organisaation tiedot.",
            response = OrganisaatioRDTOV4.class)
    ResultRDTOV4 updateOrganisaatio(
            @ApiParam(value = "Organisaation oid", required = true) @PathParam("oid") String oid,
            @ApiParam(access = "hidden") OrganisaatioRDTOV4 ordto);

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiImplicitParams(
            @ApiImplicitParam(dataType = "java.io.File", name = "organisaatio",
                    value = "Luotavan organisaation tiedot json muodossa", paramType = "body"))
    @ApiOperation(
            value = "Luo uuden organisaation",
            notes = "Operaatio luo uuden organisaation annetusta JSON:sta.",
            response = OrganisaatioRDTOV4.class)
    ResultRDTOV4 newOrganisaatio(@ApiParam(access = "hidden") OrganisaatioRDTOV4 ordto);

    @GET
    @Path("/muutetut")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(
            value = "Hakee organisaatioiden tiedot, joita muutettu annetun päivämäärän jälkeen",
            response = OrganisaatioRDTOV4.class,
            responseContainer = "List")
    List<OrganisaatioRDTOV4> haeMuutetut(@ApiParam(value = "Muokattu jälkeen", required = true) @QueryParam("lastModifiedSince") DateParam date,
                                         @ApiParam(value = "Palaulautetaanko vastauksen mukana mahdollinen organisaation kuva (voi olla iso).",
                                                 required = false, defaultValue = "false") @DefaultValue("false") @QueryParam("includeImage") boolean includeImage);

}

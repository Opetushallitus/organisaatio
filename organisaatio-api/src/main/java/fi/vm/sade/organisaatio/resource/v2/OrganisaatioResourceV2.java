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

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import fi.vm.sade.organisaatio.api.search.OrganisaatioHakutulos;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioHakutulosSuppeaDTOV2;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioNimiDTOV2;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioPaivittajaDTOV2;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioSearchCriteriaDTOV2;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioYhteystiedotDTOV2;
import fi.vm.sade.organisaatio.dto.v2.YhteystiedotSearchCriteriaDTOV2;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioLOPTietoDTOV2;
import java.util.Date;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
    @ApiOperation(
            value = "Testioperaatio, jolla voi kokeilla onko organisaatiopalvelu pystyssä.",
            notes = "Operaatio vastaa tervehdykseen ja palauttaa palvelun aikaleiman.",
            response = String.class)
    public String hello();

    @GET
    @Path("/hierarkia/hae")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(
            value = "Hakee organisaatiot puurakenteena annetuilla hakuehdoilla",
            notes = "Operaatio palauttaa hakuehtoja vastaavat organisaatiot puurakenteena. "
                    + "Hakuehtojen osuessa hierarkiassa ylemmän tason organisaatioon, "
                    + "palautetaan alemman tason organisaatio myös, siis puurakenne lehtiin asti."
                    + "Hakuehtojen osuessa hierarkiassa alemman tason organisaatioon, "
                    + "palautetaan puurakenne juureen asti (ellei hakuehdot sitä estä).",
            response = OrganisaatioHakutulos.class)
    public OrganisaatioHakutulos searchOrganisaatioHierarkia(@QueryParam("") @ApiParam(value = "hakuehdot", required = true)
            OrganisaatioSearchCriteriaDTOV2 hakuEhdot);

    @GET
    @Path("/hierarkia/hae/nimi")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(
            value = "Hakee organisaatioiden suppeat tiedot puurakenteena annetuilla hakuehdoilla",
            notes = "Operaatio palauttaa hakuehtoja vastaavat organisaatiot puurakenteena. "
                    + "Hakuehtojen osuessa hierarkiassa ylemmän tason organisaatioon, "
                    + "palautetaan alemman tason organisaatio myös, siis puurakenne lehtiin asti. "
                    + "Hakuehtojen osuessa hierarkiassa alemman tason organisaatioon, "
                    + "palautetaan puurakenne juureen asti (ellei hakuehdot sitä estä). "
                    + "Soveltuu käytettäväksi haun \"hierarkia/hae\" sijaan silloin kuin paluuarvossa riittää organisaation nimi ja oid.",
            response = OrganisaatioHakutulosSuppeaDTOV2.class)
    public OrganisaatioHakutulosSuppeaDTOV2 searchOrganisaatioHierarkiaNimet(@QueryParam("") @ApiParam(value = "hakuehdot", required = true)
            OrganisaatioSearchCriteriaDTOV2 hakuEhdot);

    @GET
    @Path("/hae")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(
            value = "Hakee organisaatiot, jotka osuvat annetuihin hakuehtoihin",
            notes = "Operaatio palauttaa vain hakuehtoja vastaavat organisaatiot.",
            response = OrganisaatioHakutulos.class)
    public OrganisaatioHakutulos searchOrganisaatiot(@QueryParam("") @ApiParam(value = "hakuehdot", required = true)
            OrganisaatioSearchCriteriaDTOV2 hakuEhdot);

    @GET
    @Path("/hae/nimi")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(
            value = "Hakee annetuihin hakuehtoihin osuvien organisaatioiden suppeat tiedot",
            notes = "Operaatio palauttaa vain hakuehtoja vastaavien organisaatioiden suppeat tiedot."
                    + "Soveltuu käytettäväksi haun \"/hae\" sijaan silloin kuin paluuarvossa riittää organisaation nimi ja oid.",
            response = OrganisaatioHakutulosSuppeaDTOV2.class)
    public OrganisaatioHakutulosSuppeaDTOV2 searchOrganisaatiotNimet(@QueryParam("") @ApiParam(value = "hakuehdot", required = true)
            OrganisaatioSearchCriteriaDTOV2 hakuEhdot);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/yhteystiedot/hae")
    @ApiOperation(
            value = "Hakee organisaatioita annetuilla hakukriteereillä ja palauttaa yhteystiedot",
            notes = "Operaatio palauttaa hakukriteerit täyttävien organisaatioiden yhteystiedot.")
    public List<OrganisaatioYhteystiedotDTOV2> searchOrganisaatioYhteystiedot(YhteystiedotSearchCriteriaDTOV2 hakuEhdot);

    @GET
    @Path("/{oid}/paivittaja")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(
            value = "Hakee organisaation viimeisimmän päivittäjän tiedot.",
            notes = "Operaatio palauttaa oid:n määrittämän organisaation viimeisimmän päivittäjän.",
            response = OrganisaatioPaivittajaDTOV2.class)
    public OrganisaatioPaivittajaDTOV2 getOrganisaatioPaivittaja(@ApiParam(value = "Organisaation oid", required = true) @PathParam("oid") String oid) throws Exception;

    @GET
    @Path("/{oid}/nimet")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(
            value = "Hakee organisaation nimihistorian.",
            notes = "Operaatio palauttaa oid:n määrittämän organisaation nimihistorian.",
            response = OrganisaatioNimiDTOV2.class)
    public List<OrganisaatioNimiDTOV2> getOrganisaatioNimet(@ApiParam(value = "Organisaation oid", required = true) @PathParam("oid") String oid) throws Exception;

    @GET
    @Path("/{id}/LOP")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(
            value = "Hakee yhden organisaation LOP tiedot.",
            notes = "Operaatio palauttaa id:n määrittämän organisaation LOP tiedot (id voi olla oid, y-tunnus, virastotunnus, oppilaitoskoodi tai toimipistekoodi). Soveltuu käytettäväksi haun \"/{id}\" sijaan silloin kuin paluuarvossa riittää pelkät LOP tiedot.",
            response = OrganisaatioLOPTietoDTOV2.class)
    public OrganisaatioLOPTietoDTOV2 getOrganisaationLOPTiedotByOID(@ApiParam(value = "Organisaation oid.", required = true) @PathParam("id") String oid);

    @PUT
    @Path("/{oid}/nimet")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Luo uuden nimen organisaatiolle",
            notes = "Operaatio luo uuden nimen organisaatiolle annetusta JSON:sta.",
            response = OrganisaatioNimiDTOV2.class)
    public OrganisaatioNimiDTOV2 newOrganisaatioNimi(@ApiParam(value = "Organisaation oid", required = true) @PathParam("oid") String oid, OrganisaatioNimiDTOV2 nimidto) throws Exception;

    @POST
    @Path("/{oid}/nimet/{date: [0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Päivittää oid:n määrittämän organisaation nimen, jonka aikaisempi alkupäivämäärä on annettu date",
            notes = "Operaatio päivittää oid:n määrittämän organisaation nimen, jonka aikaisempi alkupäivämäärä on annettu date.",
            response = OrganisaatioNimiDTOV2.class)
    public OrganisaatioNimiDTOV2 updateOrganisaatioNimi(@PathParam("oid") String oid, @PathParam("date") Date date, OrganisaatioNimiDTOV2 nimidto);

    @DELETE
    @Path("/{oid}/nimet/{date: [0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(
            value = "Poistaa oid:n määrittämän organisaation nimen, jonka alkupäivämäärä on annettu date",
            notes = "Operaatio poistaa oid:n määrittämän organisaation nimen, jonka aikaisempi alkupäivämäärä on annettu date.",
            response = String.class)
    public String deleteOrganisaatioNimi(@PathParam("oid") String oid, @PathParam("date") Date date);
}

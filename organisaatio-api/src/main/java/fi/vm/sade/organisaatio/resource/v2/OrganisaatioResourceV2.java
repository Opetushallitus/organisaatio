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
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import fi.vm.sade.organisaatio.api.DateParam;
import fi.vm.sade.organisaatio.api.search.OrganisaatioHakutulos;
import fi.vm.sade.organisaatio.dto.v2.*;
import fi.vm.sade.organisaatio.resource.dto.HakutoimistoDTO;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;


/**
 * V2 REST services for Organisaatio.
 *
 * @author simok
 */
@Path("/organisaatio/v2")
@Api(value = "/organisaatio/v2", description = "Organisaation operaatiot (rajapintaversio 2)")
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
    @ApiImplicitParams({
        @ApiImplicitParam(dataType = "String",  name = "searchStr", value = "Hakuteksti", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "boolean", name = "aktiiviset", value = "Aktiiviset organisaatiot mukaan hakutuloksiin", paramType = "query", required = true),
        @ApiImplicitParam(dataType = "boolean", name = "suunnitellut", value = "Suunnitellut organisaatiot mukaan hakutuloksiin", paramType = "query", required = true),
        @ApiImplicitParam(dataType = "boolean", name = "lakkautetut", value = "Lakkautetut organisaatiot mukaan hakutuloksiin", paramType = "query", required = true, defaultValue = "false"),
        @ApiImplicitParam(dataType = "Set<String>",  name = "kunta", value = "Haettavan organisaation kunta tai lista kunnista", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "String",  name = "organisaatiotyyppi", value = "Haettavan organisaation tyyppi", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "Set<String>",  name = "oppilaitostyyppi", value = "Haettavan oppilaitoksen tyyppi tai lista tyypeistä", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "Set<String>",  name = "kieli", value = "Haettavan organisaation kieli tai lista kielistä", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "List<String>",  name = "oidResctrictionList", value = "Lista sallituista organisaatioiden oid:stä", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "String",  name = "oid", value = "Haku oid:lla. Hakuteksti jätetään huomioimatta jos oid on annettu.", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "boolean", name = "skipParents", value = "Jätetäänkö yläorganisaatiot pois hakutuloksista", paramType = "query", required = false)})
    @ApiOperation(
            value = "Hakee organisaatiot puurakenteena annetuilla hakuehdoilla",
            notes = "Operaatio palauttaa hakuehtoja vastaavat organisaatiot puurakenteena. "
                    + "Hakuehtojen osuessa hierarkiassa ylemmän tason organisaatioon, "
                    + "palautetaan alemman tason organisaatio myös, siis puurakenne lehtiin asti."
                    + "Hakuehtojen osuessa hierarkiassa alemman tason organisaatioon, "
                    + "palautetaan puurakenne juureen asti (ellei hakuehdot sitä estä).",
            response = OrganisaatioHakutulos.class)
    public OrganisaatioHakutulos searchOrganisaatioHierarkia(@QueryParam("") @ApiParam(access = "hidden")
            OrganisaatioSearchCriteriaDTOV2 hakuEhdot);

    @GET
    @Path("/hierarkia/hae/nimi")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiImplicitParams({
        @ApiImplicitParam(dataType = "String",  name = "searchStr", value = "Hakuteksti", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "boolean", name = "aktiiviset", value = "Aktiiviset organisaatiot mukaan hakutuloksiin", paramType = "query", required = true),
        @ApiImplicitParam(dataType = "boolean", name = "suunnitellut", value = "Suunnitellut organisaatiot mukaan hakutuloksiin", paramType = "query", required = true),
        @ApiImplicitParam(dataType = "boolean", name = "lakkautetut", value = "Lakkautetut organisaatiot mukaan hakutuloksiin", paramType = "query", required = true, defaultValue = "false"),
        @ApiImplicitParam(dataType = "Set<String>",  name = "kunta", value = "Haettavan organisaation kunta tai lista kunnista", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "String",  name = "organisaatiotyyppi", value = "Haettavan organisaation tyyppi", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "Set<String>",  name = "oppilaitostyyppi", value = "Haettavan oppilaitoksen tyyppi tai lista tyypeistä", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "Set<String>",  name = "kieli", value = "Haettavan organisaation kieli tai lista kielistä", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "List<String>",  name = "oidResctrictionList", value = "Lista sallituista organisaatioiden oid:stä", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "String",  name = "oid", value = "Haku oid:lla. Hakuteksti jätetään huomioimatta jos oid on annettu.", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "boolean", name = "skipParents", value = "Jätetäänkö yläorganisaatiot pois hakutuloksista", paramType = "query", required = false)})
    @ApiOperation(
            value = "Hakee organisaatioiden nimi- ja oid tiedot puurakenteena annetuilla hakuehdoilla",
            notes = "Operaatio palauttaa hakuehtoja vastaavat organisaatiot puurakenteena. "
                    + "Hakuehtojen osuessa hierarkiassa ylemmän tason organisaatioon, "
                    + "palautetaan alemman tason organisaatio myös, siis puurakenne lehtiin asti. "
                    + "Hakuehtojen osuessa hierarkiassa alemman tason organisaatioon, "
                    + "palautetaan puurakenne juureen asti (ellei hakuehdot sitä estä). "
                    + "Soveltuu käytettäväksi haun \"hierarkia/hae\" sijaan silloin kuin paluuarvossa riittää organisaation nimi ja oid.",
            response = OrganisaatioHakutulosSuppeaDTOV2.class)
    public OrganisaatioHakutulosSuppeaDTOV2 searchOrganisaatioHierarkiaNimet(@QueryParam("") @ApiParam(access = "hidden")
            OrganisaatioSearchCriteriaDTOV2 hakuEhdot);

    @GET
    @Path("/hierarkia/hae/tyyppi")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiImplicitParams({
        @ApiImplicitParam(dataType = "String",  name = "searchStr", value = "Hakuteksti", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "boolean", name = "aktiiviset", value = "Aktiiviset organisaatiot mukaan hakutuloksiin", paramType = "query", required = true),
        @ApiImplicitParam(dataType = "boolean", name = "suunnitellut", value = "Suunnitellut organisaatiot mukaan hakutuloksiin", paramType = "query", required = true),
        @ApiImplicitParam(dataType = "boolean", name = "lakkautetut", value = "Lakkautetut organisaatiot mukaan hakutuloksiin", paramType = "query", required = true, defaultValue = "false"),
        @ApiImplicitParam(dataType = "Set<String>",  name = "kunta", value = "Haettavan organisaation kunta tai lista kunnista", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "String",  name = "organisaatiotyyppi", value = "Haettavan organisaation tyyppi", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "Set<String>",  name = "oppilaitostyyppi", value = "Haettavan oppilaitoksen tyyppi tai lista tyypeistä", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "Set<String>",  name = "kieli", value = "Haettavan organisaation kieli tai lista kielistä", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "List<String>",  name = "oidResctrictionList", value = "Lista sallituista organisaatioiden oid:stä", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "String",  name = "oid", value = "Haku oid:lla. Hakuteksti jätetään huomioimatta jos oid on annettu.", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "boolean", name = "skipParents", value = "Jätetäänkö yläorganisaatiot pois hakutuloksista", paramType = "query", required = false)})
    @ApiOperation(
            value = "Hakee organisaatioiden nimi-, oid-, ja tyyppitiedot puurakenteena annetuilla hakuehdoilla",
            notes = "Operaatio palauttaa hakuehtoja vastaavat organisaatiot puurakenteena. "
                    + "Hakuehtojen osuessa hierarkiassa ylemmän tason organisaatioon, "
                    + "palautetaan alemman tason organisaatio myös, siis puurakenne lehtiin asti. "
                    + "Hakuehtojen osuessa hierarkiassa alemman tason organisaatioon, "
                    + "palautetaan puurakenne juureen asti (ellei hakuehdot sitä estä). "
                    + "Soveltuu käytettäväksi haun \"hierarkia/hae\" sijaan silloin kuin paluuarvossa riittää organisaation nimi, oid, organisaatiotyypit ja mahdollinen oppilaitostyyppi.",
            response = OrganisaatioHakutulosSuppeaDTOV2.class)
    public OrganisaatioHakutulosSuppeaDTOV2 searchOrganisaatioHierarkiaTyypit(@QueryParam("") @ApiParam(access = "hidden")
            OrganisaatioSearchCriteriaDTOV2 hakuEhdot);

    @GET
    @Path("/hae")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiImplicitParams({
        @ApiImplicitParam(dataType = "String",  name = "searchStr", value = "Hakuteksti", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "boolean", name = "aktiiviset", value = "Aktiiviset organisaatiot mukaan hakutuloksiin", paramType = "query", required = true),
        @ApiImplicitParam(dataType = "boolean", name = "suunnitellut", value = "Suunnitellut organisaatiot mukaan hakutuloksiin", paramType = "query", required = true),
        @ApiImplicitParam(dataType = "boolean", name = "lakkautetut", value = "Lakkautetut organisaatiot mukaan hakutuloksiin", paramType = "query", required = true, defaultValue = "false"),
        @ApiImplicitParam(dataType = "Set<String>",  name = "kunta", value = "Haettavan organisaation kunta tai lista kunnista", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "String",  name = "organisaatiotyyppi", value = "Haettavan organisaation tyyppi", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "Set<String>",  name = "oppilaitostyyppi", value = "Haettavan oppilaitoksen tyyppi tai lista tyypeistä", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "Set<String>",  name = "kieli", value = "Haettavan organisaation kieli tai lista kielistä", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "List<String>",  name = "oidResctrictionList", value = "Lista sallituista organisaatioiden oid:stä", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "String",  name = "oid", value = "Haku oid:lla. Hakuteksti jätetään huomioimatta jos oid on annettu.", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "boolean", name = "skipParents", value = "Jätetäänkö yläorganisaatiot pois hakutuloksista", paramType = "query", required = false)})
    @ApiOperation(
            value = "Hakee organisaatiot, jotka osuvat annetuihin hakuehtoihin",
            notes = "Operaatio palauttaa vain hakuehtoja vastaavat organisaatiot.",
            response = OrganisaatioHakutulos.class)
    public OrganisaatioHakutulos searchOrganisaatiot(@QueryParam("") @ApiParam(access = "hidden")
            OrganisaatioSearchCriteriaDTOV2 hakuEhdot);

    @GET
    @Path("/hae/nimi")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiImplicitParams({
        @ApiImplicitParam(dataType = "String",  name = "searchStr", value = "Hakuteksti", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "boolean", name = "aktiiviset", value = "Aktiiviset organisaatiot mukaan hakutuloksiin", paramType = "query", required = true),
        @ApiImplicitParam(dataType = "boolean", name = "suunnitellut", value = "Suunnitellut organisaatiot mukaan hakutuloksiin", paramType = "query", required = true),
        @ApiImplicitParam(dataType = "boolean", name = "lakkautetut", value = "Lakkautetut organisaatiot mukaan hakutuloksiin", paramType = "query", required = true, defaultValue = "false"),
        @ApiImplicitParam(dataType = "Set<String>",  name = "kunta", value = "Haettavan organisaation kunta tai lista kunnista", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "String",  name = "organisaatiotyyppi", value = "Haettavan organisaation tyyppi", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "Set<String>",  name = "oppilaitostyyppi", value = "Haettavan oppilaitoksen tyyppi tai lista tyypeistä", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "Set<String>",  name = "kieli", value = "Haettavan organisaation kieli tai lista kielistä", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "List<String>",  name = "oidResctrictionList", value = "Lista sallituista organisaatioiden oid:stä", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "String",  name = "oid", value = "Haku oid:lla. Hakuteksti jätetään huomioimatta jos oid on annettu.", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "boolean", name = "skipParents", value = "Jätetäänkö yläorganisaatiot pois hakutuloksista", paramType = "query", required = false)})
    @ApiOperation(
            value = "Hakee annetuihin hakuehtoihin osuvien organisaatioiden nimi- ja oid tiedot",
            notes = "Operaatio palauttaa vain hakuehtoja vastaavien organisaatioiden suppeat tiedot."
                    + "Soveltuu käytettäväksi haun \"/hae\" sijaan silloin kuin paluuarvossa riittää organisaation nimi ja oid.",
            response = OrganisaatioHakutulosSuppeaDTOV2.class)
    public OrganisaatioHakutulosSuppeaDTOV2 searchOrganisaatiotNimet(@QueryParam("") @ApiParam(access = "hidden")
            OrganisaatioSearchCriteriaDTOV2 hakuEhdot);

    @GET
    @Path("/hae/tyyppi")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiImplicitParams({
        @ApiImplicitParam(dataType = "String",  name = "searchStr", value = "Hakuteksti", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "boolean", name = "aktiiviset", value = "Aktiiviset organisaatiot mukaan hakutuloksiin", paramType = "query", required = true),
        @ApiImplicitParam(dataType = "boolean", name = "suunnitellut", value = "Suunnitellut organisaatiot mukaan hakutuloksiin", paramType = "query", required = true),
        @ApiImplicitParam(dataType = "boolean", name = "lakkautetut", value = "Lakkautetut organisaatiot mukaan hakutuloksiin", paramType = "query", required = true, defaultValue = "false"),
        @ApiImplicitParam(dataType = "Set<String>",  name = "kunta", value = "Haettavan organisaation kunta tai lista kunnista", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "String",  name = "organisaatiotyyppi", value = "Haettavan organisaation tyyppi", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "Set<String>",  name = "oppilaitostyyppi", value = "Haettavan oppilaitoksen tyyppi tai lista tyypeistä", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "Set<String>",  name = "kieli", value = "Haettavan organisaation kieli tai lista kielistä", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "List<String>",  name = "oidResctrictionList", value = "Lista sallituista organisaatioiden oid:stä", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "String",  name = "oid", value = "Haku oid:lla. Hakuteksti jätetään huomioimatta jos oid on annettu.", paramType = "query", required = false),
        @ApiImplicitParam(dataType = "boolean", name = "skipParents", value = "Jätetäänkö yläorganisaatiot pois hakutuloksista", paramType = "query", required = false)})
    @ApiOperation(
            value = "Hakee annetuihin hakuehtoihin osuvien organisaatioiden nimi-, oid-, ja tyyppitiedot",
            notes = "Operaatio palauttaa vain hakuehtoja vastaavien organisaatioiden suppeat tiedot."
                    + "Soveltuu käytettäväksi haun \"/hae\" sijaan silloin kuin paluuarvossa riittää organisaation nimi, oid, organisaatiotyypit ja mahdollinen oppilaitostyyppi.",
            response = OrganisaatioHakutulosSuppeaDTOV2.class)
    public OrganisaatioHakutulosSuppeaDTOV2 searchOrganisaatiotTyypit(@QueryParam("") @ApiParam(access = "hidden")
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
            notes = "Operaatio palauttaa id:n määrittämän organisaation LOP (Learning Opportunity Provider) tiedot (id voi olla oid, y-tunnus, virastotunnus, oppilaitoskoodi tai toimipistekoodi). Soveltuu käytettäväksi haun \"/{id}\" sijaan silloin kuin paluuarvossa riittää pelkät LOP tiedot.",
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
    public OrganisaatioNimiDTOV2 updateOrganisaatioNimi(@PathParam("oid") String oid, @PathParam("date") DateParam date, OrganisaatioNimiDTOV2 nimidto);

    @DELETE
    @Path("/{oid}/nimet/{date: [0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(
            value = "Poistaa oid:n määrittämän organisaation nimen, jonka alkupäivämäärä on annettu date",
            notes = "Operaatio poistaa oid:n määrittämän organisaation nimen, jonka aikaisempi alkupäivämäärä on annettu date.",
            response = String.class)
    public String deleteOrganisaatioNimi(@PathParam("oid") String oid, @PathParam("date") DateParam date);

    @PUT
    @Path("/muokkaamonta")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Muokkaa monta organisaatiota kerralla",
            notes = "Operaatio muokkaa annettujen organisaatioden annetut tiedot.",
            response = OrganisaatioNimiDTOV2.class)
    public OrganisaatioMuokkausTulosListaDTO muokkaaMontaOrganisaatiota(List<OrganisaatioMuokkausTiedotDTO> tiedot);

    @GET
    @Path("/muutetut/oid")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(
            value = "Hakee organisaatioiden OID:t, joita muutettu annetun päivämäärän jälkeen",
            response = String.class)
    public String haeMuutettujenOid(@ApiParam(value = "Muokattu jälkeen", required = true) @QueryParam("lastModifiedSince") DateParam date);

    @GET
    @Path("/muutetut")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(
            value = "Hakee organisaatioiden tiedot, joita muutettu annetun päivämäärän jälkeen",
            response = OrganisaatioRDTO.class,
            responseContainer = "List")
    public List<OrganisaatioRDTO> haeMuutetut(@ApiParam(value = "Muokattu jälkeen", required = true) @QueryParam("lastModifiedSince") DateParam date,
            @ApiParam(value = "Palaulautetaanko vastauksen mukana mahdollinen organisaation kuva (voi olla iso).",
                    required = false, defaultValue = "false") @DefaultValue("false") @QueryParam("includeImage") boolean includeImage);

    @GET
    @Path("/{oid}/historia")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(
            value = "Hakee organisaation rakennehistorian.",
            notes = "Operaatio palauttaa oid:n määrittelemän organisaation rakennehistorian.",
            response = OrganisaatioHistoriaRDTOV2.class)
    public OrganisaatioHistoriaRDTOV2 getOrganizationHistory(@ApiParam(value = "Organisaation oid", required = true) @PathParam("oid") String oid) throws Exception;

    @POST
    @Path("/{oid}/organisaatiosuhde")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Siirtää organisaatiota puussa toisen ylemmän organisaation alle tai yhdistää kaksi samanarvoista organisaatiota")
    public void changeOrganisationRelationship(
            @ApiParam(value = "Organisaation oid", required = true) @PathParam("oid") String oid,
            @ApiParam(value = "Sulautus", required = true) @QueryParam("merge") boolean merge,
            @ApiParam(value = "Siirto päivämäärä, jos päivämäärää ei ole asetettu käytetään tätä päivämäärää", required = false) @QueryParam("moveDate") DateParam date,
            @ApiParam(value = "Uusi isäntäorganisaatio", required = true) String newParentOid
    );

    @GET
    @Path("/liitokset")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(
            value = "Hakee organisaatioiden tiedot, joita muutettu annetun päivämäärän jälkeen",
            response = OrganisaatioLiitosDTOV2.class)
    public List<OrganisaatioLiitosDTOV2> haeLiitokset(
            @ApiParam(value = "Liitokset jälkeen", required = false) @QueryParam("liitoksetAlkaen") DateParam date);

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/ryhmat")
    @ApiOperation(
            value = "Hakee organisaation alla olevat ryhmät",
            notes = "Operaatio palauttaa organisaation alla olevat ryhmät.",
            response = OrganisaatioGroupDTOV2.class)
    public List<OrganisaatioGroupDTOV2> groups() throws Exception;

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF8")
    @Path("/{oid}/hakutoimisto")
    @ApiOperation(
            value = "Hakee organisaation hakutoimiston ",
            notes = "Hakutoimisto haetaan tarvittaessa rekursiivisesti",
            response = HakutoimistoDTO.class)
    Response hakutoimisto(@PathParam("oid") String organisaatioOid);
}

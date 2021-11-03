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

import fi.vm.sade.organisaatio.api.DateParam;
import fi.vm.sade.organisaatio.api.search.OrganisaatioHakutulos;
import fi.vm.sade.organisaatio.dto.v2.*;
import fi.vm.sade.organisaatio.resource.dto.HakutoimistoDTO;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.organisaatio.resource.dto.RyhmaCriteriaDtoV2;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * V2 REST services for Organisaatio.
 *
 * @author simok
 */
public interface OrganisaatioResourceV2 {

    @GetMapping(path = "/hello", produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation(
            value = "Testioperaatio, jolla voi kokeilla onko organisaatiopalvelu pystyssä.",
            notes = "Operaatio vastaa tervehdykseen ja palauttaa palvelun aikaleiman.",
            response = String.class)
    String hello();

    @GetMapping(path = "/hierarkia/hae", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "String", name = "searchStr", value = "Hakuteksti", paramType = "query"),
            @ApiImplicitParam(dataType = "boolean", name = "aktiiviset", value = "Aktiiviset organisaatiot mukaan hakutuloksiin", paramType = "query", required = true),
            @ApiImplicitParam(dataType = "boolean", name = "suunnitellut", value = "Suunnitellut organisaatiot mukaan hakutuloksiin", paramType = "query", required = true),
            @ApiImplicitParam(dataType = "boolean", name = "lakkautetut", value = "Lakkautetut organisaatiot mukaan hakutuloksiin", paramType = "query", required = true, defaultValue = "false"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true, name = "kunta", value = "Haettavan organisaation kunta tai lista kunnista", paramType = "query"),
            @ApiImplicitParam(dataType = "String", name = "organisaatiotyyppi", value = "Haettavan organisaation tyyppi", paramType = "query"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true, name = "oppilaitostyyppi", value = "Haettavan oppilaitoksen tyyppi tai lista tyypeistä", paramType = "query"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true, name = "kieli", value = "Haettavan organisaation kieli tai lista kielistä", paramType = "query"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true, name = "oidRestrictionList", value = "Lista sallituista organisaatioiden oid:stä", paramType = "query"),
            @ApiImplicitParam(dataType = "String", name = "oid", value = "Haku oid:lla. Hakuteksti jätetään huomioimatta jos oid on annettu.", paramType = "query"),
            @ApiImplicitParam(dataType = "boolean", name = "skipParents", value = "Jätetäänkö yläorganisaatiot pois hakutuloksista", paramType = "query")})
    @ApiOperation(
            value = "Hakee organisaatiot puurakenteena annetuilla hakuehdoilla",
            notes = "Operaatio palauttaa hakuehtoja vastaavat organisaatiot puurakenteena. "
                    + "Hakuehtojen osuessa hierarkiassa ylemmän tason organisaatioon, "
                    + "palautetaan alemman tason organisaatio myös, siis puurakenne lehtiin asti."
                    + "Hakuehtojen osuessa hierarkiassa alemman tason organisaatioon, "
                    + "palautetaan puurakenne juureen asti (ellei hakuehdot sitä estä).",
            response = OrganisaatioHakutulos.class)
    OrganisaatioHakutulos searchOrganisaatioHierarkia(@ApiParam(access = "hidden")
                                                              OrganisaatioSearchCriteriaDTOV2 hakuEhdot);

    @GetMapping(path = "/hierarkia/hae/nimi", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "String", name = "searchStr", value = "Hakuteksti", paramType = "query"),
            @ApiImplicitParam(dataType = "boolean", name = "aktiiviset", value = "Aktiiviset organisaatiot mukaan hakutuloksiin", paramType = "query", required = true),
            @ApiImplicitParam(dataType = "boolean", name = "suunnitellut", value = "Suunnitellut organisaatiot mukaan hakutuloksiin", paramType = "query", required = true),
            @ApiImplicitParam(dataType = "boolean", name = "lakkautetut", value = "Lakkautetut organisaatiot mukaan hakutuloksiin", paramType = "query", required = true, defaultValue = "false"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true, name = "kunta", value = "Haettavan organisaation kunta tai lista kunnista", paramType = "query"),
            @ApiImplicitParam(dataType = "String", name = "organisaatiotyyppi", value = "Haettavan organisaation tyyppi", paramType = "query"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true, name = "oppilaitostyyppi", value = "Haettavan oppilaitoksen tyyppi tai lista tyypeistä", paramType = "query"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true, name = "kieli", value = "Haettavan organisaation kieli tai lista kielistä", paramType = "query"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true, name = "oidRestrictionList", value = "Lista sallituista organisaatioiden oid:stä", paramType = "query"),
            @ApiImplicitParam(dataType = "String", name = "oid", value = "Haku oid:lla. Hakuteksti jätetään huomioimatta jos oid on annettu.", paramType = "query"),
            @ApiImplicitParam(dataType = "boolean", name = "skipParents", value = "Jätetäänkö yläorganisaatiot pois hakutuloksista", paramType = "query")})
    @ApiOperation(
            value = "Hakee organisaatioiden nimi- ja oid tiedot puurakenteena annetuilla hakuehdoilla",
            notes = "Operaatio palauttaa hakuehtoja vastaavat organisaatiot puurakenteena. "
                    + "Hakuehtojen osuessa hierarkiassa ylemmän tason organisaatioon, "
                    + "palautetaan alemman tason organisaatio myös, siis puurakenne lehtiin asti. "
                    + "Hakuehtojen osuessa hierarkiassa alemman tason organisaatioon, "
                    + "palautetaan puurakenne juureen asti (ellei hakuehdot sitä estä). "
                    + "Soveltuu käytettäväksi haun \"hierarkia/hae\" sijaan silloin kuin paluuarvossa riittää organisaation nimi ja oid.",
            response = OrganisaatioHakutulosSuppeaDTOV2.class)
    OrganisaatioHakutulosSuppeaDTOV2 searchOrganisaatioHierarkiaNimet(@ApiParam(access = "hidden")
                                                                              OrganisaatioSearchCriteriaDTOV2 hakuEhdot);

    @GetMapping(path = "/hierarkia/hae/tyyppi", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "String", name = "searchStr", value = "Hakuteksti", paramType = "query"),
            @ApiImplicitParam(dataType = "boolean", name = "aktiiviset", value = "Aktiiviset organisaatiot mukaan hakutuloksiin", paramType = "query", required = true),
            @ApiImplicitParam(dataType = "boolean", name = "suunnitellut", value = "Suunnitellut organisaatiot mukaan hakutuloksiin", paramType = "query", required = true),
            @ApiImplicitParam(dataType = "boolean", name = "lakkautetut", value = "Lakkautetut organisaatiot mukaan hakutuloksiin", paramType = "query", required = true, defaultValue = "false"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true, name = "kunta", value = "Haettavan organisaation kunta tai lista kunnista", paramType = "query"),
            @ApiImplicitParam(dataType = "String", name = "organisaatiotyyppi", value = "Haettavan organisaation tyyppi", paramType = "query"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true, name = "oppilaitostyyppi", value = "Haettavan oppilaitoksen tyyppi tai lista tyypeistä", paramType = "query"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true, name = "kieli", value = "Haettavan organisaation kieli tai lista kielistä", paramType = "query"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true, name = "oidRestrictionList", value = "Lista sallituista organisaatioiden oid:stä", paramType = "query"),
            @ApiImplicitParam(dataType = "String", name = "oid", value = "Haku oid:lla. Hakuteksti jätetään huomioimatta jos oid on annettu.", paramType = "query"),
            @ApiImplicitParam(dataType = "boolean", name = "skipParents", value = "Jätetäänkö yläorganisaatiot pois hakutuloksista", paramType = "query")})
    @ApiOperation(
            value = "Hakee organisaatioiden nimi-, oid-, ja tyyppitiedot puurakenteena annetuilla hakuehdoilla",
            notes = "Operaatio palauttaa hakuehtoja vastaavat organisaatiot puurakenteena. "
                    + "Hakuehtojen osuessa hierarkiassa ylemmän tason organisaatioon, "
                    + "palautetaan alemman tason organisaatio myös, siis puurakenne lehtiin asti. "
                    + "Hakuehtojen osuessa hierarkiassa alemman tason organisaatioon, "
                    + "palautetaan puurakenne juureen asti (ellei hakuehdot sitä estä). "
                    + "Soveltuu käytettäväksi haun \"hierarkia/hae\" sijaan silloin kuin paluuarvossa riittää organisaation nimi, oid, organisaatiotyypit ja mahdollinen oppilaitostyyppi.",
            response = OrganisaatioHakutulosSuppeaDTOV2.class)
    OrganisaatioHakutulosSuppeaDTOV2 searchOrganisaatioHierarkiaTyypit(@ApiParam(access = "hidden")
                                                                               OrganisaatioSearchCriteriaDTOV2 hakuEhdot);

    @GetMapping(path = "/hae", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "String", name = "searchStr", value = "Hakuteksti", paramType = "query"),
            @ApiImplicitParam(dataType = "boolean", name = "aktiiviset", value = "Aktiiviset organisaatiot mukaan hakutuloksiin", paramType = "query", required = true),
            @ApiImplicitParam(dataType = "boolean", name = "suunnitellut", value = "Suunnitellut organisaatiot mukaan hakutuloksiin", paramType = "query", required = true),
            @ApiImplicitParam(dataType = "boolean", name = "lakkautetut", value = "Lakkautetut organisaatiot mukaan hakutuloksiin", paramType = "query", required = true, defaultValue = "false"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true, name = "kunta", value = "Haettavan organisaation kunta tai lista kunnista", paramType = "query"),
            @ApiImplicitParam(dataType = "String", name = "organisaatiotyyppi", value = "Haettavan organisaation tyyppi", paramType = "query"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true, name = "oppilaitostyyppi", value = "Haettavan oppilaitoksen tyyppi tai lista tyypeistä", paramType = "query"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true, name = "kieli", value = "Haettavan organisaation kieli tai lista kielistä", paramType = "query"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true, name = "oidRestrictionList", value = "Lista sallituista organisaatioiden oid:stä", paramType = "query"),
            @ApiImplicitParam(dataType = "String", name = "oid", value = "Haku oid:lla. Hakuteksti jätetään huomioimatta jos oid on annettu.", paramType = "query"),
            @ApiImplicitParam(dataType = "boolean", name = "skipParents", value = "Jätetäänkö yläorganisaatiot pois hakutuloksista", paramType = "query")})
    @ApiOperation(
            value = "Hakee organisaatiot, jotka osuvat annetuihin hakuehtoihin",
            notes = "Operaatio palauttaa vain hakuehtoja vastaavat organisaatiot.",
            response = OrganisaatioHakutulos.class)
    OrganisaatioHakutulos searchOrganisaatiot(@ApiParam(access = "hidden")
                                                      OrganisaatioSearchCriteriaDTOV2 hakuEhdot);

    @GetMapping(path = "/hae/nimi", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "String", name = "searchStr", value = "Hakuteksti", paramType = "query"),
            @ApiImplicitParam(dataType = "boolean", name = "aktiiviset", value = "Aktiiviset organisaatiot mukaan hakutuloksiin", paramType = "query", required = true),
            @ApiImplicitParam(dataType = "boolean", name = "suunnitellut", value = "Suunnitellut organisaatiot mukaan hakutuloksiin", paramType = "query", required = true),
            @ApiImplicitParam(dataType = "boolean", name = "lakkautetut", value = "Lakkautetut organisaatiot mukaan hakutuloksiin", paramType = "query", required = true, defaultValue = "false"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true, name = "kunta", value = "Haettavan organisaation kunta tai lista kunnista", paramType = "query"),
            @ApiImplicitParam(dataType = "String", name = "organisaatiotyyppi", value = "Haettavan organisaation tyyppi", paramType = "query"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true, name = "oppilaitostyyppi", value = "Haettavan oppilaitoksen tyyppi tai lista tyypeistä", paramType = "query"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true, name = "kieli", value = "Haettavan organisaation kieli tai lista kielistä", paramType = "query"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true, name = "oidRestrictionList", value = "Lista sallituista organisaatioiden oid:stä", paramType = "query"),
            @ApiImplicitParam(dataType = "String", name = "oid", value = "Haku oid:lla. Hakuteksti jätetään huomioimatta jos oid on annettu.", paramType = "query"),
            @ApiImplicitParam(dataType = "boolean", name = "skipParents", value = "Jätetäänkö yläorganisaatiot pois hakutuloksista", paramType = "query")})
    @ApiOperation(
            value = "Hakee annetuihin hakuehtoihin osuvien organisaatioiden nimi- ja oid tiedot",
            notes = "Operaatio palauttaa vain hakuehtoja vastaavien organisaatioiden suppeat tiedot."
                    + "Soveltuu käytettäväksi haun \"/hae\" sijaan silloin kuin paluuarvossa riittää organisaation nimi ja oid.",
            response = OrganisaatioHakutulosSuppeaDTOV2.class)
    OrganisaatioHakutulosSuppeaDTOV2 searchOrganisaatiotNimet(@ApiParam(access = "hidden")
                                                                      OrganisaatioSearchCriteriaDTOV2 hakuEhdot);

    @GetMapping(path = "/hae/tyyppi", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "String", name = "searchStr", value = "Hakuteksti", paramType = "query"),
            @ApiImplicitParam(dataType = "boolean", name = "aktiiviset", value = "Aktiiviset organisaatiot mukaan hakutuloksiin", paramType = "query", required = true),
            @ApiImplicitParam(dataType = "boolean", name = "suunnitellut", value = "Suunnitellut organisaatiot mukaan hakutuloksiin", paramType = "query", required = true),
            @ApiImplicitParam(dataType = "boolean", name = "lakkautetut", value = "Lakkautetut organisaatiot mukaan hakutuloksiin", paramType = "query", required = true, defaultValue = "false"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true, name = "kunta", value = "Haettavan organisaation kunta tai lista kunnista", paramType = "query"),
            @ApiImplicitParam(dataType = "String", name = "organisaatiotyyppi", value = "Haettavan organisaation tyyppi", paramType = "query"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true, name = "oppilaitostyyppi", value = "Haettavan oppilaitoksen tyyppi tai lista tyypeistä", paramType = "query"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true, name = "kieli", value = "Haettavan organisaation kieli tai lista kielistä", paramType = "query"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true, name = "oidRestrictionList", value = "Lista sallituista organisaatioiden oid:stä", paramType = "query"),
            @ApiImplicitParam(dataType = "String", name = "oid", value = "Haku oid:lla. Hakuteksti jätetään huomioimatta jos oid on annettu.", paramType = "query"),
            @ApiImplicitParam(dataType = "boolean", name = "skipParents", value = "Jätetäänkö yläorganisaatiot pois hakutuloksista", paramType = "query")})
    @ApiOperation(
            value = "Hakee annetuihin hakuehtoihin osuvien organisaatioiden nimi-, oid-, ja tyyppitiedot",
            notes = "Operaatio palauttaa vain hakuehtoja vastaavien organisaatioiden suppeat tiedot."
                    + "Soveltuu käytettäväksi haun \"/hae\" sijaan silloin kuin paluuarvossa riittää organisaation nimi, oid, organisaatiotyypit ja mahdollinen oppilaitostyyppi.",
            response = OrganisaatioHakutulosSuppeaDTOV2.class)
    OrganisaatioHakutulosSuppeaDTOV2 searchOrganisaatiotTyypit(@ApiParam(access = "hidden")
                                                                       OrganisaatioSearchCriteriaDTOV2 hakuEhdot);

    @PostMapping(path = "/yhteystiedot/hae", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Hakee organisaatioita annetuilla hakukriteereillä ja palauttaa yhteystiedot",
            notes = "Operaatio palauttaa hakukriteerit täyttävien organisaatioiden yhteystiedot.")
    List<OrganisaatioYhteystiedotDTOV2> searchOrganisaatioYhteystiedot(YhteystiedotSearchCriteriaDTOV2 hakuEhdot);

    @GetMapping(path = "/{oid}/paivittaja", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Hakee organisaation viimeisimmän päivittäjän tiedot.",
            notes = "Operaatio palauttaa oid:n määrittämän organisaation viimeisimmän päivittäjän.",
            response = OrganisaatioPaivittajaDTOV2.class)
    OrganisaatioPaivittajaDTOV2 getOrganisaatioPaivittaja(@ApiParam(value = "Organisaation oid", required = true) @PathVariable("oid") String oid) throws Exception;

    @GetMapping(path = "/{oid}/nimet", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Hakee organisaation nimihistorian.",
            notes = "Operaatio palauttaa oid:n määrittämän organisaation nimihistorian.",
            response = OrganisaatioNimiDTOV2.class)
    List<OrganisaatioNimiDTOV2> getOrganisaatioNimet(@ApiParam(value = "Organisaation oid", required = true) @PathVariable("oid") String oid) throws Exception;

    @GetMapping(path = "/{id}/LOP", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Hakee yhden organisaation LOP tiedot.",
            notes = "Operaatio palauttaa id:n määrittämän organisaation LOP (Learning Opportunity Provider) tiedot (id voi olla oid, y-tunnus, virastotunnus, oppilaitoskoodi tai toimipistekoodi). Soveltuu käytettäväksi haun \"/{id}\" sijaan silloin kuin paluuarvossa riittää pelkät LOP tiedot.",
            response = OrganisaatioLOPTietoDTOV2.class)
    OrganisaatioLOPTietoDTOV2 getOrganisaationLOPTiedotByOID(@ApiParam(value = "Organisaation oid.", required = true) @PathVariable("id") String oid);

    @PutMapping(path = "/{oid}/nimet", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Luo uuden nimen organisaatiolle",
            notes = "Operaatio luo uuden nimen organisaatiolle annetusta JSON:sta.",
            response = OrganisaatioNimiDTOV2.class)
    OrganisaatioNimiDTOV2 newOrganisaatioNimi(@ApiParam(value = "Organisaation oid", required = true) @PathVariable("oid") String oid, OrganisaatioNimiDTOV2 nimidto) throws Exception;

    @PostMapping(path = "/{oid}/nimet/{date: [0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Päivittää oid:n määrittämän organisaation nimen, jonka aikaisempi alkupäivämäärä on annettu date",
            notes = "Operaatio päivittää oid:n määrittämän organisaation nimen, jonka aikaisempi alkupäivämäärä on annettu date.",
            response = OrganisaatioNimiDTOV2.class)
    OrganisaatioNimiDTOV2 updateOrganisaatioNimi(@PathVariable("oid") String oid, @PathVariable("date") DateParam date, OrganisaatioNimiDTOV2 nimidto);

    @DeleteMapping(path = "/{oid}/nimet/{date: [0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Poistaa oid:n määrittämän organisaation nimen, jonka alkupäivämäärä on annettu date",
            notes = "Operaatio poistaa oid:n määrittämän organisaation nimen, jonka aikaisempi alkupäivämäärä on annettu date.",
            response = String.class)
    String deleteOrganisaatioNimi(@PathVariable("oid") String oid, @PathVariable("date") DateParam date);

    @PutMapping(path = "/muokkaamonta", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Muokkaa monta organisaatiota kerralla",
            notes = "Operaatio muokkaa annettujen organisaatioden annetut tiedot.",
            response = OrganisaatioNimiDTOV2.class)
    OrganisaatioMuokkausTulosListaDTO muokkaaMontaOrganisaatiota(List<OrganisaatioMuokkausTiedotDTO> tiedot);

    @GetMapping(path = "/muutetut/oid", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Hakee organisaatioiden OID:t, joita muutettu annetun päivämäärän jälkeen",
            response = String.class)
    String haeMuutettujenOid(@ApiParam(value = "Muokattu jälkeen", required = true) @RequestParam("lastModifiedSince") DateParam date);

    @GetMapping(path = "/muutetut", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Hakee organisaatioiden tiedot, joita muutettu annetun päivämäärän jälkeen",
            response = OrganisaatioRDTO.class,
            responseContainer = "List")
    @Deprecated
        // käytä OrganisaatioResourceV3#haeMuutetut
    List<OrganisaatioRDTO> haeMuutetut(@ApiParam(value = "Muokattu jälkeen", required = true) @RequestParam("lastModifiedSince") DateParam date,
                                       @ApiParam(value = "Palaulautetaanko vastauksen mukana mahdollinen organisaation kuva (voi olla iso).",
                                               defaultValue = "false") @RequestParam(defaultValue = "false") boolean includeImage);

    @GetMapping(path = "/{oid}/historia", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Hakee organisaation rakennehistorian.",
            notes = "Operaatio palauttaa oid:n määrittelemän organisaation rakennehistorian.",
            response = OrganisaatioHistoriaRDTOV2.class)
    OrganisaatioHistoriaRDTOV2 getOrganizationHistory(@ApiParam(value = "Organisaation oid", required = true) @PathVariable("oid") String oid) throws Exception;

    @PostMapping(path = "/{oid}/organisaatiosuhde", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Siirtää organisaatiota puussa toisen ylemmän organisaation alle tai yhdistää kaksi samanarvoista organisaatiota")
    void changeOrganisationRelationship(
            @ApiParam(value = "Organisaation oid", required = true) @PathVariable("oid") String oid,
            @ApiParam(value = "Sulautus", required = true) @RequestParam("merge") boolean merge,
            @ApiParam(value = "Siirto päivämäärä, jos päivämäärää ei ole asetettu käytetään tätä päivämäärää") @RequestParam("moveDate") DateParam date,
            @ApiParam(value = "Uusi isäntäorganisaatio", required = true) String newParentOid
    );

    @GetMapping(path = "/liitokset", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Hakee organisaatioiden tiedot, joita muutettu annetun päivämäärän jälkeen",
            response = OrganisaatioLiitosDTOV2.class)
    List<OrganisaatioLiitosDTOV2> haeLiitokset(
            @ApiParam(value = "Liitokset jälkeen") @RequestParam("liitoksetAlkaen") DateParam date);

    @GetMapping(path = "/ryhmat", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Hakee organisaation alla olevat ryhmät",
            notes = "Operaatio palauttaa organisaation alla olevat ryhmät.",
            response = OrganisaatioGroupDTOV2.class)
    @Deprecated
        // käytä OrganisaatioResourceV3#groups
    List<OrganisaatioGroupDTOV2> groups(RyhmaCriteriaDtoV2 criteria) throws Exception;

    @GetMapping(path = "/{oid}/hakutoimisto", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Hakee organisaation hakutoimiston ",
            notes = "Hakutoimisto haetaan tarvittaessa rekursiivisesti",
            response = HakutoimistoDTO.class)
    HakutoimistoDTO hakutoimisto(@PathVariable("oid") String organisaatioOid);
}

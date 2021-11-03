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
import fi.vm.sade.organisaatio.resource.dto.ResultRDTO;
import fi.vm.sade.organisaatio.resource.dto.YhteystietojenTyyppiRDTO;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Organisaation REST palvelut (Versio 1).
 *
 * @author mlyly
 */
public interface OrganisaatioResource {

    String OID_SEPARATOR = "/";

    @GetMapping(path = "/hae", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "String", name = "searchStr", value = "Hakuteksti", paramType = "query"),
            @ApiImplicitParam(dataType = "boolean", name = "vainAktiiviset", value = "Palautetaanko vain aktiiviset organisaatiot", paramType = "query", required = true),
            @ApiImplicitParam(dataType = "boolean", name = "vainLakkautetut", value = "Palautetaanko vain lakkautetut organisaatiot", paramType = "query", required = true, defaultValue = "false"),
            @ApiImplicitParam(dataType = "boolean", name = "suunnitellut", value = "Suunnitellut organisaatiot mukaan hakutuloksiin", paramType = "query", required = true, defaultValue = "false"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true, name = "oppilaitostyyppi", value = "Haettavan oppilaitoksen tyyppi tai lista tyypeistä", paramType = "query"),
            @ApiImplicitParam(dataType = "String", name = "organisaatiotyyppi", value = "Haettavan organisaation tyyppi", paramType = "query"),
            @ApiImplicitParam(dataType = "String", allowMultiple = true, name = "kunta", value = "Haettavan organisaation kunta tai lista kunnista", paramType = "query"),
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
    OrganisaatioHakutulos searchHierarchy(
            @ApiParam(access = "hidden") OrganisaatioSearchCriteria q);

    /**
     * NOTE: USED BY SECURITY FRAMEWORK - DON'T CHANGE
     * <p>
     * Find oids of organisaatio's parents, result oids start from root, ends to
     * given oid itself, and are separated by '/'.
     *
     * @param oid
     * @return oid/path/form/root
     * @throws Exception
     */
    @GetMapping(path = "/{oid}/parentoids", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Hakee organisaation vanhempien oid:t",
            notes = "Operaatio palauttaa organisaation vanhempien oid:t "
                    + "alkaen juuresta päättyen annetun organisaation oid:hen. "
                    + "Id:t on eroteltu kautta-merkillä ('/').",
            response = String.class)
    String parentoids(
            @ApiParam(value = "Organisaation oid", required = true) @PathVariable String oid) throws Exception;

    @GetMapping(path = "/{oid}/childoids", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Hakee organisaation alla olevien organisaatioiden oid:t",
            notes = "Operaatio palauttaa organisaation alla olevien organisaatioiden oid:t.",
            response = String.class)
    String childoids(
            @ApiParam(value = "Organisaation oid", required = true) String oid,
            @ApiParam(value = "Rekursiivisesti") @RequestParam(defaultValue = "false") boolean rekursiivisesti,
            @ApiParam(value = "Aktiiviset") @RequestParam(defaultValue = "true") boolean aktiiviset,
            @ApiParam(value = "Suunnitellut") @RequestParam(defaultValue = "true") boolean suunnitellut,
            @ApiParam(value = "Lakkautetut") @RequestParam(defaultValue = "true") boolean lakkautetut) throws Exception;

    @GetMapping(path = "/{oid}/children", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Hakee organisaation alla olevat organisaatiot",
            notes = "Operaatio palauttaa organisaation alla olevat organisaatiot.",
            response = OrganisaatioRDTO.class,
            responseContainer = "List")
    @Deprecated
        // käytä OrganisaatioResourceV3#children
    List<OrganisaatioRDTO> children(
            @ApiParam(value = "Organisaation oid", required = true) @PathVariable String oid,
            @ApiParam(value = "Palaulautetaanko vastauksen mukana mahdollinen organisaation kuva (voi olla iso).",
                    defaultValue = "false") @RequestParam(defaultValue = "false") boolean includeImage) throws Exception;

    @GetMapping(path = "/{oid}/ryhmat", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Hakee organisaation alla olevat ryhmät",
            notes = "Operaatio palauttaa organisaation alla olevat ryhmät.",
            response = OrganisaatioRDTO.class,
            responseContainer = "List")
    @Deprecated
        // käytä OrganisaatioResourceV3#groups
    List<OrganisaatioRDTO> groups(
            @ApiParam(value = "Organisaation oid", required = true,
                    defaultValue = "1.2.246.562.24.00000000001") @PathVariable String oid,
            @ApiParam(value = "Palaulautetaanko vastauksen mukana mahdollinen organisaation kuva (voi olla iso).",
                    defaultValue = "false") @RequestParam(defaultValue = "false") boolean includeImage) throws Exception;

    @GetMapping(path = "/hello", produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation(
            value = "Testioperaatio, jolla voi kokeilla onko organisaatiopalvelu pystyssä.",
            notes = "Operaatio vastaa tervehdykseen ja palauttaa palvelun aikaleiman.",
            response = String.class)
    String hello();

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Hakee organisaatioiden oid:t annetuilla hakuehdoilla",
            notes = "Operaatio palauttaa listan organisaatioiden oid:tä annetuilla hakuehdoilla.",
            response = String.class)
    List<String> search(@ApiParam(value = "Hakutermit", required = true) @RequestParam String searchTerms,
                        @ApiParam(value = "Tulosjoukon koko", required = true) @RequestParam int count,
                        @ApiParam(value = "Ensimmäisen hakutuloksen indeksi", required = true) @RequestParam int startIndex,
                        @ApiParam(value = "Muokattu ennen", required = true) @RequestParam Date lastModifiedBefore,
                        @ApiParam(value = "Muokattu jälkeen", required = true) @RequestParam Date lastModifiedSince);

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Hakee yhden organisaation annetulla id:llä (id voi olla oid, y-tunnus, virastotunnus, oppilaitoskoodi tai toimipistekoodi).",
            notes = "Operaatio palauttaa id:n määrittämän organisaation tiedot.",
            response = OrganisaatioRDTO.class)
    @Deprecated
        // käytä OrganisaatioResourceV3#getOrganisaatioByOID
    OrganisaatioRDTO getOrganisaatioByOID(
            @ApiParam(value = "Organisaation oid, y-tunnus, virastotunnus, oppilaitoskoodi tai toimipistekoodi.",
                    required = true) @PathVariable String id,
            @ApiParam(value = "Palaulautetaanko vastauksen mukana mahdollinen organisaation kuva (voi olla iso).",
                    defaultValue = "false") @RequestParam(defaultValue = "false") boolean includeImage);

    @PostMapping(path = "/{oid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiImplicitParams(@ApiImplicitParam(dataType = "java.io.File", name = "organisaatio",
            value = "Organisaation tiedot json muodossa", paramType = "body"))
    @ApiOperation(
            value = "Päivittää oid:n määrittämän organisaation tiedot",
            notes = "Operaatio päivittää oid:n määrittämän organisaation tiedot.",
            response = ResultRDTO.class)
    @Deprecated
        // käytä OrganisaatioResourceV3#updateOrganisaatio
    ResultRDTO updateOrganisaatio(
            @ApiParam(value = "Organisaation oid", required = true) @PathVariable String oid,
            @ApiParam(access = "hidden") OrganisaatioRDTO ordto);

    @DeleteMapping(path = "/{oid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Poistaa oid:n määrittämän organisaation",
            notes = "Operaatio poistaa organisaation annetulla oid:llä.",
            response = String.class)
    @Deprecated
        // käytä OrganisaatioResourceV3#deleteOrganisaatio
    String deleteOrganisaatio(
            @ApiParam(value = "Organisaation oid", required = true) @PathVariable String oid);

    @PutMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiImplicitParams(@ApiImplicitParam(dataType = "java.io.File", name = "organisaatio",
            value = "Luotavan organisaation tiedot json muodossa", paramType = "body"))
    @ApiOperation(
            value = "Luo uuden organisaation",
            notes = "Operaatio luo uuden organisaation annetusta JSON:sta.",
            response = ResultRDTO.class)
    @Deprecated
        // käytä OrganisaatioResourceV3#newOrganisaatio
    ResultRDTO newOrganisaatio(@ApiParam(access = "hidden") OrganisaatioRDTO ordto);

    @GetMapping(path = "/yhteystietometadata", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Hakee sallitut yhteystietotyypit",
            notes = "Operaatio palauttaa annetuille organisaatiotyypeille sallitut yhteystietotyypit.",
            response = YhteystietojenTyyppiRDTO.class,
            responseContainer = "List")
    Set<YhteystietojenTyyppiRDTO> getYhteystietoMetadata(
            @ApiParam(value = "Organisaatiotyypit", required = true) @RequestParam Set<String> organisaatioTyyppi);

    @GetMapping(path = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Testaa autentikoituneen käyttäjän",
            notes = "Operaatiota käytetään ennen ensimmäistä autenttikoitua POST-kutsua. "
                    + "Näin vältetään CAS + autentikoitu POST redirection ongelma.",
            response = String.class)
    String authHello();

}

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
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * V2 REST services for Organisaatio.
 *
 * @author simok
 */
@Hidden
public interface OrganisaatioResourceV2 {

    @GetMapping(path = "/hello", produces = MediaType.TEXT_PLAIN_VALUE)
    String hello();

    @GetMapping(path = "/hierarkia/hae", produces = MediaType.APPLICATION_JSON_VALUE)
    OrganisaatioHakutulos searchOrganisaatioHierarkia(
            OrganisaatioSearchCriteriaDTOV2 hakuEhdot);

    @GetMapping(path = "/hierarkia/hae/nimi", produces = MediaType.APPLICATION_JSON_VALUE)
    OrganisaatioHakutulosSuppeaDTOV2 searchOrganisaatioHierarkiaNimet(
            OrganisaatioSearchCriteriaDTOV2 hakuEhdot);

    @GetMapping(path = "/hierarkia/hae/tyyppi", produces = MediaType.APPLICATION_JSON_VALUE)
    OrganisaatioHakutulosSuppeaDTOV2 searchOrganisaatioHierarkiaTyypit(OrganisaatioSearchCriteriaDTOV2 hakuEhdot);

    @GetMapping(path = "/hae", produces = MediaType.APPLICATION_JSON_VALUE)
    OrganisaatioHakutulos searchOrganisaatiot(OrganisaatioSearchCriteriaDTOV2 hakuEhdot);

    @GetMapping(path = "/hae/nimi", produces = MediaType.APPLICATION_JSON_VALUE)
    OrganisaatioHakutulosSuppeaDTOV2 searchOrganisaatiotNimet(OrganisaatioSearchCriteriaDTOV2 hakuEhdot);

    @GetMapping(path = "/hae/tyyppi", produces = MediaType.APPLICATION_JSON_VALUE)
    OrganisaatioHakutulosSuppeaDTOV2 searchOrganisaatiotTyypit(OrganisaatioSearchCriteriaDTOV2 hakuEhdot);

    @PostMapping(path = "/yhteystiedot/hae", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    List<OrganisaatioYhteystiedotDTOV2> searchOrganisaatioYhteystiedot(YhteystiedotSearchCriteriaDTOV2 hakuEhdot);

    @GetMapping(path = "/{oid}/paivittaja", produces = MediaType.APPLICATION_JSON_VALUE)
    OrganisaatioPaivittajaDTOV2 getOrganisaatioPaivittaja(@PathVariable("oid") String oid) throws Exception;

    @GetMapping(path = "/{oid}/nimet", produces = MediaType.APPLICATION_JSON_VALUE)
    List<OrganisaatioNimiDTOV2> getOrganisaatioNimet(@PathVariable("oid") String oid) throws Exception;

    @GetMapping(path = "/{id}/LOP", produces = MediaType.APPLICATION_JSON_VALUE)
    OrganisaatioLOPTietoDTOV2 getOrganisaationLOPTiedotByOID(@PathVariable("id") String oid);

    @PutMapping(path = "/{oid}/nimet", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    OrganisaatioNimiDTOV2 newOrganisaatioNimi(@PathVariable("oid") String oid, OrganisaatioNimiDTOV2 nimidto) throws Exception;

    @PostMapping(path = "/{oid}/nimet/{date: [0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    OrganisaatioNimiDTOV2 updateOrganisaatioNimi(@PathVariable("oid") String oid, @PathVariable("date") DateParam date, OrganisaatioNimiDTOV2 nimidto);

    @DeleteMapping(path = "/{oid}/nimet/{date: [0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]}", produces = MediaType.APPLICATION_JSON_VALUE)
    String deleteOrganisaatioNimi(@PathVariable("oid") String oid, @PathVariable("date") DateParam date);

    @PutMapping(path = "/muokkaamonta", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    OrganisaatioMuokkausTulosListaDTO muokkaaMontaOrganisaatiota(List<OrganisaatioMuokkausTiedotDTO> tiedot);

    @GetMapping(path = "/muutetut/oid", produces = MediaType.APPLICATION_JSON_VALUE)
    String haeMuutettujenOid(@RequestParam("lastModifiedSince") DateParam date);

    @GetMapping(path = "/muutetut", produces = MediaType.APPLICATION_JSON_VALUE)
    @Deprecated
        // käytä OrganisaatioResourceV3#haeMuutetut
    List<OrganisaatioRDTO> haeMuutetut(@RequestParam("lastModifiedSince") DateParam date,
                                       @RequestParam(defaultValue = "false") boolean includeImage);

    @GetMapping(path = "/{oid}/historia", produces = MediaType.APPLICATION_JSON_VALUE)
    OrganisaatioHistoriaRDTOV2 getOrganizationHistory(@PathVariable("oid") String oid);

    @PostMapping(path = "/{oid}/organisaatiosuhde", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    void changeOrganisationRelationship(
            @PathVariable("oid") String oid,
            @RequestParam("merge") boolean merge,
            @RequestParam("moveDate") DateParam date,
            String newParentOid
    );

    @GetMapping(path = "/liitokset", produces = MediaType.APPLICATION_JSON_VALUE)
    List<OrganisaatioLiitosDTOV2> haeLiitokset(@RequestParam("liitoksetAlkaen") DateParam date);

    @GetMapping(path = "/ryhmat", produces = MediaType.APPLICATION_JSON_VALUE)
    @Deprecated
        // käytä OrganisaatioResourceV3#groups
    List<OrganisaatioGroupDTOV2> groups(RyhmaCriteriaDtoV2 criteria) throws Exception;

    @GetMapping(path = "/{oid}/hakutoimisto", produces = MediaType.APPLICATION_JSON_VALUE)
    HakutoimistoDTO hakutoimisto(@PathVariable("oid") String organisaatioOid);
}

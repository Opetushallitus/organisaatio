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
import io.swagger.v3.oas.annotations.Hidden;
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
@Hidden
public interface OrganisaatioResource {

    String OID_SEPARATOR = "/";

    @GetMapping(path = "/hae", produces = MediaType.APPLICATION_JSON_VALUE)
    OrganisaatioHakutulos searchHierarchy(
            OrganisaatioSearchCriteria q);

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
    @GetMapping(path = "/{oid}/parentoids", produces = MediaType.TEXT_PLAIN_VALUE)
    String parentoids(@PathVariable String oid) throws Exception;

    @GetMapping(path = "/{oid}/childoids", produces = MediaType.APPLICATION_JSON_VALUE)
    String childoids(String oid,
                     @RequestParam(defaultValue = "false") boolean rekursiivisesti,
                     @RequestParam(defaultValue = "true") boolean aktiiviset,
                     @RequestParam(defaultValue = "true") boolean suunnitellut,
                     @RequestParam(defaultValue = "true") boolean lakkautetut) throws Exception;

    @GetMapping(path = "/{oid}/children", produces = MediaType.APPLICATION_JSON_VALUE)
    @Deprecated
        // käytä OrganisaatioResourceV3#children
    List<OrganisaatioRDTO> children(
            @PathVariable String oid,
            @RequestParam(defaultValue = "false") boolean includeImage) throws Exception;

    @GetMapping(path = "/{oid}/ryhmat", produces = MediaType.APPLICATION_JSON_VALUE)
    @Deprecated
        // käytä OrganisaatioResourceV3#groups
    List<OrganisaatioRDTO> groups(
            @PathVariable String oid,
            @RequestParam(defaultValue = "false") boolean includeImage) throws Exception;

    @GetMapping(path = "/hello", produces = MediaType.TEXT_PLAIN_VALUE)
    String hello();

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    List<String> search(@RequestParam String searchTerms,
                        @RequestParam int count,
                        @RequestParam int startIndex,
                        @RequestParam Date lastModifiedBefore,
                        @RequestParam Date lastModifiedSince);

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Deprecated
        // käytä OrganisaatioResourceV3#getOrganisaatioByOID
    OrganisaatioRDTO getOrganisaatioByOID(
            @PathVariable String id,
            @RequestParam(defaultValue = "false") boolean includeImage);


    @GetMapping(path = "/yhteystietometadata", produces = MediaType.APPLICATION_JSON_VALUE)
    Set<YhteystietojenTyyppiRDTO> getYhteystietoMetadata(@RequestParam Set<String> organisaatioTyyppi);

    @GetMapping(path = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
    String authHello();

}

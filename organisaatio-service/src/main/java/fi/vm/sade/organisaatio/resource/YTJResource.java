/*
 * Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
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
 * European Union Public Licence for more details.
 */
package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.ytj.api.YTJDTO;
import fi.vm.sade.organisaatio.ytj.api.YTJKieli;
import fi.vm.sade.organisaatio.ytj.api.YTJService;
import fi.vm.sade.organisaatio.ytj.api.exception.YtjConnectionException;
import fi.vm.sade.organisaatio.ytj.api.exception.YtjExceptionType;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Hidden
@RestController
@Slf4j
@RequestMapping({"${server.internal.context-path}/ytj", "${server.rest.context-path}/ytj"})
public class YTJResource {

    @Autowired(required = true)
    private YTJService ytjService;

    @Autowired
    private ConversionService conversionService;

    /**
     * YTJ DTO as JSON.
     *
     * @param ytunnus
     * @return
     */
    @GetMapping(path = "/{ytunnus}", produces = MediaType.APPLICATION_JSON_VALUE)
    public YTJDTO findByYTunnus(@PathVariable String ytunnus) {
        try {
            return ytjService.findByYTunnus(ytunnus.trim(), YTJKieli.FI);
        } catch (YtjConnectionException ex) {
            throw new OrganisaatioResourceException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getExceptionType().name());
        }
    }

    @GetMapping(path = "/{ytunnus}/v4", produces = MediaType.APPLICATION_JSON_VALUE)
    public OrganisaatioRDTOV4 findByYTunnusV4(@PathVariable String ytunnus) {
        return conversionService.convert(getOrganisaatioByYTunnus(ytunnus), OrganisaatioRDTOV4.class);
    }

    private Organisaatio getOrganisaatioByYTunnus(String ytunnus) {
        YTJDTO ytjdto = findByYTunnus(ytunnus);
        if (ytjdto.getYtunnus() == null) {
            throw new OrganisaatioResourceException(HttpStatus.NOT_FOUND, "organisaatio.exception.organisaatio.not.found");
        }
        return conversionService.convert(ytjdto, Organisaatio.class);
    }

    @GetMapping(path = "/hae", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<YTJDTO> findByYNimi(@RequestParam String nimi) {
        List<YTJDTO> ytjList = new ArrayList<>();
        if (nimi != null && nimi.length() > 0) {
            try {
                ytjList = ytjService.findByYNimi(nimi.trim(), true, YTJKieli.FI);
            } catch (YtjConnectionException ex) {
                if(ex.getExceptionType().equals(YtjExceptionType.SOAP)){
                    //assuming fault due to too many results
                    throw new OrganisaatioResourceException(HttpStatus.BAD_REQUEST, "organisaatio.exception.too.many.results", ex.getExceptionType().name());
                }
                throw new OrganisaatioResourceException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getExceptionType().name());
            }
        }
        return ytjList;
    }

    // Api for batch searches by y-tunnuses

    @GetMapping(path = "/massahaku/{ytunnukset}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA') or hasRole('ROLE_APP_ORGANISAATIOHALLINTA_CRUD')")
    public List<YTJDTO> findByYTunnusBatch(@PathVariable List<String> ytunnuses) {
        return doYtjMassSearch(ytunnuses);
    }

    public List<YTJDTO> doYtjMassSearch(List<String> ytunnuses) {
        List<YTJDTO> ytjListResult;
        try {
            ytjListResult = ytjService.findByYTunnusBatch(ytunnuses, YTJKieli.FI);
        } catch (YtjConnectionException ex) {
            log.error("YtjService invocation failed", ex);
            throw new OrganisaatioResourceException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getExceptionType().name());
        }
        return ytjListResult;
    }
}

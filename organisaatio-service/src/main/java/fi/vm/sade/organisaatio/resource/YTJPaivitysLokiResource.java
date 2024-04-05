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

import fi.vm.sade.organisaatio.model.YtjPaivitysLoki;
import fi.vm.sade.organisaatio.repository.YtjPaivitysLokiRepository;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Hidden
@RestController
@RequestMapping({"${server.internal.context-path}/ytjpaivitysloki", "${server.rest.context-path}/ytjpaivitysloki"})
public class YTJPaivitysLokiResource {

    @Autowired
    private YtjPaivitysLokiRepository ytjPaivitysLokiRepository;


    @GetMapping(path = "/aikavali", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public List<YtjPaivitysLoki> findByDateRange(@RequestParam("alkupvm") long alkupvm,
                                                 @RequestParam("loppupvm") long loppupvm) {
        List<YtjPaivitysLoki> ytjLoki = new ArrayList<>();
        Date alkupvmDate = new Date(alkupvm);
        Date loppupvmDate = new Date(loppupvm);
        if (alkupvm != 0 && loppupvm != 0) {
            ytjLoki = ytjPaivitysLokiRepository.findByDateRange(alkupvmDate, loppupvmDate);
        }
        return ytjLoki;
    }

    @GetMapping(path = "/uusimmat", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public List<YtjPaivitysLoki> findByDateRange(@RequestParam("limit") int limit) {
        List<YtjPaivitysLoki> ytjLoki = new ArrayList<>();
        if (limit > 0) {
            ytjLoki = ytjPaivitysLokiRepository.findLatest(limit);
        }
        return ytjLoki;
    }
}

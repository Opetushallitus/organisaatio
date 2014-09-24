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

package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Organisaation ajastukset.
 *
 * @author simok
 */
@Component
public class OrganisaatioTaskScheduler {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrganisaatioBusinessService organisaatioBusinessService;

    @Value("${organisaatio-service.scheduled.name.update.cron.expression}")
    private String nameUpdateCronExpression;

    /**
     * Laukaisee ajastetusti organisaatioiden nimenpäivityksen / nimenpäivityksen
     * tarkistuksen.
     */
    @Scheduled(cron = "${organisaatio-service.scheduled.name.update.cron.expression}")
    public void scheduledNameUpdate()
    {
        LOG.debug("scheduledNameUpdate(): Cron Expression: {}, Current time: " + new Date(), nameUpdateCronExpression);

        organisaatioBusinessService.updateCurrentOrganisaatioNimet();

    }
 }

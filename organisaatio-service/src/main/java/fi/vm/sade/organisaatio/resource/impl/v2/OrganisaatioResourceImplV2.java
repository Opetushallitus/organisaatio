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

package fi.vm.sade.organisaatio.resource.impl.v2;

import fi.vm.sade.generic.service.conversion.SadeConversionService;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.dto.v2.YhteystiedotSearchCriteriaDTOV2;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioYhteystiedotDTOV2;
import fi.vm.sade.organisaatio.resource.v2.OrganisaatioResourceV2;
import java.util.Date;

import java.util.List;
import org.apache.cxf.jaxrs.cors.CrossOriginResourceSharing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author simok
 */
@Component
@CrossOriginResourceSharing(allowAllOrigins = true)
public class OrganisaatioResourceImplV2  implements OrganisaatioResourceV2 {
        
    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioResourceImplV2.class);
        
    @Autowired
    private OrganisaatioBusinessService organisaatioBusinessService;
    
    @Autowired
    private SadeConversionService conversionService;
        
    @Override
    public List<OrganisaatioYhteystiedotDTOV2> searchOrganisaatioYhteystiedot(YhteystiedotSearchCriteriaDTOV2 hakuEhdot) {
        LOG.debug("searchOrganisaatioYhteystiedot: " + hakuEhdot.getKieliList());
        LOG.debug("searchOrganisaatioYhteystiedot: " + hakuEhdot.getKuntaList());
        LOG.debug("searchOrganisaatioYhteystiedot: " + hakuEhdot.getOppilaitostyyppiList());
        LOG.debug("searchOrganisaatioYhteystiedot: " + hakuEhdot.getVuosiluokkaList());
        LOG.debug("searchOrganisaatioYhteystiedot: " + hakuEhdot.getYtunnusList());
        LOG.debug("searchOrganisaatioYhteystiedot: " + hakuEhdot.getOidList());
        LOG.debug("searchOrganisaatioYhteystiedot: " + hakuEhdot.getLimit());
        
        // TODO tarkistetaanko tässä vai business kerroksessa parametrit
        
        return conversionService.convertAll(organisaatioBusinessService.findBySearchCriteria(
                hakuEhdot.getKieliList(),
                hakuEhdot.getKuntaList(),
                hakuEhdot.getOppilaitostyyppiList(),
                hakuEhdot.getVuosiluokkaList(),
                hakuEhdot.getYtunnusList(),
                hakuEhdot.getOidList(),
                hakuEhdot.getLimit()), OrganisaatioYhteystiedotDTOV2.class);
    }

    @Override
    public String hello() {
        return "Hello V2! " + new Date();
    }
   
    
}

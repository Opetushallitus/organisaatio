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

import fi.vm.sade.organisaatio.api.search.OrganisaatioHakutulos;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioModelMapper;
import fi.vm.sade.organisaatio.dto.mapping.SearchCriteriaModelMapper;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioSearchCriteriaDTOV2;
import fi.vm.sade.organisaatio.dto.v2.YhteystiedotSearchCriteriaDTOV2;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioYhteystiedotDTOV2;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.resource.v2.OrganisaatioResourceV2;
import fi.vm.sade.organisaatio.service.search.OrganisaatioSearchService;
import fi.vm.sade.organisaatio.service.search.SearchCriteria;
import fi.vm.sade.organisaatio.service.util.OrganisaatioPerustietoUtil;
import java.lang.reflect.Type;
import java.util.Date;

import java.util.List;
import org.apache.cxf.jaxrs.cors.CrossOriginResourceSharing;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private OrganisaatioModelMapper modelMapper;

    @Autowired
    private OrganisaatioSearchService organisaatioSearchService;
    
    @Autowired
    private SearchCriteriaModelMapper searchCriteriaModelMapper;
        
    @Override
    @Transactional(readOnly = true)
    public List<OrganisaatioYhteystiedotDTOV2> searchOrganisaatioYhteystiedot(YhteystiedotSearchCriteriaDTOV2 hakuEhdot) {
        LOG.debug("searchOrganisaatioYhteystiedot: " + hakuEhdot.getKieliList());
        LOG.debug("searchOrganisaatioYhteystiedot: " + hakuEhdot.getKuntaList());
        LOG.debug("searchOrganisaatioYhteystiedot: " + hakuEhdot.getOppilaitostyyppiList());
        LOG.debug("searchOrganisaatioYhteystiedot: " + hakuEhdot.getVuosiluokkaList());
        LOG.debug("searchOrganisaatioYhteystiedot: " + hakuEhdot.getYtunnusList());
        LOG.debug("searchOrganisaatioYhteystiedot: " + hakuEhdot.getOidList());
        LOG.debug("searchOrganisaatioYhteystiedot: " + hakuEhdot.getLimit());
        
        // TODO tarkistetaanko tässä vai business kerroksessa parametrit
        
        List<Organisaatio> organisaatiot = organisaatioBusinessService.findBySearchCriteria(
                hakuEhdot.getKieliList(),
                hakuEhdot.getKuntaList(),
                hakuEhdot.getOppilaitostyyppiList(),
                hakuEhdot.getVuosiluokkaList(),
                hakuEhdot.getYtunnusList(),
                hakuEhdot.getOidList(),
                hakuEhdot.getLimit());

        // Define the target list type for mapping
        Type organisaatioYhteystiedotDTOV2ListType = new TypeToken<List<OrganisaatioYhteystiedotDTOV2>>() {}.getType();

        // Map domain type to DTO
        return modelMapper.map(organisaatiot, organisaatioYhteystiedotDTOV2ListType);
    }

    @Override
    public String hello() {
        return "Hello V2! " + new Date();
    }

    @Override
    public OrganisaatioHakutulos searchOrganisaatioRakenne(OrganisaatioSearchCriteriaDTOV2 hakuEhdot) {
        final OrganisaatioHakutulos tulos = new OrganisaatioHakutulos();

        // Map api search criteria to solr search criteria
        SearchCriteria searchCriteria = searchCriteriaModelMapper.map(hakuEhdot, SearchCriteria.class);

        // Hae organisaatiot
        List<OrganisaatioPerustieto> organisaatiot = organisaatioSearchService.searchBasicOrganisaatios(searchCriteria);

        // Rakenna hierarkia
        tulos.setOrganisaatiot(OrganisaatioPerustietoUtil.createHierarchy(organisaatiot));

        // Lukumäärä tuloksiin
        tulos.setNumHits(organisaatiot.size());
        
        return tulos;
    }
   
    
}

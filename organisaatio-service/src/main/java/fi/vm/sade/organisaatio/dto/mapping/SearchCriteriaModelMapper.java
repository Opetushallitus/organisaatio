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

package fi.vm.sade.organisaatio.dto.mapping;


import fi.vm.sade.organisaatio.api.search.OrganisaatioSearchCriteria;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioSearchCriteriaDTOV2;
import fi.vm.sade.organisaatio.service.search.SearchCriteria;
import org.modelmapper.Converter;

import org.modelmapper.PropertyMap;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;

/**
 *
 * @author simok
 */
public class SearchCriteriaModelMapper  extends ModelMapper {
                
    public SearchCriteriaModelMapper() {
        super();
    
        // Suunnitellut
        final Converter<OrganisaatioSearchCriteria, Boolean> suunnitellutConverter = new Converter<OrganisaatioSearchCriteria, Boolean>() {
            @Override
            public Boolean convert(MappingContext<OrganisaatioSearchCriteria, Boolean> mc) {
                return mc.getSource().getVainLakkautetut() == false && mc.getSource().getVainAktiiviset() == false;
            }
        };

        // Aktiiviset
        final Converter<OrganisaatioSearchCriteria, Boolean> aktiivisetConverter = new Converter<OrganisaatioSearchCriteria, Boolean>() {
            @Override
            public Boolean convert(MappingContext<OrganisaatioSearchCriteria, Boolean> mc) {
                return mc.getSource().getVainLakkautetut() == false;
            }
        };

        // Lakkautetut
        final Converter<OrganisaatioSearchCriteria, Boolean> lakkautetutConverter = new Converter<OrganisaatioSearchCriteria, Boolean>() {
            @Override
            public Boolean convert(MappingContext<OrganisaatioSearchCriteria, Boolean> mc) {
                return mc.getSource().getVainLakkautetut();
            }
        };

        
        this.addMappings(new PropertyMap<OrganisaatioSearchCriteriaDTOV2, SearchCriteria>() {
            @Override
            protected void configure() {    
                // Mappays menee suoraan ilman säätöjä
            }
        });

        this.addMappings(new PropertyMap<OrganisaatioSearchCriteria, SearchCriteria>() {
            @Override
            protected void configure() {
                
                // Note: Since a source object is given, the "false"value passed to set[Method] is unused.
                using(suunnitellutConverter).map(source).setSuunnitellut(false);
                using(aktiivisetConverter).map(source).setAktiiviset(false);
                using(lakkautetutConverter).map(source).setLakkautetut(false);
            }
        });

    }
}

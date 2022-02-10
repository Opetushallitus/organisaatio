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

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioSearchCriteria;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioSearchCriteriaDTOV2;
import fi.vm.sade.organisaatio.service.search.SearchCriteria;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 *
 * @author simok
 */
@Component
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

        // Organisaatiotyyppi
        final Converter<OrganisaatioSearchCriteria, List<String>> organisaatiotyyppiConverter = new Converter<OrganisaatioSearchCriteria, List<String>>() {
            @Override
            public List<String> convert(MappingContext<OrganisaatioSearchCriteria, List<String>> context) {
                return context.getSource().getOrganisaatiotyyppi() != null ? filterRyhma(OrganisaatioTyyppi.fromValue(context.getSource().getOrganisaatiotyyppi()).koodiValue()) : null;
            }
        };
        final Converter<OrganisaatioSearchCriteriaDTOV2, List<String>> organisaatiotyyppiConverterV2 = new Converter<OrganisaatioSearchCriteriaDTOV2, List<String>>() {
            @Override
            public List<String> convert(MappingContext<OrganisaatioSearchCriteriaDTOV2, List<String>> context) {
                return context.getSource().getOrganisaatiotyyppi() != null ? filterRyhma(OrganisaatioTyyppi.fromValue(context.getSource().getOrganisaatiotyyppi()).koodiValue()) : null;
            }
        };

        // OID
        final Converter<OrganisaatioSearchCriteria, Collection<String>> oidConverter = new Converter<OrganisaatioSearchCriteria, Collection<String>>() {
            @Override
            public List<String> convert(MappingContext<OrganisaatioSearchCriteria, Collection<String>> context) {
                return context.getSource().getOid() != null ? Arrays.asList(context.getSource().getOid()) : null;
            }
        };
        final Converter<OrganisaatioSearchCriteriaDTOV2, Collection<String>> oidConverterV2 = new Converter<OrganisaatioSearchCriteriaDTOV2, Collection<String>>() {
            @Override
            public List<String> convert(MappingContext<OrganisaatioSearchCriteriaDTOV2, Collection<String>> context) {
                return context.getSource().getOid() != null ? Arrays.asList(context.getSource().getOid()) : null;
            }
        };

        this.addMappings(new PropertyMap<OrganisaatioSearchCriteriaDTOV2, SearchCriteria>() {
            @Override
            protected void configure() {    
                using(organisaatiotyyppiConverterV2).map(source).setOrganisaatioTyyppi(emptyList());
                using(oidConverterV2).map(source).setOid(emptyList());
            }
        });

        this.addMappings(new PropertyMap<OrganisaatioSearchCriteria, SearchCriteria>() {
            @Override
            protected void configure() {
                
                // Note: Since a source object is given, the "false" value passed to set[Method] is unused.
                using(suunnitellutConverter).map(source).setSuunnitellut(false);
                using(aktiivisetConverter).map(source).setAktiiviset(false);
                using(lakkautetutConverter).map(source).setLakkautetut(false);
                using(organisaatiotyyppiConverter).map(source).setOrganisaatioTyyppi(emptyList());
                using(oidConverter).map(source).setOid(emptyList());
            }
        });

    }

    private static List<String> filterRyhma(String organisaatiotyyppi) {
        return Stream.of(organisaatiotyyppi).filter(tyyppi -> !"Ryhma".equals(tyyppi)).collect(toList());
    }

}

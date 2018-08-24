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
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioSuhdeDTOV2;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

import java.util.ArrayList;
import java.util.List;

public class OrganisaatioSuhdeModelMapper extends ModelMapper {

    public OrganisaatioSuhdeModelMapper() {
        super();

        final Converter<OrganisaatioSuhde, List<String>> parentTyypitConverter = mc -> mc.getSource().getParent() == null
                ? null
                : OrganisaatioTyyppi.fromKoodiToValue(mc.getSource().getParent().getTyypit());

        final Converter<OrganisaatioSuhde, List<String>> childTyypitConverter = mc -> mc.getSource().getChild() == null
                ? null
                : OrganisaatioTyyppi.fromKoodiToValue(mc.getSource().getChild().getTyypit());

        this.addMappings(new PropertyMap<OrganisaatioSuhde, OrganisaatioSuhdeDTOV2>() {
            @Override
            protected void configure() {
                // Monikielinen nimi
                map().getChild().setNimi(source.getChild().getNimi().getValues());
                map().getParent().setNimi(source.getParent().getNimi().getValues());
                using(parentTyypitConverter).map(source).getParent().setTyypit(new ArrayList<>());
                using(childTyypitConverter).map(source).getChild().setTyypit(new ArrayList<>());
            }
        });
    }
}

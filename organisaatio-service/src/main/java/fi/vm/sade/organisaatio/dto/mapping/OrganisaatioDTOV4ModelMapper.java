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
import fi.vm.sade.organisaatio.dto.v3.OrganisaatioRDTOV3;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrganisaatioDTOV4ModelMapper extends ModelMapper {

    public OrganisaatioDTOV4ModelMapper() {
        super();

        final Converter<List<String>, List<String>> organisaatioTyypitV3ToV4 = mc -> mc.getSource() == null
                ? null
                : OrganisaatioTyyppi.fromValueToKoodi(mc.getSource());

        final Converter<List<String>, List<String>> organisaatioTyypitV4ToV3 = mc -> mc.getSource() == null
                ? null
                : OrganisaatioTyyppi.fromKoodiToValue(mc.getSource());

        this.addMappings(new PropertyMap<OrganisaatioRDTOV3, OrganisaatioRDTOV4>() {
            @Override
            protected void configure() {
                using(organisaatioTyypitV3ToV4).map(source.getTyypit()).setTyypit(null);
            }
        });

        this.addMappings(new PropertyMap<OrganisaatioRDTOV4, OrganisaatioRDTOV3>() {
            @Override
            protected void configure() {
                using(organisaatioTyypitV4ToV3).map(source.getTyypit()).setTyypit(null);
            }
        });

    }

}

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
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioLiitosDTOV2;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

import java.util.List;

public class OrganisaatioLiitosModelMapper extends ModelMapper {

    public OrganisaatioLiitosModelMapper() {
        super();

        final Converter<List<String>, List<String>> tyyppiConverter = mc -> mc.getSource() == null
                ? null
                : OrganisaatioTyyppi.fromKoodiToValue(mc.getSource());

        this.addMappings(new PropertyMap<OrganisaatioSuhde, OrganisaatioLiitosDTOV2>() {
            @Override
            protected void configure() {
                // Monikielinen nimi
                map().getOrganisaatio().setNimi(source.getChild().getNimi().getValues());
                map().getKohde().setNimi(source.getParent().getNimi().getValues());

                map().getOrganisaatio().setOid((source.getChild().getOid()));
                // Katso tuolta miksi enum status --> string status
                // https://github.com/jhalterman/modelmapper/issues/99
                map(source.getChild().getStatus()).getOrganisaatio().setStatus(null);
                using(tyyppiConverter).map(source.getChild().getTyypit()).getOrganisaatio().setTyypit(null);

                map().getKohde().setOid((source.getParent().getOid()));
                map(source.getParent().getStatus()).getKohde().setStatus(null);
                using(tyyppiConverter).map(source.getParent().getTyypit()).getKohde().setTyypit(null);

            }
        });
    }
}

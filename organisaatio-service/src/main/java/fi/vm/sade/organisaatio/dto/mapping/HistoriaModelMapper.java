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

import fi.vm.sade.organisaatio.dto.v2.OrganisaatioHistoriaRDTOV2;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

public class HistoriaModelMapper extends ModelMapper {

    public HistoriaModelMapper() {
        super();
        this.addMappings(new PropertyMap<OrganisaatioSuhde, OrganisaatioHistoriaRDTOV2>() {
            @Override
            protected void configure() {
                // Monikielinen nimi
                map().setParentNimi(source.getParent().getNimi().getValues());
                map().setChildNimi(source.getChild().getNimi().getValues());
            }
        });
    }
}

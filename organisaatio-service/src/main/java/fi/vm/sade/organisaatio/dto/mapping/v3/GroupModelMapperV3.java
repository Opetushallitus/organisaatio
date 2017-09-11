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

package fi.vm.sade.organisaatio.dto.mapping.v3;

import fi.vm.sade.organisaatio.dto.v3.OrganisaatioGroupDTOV3;
import fi.vm.sade.organisaatio.model.Organisaatio;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

public class GroupModelMapperV3 extends ModelMapper {

    public GroupModelMapperV3() {
        super();
        this.addMappings(new PropertyMap<Organisaatio, OrganisaatioGroupDTOV3>() {
            @Override
            protected void configure() {
                // Monikielinen nimi
                map().setNimi(source.getNimi().getValues());
                map().setKuvaus(source.getKuvaus2().getValues());
                map().setParentOid(source.getParent().getOid());
            }
        });
    }
}

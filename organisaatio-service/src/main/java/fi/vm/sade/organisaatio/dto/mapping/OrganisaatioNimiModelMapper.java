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

import fi.vm.sade.organisaatio.dto.v2.OrganisaatioNimiDTOV2;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import fi.vm.sade.organisaatio.model.OrganisaatioNimi;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioNimiRDTO;
import java.util.Map;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;


/**
 *
 * @author simok
 */
public class OrganisaatioNimiModelMapper extends ModelMapper {

    public OrganisaatioNimiModelMapper() {
        super();

        // Monikielinen teksti
        final Converter<Map<String, String>, MonikielinenTeksti> monikielinentTekstiConverter = new Converter<Map<String, String>, MonikielinenTeksti>() {
            @Override
            public MonikielinenTeksti convert(MappingContext<Map<String, String>, MonikielinenTeksti> mc) {
                MonikielinenTeksti mt = new MonikielinenTeksti();
                for (Map.Entry<String, String> e : mc.getSource().entrySet()) {
                    mt.addString(e.getKey(), e.getValue());
                }
                return mt;
            }
        };

        this.addMappings(new PropertyMap<OrganisaatioNimi, OrganisaatioNimiDTOV2>() {
            @Override
            protected void configure() {
                // Monikielinen nimi
                map().setNimi(source.getNimi().getValues());
            }
        });

        this.addMappings(new PropertyMap<OrganisaatioNimiDTOV2, OrganisaatioNimi>() {
            @Override
            protected void configure() {
                // Monikielinen teksti -- nimi
                using(monikielinentTekstiConverter).map(source.getNimi()).setNimi(null);
            }
        });

        this.addMappings(new PropertyMap<OrganisaatioNimi, OrganisaatioNimiRDTO>() {
            @Override
            protected void configure() {
                // Monikielinen nimi
                map().setNimi(source.getNimi().getValues());
            }
        });

        this.addMappings(new PropertyMap<OrganisaatioNimiRDTO, OrganisaatioNimi>() {
            @Override
            protected void configure() {
                // Monikielinen teksti -- nimi
                using(monikielinentTekstiConverter).map(source.getNimi()).setNimi(null);
            }
        });
    }
}

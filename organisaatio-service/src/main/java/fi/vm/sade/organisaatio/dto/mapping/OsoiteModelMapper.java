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

import fi.vm.sade.organisaatio.dto.v2.OsoiteDTOV2;
import fi.vm.sade.organisaatio.model.Osoite;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Component;

/**
 *
 * @author simok
 */
@Component
public class OsoiteModelMapper extends ModelMapper {

    public OsoiteModelMapper() {
        super();
    
        // Osoite on rivitetty kantaan html tageillä, muutetaan tagit rivinvaihdoiksi
        final Converter<String, String> multiLineConverter = new Converter<String, String>() {
            @Override
            public String convert(MappingContext<String, String> mc) {
                return mc.getSource() == null ? null : mc.getSource().replace("<br />", "\n");
            }
        };

        this.addMappings(new PropertyMap<Osoite, OsoiteDTOV2>() {
            @Override
            protected void configure() {    
                // Osoitteen monirivisyys
                using(multiLineConverter).map(source.getOsoite()).setOsoite(null);
            }
        });
    }
}

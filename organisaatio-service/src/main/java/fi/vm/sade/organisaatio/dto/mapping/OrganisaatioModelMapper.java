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

import fi.vm.sade.organisaatio.dto.v2.OrganisaatioYhteystiedotDTOV2;
import fi.vm.sade.organisaatio.dto.v2.OsoiteDTOV2;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.Osoite;
import fi.vm.sade.organisaatio.model.Yhteystieto;
import fi.vm.sade.organisaatio.service.util.YhteystietoUtil;

import java.lang.reflect.Type;
import java.util.List;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.TypeToken;
import org.modelmapper.spi.MappingContext;


/**
 *
 * @author simok
 */
public class OrganisaatioModelMapper extends ModelMapper {
                
    public OrganisaatioModelMapper() {
        super();
    
        // PostiOsoiteConverter
        final Converter<List<Yhteystieto>, List<OsoiteDTOV2>> postiOsoiteConverter = new Converter<List<Yhteystieto>, List<OsoiteDTOV2>>() {
            @Override
            public List<OsoiteDTOV2> convert(MappingContext<List<Yhteystieto>, List<OsoiteDTOV2>> mc) {
                OsoiteModelMapper modelMapper = new OsoiteModelMapper();
                
                // Define the target list type for mapping
                Type osoiteDTOV2ListType = new TypeToken<List<OsoiteDTOV2>>() {}.getType();

                List<Osoite> postiOsoitteet = YhteystietoUtil.getPostiOsoitteet(mc.getSource());

                // Map domain type to DTO
                return modelMapper.map(postiOsoitteet, osoiteDTOV2ListType);                            
            }
        };

        // KayntiOsoiteConverter
        final Converter<List<Yhteystieto>, List<OsoiteDTOV2>> kayntiOsoiteConverter = new Converter<List<Yhteystieto>, List<OsoiteDTOV2>>() {
            @Override
            public List<OsoiteDTOV2> convert(MappingContext<List<Yhteystieto>, List<OsoiteDTOV2>> mc) {
                OsoiteModelMapper modelMapper = new OsoiteModelMapper();
                
                // Define the target list type for mapping
                Type osoiteDTOV2ListType = new TypeToken<List<OsoiteDTOV2>>() {}.getType();

                List<Osoite> postiOsoitteet = YhteystietoUtil.getKayntiOsoitteet(mc.getSource());

                // Map domain type to DTO
                return modelMapper.map(postiOsoitteet, osoiteDTOV2ListType);                            
            }
        };

        
        this.addMappings(new PropertyMap<Organisaatio, OrganisaatioYhteystiedotDTOV2>() {
            @Override
            protected void configure() {
                // Monikielinen nimi
                map().setNimi(source.getNimi().getValues());
                
                // Postiosoite
                using(postiOsoiteConverter).map(source.getYhteystiedot()).setPostiosoite(null);

                // Käyntiosoite
                using(kayntiOsoiteConverter).map(source.getYhteystiedot()).setKayntiosoite(null);
            }
        });
    }
}

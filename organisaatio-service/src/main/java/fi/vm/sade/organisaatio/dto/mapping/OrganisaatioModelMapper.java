/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
                ModelMapper modelMapper = new ModelMapper();
                
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
                ModelMapper modelMapper = new ModelMapper();
                
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

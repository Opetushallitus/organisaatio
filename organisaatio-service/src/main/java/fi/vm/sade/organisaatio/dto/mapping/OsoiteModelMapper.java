package fi.vm.sade.organisaatio.dto.mapping;

import fi.vm.sade.organisaatio.dto.v2.OsoiteDTOV2;
import fi.vm.sade.organisaatio.model.Osoite;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Component;

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

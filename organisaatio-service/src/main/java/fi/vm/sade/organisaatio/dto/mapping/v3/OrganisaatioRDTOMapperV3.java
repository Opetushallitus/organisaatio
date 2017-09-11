package fi.vm.sade.organisaatio.dto.mapping.v3;

import fi.vm.sade.organisaatio.dto.v3.OrganisaatioRDTOV3;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.organisaatio.service.util.KoodistoUtil;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import static java.util.stream.Collectors.toList;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;

public class OrganisaatioRDTOMapperV3 extends ModelMapper {

    public OrganisaatioRDTOMapperV3() {
        super();
        this.addMappings(new PropertyMap<OrganisaatioRDTOV3, OrganisaatioRDTO>() {
            @Override
            protected void configure() {
                using(new StringListConverter(KoodistoUtil::getRyhmatyyppiV1))
                        .map().setRyhmatyypit(source.getRyhmatyypit());
                using(new StringListConverter(KoodistoUtil::getKayttoryhmaV1))
                        .map().setKayttoryhmat(source.getKayttoryhmat());
            }
        });
        this.addMappings(new PropertyMap<OrganisaatioRDTO, OrganisaatioRDTOV3>() {
            @Override
            protected void configure() {
                using(new StringListConverter(KoodistoUtil::getRyhmatyyppiV3))
                        .map().setRyhmatyypit(source.getRyhmatyypit());
                using(new StringListConverter(KoodistoUtil::getKayttoryhmaV3))
                        .map().setKayttoryhmat(source.getKayttoryhmat());
            }
        });
    }

    private static class StringListConverter implements Converter<List<String>, List<String>> {

        private final Function<String, String> mapper;

        public StringListConverter(Function<String, String> mapper) {
            this.mapper = mapper;
        }

        @Override
        public List<String> convert(MappingContext<List<String>, List<String>> context) {
            List<String> source = context.getSource();
            if (source == null) {
                return null;
            }
            return source.stream().map(mapper).filter(Objects::nonNull).collect(toList());
        }

    }

}

package fi.vm.sade.organisaatio.service.converter.v3;

import fi.vm.sade.organisaatio.dto.mapping.RyhmaCriteriaDto;
import fi.vm.sade.organisaatio.resource.dto.RyhmaCriteriaDtoV3;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RyhmaCriteriaV3Converter implements Converter<RyhmaCriteriaDtoV3, RyhmaCriteriaDto> {

    private final ModelMapper modelMapper;

    public RyhmaCriteriaV3Converter(@Qualifier("modelMapper") ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public RyhmaCriteriaDto convert(RyhmaCriteriaDtoV3 source) {
        return modelMapper.map(source, RyhmaCriteriaDto.class);
    }

}

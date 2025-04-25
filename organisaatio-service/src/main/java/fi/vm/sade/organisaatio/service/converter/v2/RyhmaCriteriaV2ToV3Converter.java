package fi.vm.sade.organisaatio.service.converter.v2;

import fi.vm.sade.organisaatio.resource.dto.RyhmaCriteriaDtoV2;
import fi.vm.sade.organisaatio.resource.dto.RyhmaCriteriaDtoV3;
import fi.vm.sade.organisaatio.service.util.KoodistoUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RyhmaCriteriaV2ToV3Converter implements Converter<RyhmaCriteriaDtoV2, RyhmaCriteriaDtoV3> {

    private final ModelMapper modelMapper;

    public RyhmaCriteriaV2ToV3Converter(@Qualifier("modelMapper") ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public RyhmaCriteriaDtoV3 convert(RyhmaCriteriaDtoV2 source) {
        RyhmaCriteriaDtoV3 destination = modelMapper.map(source, RyhmaCriteriaDtoV3.class);
        Optional.ofNullable(destination.getRyhmatyyppi()).map(KoodistoUtil::getRyhmatyyppiV3).ifPresent(destination::setRyhmatyyppi);
        Optional.ofNullable(destination.getKayttoryhma()).map(KoodistoUtil::getKayttoryhmaV3).ifPresent(destination::setKayttoryhma);
        return destination;
    }

}

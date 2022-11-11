package fi.vm.sade.organisaatio.config;

import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioNimiModelMapper;
import fi.vm.sade.organisaatio.service.converter.LisatietoConverter;
import fi.vm.sade.organisaatio.service.converter.OrganisaatioRDTOToOrganisaatioConverter;
import fi.vm.sade.organisaatio.service.converter.OrganisaatioToOrganisaatioPerustietoConverter;
import fi.vm.sade.organisaatio.service.converter.OrganisaatioToOrganisaatioRDTOConverter;
import fi.vm.sade.organisaatio.service.converter.v2.RyhmaCriteriaV2ToV3Converter;
import fi.vm.sade.organisaatio.service.converter.v3.OrganisaatioRDTOV3ToOrganisaatioConverter;
import fi.vm.sade.organisaatio.service.converter.v3.OrganisaatioToOrganisaatioRDTOV3Converter;
import fi.vm.sade.organisaatio.service.converter.v3.RyhmaCriteriaV3Converter;
import fi.vm.sade.organisaatio.service.converter.v4.OrganisaatioRDTOV4ToOrganisaatioConverter;
import fi.vm.sade.organisaatio.service.converter.v4.OrganisaatioToOrganisaatioRDTOV4Converter;
import fi.vm.sade.organisaatio.service.converter.v4.YtjDtoToOrganisaatioConverter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ConverterConfig implements WebMvcConfigurer {

    private final OrganisaatioNimiModelMapper orgNimiMapper;
    private final ModelMapper modelMapper;

    public ConverterConfig(OrganisaatioNimiModelMapper orgNimiMapper, ModelMapper modelMapper) {
        this.orgNimiMapper = orgNimiMapper;
        this.modelMapper = modelMapper;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new LisatietoConverter());
        registry.addConverter(new OrganisaatioRDTOToOrganisaatioConverter(orgNimiMapper));
        registry.addConverter(new OrganisaatioToOrganisaatioPerustietoConverter());
        registry.addConverter(new OrganisaatioToOrganisaatioRDTOConverter(orgNimiMapper));
        registry.addConverter(new RyhmaCriteriaV2ToV3Converter(modelMapper));
        registry.addConverter(new OrganisaatioRDTOV3ToOrganisaatioConverter(orgNimiMapper));
        registry.addConverter(new OrganisaatioToOrganisaatioRDTOV3Converter(orgNimiMapper));
        registry.addConverter(new RyhmaCriteriaV3Converter(modelMapper));
        registry.addConverter(new OrganisaatioRDTOV4ToOrganisaatioConverter(orgNimiMapper));
        registry.addConverter(new OrganisaatioToOrganisaatioRDTOV4Converter(orgNimiMapper));
        registry.addConverter(new YtjDtoToOrganisaatioConverter());

    }
}



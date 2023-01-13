package fi.vm.sade.organisaatio.dto.mapping;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioCoreInfoDTOV2;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioSearchCriteriaDTOV2;
import fi.vm.sade.organisaatio.dto.v3.OrganisaatioRDTOV3;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioCoreInfoDTOV4;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioPerustietoV4;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioSearchCriteriaDTOV4;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OrganisaatioDTOV4ModelMapper extends ModelMapper {

    public OrganisaatioDTOV4ModelMapper() {
        super();

        final Converter<Set<String>, Set<String>> organisaatioTyypitV3ToV4 = mc -> mc.getSource() == null
                ? null
                : OrganisaatioTyyppi.fromValueToKoodi(mc.getSource());

        final Converter<Set<String>, Set<String>> organisaatioTyypitV4ToV3 = mc -> mc.getSource() == null
                ? null
                : OrganisaatioTyyppi.fromKoodiToValue(mc.getSource());

        final Converter<String, String> organisaatioTyyppiV3ToV4 = mc -> mc.getSource() == null
                ? null
                : OrganisaatioTyyppi.fromValue(mc.getSource()).koodiValue();

        final Converter<String, String> organisaatioTyyppiV4ToV3 = mc -> mc.getSource() == null
                ? null
                : OrganisaatioTyyppi.fromKoodiValue(mc.getSource()).value();

        final Converter<Set<OrganisaatioTyyppi>, Set<String>> organisaatioTyypitEnumV3ToV4 = mc -> mc.getSource() == null
                ? null
                : mc.getSource().stream().map(OrganisaatioTyyppi::koodiValue).collect(Collectors.toSet());

        final Converter<Set<String>, Set<OrganisaatioTyyppi>> organisaatioTyypitEnumV4ToV3 = mc -> mc.getSource() == null
                ? null
                : mc.getSource().stream().map(OrganisaatioTyyppi::fromKoodiValue).collect(Collectors.toSet());

        // OrganisaatioRDTO
        this.addMappings(new PropertyMap<OrganisaatioRDTOV3, OrganisaatioRDTOV4>() {
            @Override
            protected void configure() {
                using(organisaatioTyypitV3ToV4).map(source.getTyypit()).setTyypit(null);
            }
        });
        this.addMappings(new PropertyMap<OrganisaatioRDTOV4, OrganisaatioRDTOV3>() {
            @Override
            protected void configure() {
                using(organisaatioTyypitV4ToV3).map(source.getTyypit()).setTyypit(null);
            }
        });

        // OrganisaatioCoreInfoDTO
        this.addMappings(new PropertyMap<OrganisaatioCoreInfoDTOV2, OrganisaatioCoreInfoDTOV4>() {
            @Override
            protected void configure() {
                using(organisaatioTyypitV3ToV4).map(source.getTyypit()).setTyypit(null);
            }
        });
        this.addMappings(new PropertyMap<OrganisaatioCoreInfoDTOV4, OrganisaatioCoreInfoDTOV2>() {
            @Override
            protected void configure() {
                using(organisaatioTyypitV4ToV3).map(source.getTyypit()).setTyypit(null);
            }
        });

        // OrganisaatioSearchCriteriaDTO
        this.addMappings(new PropertyMap<OrganisaatioSearchCriteriaDTOV2, OrganisaatioSearchCriteriaDTOV4>() {
            @Override
            protected void configure() {
                using(organisaatioTyyppiV3ToV4).map(source.getOrganisaatiotyyppi()).setOrganisaatiotyyppi(null);
            }
        });
        this.addMappings(new PropertyMap<OrganisaatioSearchCriteriaDTOV4, OrganisaatioSearchCriteriaDTOV2>() {
            @Override
            protected void configure() {
                using(organisaatioTyyppiV4ToV3).map(source.getOrganisaatiotyyppi()).setOrganisaatiotyyppi(null);
            }
        });

        // OrganisaatioHakutulos
        this.addMappings(new PropertyMap<OrganisaatioPerustieto, OrganisaatioPerustietoV4>() {
            @Override
            protected void configure() {
                using(organisaatioTyypitEnumV3ToV4).map(source.getOrganisaatiotyypit()).setOrganisaatiotyypit(null);
                map().setMaskingActive(source.isMaskingActive());
            }
        });
        this.addMappings(new PropertyMap<OrganisaatioPerustietoV4, OrganisaatioPerustieto>() {
            @Override
            protected void configure() {
                using(organisaatioTyypitEnumV4ToV3).map(source.getOrganisaatiotyypit()).setOrganisaatiotyypit(null);
            }
        });
    }

}

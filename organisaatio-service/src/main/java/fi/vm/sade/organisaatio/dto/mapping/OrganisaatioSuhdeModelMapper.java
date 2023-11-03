package fi.vm.sade.organisaatio.dto.mapping;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioSuhdeDTOV2;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class OrganisaatioSuhdeModelMapper extends ModelMapper {

    public OrganisaatioSuhdeModelMapper() {
        super();

        final Converter<OrganisaatioSuhde, Set<String>> parentTyypitConverter = mc -> mc.getSource().getParent() == null
                ? null
                : OrganisaatioTyyppi.fromKoodiToValue(mc.getSource().getParent().getTyypit());

        final Converter<OrganisaatioSuhde, Set<String>> childTyypitConverter = mc -> mc.getSource().getChild() == null
                ? null
                : OrganisaatioTyyppi.fromKoodiToValue(mc.getSource().getChild().getTyypit());


        this.addMappings(new PropertyMap<OrganisaatioSuhde, OrganisaatioSuhdeDTOV2>() {
            @Override
            protected void configure() {
                // Monikielinen nimi
                map().getChild().setNimi(source.getChild().getNimi().getValues());
                map().getParent().setNimi(source.getParent().getNimi().getValues());

                map(source.getParent()).setParent(null);
                map(source.getChild()).setChild(null);

                using(parentTyypitConverter).map(source).getParent().setTyypit(new HashSet<>());
                using(childTyypitConverter).map(source).getChild().setTyypit(new HashSet<>());
            }
        });
    }
}

package fi.vm.sade.organisaatio.dto.mapping;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioLiitosDTOV2;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class OrganisaatioLiitosModelMapper extends ModelMapper {

    public OrganisaatioLiitosModelMapper() {
        super();

        final Converter<Set<String>, Set<String>> tyyppiConverter = mc -> mc.getSource() == null
                ? null
                : OrganisaatioTyyppi.fromKoodiToValue(mc.getSource());

        this.addMappings(new PropertyMap<OrganisaatioSuhde, OrganisaatioLiitosDTOV2>() {
            @Override
            protected void configure() {
                // Monikielinen nimi
                map().getOrganisaatio().setNimi(source.getChild().getNimi().getValues());
                map().getOrganisaatio().setMaskingActive(source.getChild().isMaskingActive());
                map().getKohde().setNimi(source.getParent().getNimi().getValues());
                map().getKohde().setMaskingActive(source.getParent().isMaskingActive());

                map().getOrganisaatio().setOid((source.getChild().getOid()));
                // Katso tuolta miksi enum status --> string status
                // https://github.com/jhalterman/modelmapper/issues/99
                map(source.getChild().getStatus()).getOrganisaatio().setStatus(null);
                using(tyyppiConverter).map(source.getChild().getTyypit()).getOrganisaatio().setTyypit(null);

                map().getKohde().setOid((source.getParent().getOid()));
                map(source.getParent().getStatus()).getKohde().setStatus(null);
                using(tyyppiConverter).map(source.getParent().getTyypit()).getKohde().setTyypit(null);

            }
        });
    }
}

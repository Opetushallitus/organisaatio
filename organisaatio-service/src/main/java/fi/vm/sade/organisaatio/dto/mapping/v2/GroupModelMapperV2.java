package fi.vm.sade.organisaatio.dto.mapping.v2;

import fi.vm.sade.organisaatio.dto.v2.OrganisaatioGroupDTOV2;
import fi.vm.sade.organisaatio.model.Organisaatio;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;

@Component
public class GroupModelMapperV2 extends ModelMapper {

    public GroupModelMapperV2() {
        super();
        this.addMappings(new PropertyMap<Organisaatio, OrganisaatioGroupDTOV2>() {
            @Override
            protected void configure() {
                // Monikielinen nimi
                map().setNimi(source.getNimi().getValues());
                map().setKuvaus(source.getKuvaus2().getValues());
                map().setParentOid(source.getParent().getOid());

                // tuetaan vanhaa formaattia ryhmätyypeille ja käyttöryhmille
                map().setRyhmatyypit(source.getRyhmatyypitV1());
                map().setKayttoryhmat(source.getKayttoryhmatV1());;
            }
        });
    }

}

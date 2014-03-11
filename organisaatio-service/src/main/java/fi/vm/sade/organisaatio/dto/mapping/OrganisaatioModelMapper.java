/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.vm.sade.organisaatio.dto.mapping;

import fi.vm.sade.organisaatio.dto.v2.OrganisaatioYhteystiedotDTOV2;
import fi.vm.sade.organisaatio.model.Organisaatio;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

/**
 *
 * @author simok
 */
public class OrganisaatioModelMapper extends ModelMapper {

    public OrganisaatioModelMapper() {
        super();
        
        this.addMappings(new PropertyMap<Organisaatio, OrganisaatioYhteystiedotDTOV2>() {
            @Override
            protected void configure() {
                map().setNimi(source.getNimi().getValues());
            }
        });
    }
}

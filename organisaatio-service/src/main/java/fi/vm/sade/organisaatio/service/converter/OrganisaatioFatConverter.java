package fi.vm.sade.organisaatio.service.converter;

import javax.persistence.EntityManager;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoArvoDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoDTO;
import fi.vm.sade.organisaatio.model.Organisaatio;
//import fi.vm.sade.organisaatio.api.model.OrganisaatioFatDTO;

/**
* @author Antti Salonen
*/
public class OrganisaatioFatConverter extends OrganisaatioConverter {

    OrganisaatioFatConverter(ConverterFactory converterFactory, EntityManager entityManager) {
        super(converterFactory, entityManager);
    }

    @Override
    protected Class initDtoClass() {
        return  OrganisaatioDTO.class;
    }

    @Override
    protected Class initJpaClass() {
        return Organisaatio.class;
    }

    @Override
    public void setValuesToDTO(Organisaatio entity, OrganisaatioDTO dto) {
        super.setValuesToDTO(entity, dto);
        OrganisaatioDTO fatdto = (OrganisaatioDTO) dto;
        fatdto.getYhteystiedot().addAll(converterFactory.convertToDTO(entity.getYhteystiedot(), YhteystietoDTO.class));
        fatdto.getYhteystietoArvos().addAll(converterFactory.convertToDTO(entity.getYhteystietoArvos(), YhteystietoArvoDTO.class));
    }

}

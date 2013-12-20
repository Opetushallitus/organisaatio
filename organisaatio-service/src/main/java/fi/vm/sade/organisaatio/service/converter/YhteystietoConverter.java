package fi.vm.sade.organisaatio.service.converter;

import javax.persistence.EntityManager;

import fi.vm.sade.organisaatio.api.model.types.YhteystietoDTO;
import fi.vm.sade.organisaatio.model.Yhteystieto;

public class YhteystietoConverter extends Converter<YhteystietoDTO, Yhteystieto>{

    public YhteystietoConverter(ConverterFactory converterFactory,
            EntityManager entityManager) {
        super(converterFactory, entityManager);
        // TODO Auto-generated constructor stub
    }


}

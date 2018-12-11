package fi.vm.sade.organisaatio.service.converter;

import fi.vm.sade.organisaatio.api.model.types.YhteystietoDTO;
import fi.vm.sade.organisaatio.model.Yhteystieto;

import javax.persistence.EntityManager;

public class YhteystietoConverter extends Converter<YhteystietoDTO, Yhteystieto>{

    public YhteystietoConverter(ConverterFactory converterFactory,
            EntityManager entityManager) {
        super(converterFactory, entityManager);
    }


}

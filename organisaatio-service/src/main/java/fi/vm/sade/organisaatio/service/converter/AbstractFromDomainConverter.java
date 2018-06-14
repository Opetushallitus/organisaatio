package fi.vm.sade.organisaatio.service.converter;

import fi.vm.sade.organisaatio.model.BaseEntity;
import org.springframework.core.convert.converter.Converter;

public abstract class AbstractFromDomainConverter<FROM extends BaseEntity, TO> implements Converter<FROM, TO> {

}

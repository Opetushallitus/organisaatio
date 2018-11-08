package fi.vm.sade.organisaatio.service.converter;

import com.google.common.base.Function;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi.Teksti;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;

import javax.annotation.Nullable;

public class MonikielinenTekstiTyyppiToEntityFunction implements Function<MonikielinenTekstiTyyppi, MonikielinenTeksti> {

    @Override
    public MonikielinenTeksti apply(@Nullable fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi source) {
        if (source == null) {
            return null;
        }
        MonikielinenTeksti entity = new MonikielinenTeksti();

        for (Teksti teksti : source.getTeksti()) {
            entity.addString(teksti.getKieliKoodi(), teksti.getValue());
        }
        return entity;
    }
}

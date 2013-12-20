package fi.vm.sade.organisaatio.revised.ui.helper;

import fi.vm.sade.generic.ui.component.FieldValueFormatter;
import fi.vm.sade.koodisto.service.types.common.KoodiType;

public class KoodistoUriAndVersionFieldFormatter implements FieldValueFormatter<KoodiType> {

    @Override
    public Object formatFieldValue(KoodiType dto) {
        // TODO Auto-generated method stub
        return dto.getKoodiUri() + "#" + dto.getVersio();
    }
    

}

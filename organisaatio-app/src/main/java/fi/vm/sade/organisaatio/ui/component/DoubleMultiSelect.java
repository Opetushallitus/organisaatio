/*
 *
 * Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * European Union Public Licence for more details.
 */

package fi.vm.sade.organisaatio.ui.component;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.ui.VerticalLayout;

/**
 * An abstract component to be used in YhteystietojenTyyppiForm for defining groups of fields.
 * The component consists of a list of DoubleCheckbox components.
 *
 * @author Markus Holi
 */
abstract class DoubleMultiSelect<T> extends VerticalLayout {

    
    protected List<DoubleCheckbox> selectOptions;
    protected List<T> model;

    DoubleMultiSelect(String[] optionValues, List<T> m) {
        model = m;
        createSelectOptions(optionValues, null);
    }

    DoubleMultiSelect(String[] optionValues, String[] optionValuesSv, List<T> m) {
        model = m;
        createSelectOptions(optionValues, optionValuesSv);
    }


    /**
     * Creating the double checbkox components for the options contained in the to optionValues array.
     * @param optionValues
     */
    private void createSelectOptions(String[] optionValues, String[] optionValuesSv) {
        int index = 0;
        selectOptions = new ArrayList<DoubleCheckbox>();
        for (String curOptionValue : optionValues) {
            DoubleCheckbox curSelectOption = createDCheckbox(curOptionValue);
            if (optionValuesSv != null) {
                curSelectOption.getModel().setNimiSv(optionValuesSv[index++]);
            }
            addComponent(curSelectOption);
            selectOptions.add(curSelectOption);
        }
    }

    /**
     * Creating the double checkbox component for an option.
     *
     */
    protected abstract DoubleCheckbox createDCheckbox(String curOptionval);

    /**
     * Model for binding the checkbox selections to the model.
     *
     */
    protected abstract List<T> saveModel();
}

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

import com.vaadin.ui.ComboBox;
import fi.vm.sade.generic.ui.component.MultipleSelectToTableWrapper;

/**
 * Multiple select that has autocomplete combobox search field, add button, and table containing the values.
 * Table also contains remove buttons (property REMOVE_BUTTON) for each row.
 * Note that MultipleSelect can be customized, eg if you need to customize table columns.
 *
 * @author Antti Salonen
 */
public class MultipleSelect extends MultipleSelectToTableWrapper {

    public MultipleSelect(Class modelClass) {
        super(modelClass);
        setField(new ComboBox());
    }

}

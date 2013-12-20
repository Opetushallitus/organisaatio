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

package fi.vm.sade.organisaatio.ui.factory;

import com.vaadin.ui.*;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.common.validation.MultiLingualText;
import fi.vm.sade.generic.ui.ValidationUtils;
import fi.vm.sade.generic.ui.component.MultiLingualTextField;
import fi.vm.sade.organisaatio.ui.component.MultipleSelect;
import fi.vm.sade.organisaatio.ui.component.OsoiteField;
import fi.vm.sade.organisaatio.ui.util.UiUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

import java.lang.reflect.Field;
import java.util.Date;

/**
 * @author Antti
 */
public class FieldFactory {
    private static final Logger LOG = LoggerFactory.getLogger(FieldFactory.class);

    public static com.vaadin.ui.Field createFieldComponent(Field field, FieldUiDefinition uiDef, String propertyId,
                                                           ListAdapter listAdapter1) {
        // prepare

        com.vaadin.ui.Field component = null;
        boolean required = field.getAnnotation(NotNull.class) != null;
        boolean dynamic = uiDef.dynamic();
        String captionKey = uiDef.caption() ? propertyId : null;

        // create component

        if (ComboBox.class.equals(uiDef.fieldClass())) {
            component = new ComboBox();
            UiUtils.configureSelect((Select) component, true, required);
            configureComponent(component, captionKey, field.getDeclaringClass(), field.getName());
            UiUtils.configureOptions((Select) component, field, uiDef.targetClass(), uiDef.targetProperty(), uiDef.itemProperty(), dynamic, listAdapter1);
        } else if (MultipleSelect.class.equals(uiDef.fieldClass())) {
            component = new MultipleSelect(uiDef.targetClass());
            Select select = (Select) ((MultipleSelect) component).getField();
            UiUtils.configureSelect(select, true, required);
            configureComponent(component, captionKey, field.getDeclaringClass(), field.getName());
            UiUtils.configureOptions(select, field, uiDef.targetClass(), uiDef.targetProperty(), uiDef.itemProperty(),
                    dynamic, listAdapter1);
        } else if (field.getType().equals(Date.class)) {
            component = new PopupDateField();
            ((PopupDateField)component).setResolution(PopupDateField.RESOLUTION_DAY);
            configureComponent(component, captionKey, field.getDeclaringClass(), field.getName());
        } else if (OsoiteField.class.equals(uiDef.fieldClass())) {
            component = new OsoiteField();
            configureComponent(component, captionKey, field.getDeclaringClass(), field.getName());
        } else if (MultiLingualText.class.isAssignableFrom(field.getType())) {
            component = new MultiLingualTextField(I18N.getLocale());
            configureComponent(component, captionKey, field.getDeclaringClass(), field.getName());
        } else if (TextArea.class.equals(uiDef.fieldClass())) {
            component = new TextArea();
            ((TextArea)component).setNullRepresentation("");
            configureComponent(component, captionKey, field.getDeclaringClass(), field.getName());
        } else if (TextField.class.equals(uiDef.fieldClass())) {
            component = new TextField();
            ((TextField)component).setNullRepresentation("");
            configureComponent(component, captionKey, field.getDeclaringClass(), field.getName());
        }

        return component;
    }

    private static void configureComponent(com.vaadin.ui.Field component, String captionKey,
                                           Class<?> validateClass, String validateField) {
        // add caption
        if (captionKey != null) {
            component.setCaption(UiUtils.getText(captionKey));
        }

        // add validator
        if (validateClass != null) {
            ValidationUtils.addValidatorAndSetRequired(component, validateClass, validateField);
        }

        // null representation
        if (component instanceof AbstractTextField) {
            AbstractTextField abstractTextField = (AbstractTextField) component;
            abstractTextField.setNullRepresentation("");
        }

        //DEBUGSAWAY:LOG.debug("configured component: "+(component != null ? component.getClass().getSimpleName() : "[null]")+", captionKey: "+captionKey+", debugId: "+component.getDebugId());
    }
}

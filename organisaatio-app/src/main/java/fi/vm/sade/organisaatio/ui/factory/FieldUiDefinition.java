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

import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;

/**
 * Defines how to create vaadin field component for certain java field
 *
 * @author Antti Salonen
 */
public class FieldUiDefinition {

    private boolean caption = true;
    private Class targetClass;
    private boolean dynamic;
    private String targetProperty;
    private String itemProperty;
    private Class<? extends Field> fieldClass = TextField.class;

    public FieldUiDefinition() {
    }

    public void setFieldClass(Class<? extends Field> fieldClass) {
        this.fieldClass = fieldClass;
    }

    boolean caption() {
        return caption;
    }

    public Class targetClass() {
        return targetClass;
    }

    boolean dynamic() {
        return dynamic;
    }

    String targetProperty() {
        return targetProperty;
    }

    String itemProperty() {
        return itemProperty;
    }

    Class<? extends Field> fieldClass() {
        return fieldClass;
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
    }
}

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

/**
 * Same as OsoiteField, but always shows all the fields. Meant for inputting foreign address.
 *
 * @author Antti Salonen
 */
public class OsoiteUlkomaaField extends OsoiteField {

    @Override
    public void setFieldsVisibleBasedOnMaa(Object organisaatioMaa) {
        // all osoite fields always visible
        osavaltio.setVisible(true);
        extraRivi.setVisible(true);
        maa.setVisible(true);
    }

    @Override
    protected void addMaaChangedListener() {
        // do nothing here because component is meant to be used for independent foreign address inputting
    }
}

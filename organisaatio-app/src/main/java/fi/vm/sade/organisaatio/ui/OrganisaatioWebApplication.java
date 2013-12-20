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
package fi.vm.sade.organisaatio.ui;

import com.vaadin.ui.Component;

import org.springframework.beans.factory.annotation.Configurable;

/**
 * @author mlyly
 */
@Configurable(preConstruction = true)
public class OrganisaatioWebApplication extends MainWindow {

    private static final long serialVersionUID = -7821941579403223801L;


    /**
     * Added all organisaatio components to single application.
     *
     * @return
     */
    @Override
    protected Component createRootComponent() {

        return createOrganisaatioMainContainer();

//        TabSheet tabSheet = new TabSheet();
//        tabSheet.setHeight(-1, Sizeable.UNITS_PIXELS);
//
//        // --- tabs ---
//        tabSheet.addTab(createOrganisaatioMainContainer(), "Organisaatiot");
//        tabSheet.addTab(createYhteystietoView(), "Yhteystietotyypit");
//        return tabSheet;
    }
}

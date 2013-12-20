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

import com.vaadin.Application;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
//import fi.vm.sade.organisaatio.service.YhteystietojenTyyppiService;
//import fi.vm.sade.organisaatio.service.YhteystietojenTyyppiServiceMock;
import fi.vm.sade.organisaatio.ui.organisaatio.YhteystietojenTyyppiEditor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple component driver application to test component separately.
 *
 * @author Jukka Raanamo <jukka.raanamo@kinetik.fi>
 */
@Configurable(preConstruction = false)
public class ComponentTestApplication extends Application {

    public void init() {
        
    }
    /*
    @Autowired(required = true)
    private YhteystietojenTyyppiService lisatiedotService;
    private Window mainWindow;
    private Form mainForm;
    private List<Component> testComponents;

    @Override
    public void init() {

        mainWindow = new Window("Component Test Application");
        mainForm = new Form();
        mainWindow.addComponent(mainForm);

        final Select testSelector = new Select("Test component:");
        testSelector.setImmediate(true);

        testComponents = initComponents();

        for (Component c : testComponents) {
            testSelector.addItem(c);
        }

        mainWindow.addComponent(testSelector);
        testSelector.addListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                showComponent((Component) event.getProperty().getValue());
            }

        });
        Button testButton = new Button("Test");
        testButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                showComponent((Component) testSelector.getValue());
            }

        });
        mainWindow.addComponent(testButton);

        setMainWindow(mainWindow);

    }

    private void showComponent(Component component) {

        if (component == null) {
            return;
        }

        Window window = new Window(component.getClass().getSimpleName());
        window.addComponent(component);
        mainWindow.addWindow(window);

    }

    /**
     * Add components to test here.
     *
     * @return
     */
 /*   private List<Component> initComponents() {

        List<Component> tests = new ArrayList<Component>();

        YhteystietojenTyyppiEditor editor = new YhteystietojenTyyppiEditor(mainForm, null);

        YhteystietojenTyyppiEditor.EditorModel model = new YhteystietojenTyyppiEditor.EditorModel(lisatiedotService.findYhteystietoMetadataForOrganisaatio(
                Collections.EMPTY_LIST));

        model.addValues(lisatiedotService.findYhteystietoArvos(YhteystietojenTyyppiServiceMock.ORGANISAATIO_WITH_COMPLETE_EXTRA_VALUES));

        editor.populate(model);
        tests.add(editor);

        return tests;

    }*/

}


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

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import fi.vm.sade.organisaatio.ui.model.VuosiluokkaSelections;


/**
 * Component for the management of organisaatio.vuosiluokat. Intended to be used in OrganisaatioForm.
 * 
 * @author markus
 *
 */
public class VuosiluokatComponent extends VerticalLayout {
    
    /**
     * Vuosiluokat custom string.
     */
    private TextField vuosiluokat;
    
    /**
     * Selection for the additional vuosiluokat values
     */
    private OptionGroup vuosiluokkaValinnat;
    
    /**
     * List for saving the values.
     */
    private List<String> model;
    
    
    /**
     * Creation of VuosiluokatComponent.
     * @param model the list of strings where the values are saved.
     */
    public VuosiluokatComponent(List<String> model) {
        super();
        this.model = model;
        initComponent();
        
    }
    
    @Override
    public void setImmediate(boolean bVal) {
        this.vuosiluokat.setImmediate(bVal);
        this.vuosiluokkaValinnat.setImmediate(bVal);
    }
    
    public void addListener(Property.ValueChangeListener listener) {
        this.vuosiluokat.addListener(listener);
        this.vuosiluokkaValinnat.addListener(listener);
    }
    
    /**
     * Initialization of the component. Creation of child components.
     */
    private void initComponent() {
        try  {
            vuosiluokat = new TextField();
            this.addComponent(vuosiluokat);
            VerticalLayout vert = new VerticalLayout();
            vert.setSizeUndefined();
            vuosiluokkaValinnat = new OptionGroup();
            vuosiluokkaValinnat.setMultiSelect(true);
            
            vuosiluokkaValinnat.addItem(VuosiluokkaSelections.ESIOPETUS);
            vuosiluokkaValinnat.addItem(VuosiluokkaSelections.LISAOPETUS);
            vuosiluokkaValinnat.addItem(VuosiluokkaSelections.AAMU_JA_ILTAPAIVA);
            
            vert.addComponent(vuosiluokkaValinnat);
            this.addComponent(vert);
            populateVuosiluokat();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            
        } 
    }
    
    /**
     * Saves the model (list of strings) containing the data given by the user.
     * @return the model.
     */
    public List<String> save() {
        List<String> savedModel = new ArrayList<String>();
        savedModel.add((String)vuosiluokat.getValue());
        Collection<String> selections = (vuosiluokkaValinnat.getValue() != null) ? (Collection<String>)vuosiluokkaValinnat.getValue() : new ArrayList<String>();
        for (String curSel : selections) {
           
            savedModel.add(curSel);
        }
        return savedModel;
    }

    /**
     * Populates the user interface based on the data in the model.
     */
    private void populateVuosiluokat() {
        //DEBUGSAWAY:log.debug("Populating vuosiluokat");
        if (model != null) {
            //DEBUGSAWAY:log.debug("vuosiluokat is not null!");
            List<String> checkboxVals = new ArrayList<String>();
            for (String curVal : model) {
                //DEBUGSAWAY:log.debug("cur vuosiluokat val: " + curVal);
                if (isValueInOpetustyyppi(curVal)) {
                    //DEBUGSAWAY:log.debug("checkbox matched");
                    checkboxVals.add(curVal);
                } else {
                    //DEBUGSAWAY:log.debug("text field match");
                    this.vuosiluokat.setValue(curVal);
                }
            }
            this.vuosiluokkaValinnat.setValue(checkboxVals);
        }
    }
    
    private boolean isValueInOpetustyyppi(String val) {
        for (String curOT : VuosiluokkaSelections.ALL) {
            if (curOT.equals(val)) {
                return true;
            }
        }
        return false;
    }
    
    public TextField getVuosiluokat() {
        return vuosiluokat;
    }

    public void setVuosiluokat(TextField vuosiluokat) {
        this.vuosiluokat = vuosiluokat;
    }

    public OptionGroup getVuosiluokkaValinnat() {
        return vuosiluokkaValinnat;
    }

    public void setVuosiluokkaValinnat(OptionGroup vuosiluokkaValinnat) {
        this.vuosiluokkaValinnat = vuosiluokkaValinnat;
    }

    public List<String> getModel() {
        return model;
    }

    public void setModel(List<String> model) {
        this.model = model;
    }

 
}

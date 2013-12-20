
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
package fi.vm.sade.organisaatio.revised.ui.component.organisaatioform;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Button;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.koodisto.widget.KoodistoComponent;
import fi.vm.sade.organisaatio.api.model.types.OsoiteDTO;
import fi.vm.sade.organisaatio.api.model.types.OsoiteTyyppi;
import fi.vm.sade.organisaatio.ui.component.OsoiteField;
import fi.vm.sade.organisaatio.ui.listener.RemoveComponentListener;
import fi.vm.sade.organisaatio.ui.model.OrganisaatioModel;
import fi.vm.sade.organisaatio.ui.util.UiUtils;

/**
 * Other addresses component for OrganisaatioEditForm.
 *
 * TODO fix the language dependant data: "ensisijainen osoite", "muu" and their comparisons and handlings / 29.1.2013 mlyly
 *
 * @author markus
 *
 */
public class MuutOsoitteetComponent extends VerticalLayout {

    /**
     * The other addresses present in the form.
     */
    private List<OsoiteField> otherAddresses = new ArrayList<OsoiteField>();
    
    /**
     * The Organisaatio model object.
     */
    private OrganisaatioModel model;
    
    /**
     * The component which shows what country is the organisaatio from. 
     * Has an effect on the OsoiteField component.
     */
    private KoodistoComponent koodistoMaa;
    
    /**
     * The form object to which the address fields are added.
     */
    private Form form;
    
    private Property.ValueChangeListener listener;


    public void setListener(Property.ValueChangeListener listener) {
        this.listener = listener;
    }

    MuutOsoitteetComponent(OrganisaatioModel model) {
        super();
        this.model = model;   
    }
    
    /**
     * Creation of the new address link button and listeners.
     * @return
     */
    Button createNewAddressLink(Property.ValueChangeListener listener) {
        this.listener = listener;
        Button newAddressBtn = UiUtils.buttonLink(null, I18N.getMessage("c_muuOsoite"), new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                OsoiteDTO newOs = new OsoiteDTO();
                newOs.setOsoiteTyyppi(OsoiteTyyppi.MUU);
                addOtherAddress(newOs);
                addOtherAddresesToLayout();
            }
        });
        return newAddressBtn;
    }
    
    /**
     * Adding of new address field to the form.
     * @param muuOsoite
     */
    private void addOtherAddress(OsoiteDTO muuOsoite) {
        if (!isOsoiteInModel(muuOsoite)) {
            model.getMuutOsoitteet().add(muuOsoite);
        }
        OsoiteField osoiteComponent = (this.koodistoMaa.getValue() != null) ? new OsoiteField(this.koodistoMaa) : new OsoiteField();
        osoiteComponent.setImmediate(true);

        String otherAddrId = getAddlAddrPropId();
        osoiteComponent.setPropertyId(otherAddrId);
        this.form.addField(otherAddrId, osoiteComponent);

        //Must be set from the "maa"- comboboxString);
        osoiteComponent.initMuuFields(new String[]{"Ensisijainen postiosoite", "muu"});
        osoiteComponent.setPropertyDataSource(new ObjectProperty(muuOsoite, OsoiteDTO.class));

        osoiteComponent.addRemoveListener(new RemoveComponentListener() {
            @Override
            public void removeComponent(Field component) {

                if (model.getMuutOsoitteet() != null && component instanceof OsoiteField) {

                    if (form.removeItemProperty(((OsoiteField) component).getPropertyId())) {
                        ((HorizontalLayout) component.getParent()).removeComponent(component);
                        OsoiteDTO osoite = (OsoiteDTO) component.getPropertyDataSource().getValue();
                        model.getMuutOsoitteet().remove(osoite);
                        model.getMuutYhteystiedot().remove(osoite);
                        otherAddresses.remove((OsoiteField) component);
                    }
                }

            }
        });
        osoiteComponent.setImmediate(true);
        osoiteComponent.addListener(listener);

        otherAddresses.add(osoiteComponent);
    }
    
    /**
     * Checks if other addresses exist in the model and if so adds 
     * them to the form.
     */
    void checkIfOtherAddressesExists() {
        if (model.getMuutOsoitteet() != null && model.getMuutOsoitteet().size() > 0) {
            otherAddresses.clear();
            List<OsoiteDTO> tempMuut = new ArrayList<OsoiteDTO>();
            for (OsoiteDTO osoite : model.getMuutOsoitteet()) {
                tempMuut.add(osoite);
            }

            for (OsoiteDTO curOs : tempMuut) {
                addOtherAddress(curOs);
            }
            addOtherAddresesToLayout();
        }
    }
    
    
    private String getAddlAddrPropId() {
        if (otherAddresses != null) {
            return "muutOsoitteet_" + otherAddresses.size();
        } else {
            return "muutOsoitteet_0";
        }
    }
    
    private boolean isOsoiteInModel(OsoiteDTO muuOsoite) {
        for (OsoiteDTO curOs : model.getMuutOsoitteet()) {
            if (curOs != null && muuOsoite.getYhteystietoOid() != null && muuOsoite.getYhteystietoOid().equals(curOs.getYhteystietoOid())) {
                return true;
            }
        }
        return false;
    }
    
    private void addOtherAddresesToLayout() {
        removeAllComponents();
        for (OsoiteField osoite : otherAddresses) {
            HorizontalLayout osoiteLayout = new HorizontalLayout();
            osoiteLayout.addComponent(osoite);
            addComponent(osoiteLayout);
        }   
        requestRepaint();
    }
    
    /**
     * Sets the koodistoMaa.
     * @param koodistoMaa
     */
    public void setKoodistoMaa(KoodistoComponent koodistoMaa) {
        this.koodistoMaa = koodistoMaa;
    }
    
    /**
     * Sets the form.
     * @param form
     */
    public void setForm(Form form) {
        this.form = form;
    }
    
    /**
     * Gets the other addresses.
     * @return
     */
    public List<OsoiteField> getOtherAddresses() {
        return otherAddresses;
    }


}

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

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Button.ClickEvent;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoElementtiDTO;
import com.vaadin.data.util.NestedMethodProperty;
import fi.vm.sade.organisaatio.ui.util.UiUtils;

/**
 * A Component for YhteystietojenTyyppiForm. Two checkboxes for selecting
 * whether the choice defined in choiceValue is used and whether it is
 * obligatory.
 *
 * @author Markus Holi
 */
public class DoubleCheckbox extends HorizontalLayout {

    private CheckBox isUsed;
    private CheckBox isObligatory;
    private Label choiceName;
    private String choiceValue;
    private Button removeButton;
    private Button editButton;
    private YhteystietoElementtiDTO model;

    public DoubleCheckbox(YhteystietoElementtiDTO modelP) {
        setSpacing(true);
        setWidth("100%");
        model = modelP;
        isUsed = new CheckBox("");
        isUsed.setPropertyDataSource(new NestedMethodProperty(model, "kaytossa"));
        addComponent(isUsed);
        isObligatory = new CheckBox("");
        isObligatory.setPropertyDataSource(new NestedMethodProperty(model, "pakollinen"));
        addComponent(isObligatory);
        choiceName = new Label();

        String lang = I18N.getLocale().getLanguage().toLowerCase();
        if (lang.equals("sv")) {
            choiceName.setPropertyDataSource(new NestedMethodProperty(model, "nimiSv"));
        } else {
            choiceName.setPropertyDataSource(new NestedMethodProperty(model, "nimi"));
        }

        addComponent(choiceName);
        setExpandRatio(isUsed, 1.0f);
        setExpandRatio(isObligatory, 1.0f);
        setExpandRatio(choiceName, 2.0f);
        setComponentAlignment(isUsed, Alignment.MIDDLE_LEFT);
        setComponentAlignment(isObligatory, Alignment.MIDDLE_LEFT);
        setChoiceValue(model.getNimi());
    }

    void addRemoveButton(final MuuYhteystietoList parent) {
        if (removeButton == null) {
            removeButton = UiUtils.buttonLink(this, I18N.getMessage("YhteystietojenTyyppiForm.poista"), new Button.ClickListener() {
                @Override
                public void buttonClick(ClickEvent event) {
                    removeElement(parent);
                }
            });

            removeButton.setWidth("17px");
        }
    }

    void addEditButton(final MuuYhteystietoList parent) {
        removeComponent(choiceName);
        String nameStr = (model.getNimi().length() > 18) ? model.getNimi().substring(0, 16) + "..." : model.getNimi();

        editButton = UiUtils.buttonLink(this, nameStr, new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {

                editElement(parent);
            }
        });
        editButton.setWidth("125px");
        editButton.setDescription(model.getNimi());

        setComponentAlignment(editButton, Alignment.TOP_RIGHT);
    }

    private void removeElement(MuuYhteystietoList parent) {
        parent.removeYhteystietoElementti(this);
    }

    private void editElement(MuuYhteystietoList parent) {
        parent.editYhteystietoElementti(this, model.getTyyppi());
    }

    public YhteystietoElementtiDTO getModel() {
        if (model.isPakollinen()) {
            model.setKaytossa(true);
        }
        return model;
    }

    void bind(YhteystietoElementtiDTO model) {
        this.model = model;
        isUsed.setPropertyDataSource(new NestedMethodProperty(model, "kaytossa"));
        isObligatory.setPropertyDataSource(new NestedMethodProperty(model, "pakollinen"));
        choiceName.setPropertyDataSource(new NestedMethodProperty(model, "nimi"));
        if (editButton != null) {
            String nameStr = (model.getNimi().length() > 18) ? model.getNimi().substring(0, 16) + "..." : model.getNimi();
            editButton.setCaption(nameStr);
            editButton.setDescription(model.getNimi());
        }
    }

    /**
     *
     * @return a boolean value indicating whether the isUsed checkbox is
     * selected.
     */
    public boolean isUsedClicked() {
        return isUsed.booleanValue();
    }

    /**
     *
     * @return a boolean value indicating whether the isObligatory checkbox is
     * selected.
     */
    public boolean isObligatoryClicked() {
        return isObligatory.booleanValue();
    }
    
    public CheckBox getUsed() {
    	return isUsed;
    }
    
    public CheckBox getObligatory() {
    	return isObligatory;
    }    

    /**
     * @return a String which is the name of the field this double checkbox
     * represents.
     */
    public String getChoiceValue() {
        return choiceValue;
    }

    public void setChoiceValue(String choiceValue) {
        this.choiceValue = choiceValue;
    }
}

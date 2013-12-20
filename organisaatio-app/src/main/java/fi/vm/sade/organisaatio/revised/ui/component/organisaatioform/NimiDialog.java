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

import static fi.vm.sade.organisaatio.KoodistoURI.KOODISTO_KIELI_URI;

import com.vaadin.data.Property;
import com.vaadin.data.util.NestedMethodProperty;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.koodisto.widget.KoodistoComponent;
import fi.vm.sade.organisaatio.revised.ui.helper.KoodistoUriFieldFormatter;
import fi.vm.sade.organisaatio.ui.component.ComponentBuilder;
import fi.vm.sade.organisaatio.ui.model.KielikaannosModel;
import fi.vm.sade.vaadin.util.UiUtil;

/**
 * The dialog for adding a localized name for hakutoimisto.
 * 
 * @author Markus
 *
 */
class NimiDialog extends VerticalLayout {
    
    private static final long serialVersionUID = 6959692670741926449L;
    
    /**
     * The language component for selecting a language for the name.
     */
    private KoodistoComponent kieliKc;
    
    /**
     * The comonent for writing the name.
     */
    private TextField nimi;
    
    /**
     * The model for language and name value.
     */
    private KielikaannosModel model;
    
    /**
     * The listener for the cancel button. given as parameter to the constructor of this dialog.
     */
    private Button.ClickListener peruutaListener;
    
    /**
     * The listener for the save button. Given as a parameter for the constructor of this dialog.
     */
    private Button.ClickListener tallennaListener;
    
    private Property.ValueChangeListener changeListener;
    
    NimiDialog(KielikaannosModel model, Button.ClickListener peruutaListener, Button.ClickListener tallennaListener, Property.ValueChangeListener listener) {
        super();
        this.changeListener = listener;
        this.model = model;
        this.peruutaListener = peruutaListener;
        this.tallennaListener = tallennaListener;
        setSpacing(true);
        setMargin(true);
        setSizeUndefined();
        setWidth("100%");
    }
    
    @Override
    public void attach() {
        buildLayout();
    }

    /**
     * Creation of the dialog ui-layout.
     */
    private void buildLayout() {
        FormLayout mainArea = new FormLayout();
        mainArea.setSpacing(true);
        kieliKc = ComponentBuilder.koodistoCombobox(KOODISTO_KIELI_URI)
                .withDataSource(model, "kielikoodi")
                .build();
        kieliKc.setFieldValueFormatter(new KoodistoUriFieldFormatter());
        kieliKc.setCaption(I18N.getMessage("OrganisaatioEditForm.koodistoKieli"));
        kieliKc.setImmediate(true);
        kieliKc.addListener(this.changeListener);
        mainArea.addComponent(kieliKc);
        nimi = UiUtil.textField(mainArea, "", "", true);
        nimi.setWidth("350px");
        nimi.setCaption(I18N.getMessage("OrganisaatioKuvailevatTiedotFormView.otNimi"));
        nimi.setPropertyDataSource(new NestedMethodProperty(model, "arvo"));
        nimi.addListener(this.changeListener);
        addComponent(mainArea);
        HorizontalLayout hl = UiUtil.horizontalLayout();
        hl.setWidth("100%");
        Button peruuta = UiUtil.button(hl, I18N.getMessage("OrganisaatioEditForm.peruutaButton"), peruutaListener);
        Button tallenna = UiUtil.button(hl, I18N.getMessage("OrganisaatioEditForm.tallennaButton"), tallennaListener);
        hl.setComponentAlignment(peruuta, Alignment.TOP_LEFT);
        hl.setComponentAlignment(tallenna, Alignment.TOP_RIGHT);
        addComponent(hl);
    }

    public KielikaannosModel getModel() {
        return model;
    }
}

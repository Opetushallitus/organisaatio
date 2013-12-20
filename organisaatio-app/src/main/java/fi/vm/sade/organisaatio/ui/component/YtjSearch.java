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


import com.vaadin.ui.*;
import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.organisaatio.ui.util.UiUtils;
import fi.vm.sade.organisaatio.ui.listener.SearchValueListener;
import fi.vm.sade.rajapinnat.ytj.api.YTJDTO;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.addon.formbinder.PropertyId;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tuomas Katva
 */

@Configurable(preConstruction = false)
public class YtjSearch extends CustomComponent {

    /* Service and internal data */
    
    private List<YTJDTO> returnValues;
    private List<SearchValueListener> listeners;



    /* Layouts */
    private GridLayout rootLayout;
    private FormLayout inputTxtLayout;

    /* Input fields */
    @PropertyId("orgpopup.ytunnus")
    private TextField organisaatioYtunnus;
    @PropertyId("orgpopup.nimi")
    private TextField organisaatioNimi;


    public YtjSearch() {
        initializeLayout();

    }

    private void initializeLayout() {
        rootLayout = new GridLayout(2,1);
        inputTxtLayout = new FormLayout();

        setOrganisaatioYtunnus(createTextField("c_ytunnus"));
        setOrganisaatioNimi(createTextField("c_nimi"));
        inputTxtLayout.addComponent(getOrganisaatioYtunnus());
        inputTxtLayout.addComponent(getOrganisaatioNimi());

        getOrganisaatioYtunnus().setWidth("100%");
        getOrganisaatioNimi().setWidth("100%");

        rootLayout.addComponent(inputTxtLayout, 0, 0,1,0);

        inputTxtLayout.setMargin(true, true, false, true);

        UiUtils.processDebugIds(this, "ytjSearch_");
        setCompositionRoot(rootLayout);
    }

  
    private TextField createTextField(String captionKey) {
        TextField tf = new TextField();
        if (captionKey != null) {
            tf.setCaption(t(captionKey));
        }
        tf.setNullRepresentation("");
        return tf;
    }

      private String t(String key) {
        return I18N.getMessage(key);
    }


    public void registerSearchValueListener(SearchValueListener<YTJDTO> listener) {
        if (this.listeners == null) {
            listeners = new ArrayList<SearchValueListener>();
        }
        listeners.add(listener);
    }

  
    /**
     * @return the organisaatioYtunnus
     */
    public TextField getOrganisaatioYtunnus() {
        return organisaatioYtunnus;
    }

    /**
     * @param organisaatioYtunnus the organisaatioYtunnus to set
     */
    public void setOrganisaatioYtunnus(TextField organisaatioYtunnus) {
        this.organisaatioYtunnus = organisaatioYtunnus;
    }

    /**
     * @return the organisaatioNimi
     */
    public TextField getOrganisaatioNimi() {
        return organisaatioNimi;
    }

    /**
     * @param organisaatioNimi the organisaatioNimi to set
     */
    public void setOrganisaatioNimi(TextField organisaatioNimi) {
        this.organisaatioNimi = organisaatioNimi;
    }
}

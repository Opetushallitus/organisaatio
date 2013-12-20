package fi.vm.sade.organisaatio.revised.ui.component.organisaatioform;


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
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;
/**
 *
 * @author Tuomas Katva
 */
class OhjePopup implements PopupView.Content {
    
     private String ohjeContent;
     private Label ohjeLabel;
     private Panel rootPanel;
    
     OhjePopup(String content) {
         this.ohjeContent = content;
         rootPanel = new Panel();
         ohjeLabel = new Label(ohjeContent);
         rootPanel.addComponent(ohjeLabel);
     }
     
    
    void setWidthAndHeight(String width,String height) {
        if (rootPanel != null) {
            rootPanel.setWidth(width);
            rootPanel.setHeight(height);
        }
    } 
     
     @Override
    public String getMinimizedValueAsHTML() {
        return "";
    }

    @Override
    public Component getPopupComponent() {
        return rootPanel;
    }
}

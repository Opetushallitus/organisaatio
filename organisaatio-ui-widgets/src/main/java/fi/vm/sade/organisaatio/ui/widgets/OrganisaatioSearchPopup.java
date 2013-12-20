package fi.vm.sade.organisaatio.ui.widgets;


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
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.VerticalLayout;
import java.util.List;

        
/**
 *
 * @author Tuomas Katva
 */
public class OrganisaatioSearchPopup implements PopupView.Content {

    private OrganisaatioSearchTree orgSearchTree;
    private Panel root;
    private String popupWidth = "400px";
    private String popupHeight = "320px";
    
    public OrganisaatioSearchPopup(OrganisaatioSearchTree orgSearch) {
        orgSearchTree = orgSearch;
        orgSearchTree.init();
        orgSearchTree.reload();
        root = new Panel();
        root.setWidth(popupWidth);
        root.setHeight(popupHeight);
        root.addComponent(orgSearchTree);
        
    }
    
    public OrganisaatioSearchPopup(OrganisaatioSearchTree orgSearch,List<String> oids) {
        orgSearchTree = orgSearch;
        orgSearchTree.init();
        orgSearchTree.reloadWithOids(oids);
        root = new Panel();
        root.setWidth(popupWidth);
        root.setHeight(popupHeight);
        root.addComponent(orgSearchTree); 
    }
    
    public void reloadWithOids(List<String> oids) {
        if (orgSearchTree != null) {
            
            orgSearchTree.reloadWithOids(oids);
        }
    }
    
    @Override
    public String getMinimizedValueAsHTML() {
        return "";
    }

    @Override
    public Component getPopupComponent() {
        return root;
    }

    /**
     * @return the popupWidth
     */
    public String getPopupWidth() {
        return popupWidth;
    }

    /**
     * @param popupWidth the popupWidth to set
     */
    public void setPopupWidth(String popupWidth) {
        this.popupWidth = popupWidth;
        if (root != null) {
            root.setWidth(popupWidth);
        }
    }

    /**
     * @return the popupHeight
     */
    public String getPopupHeight() {
        return popupHeight;
       
    }

    /**
     * @param popupHeight the popupHeight to set
     */
    public void setPopupHeight(String popupHeight) {
        this.popupHeight = popupHeight;
         if (root != null) {
            root.setHeight(popupHeight);
        }
    }

}

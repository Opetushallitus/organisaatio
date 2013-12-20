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
package fi.vm.sade.organisaatio.revised.ui;

import java.util.ArrayList;

import com.vaadin.ui.VerticalLayout;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.ui.blackboard.BlackboardContext;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioKuvailevatTiedotTyyppi;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoDTO;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.revised.ui.component.mainview.OrganisaatioMainView;
import fi.vm.sade.organisaatio.revised.ui.component.mainview.OrganisaatioMainViewImpl;
import fi.vm.sade.organisaatio.revised.ui.component.mainview.OrganisaatioMainViewTabs;
import fi.vm.sade.organisaatio.revised.ui.component.mainview.OrganisaatioModelWrapper;
import fi.vm.sade.organisaatio.revised.ui.component.organisaatioform.OrganisaatioEditForm;
import fi.vm.sade.organisaatio.revised.ui.component.search.OrganisaatioListViewContainer;
import fi.vm.sade.organisaatio.revised.ui.event.OrganisaatioFormButtonEvent;
import fi.vm.sade.organisaatio.revised.ui.event.OrganisaatioFormButtonEvent.OrganisaatioFormButtonEventListener;
import fi.vm.sade.organisaatio.revised.ui.event.OrganisaatioRowMenuEvent;
import fi.vm.sade.organisaatio.revised.ui.event.OrganisaatioRowMenuEvent.OrganisaatioRowMenuEventListener;
import fi.vm.sade.organisaatio.revised.ui.event.OrganisaatioViewButtonEvent;
import fi.vm.sade.organisaatio.revised.ui.event.OrganisaatioViewButtonEvent.OrganisaatioViewButtonEventListener;

/**
 * Main container for organisaatio views. Handles navigation between views.
 * 
 * @author markus
 *
 */
public class OrganisaatioMainContainer extends VerticalLayout {

	private static final long serialVersionUID = 1L;

	/**
     * The search view of organisaatio app.
     */
    private OrganisaatioListViewContainer searchView;
    
    /**
     * The organisaatio edit form view.
     */
    private OrganisaatioEditForm orgEditView;
    
    /**
     * The main view of organisaatio.
     */
    private OrganisaatioMainView orgView;
    
    //private List<OrganisaatioPerustietoType> organisaatios = new ArrayList<OrganisaatioPerustietoType>();
    
    /**
     * Event listener for listening to events fired from organisaatio row menu.
     */
    private final OrganisaatioRowMenuEventListener rowMenuHandler = new OrganisaatioRowMenuEventListener() {

		private static final long serialVersionUID = 1L;

		@Override
        public void onOrganisaatioRowMenuEvent(OrganisaatioRowMenuEvent event) {
            //searchResults.searchOrganisaatios(event.getSearchCriteria(), (String)sortingOptions.getValue());
            //organisaatios = event.getOrganisaatios();
            if (event.getEventType().equals(I18N.getMessage("OrganisaatioView.btnMuokkaa"))) {
                //DEBUGSAWAY:log.debug("Muokkaa organisaatiota");
                createOrganisaatioEditView(event.getOrganisaatio(),
                		event.getOrganisaatio()!=null
                		&& (event.getOrganisaatio().getYtjPaivitysPvm()!=null
                		|| event.getOrganisaatio().getTuontiPvm()!=null)); 
                
            } else if (event.getEventType().equals(I18N.getMessage("OrganisaatioView.btnLuoLapsi"))) {
                //DEBUGSAWAY:log.debug("Luo lapsi organisaatio");
                OrganisaatioDTO newOrg = new OrganisaatioDTO();
                newOrg.setParentOid(event.getOrganisaatio().getOid());
                newOrg.setKuvailevatTiedot(clearOids(event.getOrganisaatio().getKuvailevatTiedot()));
                newOrg.setMaa(I18N.getMessage("OrganisaatioEditForm.maaSuomi"));
                createOrganisaatioEditView(newOrg);
            } else if (event.getEventType().equals(I18N.getMessage("OrganisaatioView.btnTarkastele"))) {
                createOrganisaatioMainView(event.getOrganisaatio());
            } 
            
        }
    };
    
    private OrganisaatioKuvailevatTiedotTyyppi clearOids(OrganisaatioKuvailevatTiedotTyyppi lop) {
        if (lop != null && lop.getHakutoimisto() != null && lop.getHakutoimisto().getOpintotoimistoYhteystiedot() != null) {
            for (YhteystietoDTO curYT : lop.getHakutoimisto().getOpintotoimistoYhteystiedot()) {
                if (curYT.getYhteystietoOid() != null) {
                    curYT.setYhteystietoOid(null);
                }
            }
        }
        return lop;
    }
    
    /**
     * Event listener for listening to events fired by organisaatio edit form buttons.
     */
    private final OrganisaatioFormButtonEventListener formButtonHandler = new OrganisaatioFormButtonEventListener() {

		private static final long serialVersionUID = 1L;

		@Override
        public void onOrganisaatioFormButtonEvent(OrganisaatioFormButtonEvent event) {
            if (event.getEventType().equals(OrganisaatioFormButtonEvent.PERUUTA)) {
                //DEBUGSAWAY:log.debug("Navigating to organisaatio search view");
                createListViewContainer();
                
            } else if (event.getEventType().equals(OrganisaatioFormButtonEvent.JATKA)) {
                //DEBUGSAWAY:log.debug("Navigating to organisaatios main view");
                createOrganisaatioMainView(event.getOrganisaatio());
            } 
        }
    };
    
    private boolean showYtjLabel(OrganisaatioViewButtonEvent event) {
    	return event.getOrganisaatio() != null && event.getOrganisaatio().getYtjPaivitysPvm() != null;
    }
    
    /**
     * Event listener for listening to events fired by organisaatio main view buttons.
     */
    private OrganisaatioViewButtonEventListener viewButtonHandler = new OrganisaatioViewButtonEventListener() {

		private static final long serialVersionUID = 1L;

		@Override
        public void onOrganisaatioViewButtonEvent(OrganisaatioViewButtonEvent event) {
            if (event.getEventType().equals(OrganisaatioViewButtonEvent.TAKAISIN)) {
                //DEBUGSAWAY:log.debug("Navigating to organisaatio search view");
                createListViewContainer(); 
            } else if (event.getEventType().equals(OrganisaatioViewButtonEvent.MUOKKAA_YLEISTIEDOT)) {
                //DEBUGSAWAY:log.debug("Navigating to organisaatios main view");
                if (showYtjLabel(event)) {
                    createOrganisaatioEditView(event.getOrganisaatio(), OrganisaatioEditForm.Tab.YLEISTIEDOT, true); 
                } else {
                    createOrganisaatioEditView(event.getOrganisaatio(), OrganisaatioEditForm.Tab.YLEISTIEDOT); 
                }
                //createOrganisaatioEditView(event.getOrganisaatio()); 
            } else if (event.getEventType().equals(OrganisaatioViewButtonEvent.MUOKKAA_KOULUTUSTARJOAJATIEDOT)) {
                //DEBUGSAWAY:log.debug("Navigating to organisaatios main view");
                if (showYtjLabel(event)) {
                    createOrganisaatioEditView(event.getOrganisaatio(), OrganisaatioEditForm.Tab.KOULUTUSTARJOAJATIEDOT,true); 
                } else {
                    createOrganisaatioEditView(event.getOrganisaatio(), OrganisaatioEditForm.Tab.KOULUTUSTARJOAJATIEDOT); 
                }
                //createOrganisaatioEditView(event.getOrganisaatio()); 
            } else if (event.getEventType().equals(OrganisaatioViewButtonEvent.MUOKKAA_PALVELUT_OPPIJALLE)) {
                //DEBUGSAWAY:log.debug("Navigating to organisaatios main view");
                if (showYtjLabel(event)) {
                    createOrganisaatioEditView(event.getOrganisaatio(), OrganisaatioEditForm.Tab.PALVELUT_OPPIJALLE,true); 
                } else {
                    createOrganisaatioEditView(event.getOrganisaatio(), OrganisaatioEditForm.Tab.PALVELUT_OPPIJALLE); 
                }
                //createOrganisaatioEditView(event.getOrganisaatio()); 
            } else if (event.getEventType().equals(OrganisaatioViewButtonEvent.YTJ_MUOKKAA)) {
                createOrganisaatioEditView(event.getOrganisaatio(),true); 
            } else if (event.getEventType().equals(OrganisaatioViewButtonEvent.FORM_REFRESH)) {
                    String exceptionType = event.getFormExceptionName();
                    createOrganisaatioEditView(event.getOrganisaatio(),exceptionType);
            }
        }
    };
    
    public OrganisaatioMainContainer() {
        init();
    }
    
    private void init() {
    	this.setHeight(-1, UNITS_PIXELS);
    	
        createListViewContainer();
        BlackboardContext.getBlackboard().addListener(rowMenuHandler);
        BlackboardContext.getBlackboard().addListener(formButtonHandler);
        BlackboardContext.getBlackboard().addListener(viewButtonHandler);
    }
    
    /**
     * Creation searchView and addition to layout.
     */
    private void createListViewContainer() {
       removeAllComponents();
       searchView = (searchView == null) ? new OrganisaatioListViewContainer() : searchView;
       searchView.setHeight(-1, UNITS_PIXELS);
       searchView.refresh();
       addComponent(searchView);
    }



    private void createOrganisaatioEditView(OrganisaatioDTO organisaatio, String errorMsg) {
        removeAllComponents();
        orgEditView = (organisaatio != null) ? new OrganisaatioEditForm(organisaatio,errorMsg) : new OrganisaatioEditForm();
        orgEditView.setSizeFull();
        orgEditView.setHeight(-1, UNITS_PIXELS);
        addComponent(orgEditView);
    }

    private void createOrganisaatioEditView(OrganisaatioDTO organisaatio) {
    	createOrganisaatioEditView(organisaatio, OrganisaatioEditForm.Tab.YLEISTIEDOT);
    }
    
    private void createOrganisaatioEditView(OrganisaatioDTO organisaatio, boolean showYtjLabel) {
    	createOrganisaatioEditView(organisaatio, OrganisaatioEditForm.Tab.YLEISTIEDOT, showYtjLabel);
    }
    

    /**
     * Creation of orgEditView and addition to layout.
     * 
     * @param organisaatio
     */
    private void createOrganisaatioEditView(OrganisaatioDTO organisaatio, OrganisaatioEditForm.Tab tab) {
    	createOrganisaatioEditView(organisaatio, tab, false);
    }
    
     /**
     * Creation of orgEditView and addition to layout. 
     * Specifies whether to show ytjLabel
     * 
     * @param organisaatio
     * @param showYtjLabel
     */
    private void createOrganisaatioEditView(OrganisaatioDTO organisaatio, OrganisaatioEditForm.Tab tab, boolean showYtjLabel) {
        removeAllComponents();
        orgEditView = (organisaatio != null) ? new OrganisaatioEditForm(organisaatio,tab) : new OrganisaatioEditForm();
        orgEditView.setSizeFull();
        orgEditView.setHeight(-1, UNITS_PIXELS);
        orgEditView.showYtjLabel(showYtjLabel);
        addComponent(orgEditView);
    }
    
    /**
     * Create an organization overview page (not the main page of the app).
     * Creation of orgView and addition to layout.
     */
    private void createOrganisaatioMainView(OrganisaatioDTO organisaatio) {
    	
    	OrganisaatioModelWrapper orgm = new OrganisaatioModelWrapper(organisaatio, new ArrayList<OrganisaatioPerustieto>());
    	
        removeAllComponents();
        orgView = (organisaatio != null)
        		? new OrganisaatioMainViewTabs(orgm)
        		//? new OrganisaatioMainViewImpl(organisaatio.getOid(), organisaatios) 
                : new OrganisaatioMainViewImpl();
        orgView.getComponent().setSizeFull();
        orgView.getComponent().setHeight(-1, UNITS_PIXELS);
        addComponent(orgView.getComponent());
    }
}

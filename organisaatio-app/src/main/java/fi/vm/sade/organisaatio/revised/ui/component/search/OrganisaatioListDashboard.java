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
package fi.vm.sade.organisaatio.revised.ui.component.search;

import java.util.ArrayList;
import java.util.List;

import fi.vm.sade.koodisto.service.KoodistoService;
import fi.vm.sade.koodisto.service.types.SearchKoodistosCriteriaType;
import fi.vm.sade.koodisto.service.types.common.KoodistoListType;
import fi.vm.sade.koodisto.service.types.common.KoodistoRyhmaListType;
import fi.vm.sade.koodisto.service.types.common.KoodistoType;
import fi.vm.sade.koodisto.util.KoodistoServiceSearchCriteriaBuilder;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi;
import fi.vm.sade.organisaatio.ui.listener.YtjSelectListener;
import fi.vm.sade.organisaatio.ui.listener.event.YtjSelectedEventImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import fi.vm.sade.authentication.service.UserService;
import fi.vm.sade.authentication.service.types.HenkiloPagingObjectType;
import fi.vm.sade.authentication.service.types.HenkiloSearchObjectType;
import fi.vm.sade.authentication.service.types.dto.HenkiloType;
import fi.vm.sade.authentication.service.types.dto.SearchConnectiveType;
import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.ui.blackboard.BlackboardContext;
import fi.vm.sade.organisaatio.api.model.GenericFault;
import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.RemoveByOidType;
import fi.vm.sade.organisaatio.revised.ui.event.OrganisaatioRowMenuEvent;
import fi.vm.sade.organisaatio.revised.ui.event.OrganisaatioRowMenuEvent.OrganisaatioRowMenuEventListener;
import fi.vm.sade.organisaatio.revised.ui.event.OrganisaatioSearchStartEvent;
import fi.vm.sade.organisaatio.revised.ui.event.OrganisaatioSearchStartEvent.OrganisaatioSearchStartEventListener;
import fi.vm.sade.organisaatio.revised.ui.event.OrganisaatioViewButtonEvent;
import fi.vm.sade.organisaatio.revised.ui.helper.SortingOption;
import fi.vm.sade.organisaatio.revised.ui.helper.YtjToOrganisaatioMapper;
import fi.vm.sade.organisaatio.ui.PortletRole;
import fi.vm.sade.organisaatio.ui.component.ConfirmationDialog;
import fi.vm.sade.organisaatio.ui.component.OrganisaatioTable;
import fi.vm.sade.organisaatio.ui.listener.ConfirmationListener;
import fi.vm.sade.organisaatio.ui.listener.event.ConfirmationEvent;
import fi.vm.sade.organisaatio.ui.util.UiUtils;
import fi.vm.sade.rajapinnat.ytj.api.YTJDTO;
import fi.vm.sade.rajapinnat.ytj.api.YTJKieli;
import fi.vm.sade.rajapinnat.ytj.api.YTJService;

/**
 * A component which contains the hierarchical organisaatio list, and buttons
 * and checkboxes to do operations on the organisaatios.
 *
 * @author markus
 *
 */
@Configurable(preConstruction = false) class OrganisaatioListDashboard extends VerticalLayout {
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    @Autowired
    private UserService userService;    
    @Autowired
    private KoodistoService koodistoService;
    
    /**
     * The component for viewing search results.
     */
    private OrganisaatioListView searchResults;

    /**
     * Event listener for listening to events fired from searchPanel, indicating
     * that a search needs to be performed.
     */
    private OrganisaatioSearchStartEventListener searchHandler = new OrganisaatioSearchStartEventListener() {
        @Override
        public void onOrganisaatioSearchStart(OrganisaatioSearchStartEvent event) {
            
            searchResults.searchOrganisaatios(event.getSearchCriteria(), I18N.getMessage(SortingOption.KOULUTUSTOIMIJA_AAKKOSITTAIN.value()));
        }
    };

    private Window createExceptionDialog(String message) {
        final Window exceptionDialog = new Window();
        exceptionDialog.center();

        VerticalLayout windowLayout = new VerticalLayout();
        Panel msgPlaceHolder = new Panel();
        Label exceptionMsg = new Label(message);
        msgPlaceHolder.addComponent(exceptionMsg);
        windowLayout.addComponent(msgPlaceHolder);
        Button button = new Button(I18N.getMessage("confirmationDialog.okButton"));
        button.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                getWindow().removeWindow(exceptionDialog);
            }
        });
        windowLayout.addComponent(button);

        exceptionDialog.setContent(windowLayout);

        exceptionDialog.setImmediate(true);
        msgPlaceHolder.setSizeUndefined();
        windowLayout.setSizeUndefined();
        return exceptionDialog;
    }

    private String getPoistaMsg(MonikielinenTekstiTyyppi orgname) {
        StringBuilder sb = new StringBuilder();
        String lang = getLocale().getLanguage().trim();
        sb.append(I18N.getMessage("OrganisaatioMainView.poistaDialogMsg"));
        sb.append("\n");
        for(MonikielinenTekstiTyyppi.Teksti teksti : orgname.getTeksti()) {
           if (teksti.getKieliKoodi().trim().equalsIgnoreCase(lang)) {
               sb.append(teksti.getValue());
               sb.append("\n");
           }
        }
        sb.append(I18N.getMessage("OrganisaatioMainView.poistaDialogMsgContinued"));
        return sb.toString();
    }

    private void showDeleteConfirmationDialog(final String oid, final MonikielinenTekstiTyyppi orgname) {
        final Window confirmationDialogWindow = new Window();	
        confirmationDialogWindow.center();
        getWindow().addWindow(confirmationDialogWindow);
        ConfirmationDialog confirmationDialog = new ConfirmationDialog(getPoistaMsg(orgname));
        confirmationDialogWindow.setContent(confirmationDialog);
        confirmationDialog.setImmediate(true);
        confirmationDialogWindow.setModal(true);
        confirmationDialog.setWidth("500px");

        confirmationDialog.addListener(new ConfirmationListener() {
            @Override
            public void handleConfirmation(ConfirmationEvent confirmationEvent) {
                if (confirmationEvent.getConfirmation()) {
                    try {
                        RemoveByOidType oidT = new RemoveByOidType();
                        oidT.setOid(oid);
                        organisaatioService.removeOrganisaatioByOid(oidT);
                        searchResults.refreshTree(I18N.getMessage(SortingOption.KOULUTUSTOIMIJA_AAKKOSITTAIN.value()));
                        getWindow().removeWindow(confirmationDialogWindow);
                    } catch (Exception ex) {
                        getWindow().removeWindow(confirmationDialogWindow);
                        String exceptionMsg = "";
                        if (ex instanceof GenericFault) {
                            exceptionMsg = I18N.getMessage(((GenericFault) ex).getFaultInfo().getErrorCode());

                        } else {
                            exceptionMsg = ex.toString();
                        }
                        getWindow().addWindow(createExceptionDialog(exceptionMsg));
                    }
                } else {
                    getWindow().removeWindow(confirmationDialogWindow);
                }
            }
        });
    }
    /**
     * Event listener for listening to events fired from organisaatio row menu.
     */
    private OrganisaatioRowMenuEventListener rowMenuHandler = new OrganisaatioRowMenuEventListener() {
        @Override
        public void onOrganisaatioRowMenuEvent(OrganisaatioRowMenuEvent event) {

            if (event.getEventType().equals(I18N.getMessage("c_poista"))) {

                HenkiloSearchObjectType searchType = new HenkiloSearchObjectType();
                searchType.setConnective(SearchConnectiveType.AND);

                searchType.getOrganisaatioOids().add(event.getOrganisaatio().getOid());
                HenkiloPagingObjectType paging = new HenkiloPagingObjectType();
                List<HenkiloType> henkilos = new ArrayList<HenkiloType>();
                try {
                    henkilos = userService.listHenkilos(searchType, paging);
                } catch (Exception ex) {
                }
            	                
            	if (henkilos == null || henkilos.size() == 0) {
            		boolean foundKoodisto = false;
                    List<KoodistoRyhmaListType> koodistoRyhmas = koodistoService.listAllKoodistoRyhmas();
                    if (koodistoRyhmas != null && koodistoRyhmas.size() > 0) {
                    	outer: 
                    	for (KoodistoRyhmaListType group : koodistoRyhmas) {
                    		for (KoodistoListType koodisto : group.getKoodistos()) {
                    			if (koodisto.getOrganisaatioOid().equals(event.getOrganisaatio().getOid())) {
                    				foundKoodisto = true;
                    				break outer;
                    			}
                    		}
                    	}
                    	
                    }
                    if (!foundKoodisto) {
                    	showDeleteConfirmationDialog(event.getOrganisaatio().getOid(),event.getOrganisaatio().getNimi());
                    } else {
                    	getWindow().showNotification(I18N.getMessage("OrganizationDelete.organizationUsed"), com.vaadin.ui.Window.Notification.TYPE_ERROR_MESSAGE);
                    }
   
            	} else {
            		 getWindow().showNotification(I18N.getMessage("OrganizationDelete.organizationUsed"), com.vaadin.ui.Window.Notification.TYPE_ERROR_MESSAGE);
            	}
            }

        }
    };
    @Autowired
    private OrganisaatioService organisaatioService;
    @Autowired
    private YTJService ytjService;
    private Window confirmationDialogWindow;

    public OrganisaatioListDashboard() {
        init();
        BlackboardContext.getBlackboard().addListener(searchHandler);
        BlackboardContext.getBlackboard().addListener(rowMenuHandler);
    }

    private void init() {
        //Creating the buttons above the organisaatio listing
        createButtons();

        buildSelectAllCheckBox();

        //Creating the search result list
        searchResults = new OrganisaatioListView();
        searchResults.setWidth("100%");
        searchResults.addListener(new Listener () {

            private static final long serialVersionUID = 1L;

            @Override
            public void componentEvent(Event event) {
                fireEvent(event);
            }
            
        });
        addComponent(searchResults);
    }

    private void buildSelectAllCheckBox() {
        HorizontalLayout lightLayout = new HorizontalLayout();
        lightLayout.setWidth(100, UNITS_PERCENTAGE);
        lightLayout.setSpacing(true);
        lightLayout.setMargin(false, false, false, true);

        addComponent(lightLayout);
    }

    //Creating the buttons above the searchResults tree.
    private void createButtons() {
        HorizontalLayout buttonContainer = new HorizontalLayout();
        buttonContainer.setWidth(100, UNITS_PERCENTAGE);
        buttonContainer.setSpacing(true);
        buttonContainer.setMargin(true, true, true, true);


        HorizontalLayout hlRight = new HorizontalLayout();
        final Button createNew = UiUtils.buttonSmallPrimary(hlRight, I18N.getMessage("t_otsikko"), new Button.ClickListener() {
            private static final long serialVersionUID = 5019806363620874205L;

            @Override
            public void buttonClick(ClickEvent event) {
                createOrganisaatio();
            }
        });
        
        createNew.setVisible(PortletRole.getInstance().getPermissionService().userCanCreateRootOrganisation());

        buttonContainer.addComponent(hlRight);
        buttonContainer.setExpandRatio(hlRight, 1f);

        buttonContainer.setComponentAlignment(hlRight, Alignment.TOP_RIGHT);
        
        addComponent(buttonContainer);
    }

    //Starts the creation of an organization
    private void createOrganisaatio() {

        final Window window = getApplication().getMainWindow();
        List<YTJDTO> ytjDtos = new ArrayList<YTJDTO>();//ytjService.findByYNimi(organisationName, true, YTJKieli.FI);
        final Window orgSelectPopup = new Window(I18N.getMessage("c_yritysValintaHdr"));
        orgSelectPopup.center();
        window.addWindow(orgSelectPopup);
        final OrganisaatioTable ot = new OrganisaatioTable(ytjDtos, null);
        ot.addListener(new YtjSelectListener() {
            @Override
            public void organizationSelected(YtjSelectedEventImpl event) {
                try {

                    if (!event.isCancelled() && event.getYtjDto() != null) {
                        YTJDTO value = event.getYtjDto();
                        YTJDTO ytj = ytjService.findByYTunnus(value.getYtunnus(), YTJKieli.FI);
                        OrganisaatioDTO organisaatio = new OrganisaatioDTO();
                        if (ytj != null) {
                            organisaatio = YtjToOrganisaatioMapper.mapYtjToOrganisaatio(ytj, organisaatio);
                        }
                        window.removeWindow(orgSelectPopup);
                        organisaatio.setMaa(I18N.getMessage("OrganisaatioEditForm.maaSuomi"));
                        BlackboardContext.getBlackboard().fire(new OrganisaatioViewButtonEvent(organisaatio, OrganisaatioViewButtonEvent.YTJ_MUOKKAA));
                        //setYtjDtoValues(ytj);
                        //TODO add navigation to organization edit form
                    } else if (!event.isCancelled() && event.getYtjDto() == null) {
                        window.removeWindow(orgSelectPopup);
                        OrganisaatioDTO organisaatio = new OrganisaatioDTO();
                        organisaatio.setMaa(I18N.getMessage("OrganisaatioEditForm.maaSuomi"));
                        BlackboardContext.getBlackboard().fire(new OrganisaatioViewButtonEvent(organisaatio, OrganisaatioViewButtonEvent.MUOKKAA_YLEISTIEDOT));
                    } else {
                        window.removeWindow(orgSelectPopup);
                    }

                } catch (Exception exp) {
                    getWindow().showNotification("Exception in YTJ-service", Window.Notification.TYPE_TRAY_NOTIFICATION);
                }
            }
        });
        orgSelectPopup.setContent(ot);
        orgSelectPopup.setResizable(false);
        orgSelectPopup.setModal(true);
        ot.setSizeUndefined();
    }

    /**
     * Refreshes the organisaatio list according to current search criteria.
     */
    void refreshSearchResults() {
        this.searchResults.searchOrganisaatios(I18N.getMessage(SortingOption.KOULUTUSTOIMIJA_AAKKOSITTAIN.value()));
    }
}

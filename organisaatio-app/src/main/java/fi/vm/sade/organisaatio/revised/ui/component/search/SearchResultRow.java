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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.LayoutEvents;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.ui.blackboard.BlackboardContext;
import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.auth.OrganisaatioContext;
import fi.vm.sade.organisaatio.auth.OrganisaatioPermissionServiceImpl;
import fi.vm.sade.organisaatio.helper.OrganisaatioDisplayHelper;
import fi.vm.sade.organisaatio.revised.ui.event.OrganisaatioRowMenuEvent;
import fi.vm.sade.organisaatio.ui.PortletRole;
import fi.vm.sade.vaadin.ui.OphRowMenuBar;
import fi.vm.sade.vaadin.util.UiUtil;

/**
 * A component for displaying search results in the OrganisaatioListView. It shows in this order: The checkbox to select the search result, a button to open an
 * action menu, and the name of the organisaatio.
 *
 * @author markus
 */
class SearchResultRow extends HorizontalLayout {

    private static final long serialVersionUID = 7900022121895654353L;

    private static final Logger LOG = LoggerFactory.getLogger(SearchResultRow.class);

    /**
     * The displayed organisaatio
     */
    private OrganisaatioPerustieto organisaatio;
    
    HorizontalLayout menuLayout; 
    
    /**
     * The action menubar (Muokkaa, luo aliorganisaatio, poista).
     */
    private OphRowMenuBar menu;
    /**
     * Label to display the caption of the organisaatio
     */
    private Button organisaatioNimi;
    private OrganisaatioService organisaatioService;
    
    private boolean commandsAdded = false;
    
    private List<OrganisaatioPerustieto> organisaatios = new ArrayList<OrganisaatioPerustieto>();

    SearchResultRow(OrganisaatioPerustieto organisaatio, OrganisaatioService organisaatioService, List<OrganisaatioPerustieto> organisaatios) {
        setWidth(-1, UNITS_PIXELS);
        setHeight(-1, UNITS_PIXELS);
        //setSpacing(true);
        this.organisaatio = organisaatio;
        this.organisaatioService = organisaatioService;
        this.setOrganisaatios(organisaatios);
        init();
    }

    private boolean checkDate() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Date today = c.getTime();
        if (organisaatio.getLakkautusPvm() != null) {
            return today.after(organisaatio.getLakkautusPvm());
        } else {
            return false;
        }

    }

    private void init() {
        menuLayout = UiUtil.horizontalLayout();
        menuLayout.setImmediate(true);
        menu = new OphRowMenuBar("../oph/img/icon-treetable-button.png");
        menu.setImmediate(true);

        menu.addMenuCommand(I18N.getMessage("OrganisaatioView.btnTarkastele"), menuCommand);
        
        
        menuLayout.addListener(new LayoutEvents.LayoutClickListener() {

            private static final long serialVersionUID = -8105569099606611945L;

            @Override
            public void layoutClick(LayoutClickEvent event) {
                if (!commandsAdded) {
                    addPermissionSpecificCommands();
                    commandsAdded = true;
                }
                
            }
        });
        
        menuLayout.addComponent(menu);
        addComponent(menuLayout);
        
        
        
        organisaatioNimi = UiUtil.buttonLink(null, getOrganisaatioCaption());//new Label(getOrganisaatioCaption());
        organisaatioNimi.setStyleName("link-row");
        organisaatioNimi.addListener(new Button.ClickListener() {

            private static final long serialVersionUID = -4842452805602523500L;

            @Override
            public void buttonClick(ClickEvent event) {
                OrganisaatioDTO fullOrg = organisaatioService.findByOid(organisaatio.getOid());
                BlackboardContext.getBlackboard().fire(new OrganisaatioRowMenuEvent(fullOrg, I18N.getMessage("OrganisaatioView.btnTarkastele"), organisaatios));
            }
        });

        organisaatioNimi.setSizeUndefined();
        addComponent(organisaatioNimi);
        setExpandRatio(organisaatioNimi, 1f);
    }
    
    private void addPermissionSpecificCommands() {
        final OrganisaatioPermissionServiceImpl permissionService = PortletRole.getInstance().getPermissionService();
        final OrganisaatioContext context = OrganisaatioContext.get(organisaatio);
        
        if (permissionService.userCanUpdateOrganisation(context)) {
            menu.addMenuCommand(I18N.getMessage("OrganisaatioView.btnMuokkaa"), menuCommand);
        }
        if (!checkDate() && permissionService.userCanCreateOrganisation(context)) {
            menu.addMenuCommand(I18N.getMessage("OrganisaatioView.btnLuoLapsi"), menuCommand);
        }
        if (permissionService.userCanDeleteOrganisation(context)) {
            MenuItem mi = menu.addMenuCommand(I18N.getMessage("c_poista"), menuCommand);
            if (organisaatio.getAliOrganisaatioMaara()>0) {
                mi.setEnabled(false);
                mi.setDescription(I18N.getMessage("child.orgs.exists"));
            }
        }
        menuLayout.requestRepaintAll();
        menu.requestRepaint();
        this.getWindow().getApplication().getMainWindow().executeJavaScript("javascript:vaadin.forceSync();");
        
    }

    private String getOrganisaatioCaption() {
        String capt = (I18N.getLocale() != null) ? " " + OrganisaatioDisplayHelper.getClosestBasic(I18N.getLocale(), organisaatio) : " " + OrganisaatioDisplayHelper.getAvailableNameBasic(organisaatio);

        String statusStr = getStatusDescription();
        if (statusStr != null) {
            capt = capt + statusStr;
        }
        return capt;
    }

    private String getStatusDescription() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Date today = c.getTime();
        if (organisaatio.getAlkuPvm() != null && organisaatio.getAlkuPvm().after(today)) {
            return " (" + I18N.getMessage("SearchResultRow.status.planned") + ")";
        } else if (organisaatio.getLakkautusPvm() != null && organisaatio.getLakkautusPvm().before(today)) {
            return " (" + I18N.getMessage("SearchResultRow.status.passivated") + ")";
        } else {
            return null;
        }
    }

    public List<OrganisaatioPerustieto> getOrganisaatios() {
        return organisaatios;
    }

    public void setOrganisaatios(List<OrganisaatioPerustieto> organisaatios) {
        this.organisaatios = organisaatios;
    }

    private Command menuCommand = new Command() {

        private static final long serialVersionUID = -6499104044483799516L;

        public void menuSelected(MenuItem selectedItem) {
            LOG.debug("menuSelected: action '{}' - oid={}", selectedItem.getText(), (organisaatio != null) ? organisaatio.getOid() : null);
            OrganisaatioDTO fullOrg = organisaatioService.findByOid(organisaatio.getOid());
            BlackboardContext.getBlackboard().fire(new OrganisaatioRowMenuEvent(fullOrg, selectedItem.getText(), organisaatios));
        }
    };
}
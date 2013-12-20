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

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.TreeTable;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.api.search.OrganisaatioSearchCriteria;
import fi.vm.sade.organisaatio.helper.OrganisaatioDisplayHelper;
import fi.vm.sade.organisaatio.revised.ui.helper.SortingOption;
import fi.vm.sade.organisaatio.service.search.OrganisaatioSearchService;
import fi.vm.sade.organisaatio.ui.PortletRole;
import fi.vm.sade.organisaatio.ui.UserContext;

/**
 * The component for displaying search results as a tree table.
 *
 * @author markus
 */
@Configurable(preConstruction = false) 
class OrganisaatioListView extends TreeTable {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioListView.class);

    private static final String ORGANISAATIO_PROPERTY = "organisaatio";
    private static final String STATE_PROPERTY = "state";
    private static final String TUNNUS_PROPERTY = "tunnus";
    private static final String TYYPPI_PROPERTY = "organisaatiotyyppi";
    @Autowired
    private OrganisaatioService organisaatioService;

    @Autowired
    private OrganisaatioSearchService organisaatioSearchService;

    private boolean organizationSelected = false;
    /**
     * The list of organisaatios shown in the tree.
     */
    private List<OrganisaatioPerustieto> organisaatios;
    /**
     * The criteria according to which organisaatios are searched.
     */
    private OrganisaatioSearchCriteria searchCriteria;

    private String ophOid = "1.2.246.562.10.00000000001";

    private static class Notification{
        private String message;
        private int type;
        
        public Notification(String message, int type) {
            this.message = message;
            this.type = type;
        }
    }
    private final List<Notification> notifications = new ArrayList<OrganisaatioListView.Notification>();

    
    public OrganisaatioListView() {
        setColumnHeaderMode(COLUMN_HEADER_MODE_HIDDEN);
        setColumnExpandRatio(ORGANISAATIO_PROPERTY, 0.65f);
        setColumnExpandRatio(STATE_PROPERTY, 0.1f);
        setColumnExpandRatio(TUNNUS_PROPERTY, 0.1f);
        setColumnExpandRatio(TYYPPI_PROPERTY, 0.25f);
        this.setPageLength(30);
    }

    /**
     * Searches the organisaatios based on the search criteria given as
     * parameter. Displaying results according to sorting type given as second
     * parameter.
     *
     * @param searchCriteria - The criteria based on which to search
     * @param sortType - The type of sorting to be applied to the result set.
     */
    void searchOrganisaatios(OrganisaatioSearchCriteria searchCriteria, String sortType) {
        LOG.debug("searchOrganisaatios()");
        this.searchCriteria = searchCriteria;
        this.organisaatios = new ArrayList<OrganisaatioPerustieto>();
        removeAllItems();
        try {
            //search resrictions
            searchCriteria.getOidRestrictionList().clear();
            
            final UserContext context = PortletRole.getInstance().getUserContext();
            if(context!=null && context.isUseRestriction()) {
                LOG.info("Setting restriction:" + context.getUserOrganisaatios());
                searchCriteria.getOidRestrictionList().addAll(context.getUserOrganisaatios());
            }
            
            organisaatios = organisaatioSearchService.searchBasicOrganisaatios(searchCriteria);
            if (organisaatios.isEmpty()) {
                showNotification(I18N.getMessage("OrganisaatioListView.noResults"), com.vaadin.ui.Window.Notification.TYPE_WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            LOG.warn("error in search: "+ex, ex);
            if (ex.getMessage() != null && ex.getMessage().contains("organisaatioSearch.tooManyResults")) {
                showNotification(I18N.getMessage("OrganisaatioListView.tooManyResults"), com.vaadin.ui.Window.Notification.TYPE_WARNING_MESSAGE);
            } else {
                showNotification(I18N.getMessage("OrganisaatioListView.errorInSearch"), com.vaadin.ui.Window.Notification.TYPE_ERROR_MESSAGE);
            }
            organisaatios = new ArrayList<OrganisaatioPerustieto>();
        }
        fireEvent(new SearchCountEvent(this, organisaatios.size()));

        if (sortType != null
                && sortType.equals(I18N.getMessage(SortingOption.KOULUTUSTOIMIJA_AAKKOSITTAIN.value()))) {
            sortAlphabetically();
        }
        displayResults();
    }

    @Override
    public void attach() {
        super.attach();
        // display delayed messages
        if (getWindow() != null) {
            for (Notification notification : notifications) {
                getWindow().showNotification(notification.message, notification.type);
            }
        }
        notifications.clear();
    }
    
    private void showNotification(String message, int type) {
        if(this.getWindow()==null) {
            //not yet attached? display in attach instead
            notifications.add(new Notification(message,  type));
        } else {
            this.getWindow().showNotification(message, type);
        }
    }

    void searchOrganisaatios(String sortType) {
        if (searchCriteria != null && searchCriteria.getSearchStr() != null && !searchCriteria.getSearchStr().isEmpty()) {
            searchOrganisaatios(searchCriteria, sortType);
        }
    }

    /**
     * Refreshing the tree. I.e., performing a search according to the current
     * searchCriteria. Sorts according to the sortType given as parameter. The
     * method is intended to be used e.g., after an organization in the current
     * tree is removed from the database.
     *
     * @param sortType - The type of sorting to be applied to the result set.
     */
    void refreshTree(String sortType) {
        searchOrganisaatios(this.searchCriteria, sortType);
    }

    /**
     * Sorts the organisaatios in the result tree according to type given as
     * parameters.
     *
     * @param sortingType - the type of sorting to use.
     */
    void sortOrganisaatios(String sortingType) {
        LOG.debug("sortOrganisaatios()");
        if (sortingType != null && sortingType.equals(I18N.getMessage(SortingOption.KOULUTUSTOIMIJA_AAKKOSITTAIN.value()))) {
            removeAllItems();
            sortAlphabetically();
            displayResults();
        }
    }


    private void sortAlphabetically() {
        Collections.sort(organisaatios, new Comparator<OrganisaatioPerustieto>() {
            public int compare(OrganisaatioPerustieto f1, OrganisaatioPerustieto f2) {
                return OrganisaatioDisplayHelper.getClosestBasic(I18N.getLocale(), f1).toLowerCase(I18N.getLocale()).compareTo(OrganisaatioDisplayHelper.getClosestBasic(I18N.getLocale(), f2).toLowerCase(I18N.getLocale()));
            }
        });
    }

    private void displayResults() {
        HierarchicalContainer hc = new HierarchicalContainer();
        hc.addContainerProperty(ORGANISAATIO_PROPERTY, SearchResultRow.class, null);
        hc.addContainerProperty(STATE_PROPERTY, String.class, null);
        hc.addContainerProperty(TUNNUS_PROPERTY, String.class, null);
        hc.addContainerProperty(TYYPPI_PROPERTY, String.class, null);
        for (OrganisaatioPerustieto curOrg : organisaatios) {
            if (!curOrg.getOid().equals(getOphOid())) {
                hc.addItem(curOrg);
                SearchResultRow curRow = new SearchResultRow(curOrg, organisaatioService, new ArrayList<OrganisaatioPerustieto>());
                hc.getContainerProperty(curOrg, ORGANISAATIO_PROPERTY).setValue(curRow);
                hc.getContainerProperty(curOrg, STATE_PROPERTY).setValue(getOrganisationState(curOrg));
                hc.getContainerProperty(curOrg, TUNNUS_PROPERTY).setValue(getOrganisaatioTunnus(curOrg));
                hc.getContainerProperty(curOrg, TYYPPI_PROPERTY).setValue(getOrganisaatioTyypit(curOrg));
            }
        }
        this.setContainerDataSource(hc);
        createHierarchy(hc);
        
        
    }

    private void createHierarchy(HierarchicalContainer hc) {
        HashMap<String, String> childParent = new HashMap<String, String>();
        HashMap<String, OrganisaatioPerustieto> oidOrg = new HashMap<String, OrganisaatioPerustieto>();
        HashSet<String> doesNotHaveChildren = new HashSet<String>();
        for (OrganisaatioPerustieto curOrg : organisaatios) {
            childParent.put(curOrg.getOid(), curOrg.getParentOid());
            oidOrg.put(curOrg.getOid(), curOrg);
            doesNotHaveChildren.add(curOrg.getOid());
        }

        for (OrganisaatioPerustieto curOrg : organisaatios) {
            final OrganisaatioPerustieto parent = oidOrg.get(curOrg.getParentOid());
            if (parent!=null) {
                // has parent!
                hc.setParent(curOrg, parent);
                openTree(curOrg, parent);
                hc.setChildrenAllowed(parent, true);
                
                doesNotHaveChildren.remove(parent.getOid());
            }
        }
        
        for(String oid: doesNotHaveChildren) {
            hc.setChildrenAllowed(oidOrg.get(oid), false);
        }
    }

    private void openTree(OrganisaatioPerustieto organisaatio, OrganisaatioPerustieto parentOrg) {
    	if (searchCriteria.getSearchStr() != null && !searchCriteria.getSearchStr().isEmpty()
    			&& getOrganisaatioStr(organisaatio).toLowerCase().contains(searchCriteria.getSearchStr().toLowerCase())) {
    		unCollapsePath(parentOrg); //this.setCollapsed(parentOrg, false);
    	}
    }

    private void unCollapsePath(OrganisaatioPerustieto org) {
    	setCollapsed(org, false);
    	OrganisaatioPerustieto parent = (OrganisaatioPerustieto)getParent(org);
    	if (parent != null) {
    		unCollapsePath(parent);
    	}
    }

    /**
     * Returns the caption shown in the result tree for the organisaatio given
     * as a parameter.
     *
     * @param organisaatio - the organisaatio the caption of which is returned.
     * @return
     */
    private String getOrganisaatioStr(OrganisaatioPerustieto organisaatio) {
        String capt = OrganisaatioDisplayHelper.getClosestBasic(I18N.getLocale(), organisaatio)
                +  " " + getOrganisaatioTunnus(organisaatio)
                + " " + this.getOrganisaatioTyypit(organisaatio);
        return capt;
    }

    /**
     * @return the ophOid
     */
    public String getOphOid() {
        return ophOid.trim();
    }

    /**
     * @param ophOid the ophOid to set
     */
    public void setOphOid(String ophOid) {
        this.ophOid = ophOid;
    }

    private String getOrganisationState(OrganisaatioPerustieto organisaatio) {
        String state;
        if (organisaatio.getAlkuPvm().after(new Date())) {
            state = "Suunniteltu";
        } else if (organisaatio.getAlkuPvm().before(new Date()) && (organisaatio.getLakkautusPvm() == null || organisaatio.getLakkautusPvm().after(new Date()))) {
            state = "Aktiivinen";
        } else {
            state = "Passivoitu";
        }
        return state;
    }

    private String getOrganisaatioTunnus(OrganisaatioPerustieto organisaatio) {
        String tunnus = null;
        if (organisaatio.getOrganisaatiotyypit().contains(OrganisaatioTyyppi.KOULUTUSTOIMIJA)) {
            tunnus = ((organisaatio.getYtunnus() != null) ? organisaatio.getYtunnus() : "");
        } else if (organisaatio.getOrganisaatiotyypit().contains(OrganisaatioTyyppi.OPPILAITOS) && organisaatio.getOppilaitosKoodi() != null) {
            tunnus = organisaatio.getOppilaitosKoodi();
        } else {
            tunnus = "";
        }
        return tunnus;
    }

    private String getOrganisaatioTyypit(OrganisaatioPerustieto organisaatio) {
        String tyypit = "";
        if (organisaatio.getOrganisaatiotyypit().contains(OrganisaatioTyyppi.KOULUTUSTOIMIJA)) {
            tyypit += I18N.getMessage(OrganisaatioTyyppi.KOULUTUSTOIMIJA.name());
        }
        if (organisaatio.getOrganisaatiotyypit().contains(OrganisaatioTyyppi.OPPILAITOS)) {
            tyypit += tyypit.length() > 0 ? ", " : "";
            tyypit += I18N.getMessage(OrganisaatioTyyppi.OPPILAITOS.name());
        }
        if (organisaatio.getOrganisaatiotyypit().contains(OrganisaatioTyyppi.OPETUSPISTE)) {
            tyypit += tyypit.length() > 0 ? ", " : "";
            tyypit += I18N.getMessage(OrganisaatioTyyppi.OPETUSPISTE.name());
        }
        if (organisaatio.getOrganisaatiotyypit().contains(OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE)) {
            tyypit += tyypit.length() > 0 ? ", " : "";
            tyypit += I18N.getMessage(OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE.name());
        }
        if (organisaatio.getOrganisaatiotyypit().contains(OrganisaatioTyyppi.MUU_ORGANISAATIO)) {
            tyypit += tyypit.length() > 0 ? ", " : "";
            tyypit += I18N.getMessage(OrganisaatioTyyppi.MUU_ORGANISAATIO.name());
        }
        return tyypit;
    }

    class SearchCountEvent extends Component.Event {

        private int resultCount;

        private SearchCountEvent(Component source, int resultCount) {
            super(source);
            this.resultCount = resultCount;
        }

        public SearchCountEvent(Component source) {
            super(source);
        }

        public int getResultCount() {
            return resultCount;
        }

        public void setResultCount(int resultCount) {
            this.resultCount = resultCount;
        }
    }
}

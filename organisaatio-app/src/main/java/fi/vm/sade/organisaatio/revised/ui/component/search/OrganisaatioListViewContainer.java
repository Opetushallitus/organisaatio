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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.organisaatio.revised.ui.component.organisaatioform.OhjePopupComponent;
import fi.vm.sade.organisaatio.revised.ui.component.search.OrganisaatioListView.SearchCountEvent;

/**
 *
 * The container for searching a viewing organisaatio search results (Etsi
 * organisaatioita).
 *
 * @author markus
 *
 */
public class OrganisaatioListViewContainer extends VerticalLayout {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * The component for specifying the search criteria
     */
    private SearchPanel searchPanel;
    /**
     * The component for which contains the hierarchical organisaatio list, and
     * buttons and checkboxes to do operations on the organisaatios.
     */
    private OrganisaatioListDashboard organisaatioListBoard;
    
    private TabSheet resContainer;

    public OrganisaatioListViewContainer() {
        setSizeFull();

        final HorizontalLayout hl = new HorizontalLayout();
        //hl.setWidth("100%");
        addComponent(hl);
        final SearchRestrictionComponent src = new SearchRestrictionComponent();
        hl.addComponent(src);
        searchPanel = new SearchPanel();
        addComponent(searchPanel);
        resContainer = new TabSheet();
        organisaatioListBoard = new OrganisaatioListDashboard();
        // vaadin-workaround: estää vierityspalkkia jäämästä piiloon
        organisaatioListBoard.setMargin(false,true,false,false);
        organisaatioListBoard.addListener(new Listener() {
            @Override
            public void componentEvent(Event event) {
               if (event instanceof SearchCountEvent) {
                   setResultCount(((SearchCountEvent) event).getResultCount());
               }
            }
        });
        resContainer.addTab(organisaatioListBoard, I18N.getMessage("c_organisaatiot"));
        
        addComponent(resContainer);
    }
    
    public void setResultCount(int resultCount) {
        try {
            resContainer.getTab(organisaatioListBoard).setCaption(I18N.getMessage("c_organisaatiot") + " (" + resultCount + ")");
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }
    
    public void refresh() {
        this.organisaatioListBoard.refreshSearchResults();
    }
}

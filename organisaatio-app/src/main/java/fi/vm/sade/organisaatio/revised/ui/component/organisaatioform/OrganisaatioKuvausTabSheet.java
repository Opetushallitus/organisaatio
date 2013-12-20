
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wolfie.blackboard.annotation.ListenerMethod;
import com.vaadin.data.Property;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.koodisto.widget.KoodistoComponent;
import fi.vm.sade.koodisto.widget.WidgetFactory;
import fi.vm.sade.organisaatio.revised.ui.helper.KoodistoUriFieldFormatter;
import fi.vm.sade.organisaatio.ui.component.ConfirmationDialog;
import fi.vm.sade.organisaatio.ui.listener.ConfirmationListener;
import fi.vm.sade.organisaatio.ui.listener.event.ConfirmationEvent;
import fi.vm.sade.vaadin.constants.UiConstant;

/**
 * The language tab sheet for organisaatio.
 * 
 * @author Markus
 *
 */
class OrganisaatioKuvausTabSheet extends TabSheet {

	private static final long serialVersionUID = 1L;
	
	private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioKuvausTabSheet.class);
    private static final ThemeResource TAB_ICON_PLUS = new ThemeResource(UiConstant.RESOURCE_URL_OPH_IMG + "icon-add-black.png");
    private String _koodistoUri;
    private VerticalLayout _rootSelectionTabLayout = new VerticalLayout();
    private KoodistoComponent _kcSelection;
    // Map of tabs, key is the koodisto koodi uri
    private Map<String, Tab> _tabs = new HashMap<String, Tab>();

    /**
     * Createt tabsheet with given koodisto used for tabs "keys".
     *
     * @param koodistoUri
     */
    OrganisaatioKuvausTabSheet(String koodistoUri) {
        super();
        _rootSelectionTabLayout.setSizeUndefined();
        _koodistoUri = koodistoUri;
        _kcSelection = createKoodistoComponent();
        buildSelectionTabAndAddMonitoring();
    }

    public Map<String, Tab> getTabs() {
        return _tabs;
    }

    Tab getTab(String koodiUri) {
        return _tabs.get(koodiUri);
    }

    Tab addTab(String koodiUri, Component c, String caption) {
        Tab tab = super.addTab(c, caption);
        _tabs.put(koodiUri, tab);
        return tab;
    }

    public boolean tryRemoveTab(String koodiUri) {
    	return removeTab(koodiUri);
    }
    
    public boolean removeTab(String koodiUri) {
    	
        Tab tab = _tabs.remove(koodiUri);

        if (tab != null) {
            super.removeTab(tab);
            return true;
        } else {
        	return false;
        }

    }

    /**
     * Use to get the internal KoodistoComponent.
     *
     * @return
     */
    public KoodistoComponent getKcSelection() {
        return _kcSelection;
    }
    
    private void confirmRemoval(final Set<String> removeds) {
    	final Window cfw = new Window();
    	ConfirmationDialog cd = new ConfirmationDialog(I18N.getMessage(removeds.size()==1
    			? "OrganisaatioKuvausTabSheet.confirmLangRemoval"
				: "OrganisaatioKuvausTabSheet.confirmLangRemovals"));
        cd.setSizeUndefined();
        
    	cd.addListener(new ConfirmationListener() {
			
			@Override
			@ListenerMethod
			public void handleConfirmation(ConfirmationEvent confirmationEvent) {
				if (confirmationEvent.getConfirmation()) {
					for (String uri : removeds) {
						removeTab(uri);
					}
				} else {
					@SuppressWarnings("unchecked")
					Set<String> values = new HashSet<String>((Set<String>) _kcSelection.getValue());
					values.addAll(removeds);
					_kcSelection.setValue(values);
				}
				getWindow().removeWindow(cfw);
			}
		});
    	
		cfw.center();
		getWindow().addWindow(cfw);
		cfw.setContent(cd);
		cfw.setModal(true);
    	    	
    }

    /**
     * Create selection tab with the selection component and add listener to
     * monitor changes.
     */
    private void buildSelectionTabAndAddMonitoring() {

        // Manage tab additions and deletions
        _kcSelection.addListener(new Property.ValueChangeListener() {
            private static final long serialVersionUID = -382717228031608542L;

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                LOG.info("new values: {}", event.getProperty().getValue());

                // New values
                @SuppressWarnings("unchecked")
				Set<String> values = new HashSet<String>((Set<String>) event.getProperty().getValue());
                values.remove(null); // voiko tätä tapahtua?
                
                Set<String> addeds = new HashSet<String>(values);
                addeds.removeAll(_tabs.keySet());
                Set<String> removeds = new HashSet<String>(_tabs.keySet());
                removeds.removeAll(values);
                
                // lisää lisäykset
                for (String uri : addeds) {
                    doAddTab(uri);
              }
          	
                if (!removeds.isEmpty()) {
                	// varmista poistot
                	confirmRemoval(removeds);
                }

                /*

                List<String> tabsToBeRemoved = new ArrayList<String>();

                // Check for tab removals
                for (String uri : _tabs.keySet()) {
                    if (!values.contains(uri) && uri != null) {
                        tabsToBeRemoved.add(uri);
                    }
                }

                // Check for additions
                for (String uri : values) {
                    if (!_tabs.containsKey(uri) && uri != null) {
                        doAddTab(uri);
                    }
                }

                // Remove needed tabs
                for (String uri : tabsToBeRemoved) {
                	tryRemoveTab(uri);
                }
                */
            }
        });

        addTab(_rootSelectionTabLayout, "", TAB_ICON_PLUS);
    }

    /**
     * Override this to actually add the new tab. Only dummy tab added here.
     *
     * Call "addTab(uri, component, caption)" to do the adding.
     *
     * @param uri
     */
    public void doAddTab(String uri) {
        Label label = new Label();
        this.addTab(uri, label, uri);
    }

    /**
     * By default TwinColSelect is created, override to create something else.
     *
     * @return
     */
    private KoodistoComponent createKoodistoComponent() {
        KoodistoComponent koodisto = WidgetFactory.create(this._koodistoUri);
        koodisto.setCaption("");
        TwinColSelect koodistoSelect = new TwinColSelect();
        koodisto.setField(koodistoSelect);
        koodisto.setEnabled(true);
        koodisto.setImmediate(true);
        koodisto.setFieldValueFormatter(new KoodistoUriFieldFormatter());
        _rootSelectionTabLayout.addComponent(koodisto);
        return koodisto;
    }
}

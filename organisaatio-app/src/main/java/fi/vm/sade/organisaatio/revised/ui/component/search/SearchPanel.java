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

import com.vaadin.event.ShortcutAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wolfie.blackboard.exception.EventNotRegisteredException;
import com.vaadin.data.util.NestedMethodProperty;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbstractSelect.Filtering;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.ui.blackboard.BlackboardContext;
import fi.vm.sade.koodisto.widget.KoodistoComponent;
import fi.vm.sade.koodisto.widget.WidgetFactory;
import fi.vm.sade.organisaatio.KoodistoURI;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioSearchCriteria;
import fi.vm.sade.organisaatio.revised.ui.event.OrganisaatioSearchStartEvent;
import fi.vm.sade.organisaatio.revised.ui.helper.KoodistoHelper;
import fi.vm.sade.organisaatio.revised.ui.helper.KoodistoUriAndVersionFieldFormatter;
import fi.vm.sade.organisaatio.revised.ui.helper.KoodistoUriFieldFormatter;
import fi.vm.sade.organisaatio.ui.PortletRole;
import fi.vm.sade.organisaatio.ui.util.UiUtils;
import fi.vm.sade.vaadin.Oph;

/**
 * 
 * The panel for specifying search criteria for organisaatio search.
 * 
 * @author markus
 * 
 */
class SearchPanel extends VerticalLayout {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private HorizontalLayout lowerRow = new HorizontalLayout();
    private TextField searchBox;
    private KoodistoComponent kunta;
    private ComboBox organisaatioTyyppi;
    private KoodistoComponent oppilaitosTyyppi;
    private Button searchButton;
    private Button clearButton;
    private CheckBox vainLakkautetut;
    private CheckBox vainAktiiviset;
    // private CheckBox oikeudet;
    private OrganisaatioSearchCriteria searchCriteria;
    private KoodistoHelper koodistoHelper;

    public SearchPanel() {
        init();
        koodistoHelper = new KoodistoHelper();
    }

    /**
     * Initialization of components
     */
    private void init() {
        setWidth(100, Sizeable.UNITS_PERCENTAGE);
        searchCriteria = new OrganisaatioSearchCriteria();
        buildSearchLayout();

        initCheckboxes();
        addComponent(lowerRow);
        bind();
    }

    private void buildSearchLayout() {
        // LAYOUT
        HorizontalLayout hl = new HorizontalLayout();
        hl.setHeight(-1, UNITS_PIXELS);
        hl.setWidth(-1, Sizeable.UNITS_PIXELS);
        hl.setWidth(100, UNITS_PERCENTAGE);
        hl.setSpacing(true);
        hl.setMargin(false, false, true, false);

        /*
         * INITIALIZE COMPONENTS:
         */
        searchButton = UiUtils.buttonSmallSecodary(null, I18N.getMessage("c_hae"), new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                startSearch();
            }
        });
        searchButton.setClickShortcut(ShortcutAction.KeyCode.ENTER);

        searchButton.setWidth(-1, UNITS_PIXELS); // relative size

        clearButton = UiUtils.buttonSmallSecodary(null, I18N.getMessage("c_tyhjenna"), new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                clearSelections();
            }
        });

        clearButton.setWidth(-1, UNITS_PIXELS); // relative size

        kunta = initKoodistoComponentCombo(KoodistoURI.KOODISTO_KOTIPAIKKA_URI, "c_kunta");
        kunta.setWidth(-1, UNITS_PIXELS);
        kunta.setFieldValueFormatter(new KoodistoUriFieldFormatter());
        organisaatioTyyppi = initOrganisaatioTyyppiCombo();
        oppilaitosTyyppi = initKoodistoComponentCombo(KoodistoURI.KOODISTO_OPPILAITOSTYYPPI_URI, "c_olTyyppi");
        oppilaitosTyyppi.setFieldValueFormatter(new KoodistoUriAndVersionFieldFormatter());
        searchBox = new TextField(I18N.getMessage("SearchPanel.Hakupuu.lblHaku"));
        searchBox.setNullRepresentation("");
        searchBox.setWidth("300px");

        // Perform the search with ENTER-key
        searchBox.setImmediate(true);

        /*
         * SET COMPONENTS STYLES:
         */
        searchBox.addStyleName(Oph.TEXTFIELD_SEARCH);
        searchButton.addStyleName(Oph.BUTTON_SMALL);
        searchButton.addStyleName(Oph.BUTTON_PRIMARY);

        /*
         * ADD COMPONENTS TO LAYOUT:
         */
        hl.addComponent(searchBox);
        hl.addComponent(kunta);
        hl.addComponent(organisaatioTyyppi);
        hl.addComponent(oppilaitosTyyppi);
        hl.addComponent(searchButton);
        hl.addComponent(clearButton);

        /*
         * SET ALIGNMENTS:
         */
        hl.setComponentAlignment(searchBox, Alignment.BOTTOM_LEFT);
        hl.setComponentAlignment(searchButton, Alignment.BOTTOM_LEFT);
        hl.setComponentAlignment(kunta, Alignment.BOTTOM_RIGHT);
        hl.setComponentAlignment(organisaatioTyyppi, Alignment.BOTTOM_RIGHT);
        hl.setComponentAlignment(oppilaitosTyyppi, Alignment.BOTTOM_RIGHT);
        hl.setComponentAlignment(clearButton, Alignment.BOTTOM_RIGHT);

        /*
         * EXPAND RATION:
         */
        hl.setExpandRatio(searchBox, 1f);

        addComponent(hl);
    }

    @Override
    public void attach() {
        super.attach();

        if (PortletRole.getInstance().getUserContext() != null && PortletRole.getInstance().getUserContext().isDoAutoSearch()) {
            log.info("executing auto search");
            startSearch();
        }
        koodistoHelper.filterOutOldKoodit(kunta, KoodistoURI.KOODISTO_KOTIPAIKKA_URI);
    }

    private void bind() {
        searchBox.setPropertyDataSource(new NestedMethodProperty(searchCriteria, "searchStr"));
        kunta.setPropertyDataSource(new NestedMethodProperty(searchCriteria, "kunta"));
        organisaatioTyyppi.setPropertyDataSource(new NestedMethodProperty(searchCriteria, "organisaatioTyyppi"));
        oppilaitosTyyppi.setPropertyDataSource(new NestedMethodProperty(searchCriteria, "oppilaitosTyyppi"));
        // oikeudet.setPropertyDataSource(new NestedMethodProperty(model, "oikeudet"));
        vainLakkautetut.setPropertyDataSource(new NestedMethodProperty(searchCriteria, "vainLakkautetut"));
        vainAktiiviset.setPropertyDataSource(new NestedMethodProperty(searchCriteria, "vainAktiiviset"));
    }

    /**
     * Clear all selections
     */
    private void clearSelections() {

        searchBox.setImmediate(false);
        searchCriteria.setVainAktiiviset(false);
        searchCriteria.setVainLakkautetut(false);
        searchCriteria.setOppilaitosTyyppi(null);
        searchCriteria.setOrganisaatioTyyppi(null);
        searchCriteria.setSearchStr(null);
        searchCriteria.setKunta(null);
        // model.setOikeudet(null);
        bind();
        searchBox.setImmediate(true);

    }

    private void startSearch() {

        try {
            BlackboardContext.getBlackboard().fire(new OrganisaatioSearchStartEvent(searchCriteria));
        } catch (EventNotRegisteredException ex) {
            log.info("hmmm... search event has not been registered... search event not sent!");
        }
    }

    private void initCheckboxes() {
        vainLakkautetut = new CheckBox(I18N.getMessage("c_vainLakkautetut"));
        vainLakkautetut.setImmediate(true);
        vainLakkautetut.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                System.out.println("asdasdasdas");
                vainAktiiviset.setValue(false);
            }
        });
        lowerRow.addComponent(vainLakkautetut);

        vainAktiiviset = new CheckBox(I18N.getMessage("c_vainAktiiviset"));
        vainAktiiviset.setImmediate(true);
        vainAktiiviset.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                System.out.println("asdasdasdas");
                vainLakkautetut.setValue(false);
            }
        });
        lowerRow.addComponent(vainAktiiviset);

        lowerRow.setSpacing(true);
        lowerRow.setMargin(false, false, true, false);
    }

    private KoodistoComponent initKoodistoComponentCombo(String koodistoUri, String message) {
        KoodistoComponent compToInit = WidgetFactory.create(koodistoUri, true);
        compToInit.setCaption(I18N.getMessage(message));
        ComboBox oppilaitosTyyppiC = new ComboBox();

        oppilaitosTyyppiC.setFilteringMode(Filtering.FILTERINGMODE_CONTAINS);
        compToInit.setField(oppilaitosTyyppiC);
        compToInit.setWidth(-1, UNITS_PIXELS); // relative size

        return compToInit;
    }

    private ComboBox initOrganisaatioTyyppiCombo() {
        ComboBox cb = new ComboBox(I18N.getMessage("c_orgTyyppi"));
        addOrganisaatioTyyppis(cb);
        cb.setImmediate(true);
        cb.setWidth(-1, UNITS_PIXELS); // relative size
        return cb;
    }

    private void addOrganisaatioTyyppis(ComboBox cb) {
        for (OrganisaatioTyyppi curType : OrganisaatioTyyppi.values()) {
            cb.addItem(curType.value());
            cb.setItemCaption(curType.value(), I18N.getMessage(curType.name()));
        }
    }

    public TextField getSearchBox() {
        return searchBox;
    }

    public void setSearchBox(TextField searchBox) {
        this.searchBox = searchBox;
    }

    public KoodistoComponent getKunta() {
        return kunta;
    }

    public void setKunta(KoodistoComponent kunta) {
        this.kunta = kunta;
    }

    public ComboBox getOrganisaatioTyyppi() {
        return organisaatioTyyppi;
    }

    public void setOrganisaatioTyyppi(ComboBox organisaatioTyyppi) {
        this.organisaatioTyyppi = organisaatioTyyppi;
    }

    public KoodistoComponent getOppilaitosTyyppi() {
        return oppilaitosTyyppi;
    }

    public void setOppilaitosTyyppi(KoodistoComponent oppilaitosTyyppi) {
        this.oppilaitosTyyppi = oppilaitosTyyppi;
    }

    public Button getSearchButton() {
        return searchButton;
    }

    public void setSearchButton(Button searchButton) {
        this.searchButton = searchButton;
    }

    public Button getClearButton() {
        return clearButton;
    }

    public void setClearButton(Button clearButton) {
        this.clearButton = clearButton;
    }

    public CheckBox getVainLakkautetut() {
        return vainLakkautetut;
    }

    public void setVainLakkautetut(CheckBox vainLakkautetut) {
        this.vainLakkautetut = vainLakkautetut;
    }

    public CheckBox getVainaktiiviset() {
        return vainAktiiviset;
    }

    public void setVainAktiiviset(CheckBox vainAktiiviset) {
        this.vainAktiiviset = vainAktiiviset;
    }

    public OrganisaatioSearchCriteria getSearchCriteria() {
        return searchCriteria;
    }

    public void setSearchCriteria(OrganisaatioSearchCriteria model) {
        this.searchCriteria = model;
    }
}

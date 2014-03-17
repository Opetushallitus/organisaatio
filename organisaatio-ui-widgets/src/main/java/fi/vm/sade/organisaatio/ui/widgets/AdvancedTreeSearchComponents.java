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

package fi.vm.sade.organisaatio.ui.widgets;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.NestedMethodProperty;
import com.vaadin.ui.*;
import com.vaadin.ui.AbstractSelect.Filtering;
import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.ui.StyleNames;
import fi.vm.sade.koodisto.widget.KoodistoComponent;
import fi.vm.sade.koodisto.widget.WidgetFactory;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioSearchCriteriaDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Component for additional selections in SearchableTree component.
 *
 * @author markus
 *
 */

public class AdvancedTreeSearchComponents extends VerticalLayout {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final static String KOODISTO_OPPILAITOSTYYPPI_URI = "http://oppilaitostyyppi";

    private OrganisaatioSearchTree tree;

    private OrganisaatioSearchCriteriaDTO model;

    private Label otsikko;
    private HorizontalLayout bodyArea;
    private VerticalLayout leftPanel;
    private VerticalLayout rightPanel;
    private CheckBox yTunnus;
    private CheckBox olKoodi;
    private ComboBox organisaatioTyyppi;
    private ComboBox oppilaitosTyyppi;
    private CheckBox vainLakkautetut;
    private CheckBox vainAktiiviset;
    private static int counter = 0;


    public AdvancedTreeSearchComponents(OrganisaatioSearchCriteriaDTO model, OrganisaatioSearchTree tree) {
        this.model = model;
        this.tree = tree;
        initializeComponent();
    }

    public AdvancedTreeSearchComponents(OrganisaatioSearchCriteriaDTO model, OrganisaatioSearchTree tree, OrganisaatioSearchType type) {
        this.model = model;
        this.tree = tree;
        if (type == OrganisaatioSearchType.ADVANCED) {
            initAdvSearchCriterias();
        } else if (type == OrganisaatioSearchType.ALL_FIELDS) {
            initializeComponent();
        }
    }


    private void initializeLayout() {
        otsikko = new Label(I18N.getMessage("c_haeEhdolla"));
        addComponent(otsikko);
        bodyArea = new HorizontalLayout();
        bodyArea.addStyleName(StyleNames.GRID_16);
        leftPanel = new VerticalLayout();
        leftPanel.addStyleName(StyleNames.GRID_8);
        rightPanel = new VerticalLayout();
        rightPanel.addStyleName(StyleNames.GRID_8);
    }


    private void initLeftCheckboxes() {
        yTunnus = initializeCheckbox("c_ytunnus", "yTunnusSearch", "ytunnus", leftPanel);
        olKoodi = initializeCheckbox("c_oppilaitoskoodi", "olKoodiSearch", "olKoodi", leftPanel);
        bodyArea.addComponent(leftPanel);
    }

    private void initOrganisaatioTyyppiCombo() {
        organisaatioTyyppi = new ComboBox(I18N.getMessage("c_orgTyyppi"), getOrganisaatioTyyppisStr(OrganisaatioTyyppi.values()));
        organisaatioTyyppi.setDebugId(createDebugId("orgTyyppiSearch"));

        organisaatioTyyppi.setImmediate(true);
        organisaatioTyyppi.setPropertyDataSource(new NestedMethodProperty(model, "organisaatioTyyppi"));
        organisaatioTyyppi.addListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                // TODO Auto-generated method stub
                //DEBUGSAWAY:log.debug("organisaatioTyyppi");
                tree.reloadWithSearchData(model);
            }
        });
        rightPanel.addComponent(organisaatioTyyppi);
    }

    private List<String> getOrganisaatioTyyppisStr(OrganisaatioTyyppi[] orgTyyppis) {
        List<String> orgTyyppiStr = new ArrayList<String>();
        for (OrganisaatioTyyppi curType : orgTyyppis) {
            orgTyyppiStr.add(curType.value());
        }
        return orgTyyppiStr;
    }

    private void initOppilaitostyyppi() {
        KoodistoComponent koodistOLTyyppi = WidgetFactory.create(KOODISTO_OPPILAITOSTYYPPI_URI);
        koodistOLTyyppi.setCaption(I18N.getMessage("c_olTyyppi"));
        oppilaitosTyyppi = new ComboBox();
        oppilaitosTyyppi.setDebugId(createDebugId("olTyyppiSearch"));

        oppilaitosTyyppi.setFilteringMode(Filtering.FILTERINGMODE_CONTAINS);
        oppilaitosTyyppi.setImmediate(true);
        koodistOLTyyppi.setField(oppilaitosTyyppi);
        koodistOLTyyppi.setPropertyDataSource(new NestedMethodProperty(model, "oppilaitosTyyppi"));
        rightPanel.addComponent(koodistOLTyyppi);

        koodistOLTyyppi.addListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                //DEBUGSAWAY:log.debug("oppilaitosTyyppi");
                tree.reloadWithSearchData(model);
            }
        });
    }

    private void initRightCheckboxes() {
        vainLakkautetut = initializeCheckbox("c_vainLakkautetut", "lakkautetutSearch", "lakkautetut", rightPanel);
        //model.setLakkautetut(true);
        vainAktiiviset = initializeCheckbox("c_vainAktiiviset", "aktiivisetSearch", "aktiiviset", rightPanel);
    }
    /*
     * Initialization of components with data.
     */

    private void initAdvSearchCriterias() {
        removeAllComponents();
        initializeLayout();
        initOrganisaatioTyyppiCombo();
        initOppilaitostyyppi();
        bodyArea.addComponent(rightPanel);
        addComponent(bodyArea);
    }

    public void initializeComponent() {
        removeAllComponents();
        initializeLayout();
        initLeftCheckboxes();
        initOrganisaatioTyyppiCombo();
        initOppilaitostyyppi();
        initRightCheckboxes();
        //model.setSuunnitellut(true);
        bodyArea.addComponent(rightPanel);
        addComponent(bodyArea);
    }

    private String createDebugId(String debugIdPostfix) {
        counter++;
        return "advOrgSearch_" + debugIdPostfix + "_" +counter;
    }

    public OrganisaatioSearchCriteriaDTO getModel() {
        return model;
    }

    private CheckBox initializeCheckbox(String messageKey, String debugId, String dataSourceProp, VerticalLayout parentC) {
        CheckBox cb = new CheckBox(I18N.getMessage(messageKey));
        cb.setDebugId(createDebugId(debugId));
        cb.setImmediate(true);
        //model.setSuunnitellut(true);
        cb.setPropertyDataSource(new NestedMethodProperty(model, dataSourceProp));
        cb.addListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                //DEBUGSAWAY:log.debug("checkbox was clicked!!!");
                tree.reloadWithSearchData(model);
            }

        });

        parentC.addComponent(cb);
        return cb;

    }

    public CheckBox getyTunnus() {
        return yTunnus;
    }

    public CheckBox getOlKoodi() {
        return olKoodi;
    }

    public ComboBox getOrganisaatioTyyppi() {
        return organisaatioTyyppi;
    }

    public ComboBox getOppilaitosTyyppi() {
        return oppilaitosTyyppi;
    }

    public CheckBox getVainlakkautetut() {
        return vainLakkautetut;
    }

    public CheckBox getVainaktiiviset() {
        return vainAktiiviset;
    }





}

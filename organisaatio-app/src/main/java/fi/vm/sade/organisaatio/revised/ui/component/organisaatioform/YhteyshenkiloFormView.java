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

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.addon.formbinder.FormFieldMatch;
import org.vaadin.addon.formbinder.FormView;
import org.vaadin.addon.formbinder.PropertyId;

import fi.vm.sade.authentication.service.UserService;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

import fi.vm.sade.generic.common.I18NHelper;
import fi.vm.sade.generic.common.validation.ValidationConstants;
import fi.vm.sade.generic.ui.validation.JSR303FieldValidator;
import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.service.search.OrganisaatioSearchService;
import fi.vm.sade.organisaatio.ui.model.YhteyshenkiloModel;
import fi.vm.sade.vaadin.util.UiUtil;
import fi.vm.sade.authentication.service.types.HenkiloPagingObjectType;
import fi.vm.sade.authentication.service.types.HenkiloSearchObjectType;
import fi.vm.sade.authentication.service.types.dto.HenkiloType;
import fi.vm.sade.authentication.service.types.dto.SearchConnectiveType;

import com.vaadin.data.Property;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ShortcutAction;

/**
 * ECTS-yhteyshenkilo form view. Contains the autocomplete functionality for adding
 * ECTS-yhteyshenkilo.
 * 
 * @author Markus
 */
@FormView(matchFieldsBy = FormFieldMatch.ANNOTATION)
@Configurable(preConstruction = false)
class YhteyshenkiloFormView extends FormLayout implements Handler {

    private static final long serialVersionUID = 1L;
    
    @PropertyId("kokoNimi")
    private TextField yhNimi;
    @PropertyId("titteli")
    private TextField yhTitteli;
    @PropertyId("email")
    @Pattern(regexp = ValidationConstants.EMAIL_PATTERN, message = "{validation.ectsKoordinaattori.invalid.email}")
    private TextField yhEmail;
    @PropertyId("puhelin")
    @Pattern(regexp = "(\\+|\\-| |\\(|\\)|[0-9]){3,100}", message = "{validation.ectsKoordinaattori.invalid.phone}")
    private TextField yhPuhelin;
   
    /* The suggestion list of users. */
    private ListSelect suggestionList;
    
    /* The button to clear current values in yhteyshenkilo fields.*/
    private Button clearYhtHenkiloB;
    
    /* The current list of users in the suggestionList*/
    private List<HenkiloType> henkilos;
    
    private List<String> organisaatios = new ArrayList<String>();
    
    /*The currently selected index in the henkilos list. */
    private int selectedIndex = -1;
    
    /*The text typed by the user. */
    private String typedText;
    
    private YhteyshenkiloModel model;
    
    private String baseOrganisaatioOid;
    
    private I18NHelper i18n = new I18NHelper(this);
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private OrganisaatioService organisaatioService;
    
    private Action arrowDownAction = new ShortcutAction("Arrow down", ShortcutAction.KeyCode.ARROW_DOWN, null);
    private Action arrowUpAction = new ShortcutAction("Arrow up", ShortcutAction.KeyCode.ARROW_UP, null);
    private Action enterAction = new ShortcutAction("Enter", ShortcutAction.KeyCode.ENTER, null);
    private Action tabAction = new ShortcutAction("Tab", ShortcutAction.KeyCode.TAB, null);
    
    private String initialYhTitteli;
    private String initialYhEmail;
    private String initialYhPuhelin;
    
    private boolean nimiFocused = false;
    private Property.ValueChangeListener changeListener;

    @Autowired
    private OrganisaatioSearchService organisaatioSearchService;
    
    YhteyshenkiloFormView(YhteyshenkiloModel model, String baseOrganisaatioOid, VerticalLayout mainFormLayout, Property.ValueChangeListener listener) {
        super();
        this.changeListener = listener;
        setWidth("100%");

        
        this.model = model;
        this.baseOrganisaatioOid = baseOrganisaatioOid;
        buildLayout();
    }
    
    public YhteyshenkiloModel getModel() {
        return this.model;
    }
    
    void initializeData() {
        initialYhTitteli = model.getTitteli();
        initialYhEmail = model.getEmail();
        initialYhPuhelin = model.getPuhelin();
        
        createOrgList();
    }
    
    private void createOrgList() {
        //TODO query from solr
        organisaatios.clear();
        organisaatios.addAll(organisaatioSearchService.findParentOids(baseOrganisaatioOid));
    }
    
    private void buildLayout() {
        
        buildAutocompleteTextField();
        
       
        yhTitteli = UiUtil.textField(this, "", "", true);
        yhTitteli.setCaption(T("prompt.titteli"));
        yhTitteli.addListener(this.changeListener);
        
        yhEmail = UiUtil.textField(this, "", "", true);
        yhEmail.setCaption(T("prompt.email"));
        yhEmail.addListener(this.changeListener);
        
        yhPuhelin = UiUtil.textField(this, "", "", true);
        yhPuhelin.setCaption(T("prompt.puhelin"));
        yhPuhelin.addListener(this.changeListener);
        
        JSR303FieldValidator.addValidatorsBasedOnAnnotations(this);
        
    }
    
    private void buildAutocompleteTextField() {
       
        
        
        VerticalLayout nimifield = UiUtil.verticalLayout();
        nimifield.setSizeUndefined();
        HorizontalLayout hl = UiUtil.horizontalLayout();
        hl.setSizeUndefined();
        hl.setSpacing(true);
        yhNimi = UiUtil.textField(hl, "", "",  true);
        yhNimi.addListener(this.changeListener);
        nimifield.setCaption(T("ectsKoordinaattori"));
        
        //Adding the listener for listening to the chars entered by the user.
        //The suggestion list is updated after each char.
        yhNimi.addListener(new TextChangeListener() {

            private static final long serialVersionUID = -2079651800984069901L;


            @Override
            public void textChange(TextChangeEvent event) {
                typedText = event.getText();
                populateYhtHenkiloSuggestions(searchYhteyshenkilo(event.getText()));
                
            }
            
        });
        
        yhNimi.addListener(new FocusListener() {

            private static final long serialVersionUID = -7917016931392456002L;

            @Override
            public void focus(FocusEvent event) {
                nimiFocused = true;
            }
            
        });
        
        yhNimi.addListener(new BlurListener() {

            private static final long serialVersionUID = 8482347295139943045L;

            @Override
            public void blur(BlurEvent event) {
                nimiFocused = false;
                
            }
            
        });

        
        //The clear button. When the button is pressed the yhteyshenkilo fields are cleared.
        clearYhtHenkiloB = UiUtil.buttonLink(hl, T("tyhjenna"), new Button.ClickListener() {
            
            private static final long serialVersionUID = -6386527358361971773L;

            @Override
            public void buttonClick(ClickEvent event) {
                handleClearButtonClick();
                
            }
        });
        
        hl.setComponentAlignment(yhNimi, Alignment.MIDDLE_LEFT);
        hl.setComponentAlignment(clearYhtHenkiloB, Alignment.MIDDLE_LEFT);
        nimifield.addComponent(hl);
        
        nimifield.addComponent(buildSuggestionList());
        
        addComponent(nimifield);
        
      
        
    }
    
    private List<HenkiloType> searchYhteyshenkilo(String searchText) {
      //If given string is null or empty returning an empty list, i.e. not doing an empty search.
        if (searchText == null || searchText.isEmpty()) {
            return new ArrayList<HenkiloType>();
        }
        //Doing the search to UserService
        HenkiloSearchObjectType searchType = new HenkiloSearchObjectType();
        searchType.setConnective(SearchConnectiveType.AND);
        String[] nimetSplit = searchText.split(" ");
        if (nimetSplit.length > 1) {
            searchType.setSukunimi(nimetSplit[nimetSplit.length - 1]);
            searchType.setEtunimet(searchText.substring(0, searchText.lastIndexOf(' ')));
        } else {
            searchType.setEtunimet(searchText);
        }
        searchType.getOrganisaatioOids().addAll(organisaatios);
        HenkiloPagingObjectType paging = new HenkiloPagingObjectType();
        List<HenkiloType> henkilos = new ArrayList<HenkiloType>();
        try {
            henkilos = this.userService.listHenkilos(searchType, paging);
        } catch (Exception ex) {
            //LOG.error("Problem fetching henkilos: {}", ex.getMessage());
        }

        //Returning the list of found henkilos.
        return henkilos;
    }
    
    
    
    /*
     * Builds the suggestion list with listeners.
     */
    private ListSelect buildSuggestionList() {
        suggestionList = new ListSelect();
        
        suggestionList.setSizeUndefined();
        suggestionList.setWidth("175px");
        suggestionList.setNullSelectionAllowed(false);
        suggestionList.setImmediate(true);
        suggestionList.setVisible(false);
        
        //Adding listener to value change. On value change the yhteyshenkilo 
        //fields are updated according to the selected user.
        suggestionList.addListener(new Property.ValueChangeListener() {

            private static final long serialVersionUID = 3743367454230254280L;

            @Override
            public void valueChange(
                    com.vaadin.data.Property.ValueChangeEvent event) {
                handleValueChange();
                
            }
        });
        return suggestionList;
    }
    
    
    /*Handling of clear button click. Setting the text field to null and fireing event.
     * Event is catched by EditKoulutusPerustiedotForm which sets other yhteyshenkilo fields to null. */
    private void handleClearButtonClick() {
        yhNimi.setValue(null);
        model.setOid(null);
        model.setEmail(null);
        model.setKokoNimi(null);
        model.setPuhelin(null);
        model.setTitteli(null);
        this.clearInitialValuestoYhtHenkiloFields();
    }
 

    /*
     * Populates the henkilo suggestions under the yhtHenkKokoNimi field in according
     * to current search results from UserService.
     */
    private void populateYhtHenkiloSuggestions(List<HenkiloType> henkilos) {
        this.henkilos = henkilos;
        selectedIndex = -1;
        if (!henkilos.isEmpty()) {
            getWindow().addActionHandler(this);
            suggestionList.setVisible(true);
            suggestionList.removeAllItems();
            suggestionList.setRows(henkilos.size() + 1);
            
            for (HenkiloType curHenkilo : henkilos) {
                suggestionList.addItem(curHenkilo);
                suggestionList.setItemCaption(curHenkilo, curHenkilo.getEtunimet() + " " + curHenkilo.getSukunimi());
            }
        } else {
            getWindow().removeActionHandler(this);
            suggestionList.setVisible(false);
            suggestionList.removeAllItems();
            model.setOid(null);
            restoreInitialValuesToYhtHenkiloFields();
        }
    }
    /*
     * Handling of value change event. Fires an event witch is listened by EditKoulutusPerustiedotForm which updates the yhteyshenkilo fields. 
     */
    private void handleValueChange() {
        populateYhtHenkiloFields((HenkiloType)(suggestionList.getValue()));
        if (!nimiFocused) {
            handleEnter();
        }
    }
    
    /*
     * Handling of arrow down key events. If the suggestion list is currently visible
     * and there are suggestions in the list, the next user in the list is selected.
     */
    private void arrowDownHandler() {
        if (!suggestionList.isVisible() || henkilos == null || henkilos.isEmpty()) {
            return;
        }
        
        //If last user is already selected index is not updated.
        if (selectedIndex < this.henkilos.size() - 1) {
            ++selectedIndex;
        }
        HenkiloType selectedHenkilo = henkilos.get(selectedIndex);
        suggestionList.select(selectedHenkilo);
    }
    
    /*
     * Handling of arrow up key events. If the list is currently visible
     * and there are suggestions in the list, the previous user in the list is selected.
     */
    private void arrowUpHandler() {
        if (!suggestionList.isVisible() || henkilos == null || henkilos.isEmpty()) {
            return;
        }
        --selectedIndex;
        //If the currently selected user is not the first in the list the previous
        //user is selected, otherwise the selection is removed and a user not selected.
        //is fired and the string typed by the user is set as the value of the text field. 
        if (selectedIndex >= 0) {
            HenkiloType selectedHenkilo = henkilos.get(selectedIndex);
            suggestionList.select(selectedHenkilo);
        } else {
            yhNimi.setValue(typedText);
            suggestionList.unselect(henkilos.get(selectedIndex+1));
            restoreInitialValuesToYhtHenkiloFields();
        }
    }
    
    /**
     * Populating the yhteyshenkilo fields based on user's selection from the autocomplete list
     * @param henkiloType
     */
    private void populateYhtHenkiloFields(HenkiloType henkiloType) {
        if (henkiloType == null) {
            return;
        }
        this.yhNimi.setValue(henkiloType.getEtunimet() + " " + henkiloType.getSukunimi());
        model.setOid(henkiloType.getOidHenkilo());
        if (henkiloType.getOrganisaatioHenkilos() != null && !henkiloType.getOrganisaatioHenkilos().isEmpty()) {
            this.yhEmail.setValue(henkiloType.getOrganisaatioHenkilos().get(0).getSahkopostiosoite());
            this.yhPuhelin.setValue(henkiloType.getOrganisaatioHenkilos().get(0).getPuhelinnumero());
            this.yhTitteli.setValue(henkiloType.getOrganisaatioHenkilos().get(0).getTehtavanimike());
        } else {
            this.yhEmail.setValue(null);
            this.yhPuhelin.setValue(null);
            this.yhTitteli.setValue(null);
        }
    }
    
    /*
     * Restoring the initial values to yhteyshenkilo fields. this functionality is to enable the user
     * to try different yhteyshenkilos from search but then return to the old one. This is
     * important in the cases that the current yhteyshenkilo is not in the user register but is
     * created by the editor of this koulutus (to not loose data).
     */
    private void restoreInitialValuesToYhtHenkiloFields() {
        this.yhEmail.setValue(initialYhEmail);
        this.yhPuhelin.setValue(initialYhPuhelin);
        this.yhTitteli.setValue(initialYhTitteli);
        model.setOid(null);
    }
    
    /*
     * Nullifying the initial values to yhteyshenkilo fields. When the user decides to
     * remove the existing yhteyshenkilo and possibly add a new one.
     */
    private void clearInitialValuestoYhtHenkiloFields() {
        initialYhEmail = null;
        initialYhPuhelin = null;
        initialYhTitteli = null;
        restoreInitialValuesToYhtHenkiloFields();
    }
    
    /*
     * Handling of of enter key events. The suggestion list is hidden if it is visible.
     */
    private void handleEnter() {
        if (!suggestionList.isVisible() || henkilos == null || henkilos.isEmpty()) {
            return;
        }
        suggestionList.removeAllItems();
        suggestionList.setVisible(false);
        getWindow().removeActionHandler(this);
    }

    //Handler interface methods. The interface is implemented to be able to listen to keyboard events.
    
    @Override
    public Action[] getActions(Object target, Object sender) {
        return new Action[]{arrowDownAction, arrowUpAction, enterAction, tabAction};
    }

    @Override
    public void handleAction(Action action, Object sender, Object target) {
        if (action == arrowDownAction) {
            arrowDownHandler();
        }
        if (action == arrowUpAction) {
            arrowUpHandler();
        }
        if (action == enterAction || action == tabAction) {
            handleEnter();
        }
    }
    
    private String T(String key, Object... args) {
        return i18n.getMessage(key, args);
    }
}

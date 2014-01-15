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

import static fi.vm.sade.generic.common.validation.ValidationConstants.EMAIL_PATTERN;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.Pattern;

import com.vaadin.event.FieldEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.addon.formbinder.FormFieldMatch;
import org.vaadin.addon.formbinder.FormView;
import org.vaadin.addon.formbinder.PropertyId;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanItem;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Form;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.common.I18NHelper;
import fi.vm.sade.generic.common.validation.ValidationConstants;
import fi.vm.sade.generic.ui.component.MultiLingualTextField;
import fi.vm.sade.generic.ui.validation.JSR303FieldValidator;
import fi.vm.sade.generic.ui.validation.ValidatingViewBoundForm;
import fi.vm.sade.organisaatio.KoodistoURI;
import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.organisaatio.revised.ui.event.KuvausEvent;
import fi.vm.sade.organisaatio.revised.ui.helper.KoodistoHelper;
import fi.vm.sade.organisaatio.ui.component.OsoiteField;
import fi.vm.sade.organisaatio.ui.model.KielikaannosModel;
import fi.vm.sade.organisaatio.ui.model.LOPTiedotModel;
import fi.vm.sade.organisaatio.ui.model.OrganisaatioKuvailevatTiedotModel;
import fi.vm.sade.organisaatio.ui.model.YhteyshenkiloModel;
import fi.vm.sade.vaadin.Oph;
import fi.vm.sade.vaadin.util.UiUtil;

/**
 * The kuvailevat tiedot form. Contains the hakutoimisto data, yhteyshenkilo, SoMe urls and localized free text 
 * fields (LOPKuvauksetFormViews).
 * 
 * @author Markus
 *
 */
@FormView(matchFieldsBy = FormFieldMatch.ANNOTATION)
@Configurable(preConstruction = false) 
class OrganisaatioKuvailevatTiedotFormView extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    private OrganisaatioKuvausTabSheet kuvailevatTiedotSheet;
    
    /**
     * The name of the hakutoimisto
     */
    @PropertyId("otNimi")
    private MultiLingualTextField otNimi;
    
    /**
     * Does the finnish name apply to all languages?
     */
    @PropertyId("yksiNimi")
    private CheckBox yksiNimi;
    
    /**
     * Button for adding a new localized name for hakutoimisto.
     */
    private Button uusiKieliB;
    
    /**
     * The kayntiosoite for hakutoimisto.
     */
    @PropertyId("kayntiosoite")
    private OsoiteField otKayntiOsoite;
    
    /**
     * The postiosoite for hakutoimisto
     */
    @PropertyId("postiosoite")
    private OsoiteField otPostiOsoite;
    
    @PropertyId("ruotsiKayntiOsoite")
    private OsoiteField ruotsiKayntiOsoite;

    @PropertyId("ruotsiPostiOsoite")
    private OsoiteField ruotsiPostiOsoite;

    @PropertyId("englantiKayntiOsoite")
    private OsoiteField englantiKayntiOsoite;

    @PropertyId("englantiPostiOsoite")
    private OsoiteField englantiPostiOsoite;

    
    private Label koRuotsiLbl;
    private Label poRuotsiLbl;
    private Label koSuomiLbl;
    private Label poSuomiLbl;
    private Label koEnglantiLbl;
    private Label poEnglantiLbl;
    

    /**
     * Hakutoimisto phone
     */
    @Pattern(regexp = "(\\+|\\-| |\\(|\\)|[0-9]){3,100}", message = "{validation.hakutoimisto.invalid.phone}")
    @PropertyId("puhelinnumero")
    private TextField otPuhelin;

    /**
     * Hakutoimisto email address
     */
    @Pattern(regexp = EMAIL_PATTERN, message = "{validation.hakutoimisto.invalid.email}")
    @PropertyId("emailOsoite")
    private TextField otEmail;

    /**
     * Hakutoimisto email address
     */
    @Pattern(regexp = ValidationConstants.WWW_PATTERN, message = "{validation.hakutoimisto.invalid.www}")
    @PropertyId("wwwOsoite")
    private TextField otWww;
    
    /**
     * Hakutoimisto fax number.
     */
    @Pattern(regexp = "(\\+|\\-| |\\(|\\)|[0-9]){3,100}", message = "{validation.hakutoimisto.invalid.fax}")
    @PropertyId("faksinumero")
    private TextField otFax;
    
    //Below the SoMe link fields.
    @PropertyId("facebook")
    private TextField facebookLink;
    @PropertyId("linkedin")
    private TextField linkedInLink;
    @PropertyId("twitter")
    private TextField twitterLink;
    @PropertyId("googlePlus")
    private TextField googlePlusLink;
    @PropertyId("muu1")
    private TextField muuLink1;
    @PropertyId("muu2")
    private TextField muuLink2;
    
    /**
     * The component for uploading organization photo.
     */
    private ImageUploader imageUploader;
    
    private I18NHelper i18n = new I18NHelper(this);
    
    /**
     * The model for the data of this form.
     */
    private OrganisaatioKuvailevatTiedotModel model;
    
    /**
     * Hash map 
     */
    //private HashMap<String, LOPKuvauksetFormView> kuvausFormViews = new HashMap<String, LOPKuvauksetFormView>();
    private YhteyshenkiloFormView yhFormView;
    
    /**
     * Map containing the forms for the languages in which free text descriptions are given. Used when commiting
     * the forms.
     */
    private HashMap<String, Form> kuvausForms = new HashMap<String, Form>();
    /**
     * The yhteyshenkilo form.
     */
    private Form yhForm;
    
    /**
     * The parent oid of the edited organisaation. Used when fetching possible yhteyshenkilos for the orgaanisaatio.
     */
    private String parentOid;
    
    /**
     * The window in which the name dialog is displayed.
     */
    private Window nimiDialogWindow;
    
    /**
     * the dialog to adda localized name for the hakutoimisto.
     */
    private NimiDialog nimiForm;
    
    /**
     * The container to display additional localized names for the hakutoimisto.
     */
    private GridLayout additionalNimiContainer;
    
    @Autowired
    private OrganisaatioService organisaatioService;
    
    /**
     * The main grid in which form fiels are displayed.
     */
    private GridLayout mainFormGrid;
    
    private Property.ValueChangeListener changeListener;
    
    private OrganisaatioEditForm mainForm;
    
    public OrganisaatioKuvailevatTiedotFormView() {
        this(new OrganisaatioKuvailevatTiedotModel(), null, null);
    }
    
    public OrganisaatioKuvailevatTiedotFormView(Property.ValueChangeListener listener) {
        this(new OrganisaatioKuvailevatTiedotModel(), null, listener);
    }
    
    OrganisaatioKuvailevatTiedotFormView(OrganisaatioKuvailevatTiedotModel model, String parentOid, Property.ValueChangeListener listener) {
        super();
        this.changeListener = listener;
        this.model = model;
        this.parentOid = parentOid;
        buildLayout();
        
    }
    
    /**
     * Initializing image, the yhteyshenkilo data and pre-selecting the finnish language tab
     * for free text descriptions.
     */
    @Override
    public void attach() {
        super.attach();
        yhFormView.initializeData();
        imageUploader.attachImage();
        kuvailevatTiedotSheet.setSelectedTab(kuvailevatTiedotSheet.getTab(T("fiUri")));
        if (this.mainForm != null) {
            mainForm.makeUnmodified();
        }
    }
    
    /**
     * Commits the subforms used in this view. Passes the errors to the caller as
     * a list of exceptions. The caller displays them on the error view.
     * 
     * @return A list of validation exceptions thrown by the different forms.
     * @throws Exception
     */
    List<Validator.InvalidValueException> commitForm() throws Exception {
        List<Validator.InvalidValueException> errors = new ArrayList<Validator.InvalidValueException>();
        this.yhForm.commit();
        for (Map.Entry<String, Form> curEntry : kuvausForms.entrySet()) {
            try {
                curEntry.getValue().commit();
            } catch (Validator.InvalidValueException ex) {
                errors.add(ex);
            }
        }
        return errors;
    }
    
    public OrganisaatioKuvailevatTiedotModel getModel() {
        return this.model;
    }
    
    /**
     * Builds the ui-layout.
     */
    private void buildLayout() {
        setMargin(false, false, true, false);
        setSpacing(true);
        setSizeFull();
        VerticalLayout placeholder = UiUtil.verticalLayout();
        placeholder.setHeight("30px");
        addComponent(placeholder);
        mainFormGrid = new GridLayout(4, 21);
        mainFormGrid.setSpacing(true);
        mainFormGrid.setSizeUndefined();
        
        //Creation of image uploader component
        imageUploader = new ImageUploader(model.getKuva(), mainFormGrid, this.changeListener);
        
        mainFormGrid.addComponent(buildSplitPanel(), 0, 2, 3, 2);
        
        //Building the hakutoimisto ui layout.
        buildOpintotoimistoLayout();
        
        mainFormGrid.addComponent(buildSplitPanel(), 0, 13, 3, 13);
        
        //Building the yhteyshenkilo ui layout
        buildYhteyshenkiloLayout();
        
        mainFormGrid.addComponent(buildSplitPanel(), 0, 15, 3, 15);
        
        //building the SoMe ui layout
        buildSoMeLayout();
        addComponent(mainFormGrid);
        
        //Building the free description ui layout.
        buildKuvailevatTekstitLayout();
        addComponent(buildSplitPanel());
        
        JSR303FieldValidator.addValidatorsBasedOnAnnotations(this);
        
    }
    
    /**
     * Building the SoMe fields.
     */
    private void buildSoMeLayout() {
        HorizontalLayout hl = UiUtil.horizontalLayout();
        hl.setMargin(false, false, false, true);
        Label soMeLabel = UiUtil.label(hl, T("soMeLabel"));
        soMeLabel.addStyleName(Oph.LABEL_H2);
        mainFormGrid.addComponent(hl, 0, 16);
        
        facebookLink = createLeftSomeField("facebook", 17);
        facebookLink.setSizeFull();
        linkedInLink = createLeftSomeField("linkedIn", 18);
        linkedInLink.setSizeFull();
        muuLink1 = createLeftSomeField("muu", 19);
        muuLink1.setSizeFull();

        GridLayout ytVl = new GridLayout(2,3);
        ytVl.setSizeFull();
        ytVl.setSpacing(true);
        
        twitterLink = createRightSomeField("twitter", ytVl, 0);
        twitterLink.setSizeFull();
        googlePlusLink = createRightSomeField("googlePlus", ytVl, 1);
        googlePlusLink.setSizeFull();
        muuLink2 = createRightSomeField("muu", ytVl, 2);
        muuLink2.setSizeFull();

        this.mainFormGrid.addComponent(ytVl, 2, 17, 2, 19);
        mainFormGrid.setComponentAlignment(ytVl, Alignment.TOP_LEFT);
    }
    
    private TextField createLeftSomeField(String labelKey, int row) {
        VerticalLayout fieldLabel = this.createLabelLayout(T(labelKey));
        mainFormGrid.addComponent(fieldLabel, 0, row);
        mainFormGrid.setComponentAlignment(fieldLabel, Alignment.MIDDLE_RIGHT);
        VerticalLayout fieldLayout = UiUtil.verticalLayout();
        fieldLayout.setHeight("30px");
        fieldLayout.setWidth("400px");
        TextField field =  UiUtil.textField(fieldLayout, "", "", false);
        mainFormGrid.addComponent(fieldLayout, 1, row);
        mainFormGrid.setComponentAlignment(fieldLayout, Alignment.MIDDLE_LEFT);

        field.addListener(this.changeListener);
        return field;
    }
    
    private TextField createRightSomeField(String labelKey, GridLayout ytVl, int row) {
        VerticalLayout fieldLabel = this.createLabelLayout(T(labelKey));
        fieldLabel.setWidth("100px");
        fieldLabel.setSpacing(true);
        ytVl.addComponent(fieldLabel, 0, row);
        ytVl.setComponentAlignment(fieldLabel, Alignment.MIDDLE_RIGHT);
        VerticalLayout fieldLayout = UiUtil.verticalLayout();
        fieldLayout.setHeight("30px");
        fieldLayout.setWidth("400px");
        TextField field = UiUtil.textField(fieldLayout, "", "", false);
        ytVl.addComponent(fieldLayout, 1, row);
        ytVl.setComponentAlignment(fieldLayout, Alignment.MIDDLE_LEFT);

        field.addListener(this.changeListener);
        return field;
    }
    
    

    /**
     * Building the opintotoimisto layout.
     */
    private void buildOpintotoimistoLayout() {
        HorizontalLayout hl = UiUtil.horizontalLayout();
        hl.setMargin(false, false, false, true);
        Label otLabel = UiUtil.label(hl, T("opintotoimistoLabel"));
        otLabel.addStyleName(Oph.LABEL_H2);
        mainFormGrid.addComponent(hl, 0, 3);
        mainFormGrid.setComponentAlignment(hl, Alignment.TOP_LEFT);
        
        //Creation of the nimi components
        createNimiLayout();
        
        //Creation of the address and other contact components.
        createKayntiOsoitteetLayout();
        createPostiOsoitteetLayout();
        
        createEnglantiKayntiOsoitteetLayout();
        createEnglantiPostiOsoitteetLayout();
        
        createMuutYhteystiedotLayout();
    }
    

    
    /**
     * Creation of the address and other contact components.
     */
    private void createMuutYhteystiedotLayout() {
        otPuhelin = createMuuYhteystietoFieldLayout("otPuhelin", 9); 
        otFax = createMuuYhteystietoFieldLayout("otFax", 10);
        otEmail = createMuuYhteystietoFieldLayout("otEmail", 11);
        otEmail.setWidth("327px");
        otWww = createMuuYhteystietoFieldLayout("otWww", 12);
        otWww.setWidth("327px");
    }

    private TextField createMuuYhteystietoFieldLayout(String labelKey, int row) {
        VerticalLayout fieldLabel = createLabelLayout(T(labelKey));
        mainFormGrid.addComponent(fieldLabel, 0, row);
        mainFormGrid.setComponentAlignment(fieldLabel, Alignment.MIDDLE_RIGHT);
 
        VerticalLayout ytLayout = new VerticalLayout();
        ytLayout.setHeight("40px");
        ytLayout.setWidth("450px");
        ytLayout.setSpacing(false);
        TextField ytField = UiUtil.textField(ytLayout, "", "", false);
        ytLayout.setComponentAlignment(ytField, Alignment.MIDDLE_LEFT);
        mainFormGrid.addComponent(ytLayout, 1, row);
        mainFormGrid.setComponentAlignment(ytLayout, Alignment.MIDDLE_LEFT);
        ytField.addListener(this.changeListener);
        
        return ytField;
    }
    
    private void createKayntiOsoitteetLayout()  {
        VerticalLayout kayntiOsoiteLabel = createLabelLayout(T("otKayntiOsoite"));
        mainFormGrid.addComponent(kayntiOsoiteLabel, 0, 5);
        mainFormGrid.setComponentAlignment(kayntiOsoiteLabel, Alignment.TOP_RIGHT);
        
        VerticalLayout koVl1 = new VerticalLayout();
        
        koSuomiLbl = new Label(T("suomeksi"));
        HorizontalLayout hlMarginWrapper = new HorizontalLayout();
        otKayntiOsoite = new OsoiteField();
        otKayntiOsoite.setImmediate(true);
        otKayntiOsoite.addListener(this.changeListener);
        hlMarginWrapper.addComponent(otKayntiOsoite);
        hlMarginWrapper.setMargin(false, true, false, false);

        koVl1.addComponent(koSuomiLbl);
        koVl1.addComponent(hlMarginWrapper);

        mainFormGrid.addComponent(koVl1, 1, 5, 1, 5);

        VerticalLayout koVl2 = new VerticalLayout();
        
        koRuotsiLbl = new Label(T("ruotsiksi"));
        ruotsiKayntiOsoite = new OsoiteField();
        ruotsiKayntiOsoite.setImmediate(true);
        ruotsiKayntiOsoite.addListener(this.changeListener);
        ruotsiKayntiOsoite.setArSvensk(true);
        koVl2.addComponent(koRuotsiLbl);
        koVl2.addComponent(ruotsiKayntiOsoite);

        otKayntiOsoite.setAlternative(ruotsiKayntiOsoite);

        mainFormGrid.addComponent(koVl2, 2, 5, 2, 5);
    }

    private void createPostiOsoitteetLayout() {
        VerticalLayout postiOsoiteLabel = createLabelLayout(T("otPostiOsoite"));
        mainFormGrid.addComponent(postiOsoiteLabel, 0, 6);
        mainFormGrid.setComponentAlignment(postiOsoiteLabel, Alignment.TOP_RIGHT);
        
        VerticalLayout poVl1 = new VerticalLayout();
        poSuomiLbl = new Label(T("suomeksi"));
        HorizontalLayout hlPostiMarginWrapper = new HorizontalLayout();
        otPostiOsoite = new OsoiteField();
        otPostiOsoite.setImmediate(true);
        otPostiOsoite.addListener(this.changeListener);
        hlPostiMarginWrapper.addComponent(otPostiOsoite);
        hlPostiMarginWrapper.setMargin(false,true,false,false);
        poVl1.addComponent(poSuomiLbl);
        poVl1.addComponent(hlPostiMarginWrapper);

        mainFormGrid.addComponent(poVl1, 1, 6, 1, 6);
        
        VerticalLayout poVl2 = new VerticalLayout();
        poRuotsiLbl = new Label(T("ruotsiksi"));
        ruotsiPostiOsoite = new OsoiteField();
        ruotsiPostiOsoite.setImmediate(true);
        ruotsiPostiOsoite.addListener(this.changeListener);
        ruotsiPostiOsoite.setArSvensk(true);

        poVl2.addComponent(poRuotsiLbl);
        poVl2.addComponent(ruotsiPostiOsoite);
        
        otPostiOsoite.setAlternative(ruotsiPostiOsoite);

        mainFormGrid.addComponent(poVl2, 2, 6, 2, 6);   
    }

    private void createEnglantiKayntiOsoitteetLayout()  {
        VerticalLayout kayntiOsoiteLabel = createLabelLayout(T("otKayntiOsoite"));
        mainFormGrid.addComponent(kayntiOsoiteLabel, 0, 7);
        mainFormGrid.setComponentAlignment(kayntiOsoiteLabel, Alignment.TOP_RIGHT);
        
        VerticalLayout enKoVl = new VerticalLayout();
        
        koEnglantiLbl = new Label(T("englanniksi"));
        englantiKayntiOsoite = new OsoiteField();
        englantiKayntiOsoite.setImmediate(true);
        englantiKayntiOsoite.addListener(this.changeListener);
        englantiKayntiOsoite.reCreateForeignLayout(false);
        enKoVl.addComponent(koEnglantiLbl);
        enKoVl.addComponent(englantiKayntiOsoite);

        mainFormGrid.addComponent(enKoVl, 1, 7, 1, 7);
    }

    private void createEnglantiPostiOsoitteetLayout()  {
        VerticalLayout postiOsoiteLabel = createLabelLayout(T("otPostiOsoite"));
        mainFormGrid.addComponent(postiOsoiteLabel, 0, 8);
        mainFormGrid.setComponentAlignment(postiOsoiteLabel, Alignment.TOP_RIGHT);
        
        VerticalLayout enPoVl = new VerticalLayout();
        
        poEnglantiLbl = new Label(T("englanniksi"));
        englantiPostiOsoite = new OsoiteField();
        englantiPostiOsoite.setImmediate(true);
        englantiPostiOsoite.addListener(this.changeListener);
        englantiPostiOsoite.reCreateForeignLayout(false);
        enPoVl.addComponent(poEnglantiLbl);
        enPoVl.addComponent(englantiPostiOsoite);

        mainFormGrid.addComponent(enPoVl, 1, 8, 1, 8);
    }
    
    /**
     * Creation of the nimi components.
     */
    private void createNimiLayout() {
        
        GridLayout nimiLayout = new GridLayout(2,2);
        nimiLayout.setWidth("450px");
        nimiLayout.setSpacing(true);
        otNimi = new MultiLingualTextField(I18N.getLocale());
        otNimi.addListener(this.changeListener);
        otNimi.setWidth("300px");

        
        VerticalLayout labelVl = createLabelLayout(T("otNimi"));
        mainFormGrid.addComponent(labelVl, 0,4);
        mainFormGrid.setComponentAlignment(labelVl, Alignment.TOP_RIGHT);
        
        nimiLayout.addComponent(otNimi, 0,0);
        
        VerticalLayout nimiButtons = UiUtil.verticalLayout();
        nimiButtons.setSizeUndefined();
        nimiButtons.addComponent(new Label());
        yksiNimi = UiUtil.checkbox(nimiButtons, T("yksiNimi"));
        yksiNimi.setImmediate(true);
        yksiNimi.addListener(this.changeListener);
        //Adding the listener which responds to users selections on whether the finnish name
        //Should apply to all languages.
        yksiNimi.addListener(new Property.ValueChangeListener() {

            private static final long serialVersionUID = 3358321169436950647L;

            @Override
            public void valueChange(ValueChangeEvent event) {
                toggleYksiNimi(yksiNimi.booleanValue());
            }
        });



        nimiButtons.addComponent(new Label());
        
        /*
         * Adding the listener to responds to the user's click on the add new name for the hakutoimisto.
         */
        uusiKieliB = UiUtil.buttonLink(nimiButtons, T("uusiNimi"), new Button.ClickListener() {
            
            private static final long serialVersionUID = -7693201687348519856L;

            @Override
            public void buttonClick(ClickEvent event) {
                createNimiDialog();
            }
        });
        uusiKieliB.setImmediate(true);
        
        nimiLayout.addComponent(nimiButtons, 1,0);
        nimiLayout.setComponentAlignment(nimiButtons, Alignment.BOTTOM_LEFT);
        nimiLayout.setColumnExpandRatio(0, 1.0f);
        nimiLayout.setColumnExpandRatio(1, 0.5f);
        
        createAdditionalNimet();
        
        nimiLayout.addComponent(additionalNimiContainer, 0,1);
        
        mainFormGrid.addComponent(nimiLayout, 1,4);
        
        mainFormGrid.setComponentAlignment(nimiLayout, Alignment.MIDDLE_LEFT);
    }
    
    /**
     * Creation of a label for a field.
     * @param caption
     * @return
     */
    private VerticalLayout createLabelLayout(String caption) {
        VerticalLayout vl = UiUtil.verticalLayout();
        vl.setSizeUndefined();
        vl.setWidth("200px");
        Label label = UiUtil.label(vl, caption);
        vl.setComponentAlignment(label,Alignment.TOP_RIGHT);
        return vl;
    }
    
    /**
     * Creation of the component to display additional (not finnish, english, or swedish) names for
     * the hakutoimisto. 
     */
    private void createAdditionalNimet() {
        additionalNimiContainer = new GridLayout(3, (this.model.getAdditionalNimet().size() > 0) ? this.model.getAdditionalNimet().size() : 1);
        additionalNimiContainer.setSpacing(true);
        additionalNimiContainer.setSizeUndefined();
        additionalNimiContainer.setWidth("300px");
        populateNimiContainer();
    }
    
    /**
     * Building the components for editing yhteyshenkilo for this organisation.
     * The yhteyshenkilo fields are a separate form which is added to the main layout.
     */
    private void buildYhteyshenkiloLayout() {
        HorizontalLayout hl = UiUtil.horizontalLayout();
        hl.setMargin(false, false, false, true);
        Label koordLabel = UiUtil.label(hl, T("koordLabel"));
        koordLabel.addStyleName(Oph.LABEL_H2);
        mainFormGrid.addComponent(hl, 0, 14);
        mainFormGrid.setComponentAlignment(hl, Alignment.TOP_LEFT);
        
        yhFormView = new YhteyshenkiloFormView(this.model.getEctsYhteyshenkilo(), parentOid, this, this.changeListener);
        
        BeanItem<YhteyshenkiloModel> bItem = new BeanItem<YhteyshenkiloModel>(yhFormView.getModel());
        // luodaan yhteyshenkiloform
        yhForm = new ValidatingViewBoundForm(yhFormView);
        yhForm.setItemDataSource(bItem);
        
        yhForm.setValidationVisible(false);
        yhForm.setValidationVisibleOnCommit(false);
        yhForm.setWidth("100%");
                                                                                                                                                                                                                                                                                                                                                                                                                                          
        mainFormGrid.addComponent(yhForm, 1, 14);        
    }
    
    private VerticalSplitPanel buildSplitPanel() {
        VerticalSplitPanel splitPanel = new VerticalSplitPanel();
        splitPanel.setWidth("100%");
        splitPanel.setHeight("2px");
        splitPanel.setLocked(true);
        return splitPanel;
    }
    
    /**
     * Performing the addition of a new language to edit free descrptions.
     * @param uri
     */
    private void performAddTab(String uri) {
        KoodistoHelper helper = new KoodistoHelper();
        String caption = uri != null ?  helper.tryGetArvoByKoodi(uri) : "";
        Form rtForm = createRichTextEditor(uri, caption);
        
        kuvailevatTiedotSheet.addTab(uri, rtForm, caption);
    }
    
    /**
     * Propagation of a save (or preview) click in a free text editing form
     * to the main organisaatio form view.
     * @param event
     */
    private void propagateEvent(KuvausEvent event) {
        fireEvent(event);
    }
    
    /**
     * Builds the free description layout.
     */
    private void buildKuvailevatTekstitLayout() {
        
        kuvailevatTiedotSheet = new OrganisaatioKuvausTabSheet(KoodistoURI.KOODISTO_KIELI_URI){
            private static final long serialVersionUID = -7916177514458213528L;
            @Override
            public void doAddTab(String uri) {
                performAddTab(uri);
            }
            
            @Override
            public boolean removeTab(String uri) {
            	if (super.removeTab(uri)) {
                    handleTabRemoval(uri);
                    return true;
            	} else {
            		return false;
            	}
            }
            
        };
        addComponent(kuvailevatTiedotSheet);
        
        //Initialization of language tabs based on data.
        //Finnish language tab is added as first and set as the default tab.
        Set<String> tabkeys = new HashSet<String>();
        if (!model.getLopTiedot().keySet().contains(T("fiUri"))) {
            tabkeys.add(T("fiUri"));
        }
        
        for (String curKey : model.getLopTiedot().keySet()) {
            tabkeys.add(curKey);
        }
        
        kuvailevatTiedotSheet.getKcSelection().setValue(tabkeys);
        kuvailevatTiedotSheet.setSelectedTab(kuvailevatTiedotSheet.getTab(T("fiUri")));
    }
    
    /**
     * Removes a name from the additional name container.
     * @param nimiIndex
     */
    private void removeNimi(int nimiIndex) {
        this.model.getAdditionalNimet().remove(nimiIndex);
        populateNimiContainer();
        
    }
    
    /**
     * The functionality for responding to user's change of selection
     * of whether finnish name applies to all organisations.
     * @param selected
     */
    private void toggleYksiNimi(boolean selected) {
        //If selected, all additional names are removed and components for adding other names are disabled
        if (selected) {
            if (!otNimi.getTextFi().getValue().toString().isEmpty()) {
                otNimi.getTextEn().setValue(otNimi.getTextFi().getValue());
                otNimi.getTextSv().setValue(otNimi.getTextFi().getValue());
            } else if (!otNimi.getTextSv().getValue().toString().isEmpty()) {
                otNimi.getTextEn().setValue(otNimi.getTextSv().getValue());
                otNimi.getTextFi().setValue(otNimi.getTextSv().getValue());
            } else if (!otNimi.getTextEn().getValue().toString().isEmpty()) {
                otNimi.getTextSv().setValue(otNimi.getTextEn().getValue());
                otNimi.getTextFi().setValue(otNimi.getTextEn().getValue());
            }
            populateNimiContainer();
        }
        otNimi.getTextEn().setWidth("300px");
        otNimi.getTextSv().setWidth("300px");
        otNimi.getTextFi().setWidth("300px");
    }
    
    /**
     * Populating the container to display additional names for hakutoimisto.
     * I.e. names not in finnish, swedish or english.
     */
    private void populateNimiContainer() {
        KoodistoHelper helper = new KoodistoHelper();
        additionalNimiContainer.removeAllComponents();
        additionalNimiContainer.setColumnExpandRatio(0, 0.5f);
        additionalNimiContainer.setColumnExpandRatio(1, 1.3f);
        additionalNimiContainer.setColumnExpandRatio(2, 0.2f);
        int nimetSize = this.model.getAdditionalNimet().size();
        additionalNimiContainer.setRows(nimetSize > 0 ? nimetSize : 1);
        int rowCounter = 0;
        for (KielikaannosModel curNimi : this.model.getAdditionalNimet()) {
            final int finalCounter = rowCounter;
            if (curNimi.getKielikoodi() != null) {
                additionalNimiContainer.addComponent(new Label(helper.tryGetArvoByKoodi(curNimi.getKielikoodi()) + ":"), 0, rowCounter);
            } 
            final Label curNimiLabel = new Label(curNimi.getArvo());
            curNimiLabel.addListener(this.changeListener);
            additionalNimiContainer.addComponent(curNimiLabel, 1, rowCounter);
            Button poistaB = UiUtil.button(null, I18N.getMessage("YhteystietojenTyyppiForm.poista"), new Button.ClickListener() {

                private static final long serialVersionUID = 7898077432801155877L;

                @Override
                public void buttonClick(ClickEvent event) {
                    curNimiLabel.setValue(curNimiLabel.getValue() + " remove");
                    removeNimi(finalCounter);
                }
            });
            additionalNimiContainer.addComponent(poistaB, 2, rowCounter);
            additionalNimiContainer.setComponentAlignment(poistaB, Alignment.MIDDLE_RIGHT);
            ++rowCounter;
        }
    }
    
    /**
     * Creation and opening of the dialog to add a localized name for the hakutoimisto.
     */
    private void createNimiDialog() {
        nimiDialogWindow = new Window();
        nimiDialogWindow.setWidth("420px");
        nimiDialogWindow.setModal(true);
        nimiDialogWindow.center();
        nimiDialogWindow.setCaption(T("lisaaHakutoimistoNimi"));
        nimiForm = new NimiDialog(new KielikaannosModel(null, null), new Button.ClickListener() {

            private static final long serialVersionUID = -4089487708330288101L;

            @Override
            public void buttonClick(ClickEvent event) {
                removeNimiDialog();
            }
        },
        new Button.ClickListener() {

            private static final long serialVersionUID = -2930327348146477031L;

            @Override
            public void buttonClick(ClickEvent event) {
                handleTallennaNimi();
                removeNimiDialog();
            }
        }, this.changeListener);
        nimiDialogWindow.setContent(nimiForm);
        getWindow().addWindow(nimiDialogWindow);
    }
    
    /**
     * saving a new localized name for hakutoimisto.
     * Populating the container to display the new name.
     */
    private void handleTallennaNimi() {
        this.model.getAdditionalNimet().add(nimiForm.getModel());
        populateNimiContainer();
    }
    
    /**
     * Closing the name dialog after cancel or save actions.
     */
    private void removeNimiDialog() {
        if (nimiDialogWindow != null) {
            getWindow().removeWindow(nimiDialogWindow);
            nimiDialogWindow = null;
        }
    }
    
    /**
     * Removing a free description tab based on the language uri given as parameter. 
     * 
     * @param uri the language for which the descrption for is removed.
     */
    private void handleTabRemoval(String uri) {
        this.kuvausForms.remove(uri);
        //this.kuvausFormViews.remove(uri);
        LOPTiedotModel curTiedotModel = this.model.getLopTiedot().get(uri);
        if (this.model.getLopTiedot().get(uri) != null) {
            clearRelevantKuvaukset(curTiedotModel, uri);
        }
        
    }
    
    /**
     * Removing the LOP description datas for the given language that correspond
     * to this view.
     * 
     * @param curTiedotModel The model that contains the descrption data
     * @param kieliUri the language for which removal is done.
     */
    private void clearRelevantKuvaukset(LOPTiedotModel curTiedotModel, String kieliUri) {
        curTiedotModel.clearKuvailevatTiedot();
        if (curTiedotModel.getTiedot().isEmpty()) {
            this.model.getLopTiedot().remove(kieliUri);
        }
    }
    
    /**
     * Creation of the rich text editing form for free descrptions.
     * 
     * @param uri the language uri for which the form is created
     * @param kieli the language name for which the form is created.
     * @return
     */
    private Form createRichTextEditor(String uri, String kieli) {
        LOPTiedotModel lopTiedotLang = null;
        //If data for this language exists, it is set as model for the created form.
        if (this.model.getLopTiedot().containsKey(uri)) {
            lopTiedotLang = this.model.getLopTiedot().get(uri);
        //Otherwise an empty model is created
        } else {
            lopTiedotLang = new LOPTiedotModel();
            this.model.getLopTiedot().put(uri, lopTiedotLang);
        }
        LOPKuvauksetFormView lopFormView = new LOPKuvauksetFormView(lopTiedotLang, kieli, this.changeListener);
        
        //Adding a listener to pass the save (or preview) button clicks to a parent container
        //i.e. OrganisaatioEditForm which handles saving of all forms.
        lopFormView.addListener(new Listener() {

            private static final long serialVersionUID = 5067713898530221136L;

            @Override
            public void componentEvent(Event event) {
                if (event instanceof KuvausEvent) {
                    propagateEvent((KuvausEvent)event);
                }
            }
            
        });
        //this.kuvausFormViews.put(uri, lopFormView);
        //Creation of the actual form.
        BeanItem<LOPTiedotModel> bItem = new BeanItem<LOPTiedotModel>(lopFormView.getModel());
        Form lopForm = new ValidatingViewBoundForm(lopFormView);
        lopForm.setItemDataSource(bItem);
        
        lopForm.setValidationVisible(false);
        lopForm.setValidationVisibleOnCommit(false);
        this.kuvausForms.put(uri, lopForm);
        return lopForm;
    }
    
    private String T(String key, Object... args) {
        return i18n.getMessage(key, args);
    }
    
    public OsoiteField getOtKayntiOsoite() {
        return otKayntiOsoite;
    }

    public OsoiteField getOtPostiOsoite() {
        return otPostiOsoite;
    }  
    
    public void setMainForm(OrganisaatioEditForm mainForm) {
        this.mainForm = mainForm;
    }
}

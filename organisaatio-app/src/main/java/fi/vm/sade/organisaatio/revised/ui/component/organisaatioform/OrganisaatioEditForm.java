
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
 * European Union Public Licence for more details.
 */
package fi.vm.sade.organisaatio.revised.ui.component.organisaatioform;


import static fi.vm.sade.generic.common.validation.ValidationConstants.EMAIL_PATTERN;
import static fi.vm.sade.generic.common.validation.ValidationConstants.WWW_PATTERN;
import static fi.vm.sade.organisaatio.KoodistoURI.KOODISTO_KIELI_URI;
import static fi.vm.sade.organisaatio.KoodistoURI.KOODISTO_KOTIPAIKKA_URI;
import static fi.vm.sade.organisaatio.KoodistoURI.KOODISTO_MAA_URI;
import static fi.vm.sade.organisaatio.KoodistoURI.KOODISTO_OPPILAITOSTYYPPI_URI;
import static fi.vm.sade.organisaatio.KoodistoURI.KOODISTO_VUOSILUOKAT_URI;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.vaadin.ui.*;
import fi.vm.sade.generic.ui.message.MessageContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.addon.formbinder.FormFieldMatch;
import org.vaadin.addon.formbinder.FormView;
import org.vaadin.addon.formbinder.PropertyId;
import org.vaadin.addon.formbinder.ViewBoundForm;

import com.github.wolfie.blackboard.Blackboard;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.Window.Notification;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.common.I18NHelper;
import fi.vm.sade.generic.ui.blackboard.BlackboardContext;
import fi.vm.sade.generic.ui.component.MultiLingualTextField;
import fi.vm.sade.generic.ui.validation.JSR303FieldValidator;
import fi.vm.sade.generic.ui.validation.ValidatingViewBoundForm;
import fi.vm.sade.koodisto.widget.KoodistoComponent;
import fi.vm.sade.koodisto.widget.WidgetFactory;
import fi.vm.sade.oid.service.OIDService;
import fi.vm.sade.organisaatio.KoodistoURI;
import fi.vm.sade.organisaatio.api.OrganisaatioValidationConstraints;
import fi.vm.sade.organisaatio.api.model.GenericFault;
import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.model.types.OsoiteDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietojenTyyppiDTO;
import fi.vm.sade.organisaatio.auth.OrganisaatioContext;
import fi.vm.sade.organisaatio.revised.ui.event.KuvausEvent;
import fi.vm.sade.organisaatio.revised.ui.event.OrganisaatioFormButtonEvent;
import fi.vm.sade.organisaatio.revised.ui.event.OrganisaatioViewButtonEvent;
import fi.vm.sade.organisaatio.revised.ui.helper.KoodistoHelper;
import fi.vm.sade.organisaatio.revised.ui.helper.KoodistoUriAndVersionFieldFormatter;
import fi.vm.sade.organisaatio.revised.ui.helper.KoodistoUriFieldFormatter;
import fi.vm.sade.organisaatio.revised.ui.helper.OidGenerator;
import fi.vm.sade.organisaatio.revised.ui.helper.ValidationHelper;
import fi.vm.sade.organisaatio.ui.MainWindow;
import fi.vm.sade.organisaatio.ui.PortletRole;
import fi.vm.sade.organisaatio.ui.component.ComponentBuilder;
import fi.vm.sade.organisaatio.ui.component.ConfirmationDialog;
import fi.vm.sade.organisaatio.ui.component.OsoiteField;
import fi.vm.sade.organisaatio.ui.listener.ConfirmationListener;
import fi.vm.sade.organisaatio.ui.listener.event.ConfirmationEvent;
import fi.vm.sade.organisaatio.ui.listener.event.MaaChangedEvent;
import fi.vm.sade.organisaatio.ui.model.OrganisaatioKuvailevatTiedotModel;
import fi.vm.sade.organisaatio.ui.model.OrganisaatioModel;
import fi.vm.sade.organisaatio.ui.organisaatio.YhteystietojenTyyppiEditor;
import fi.vm.sade.organisaatio.ui.util.UiUtils;
import fi.vm.sade.vaadin.Oph;
import fi.vm.sade.vaadin.util.UiUtil;

/**
 *
 * @author Tuomas Katva
 */
@SuppressWarnings("deprecation")
@FormView(matchFieldsBy = FormFieldMatch.ANNOTATION)
@Configurable(preConstruction = false)
public class OrganisaatioEditForm extends CustomComponent implements Property.ValueChangeListener {

    private static final long serialVersionUID = 1L;



    public static enum Tab {
        YLEISTIEDOT,
        KOULUTUSTARJOAJATIEDOT,
        PALVELUT_OPPIJALLE
    }

    private final Logger log = LoggerFactory.getLogger(getClass());

    //Form model
    private OrganisaatioModel model;
    //Layouts
    private VerticalLayout topButtonLayout;
    //The rootlayout
    private FormLayout organisaationPaaTiedotLayout;
    private FormLayout organisaatioTyyppiTiedotLayout;
    private FormLayout organisaatioTyyppiTiedotBottomLayout;
    private HorizontalLayout bottonButtonLayout;
    //Top button layout buttons
    private Label ytjMessageLabel;
    private Button peruutaButton;
    private Button tallennaButton;
    private Button jatkaButton;
    //Bottom buttons
    private Button bottomPeruutaButton;
    private Button bottomTallennaButton;
    private Button bottomJatkaButton;
    //Form koodisto fields
    @PropertyId("organisaatioKielet")
    private KoodistoComponent koodistoKieli;
    private KoodistoComponent koodistoMaa;
    @PropertyId("kotipaikka")
    private KoodistoComponent koodistoKotipaikka;
    @PropertyId("oppilaitostyyppi")
    private KoodistoComponent koodistoOppilaitostyyppi;
    
    private VerticalLayout nameLayout;
    private Label nameLabel;
    private Label koRuotsiLbl;
    private Label poRuotsiLbl;
    private Label koSuomiLbl;
    private Label poSuomiLbl;

    private OptionGroup osoiteOption;
    @Autowired
    private KoodistoURI koodistoURI;
    //Form fields
    @PropertyId("mlNimi")
    private MultiLingualTextField mlNimi;
    @PropertyId("alkuPvm")
    private DateField voimassaoloAlkaa;
    @PropertyId("lakkautusPvm")
    private DateField voimassaoloLoppuu;
    @PropertyId("postiosoite")
    private OsoiteField postiOsoite;
    @PropertyId("kayntiosoite")
    private OsoiteField kayntiOsoite;

    @Valid
    @NotNull
    private Label kayntiOsoiteLbl;

    @PropertyId("ruotsiKayntiOsoite")
    private OsoiteField ruotsiKayntiOsoite;

    @PropertyId("ruotsiPostiOsoite")
    private OsoiteField ruotsiPostiOsoite;

    @NotNull(message = "{validation.Organisaatio.puhelinNull}")
    @Pattern(regexp = "(\\+|\\-| |\\(|\\)|[0-9]){3,100}", message = "{validation.invalid.phone}")
    @PropertyId("puhelinnumero")
    private TextField puhelin;
    @Pattern(regexp = "(\\+|\\-| |\\(|\\)|[0-9]){3,100}", message = "{validation.invalid.fax}")
    @PropertyId("faksinumero")
    private TextField faksi;
    @NotNull(message = "{validation.Organisaatio.emailNull}")
    @Pattern(regexp = EMAIL_PATTERN, message = "{validation.invalid.email}")
    @PropertyId("emailOsoite")
    private TextField email;
    @Pattern(regexp = WWW_PATTERN, message = "{validation.invalid.www}")
    @PropertyId("wwwOsoite")
    private TextField www;
    //Fields in organisaatioTyyppiTiedotLayout
    @NotNull(message = "{validation.Organisaatio.orgtyyppiNull}")
    @Size(min = 1, message = "{validation.Organisaatio.tyypit.min}")
    @PropertyId("organisaatiotyypit")
    private OrganisaatiotyyppiComponent organisaatiotyyppi;
    @Pattern(regexp = "[0-9]{5}", message = "{validation.Organisaatio.oppilaitosKoodi}")
    @PropertyId("oppilaitosKoodi")
    private TextField oppilaitosKoodi;
    
    @Pattern(regexp=OrganisaatioValidationConstraints.YTUNNUS_PATTERN, message="{validation.Organisaatio.ytunnus}")
    @PropertyId("ytunnus")
    private TextField yTunnus;

    @Pattern(regexp=OrganisaatioValidationConstraints.VIRASTOTUNNUS_PATTERN, message="{validation.Organisaatio.virastotunnus}")
    @PropertyId("virastoTunnus")
    private TextField virastoTunnus;

    @PropertyId("vuosiluokat")
    private KoodistoComponent koodistoVuosiluokat;
    private YhteystietojenTyyppiEditor lisatiedotEditor;
    private MuutOsoitteetComponent otherAddressesLayout;
    private Label serverMessage = new Label("");
    private Form form;
    private BeanItem<OrganisaatioModel> beanItem;
    @Autowired
    private OrganisaatioService organisaatioService;
    @Autowired
    private OIDService oidService;
    private KoodistoHelper koodistoHelper;
    
    private boolean organisaatioEdited = false;
    private boolean isParentOrg;
    private String ophOid = "1.2.246.562.10.00000000001";
    
    private MessageContainer topMessageContainer;
    private MessageContainer bottomMessageContainer;
    
    private CheckBox sameAddress;
    
    private Property.ValueChangeListener changeListener = new Property.ValueChangeListener() {
        @Override
        public void valueChange(ValueChangeEvent event) {
            if (sameAddress != null && sameAddress.booleanValue()) {
            	postiOsoite.getOsoite().setValue(kayntiOsoite.getOsoite().getValue());
            	postiOsoite.getPostinumero().setValue(kayntiOsoite.getPostinumero().getValue());
                ruotsiPostiOsoite.getOsoite().setValue(ruotsiKayntiOsoite.getOsoite().getValue());
                ruotsiPostiOsoite.getPostinumero().setValue(ruotsiKayntiOsoite.getPostinumero().getValue());
            }
            organisaatioEdited = true;
        }
    };

    private I18NHelper i18nHelper = new I18NHelper(OrganisaatioEditForm.class);
    private String T(String key) {
        return i18nHelper.getMessage(key);
    }

    private OrganisaatioKuvailevatTiedotFormView organisaatioKuvailevatTiedot;

    private OrganisaatioPalvelutOppijalleForm palvelutOppijalleForm;

    private Form kuvailevatForm;

    private TabSheet organisaatioSheet;
    private Component selectedTab;

    private Date originalLakkautusPvm;

    private boolean canEditDates = true;
    private boolean perustiedotInvalid = false;

    private HorizontalLayout parentHL;


    public OrganisaatioEditForm() {
        this(new OrganisaatioDTO(), Tab.YLEISTIEDOT);
    }

    public OrganisaatioEditForm(OrganisaatioDTO organisaatio, Tab initial) {
        Preconditions.checkNotNull(organisaatio, "organisaatio cannot be null");
        
        this.model = new OrganisaatioModel(organisaatio);
        originalLakkautusPvm = organisaatio.getLakkautusPvm();
        otherAddressesLayout = new MuutOsoitteetComponent(this.model);

        setCompositionRoot(buildMainLayout(initial));
        setIsParentOrg();
        setHeight(-1, UNITS_PIXELS);
        koodistoHelper = new KoodistoHelper();
    }

    public OrganisaatioEditForm(OrganisaatioDTO organisaatio, String errorMsg) {
        this(organisaatio, Tab.YLEISTIEDOT);
        String msg = I18N.getMessage(errorMsg);
        topMessageContainer.addErrorMessage(msg);
        bottomMessageContainer.addErrorMessage(msg);
    }

    @Override
    public void attach() {
        super.attach();
        koodistoHelper.filterOutOldKoodit(koodistoKotipaikka, KOODISTO_KOTIPAIKKA_URI);
        if (perustiedotInvalid) {
            showTabChangeDialog();
            perustiedotInvalid = false;
        }
    }

    
    private void setButtonsEnabled(boolean enabled) {
    	peruutaButton.setEnabled(enabled);
    	tallennaButton.setEnabled(enabled);
    	jatkaButton.setEnabled(enabled);
    }

    private void initForm() {
        this.beanItem = new BeanItem<OrganisaatioModel>(model);
        // luodaan form

        form = new ValidatingViewBoundForm(this);
        form.setItemDataSource(beanItem);
        form.getFooter().addComponent(serverMessage);
        // add jsr-303 annotation based validators
        JSR303FieldValidator.addValidatorsBasedOnAnnotations(this);
        this.form.setValidationVisible(false);
        this.form.setValidationVisibleOnCommit(false);
        this.otherAddressesLayout.setForm(form);

        setAddressOptionGroup();

        osoiteOption.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent valueChangeEvent) {
            if ( ((String)valueChangeEvent.getProperty().getValue()).equalsIgnoreCase(I18N.getMessage("OrganisaatioEditForm.suomalainenOsoite")))  {
                reCreateDomesticAddresses();
            } else if ( ((String)valueChangeEvent.getProperty().getValue()).equalsIgnoreCase(I18N.getMessage("OrganisaatioEditForm.kansainvalinenOsoite"))) {
                reCreateForeignAddressed(true);
            }
            }
        });

        constructPassivationConfirmationDialog();
        setDatesEditableByPermission();
        setNameEditableByPermission();
        
        if (model.getOrganisaatio().getYtjPaivitysPvm()!=null || model.getOrganisaatio().getTuontiPvm()!=null) {
            yTunnus.setEnabled(false);
            deEnableNimi();
            deEnableOsoitteet();
        } else {
            yTunnus.setEnabled(true);
        }
    }

    private void setNameEditableByPermission() {
        boolean canEditName = PortletRole.getInstance().getPermissionService().userCanEditName(OrganisaatioContext.get(model.getOrganisaatio()));
        if(!canEditName) {
            mlNimi.getTextFi().setEnabled(false);
            mlNimi.getTextEn().setEnabled(false);
            mlNimi.getTextSv().setEnabled(false);
        }        
    }

    private void setDatesEditableByPermission() {
        if (model.getOrganisaatio().getOid() != null) {
            //editing??
            canEditDates = PortletRole.getInstance()
                    .getPermissionService()
                    .userCanEditDates(OrganisaatioContext.get(model.getOrganisaatio()));
        }

        voimassaoloAlkaa.setEnabled(canEditDates);
        voimassaoloLoppuu.setEnabled(canEditDates);
    }

    private void setAddressOptionGroup() {
        if (model != null && osoiteOption != null) {
            if (osoiteOption.getValue() == null) {
                if (isDomesticAddress()) {
                    osoiteOption.select(I18N.getMessage("OrganisaatioEditForm.suomalainenOsoite"));
                } else {
                    osoiteOption.select(I18N.getMessage("OrganisaatioEditForm.kansainvalinenOsoite"));
                }
            }
        }
    }
    
    private VerticalLayout buildNameLayout() {
    	nameLayout = new VerticalLayout();
    	nameLayout.setImmediate(false);
    	nameLayout.setMargin(false);
        
    	nameLabel = new Label();
        nameLabel.addStyleName(Oph.LABEL_H1);
        nameLabel.addStyleName(Oph.SPACING_BOTTOM_30);
        nameLayout.addComponent(nameLabel);

        
        nameLayout.addStyleName(Oph.SPACING_TOP_20);
        nameLayout.addStyleName(Oph.SPACING_RIGHT_20);
        nameLayout.addStyleName(Oph.SPACING_LEFT_20);

        return nameLayout;
    }
    
    private VerticalLayout buildMainLayout(Tab initial) {
        VerticalLayout vl = new VerticalLayout();
        vl.setMargin(false, false, true, false);
        vl.setSizeFull();

        organisaatioSheet = new TabSheet();
        organisaatioSheet.addListener(new TabSheet.SelectedTabChangeListener() {

            private static final long serialVersionUID = -8170879319097765154L;

            @Override
            public void selectedTabChange(SelectedTabChangeEvent event) {
                if (organisaatioSheet.getSelectedTab() != selectedTab && organisaatioEdited && getWindow() != null) {
                    getWindow().showNotification(T("noSave"), Notification.TYPE_WARNING_MESSAGE);
                    organisaatioSheet.setSelectedTab(selectedTab);
                } else {
                    selectedTab = organisaatioSheet.getSelectedTab();
                }
                
            }
        });

        //TOP BUTTONS
        vl.addComponent(buildTopButtonLayout());

        CssLayout topMessageWrapper = new CssLayout();
        topMessageWrapper.setSizeFull();
        topMessageContainer = new MessageContainer();
        topMessageWrapper.setMargin(false, false, true, false);
        topMessageWrapper.addComponent(topMessageContainer);
        vl.addComponent(topMessageWrapper);
        
        vl.addComponent(buildNameLayout());

        boolean isNew = (model.getOrganisaatio().getOid() == null);
        if (isNew) {
            nameLabel.setValue(T("uusiOrganisaatioLbl"));
        } else {
            nameLabel.setValue(model.getOrganisaatio().getNimi().getTeksti().get(0).getValue());
        }

        parentHL = UiUtil.horizontalLayout();
        parentHL.setMargin(false, false, true, false);

        vl.addComponent(parentHL);

        VerticalLayout perustietoLayout = new VerticalLayout();
        perustietoLayout.setMargin(false, false, true, false);
        perustietoLayout.setSizeFull();


        //SPLIT LINE

        perustietoLayout.addComponent(buildSplitPanel());

        //MIDDLE FORMS 1/2
        HorizontalLayout hlFormsPart1 = new HorizontalLayout();
        hlFormsPart1.setWidth("100%");
        //Organisaation paatiedot components are added later becaus
        //Spring dependent koodist components must be wired
        organisaationPaaTiedotLayout = new FormLayout();
        organisaationPaaTiedotLayout.setWidth("600px");
        hlFormsPart1.addComponent(organisaationPaaTiedotLayout);
        hlFormsPart1.setExpandRatio(organisaationPaaTiedotLayout, 0.7f);
        hlFormsPart1.setComponentAlignment(organisaationPaaTiedotLayout, Alignment.TOP_CENTER);
        perustietoLayout.addComponent(hlFormsPart1);

        //SPLIT LINE
        perustietoLayout.addComponent(buildSplitPanel());

        //MIDDLE FORMS 2/2
        VerticalLayout vlFormsPart2 = new VerticalLayout();
        vlFormsPart2.setWidth("100%");
        //the organisaatiotyyppi components are added later because
        //koodisto components must we wired first
        organisaatioTyyppiTiedotLayout = new FormLayout();
        organisaatioTyyppiTiedotLayout.setWidth("600px");

        vlFormsPart2.addComponent(organisaatioTyyppiTiedotLayout);
        vlFormsPart2.setExpandRatio(organisaatioTyyppiTiedotLayout, 0.7f);
        vlFormsPart2.setComponentAlignment(organisaatioTyyppiTiedotLayout, Alignment.TOP_CENTER);
        
        organisaatioTyyppiTiedotBottomLayout = new FormLayout();
        organisaatioTyyppiTiedotBottomLayout.setWidth("600px");
        vlFormsPart2.addComponent(organisaatioTyyppiTiedotBottomLayout);
        vlFormsPart2.setExpandRatio(organisaatioTyyppiTiedotBottomLayout, 0.7f);
        vlFormsPart2.setComponentAlignment(organisaatioTyyppiTiedotBottomLayout, Alignment.TOP_CENTER);
        
        perustietoLayout.addComponent(vlFormsPart2);
        //SPLIT LINE
        perustietoLayout.addComponent(buildSplitPanel());

        organisaatioSheet.addTab(perustietoLayout, T("organisaationTiedotLbl"));
        selectedTab = perustietoLayout;
        organisaatioKuvailevatTiedot = new OrganisaatioKuvailevatTiedotFormView(model.getKuvailevatTiedot(), model.getParentOid(), this.changeListener);
        organisaatioKuvailevatTiedot.addListener(new Listener() {

            private static final long serialVersionUID = -3487289408130074328L;

            @Override
            public void componentEvent(Event event) {
                if (event instanceof KuvausEvent
                        && ((KuvausEvent)event).getType().equals(KuvausEvent.SAVE)) {
                    saveOrganisaatio();
                }

            }

        });
        
        organisaatioKuvailevatTiedot.setMainForm(this);

        BeanItem<OrganisaatioKuvailevatTiedotModel> beanI = new BeanItem<OrganisaatioKuvailevatTiedotModel>(organisaatioKuvailevatTiedot.getModel());
        // luodaan form
        kuvailevatForm = new ValidatingViewBoundForm(organisaatioKuvailevatTiedot);
        kuvailevatForm.setItemDataSource(beanI);
        kuvailevatForm.setSizeFull();
        kuvailevatForm.setValidationVisible(false);
        kuvailevatForm.setValidationVisibleOnCommit(false);

        organisaatioSheet.addTab(kuvailevatForm, T("organisaationKuvailuLbl"));
        palvelutOppijalleForm = new OrganisaatioPalvelutOppijalleForm(model.getKuvailevatTiedot(), this.changeListener);

        palvelutOppijalleForm.addListener(new Listener() {

            private static final long serialVersionUID = -6965862056585039923L;

            @Override
            public void componentEvent(Event event) {
                if (event instanceof KuvausEvent
                        && ((KuvausEvent)event).getType().equals(KuvausEvent.SAVE)) {
                    saveOrganisaatio();
                }

            }

        });

        palvelutOppijalleForm.setMainForm(this);
        
        organisaatioSheet.addTab(palvelutOppijalleForm, T("palvelutOppijalleLbl"));
        if (this.model.getOid() == null) {
            organisaatioSheet.getTab(kuvailevatForm).setEnabled(false);
            organisaatioSheet.getTab(palvelutOppijalleForm).setEnabled(false);
        }

        vl.addComponent(organisaatioSheet);

        CssLayout bottomMessageWrapper = new CssLayout();
        bottomMessageWrapper.setSizeFull();
        bottomMessageContainer = new MessageContainer();
        bottomMessageWrapper.setMargin(true, false, false, false);
        bottomMessageWrapper.addComponent(bottomMessageContainer);
        vl.addComponent(bottomMessageWrapper);

        //BOTTOM BUTTONS
        vl.addComponent(buildBottomButtonLayout());

        switch (initial) {
            case YLEISTIEDOT:
                organisaatioSheet.setSelectedTab(perustietoLayout);
                break;
            case KOULUTUSTARJOAJATIEDOT:
                organisaatioSheet.setSelectedTab(kuvailevatForm);
                selectedTab = kuvailevatForm;
                break;
            case PALVELUT_OPPIJALLE:
                organisaatioSheet.setSelectedTab(palvelutOppijalleForm);
                selectedTab = palvelutOppijalleForm;
                break;
        }

        return vl;
    }

    private void setIsParentOrg() {
        String parentOid = (model != null && model.getOrganisaatio() != null) ? model.getOrganisaatio().getParentOid() : null;
        isParentOrg = (parentOid == null || ophOid.equals(parentOid));
    }

    private VerticalSplitPanel buildSplitPanel() {
        VerticalSplitPanel splitPanel = new VerticalSplitPanel();
        splitPanel.setWidth("100%");
        splitPanel.setHeight("2px");
        splitPanel.setLocked(true);
        return splitPanel;
    }

    public void showYtjLabel(boolean showLabel) {
        if (ytjMessageLabel != null) {
            if (showLabel) {
                String nimi = "";
                if (model.getMlNimiFi() != null && model.getMlNimiFi().trim().length() > 0) {
                    nimi = model.getMlNimiFi();
                } else if (model.getMlNimiSv() != null && model.getMlNimiSv().trim().length() > 0 ) {
                    nimi = model.getMlNimiSv();
                } else if (model.getMlNimiEn() != null && model.getMlNimiEn().trim().length() > 0) {
                    nimi = model.getMlNimiEn();
                }
                ytjMessageLabel.setValue(T("ytjMessageLabel") + " " + model.getOrganisaatio().getYtunnus() + ", " + nimi);
                deEnableOsoitteet();
                deEnableNimi();
            } else {
                ytjMessageLabel.setValue("");
            }
        }
    }

    private void deEnableNimi() {
    	
        if (model.getOrganisaatio().getYtunnus() != null || model.getOrganisaatio().getTuontiPvm() != null) {
            if (model.getMlNimiFi() != null ) {
                mlNimi.getTextFi().setEnabled(false);
            }
            if (model.getMlNimiEn() != null) {
                mlNimi.getTextEn().setEnabled(false);
            }
            if (model.getMlNimiSv() != null) {
                mlNimi.getTextSv().setEnabled(false);
            }
        }
    }

    public void valueChange(ValueChangeEvent event) {
        if (event.getProperty().getValue() != null
                && !Objects.equal(event.getProperty().getValue(), originalLakkautusPvm)) {
        	setButtonsEnabled(false);
            final Window main = getWindow();
            final Window confirmationDialogWindow = new Window();

            confirmationDialogWindow.setClosable(false);

            confirmationDialogWindow.center();
            main.addWindow(confirmationDialogWindow);
            ConfirmationDialog confirmationDialog = new ConfirmationDialog(T("passivointiMsg"));

            confirmationDialogWindow.setContent(confirmationDialog);
            confirmationDialog.setImmediate(true);
            confirmationDialogWindow.setModal(true);
            confirmationDialog.setSizeUndefined();

            confirmationDialog.addListener(new ConfirmationListener() {
                @Override
                public void handleConfirmation(ConfirmationEvent confirmationEvent) {
                    if (confirmationEvent.getConfirmation()) {
                        originalLakkautusPvm = (Date) voimassaoloLoppuu.getValue();
                    } else {
                        voimassaoloLoppuu.setValue(originalLakkautusPvm);
                    }
                    
                    //OVT-2168, bugin mukaan lomaketta ei tallenneta passivoidessa / saveOrganisaatio();
                    
                    main.removeWindow(confirmationDialogWindow);
                    setButtonsEnabled(true);
                    
                }
            });
        }

}

    private void constructPassivationConfirmationDialog() {
        voimassaoloLoppuu.addListener(this);
    }

    private void deEnableOsoitteet() {
        OsoiteDTO kayntiOsoiteDto = (OsoiteDTO)kayntiOsoite.getValue();


        if (kayntiOsoiteDto.getOsoite() != null && kayntiOsoiteDto.getOsoite().trim().length() > 0 && kayntiOsoiteDto.getYtjPaivitysPvm() != null) {
            kayntiOsoite.enableOrDeEnableOsoiteFields(false);

        }
        OsoiteDTO postiOsoiteDto = (OsoiteDTO)postiOsoite.getValue();
        if (postiOsoiteDto.getOsoite() != null && postiOsoiteDto.getOsoite().trim().length() > 0 && postiOsoiteDto.getYtjPaivitysPvm() != null) {
            postiOsoite.enableOrDeEnableOsoiteFields(false);


        }
    }

    private HorizontalLayout buildBottomButtonLayout() {
        bottonButtonLayout = new HorizontalLayout();
        bottonButtonLayout.setMargin(true, false, false, true);
        bottomPeruutaButton = UiUtils.button(bottonButtonLayout, "", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                BlackboardContext.getBlackboard().fire(new OrganisaatioFormButtonEvent(model.getOrganisaatio(), OrganisaatioFormButtonEvent.PERUUTA));
            }
        });
        bottomPeruutaButton.addStyleName(Oph.BUTTON_BACK);
        
        bottomTallennaButton = UiUtils.buttonSmallPrimary(bottonButtonLayout, T("tallennaButton"), new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                saveOrganisaatio();
            }
        });

        bottomJatkaButton = UiUtils.buttonSmallPrimary(bottonButtonLayout, T("jatkaButton"), new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                jatkaButtonClicked(event);

            }
        });

        bottomJatkaButton.setEnabled(model.getOid() != null);
        return bottonButtonLayout;
    }

    private VerticalLayout buildTopButtonLayout() {
        topButtonLayout = new VerticalLayout();
        topButtonLayout.setWidth(100, UNITS_PERCENTAGE);

        ytjMessageLabel = new Label("");

        topButtonLayout.addComponent(ytjMessageLabel);

        HorizontalLayout btns = new HorizontalLayout();

        btns.setMargin(true, false, true, true);

        peruutaButton = UiUtils.button(btns, "", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                BlackboardContext.getBlackboard().fire(new OrganisaatioFormButtonEvent(model.getOrganisaatio(), OrganisaatioFormButtonEvent.PERUUTA));
            }
        });
        peruutaButton.addStyleName(Oph.BUTTON_BACK);

        tallennaButton = UiUtils.buttonSmallPrimary(btns, T("tallennaButton"), new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
           		saveOrganisaatio();
            }
        });

        jatkaButton = UiUtils.buttonSmallPrimary(btns, T("jatkaButton"), new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                jatkaButtonClicked(event);
            }
        });
        jatkaButton.setEnabled(model.getOid() != null);

        topButtonLayout.addComponent(btns);

        form = new ViewBoundForm(this);

        return topButtonLayout;
    }

    private void jatkaButtonClicked(ClickEvent event) {    	
        if (!organisaatioEdited && model.getOrganisaatio() != null && model.getOrganisaatio().getOid() != null) {
            BlackboardContext.getBlackboard().fire(new OrganisaatioFormButtonEvent(model.getOrganisaatio(), OrganisaatioFormButtonEvent.JATKA));
        } else {
            final Window dialog = new Window(I18N.getMessage("c_tallennatko"));
            dialog.setModal(true);
            dialog.setWidth("430px");
            dialog.setClosable(false);

            setButtonsEnabled(false);
            this.getApplication().getMainWindow().addWindow(dialog);
            VerticalLayout vl = new VerticalLayout();
            Label kysymys = new Label(I18N.getMessage("c_tallennaKysymys"));
            vl.addComponent(kysymys);
            HorizontalLayout buttonL = new HorizontalLayout();
            UiUtils.buttonSmallSecodary(buttonL, I18N.getMessage("c_jatkaTallentamatta"), new Button.ClickListener() {
                @Override
                public void buttonClick(ClickEvent event) {
                    closeDialog(dialog);                    
                    BlackboardContext.getBlackboard().fire(new OrganisaatioFormButtonEvent(organisaatioService.findByOid(model.getOrganisaatio().getOid()), OrganisaatioFormButtonEvent.JATKA));
                }
            });

            UiUtils.buttonSmallSecodary(buttonL, I18N.getMessage("c_peruuta"), new Button.ClickListener() {
                @Override
                public void buttonClick(ClickEvent event) {
                    closeDialog(dialog);
                }
            });

            Button tallennaB = UiUtils.buttonSmallSecodary(buttonL, I18N.getMessage("c_tallennaJaJatka"), new Button.ClickListener() {
                @Override
                public void buttonClick(ClickEvent event) {
                    saveOrganisaatio();
                    closeDialog(dialog);
                    if (!topMessageContainer.hasErrorMessages()) {
                        BlackboardContext.getBlackboard().fire(new OrganisaatioFormButtonEvent(model.getOrganisaatio(), OrganisaatioFormButtonEvent.JATKA));
                    }
                }
            });
            tallennaB.focus();

            vl.addComponent(buttonL);
            dialog.addComponent(vl);
        }
    }

    private void closeDialog(Window dialog) {
        this.getWindow().removeWindow(dialog);
        setButtonsEnabled(true);
    }



    private boolean doValidation() throws Exception {
        try {
            topMessageContainer.resetMessages();
            bottomMessageContainer.resetMessages();
            if (!booleanCheckDates()) {
                String msg = T("dateValidationFailed");
                topMessageContainer.addErrorMessage(msg);
                bottomMessageContainer.addErrorMessage(msg);
                return false;
            }

            if (voimassaoloAlkaa.getValue() == null) {
                String msg = T("alkupvmNull");
                topMessageContainer.addErrorMessage(msg);
                bottomMessageContainer.addErrorMessage(msg);
                return false;
            }

            if (koodistoKotipaikka.getValue() == null) {
                String msg = T("kotipaikkaNull");
                topMessageContainer.addErrorMessage(msg);
                bottomMessageContainer.addErrorMessage(msg);
                return false;
            }

            //Osoitteet ja lisatiedot pitaa validoida kasin, eivat validoidu formin commitissa!!
            ValidationHelper validator = new ValidationHelper(topMessageContainer.getError(),
                    kayntiOsoite,
                    postiOsoite,
                    lisatiedotEditor,
                    otherAddressesLayout,
                    organisaatioKuvailevatTiedot.getOtKayntiOsoite(),
                    organisaatioKuvailevatTiedot.getOtPostiOsoite(),
                    model.getMlNimi(),
                    ruotsiKayntiOsoite,
                    ruotsiPostiOsoite
            );
            boolean areExtraValid = validator.validateExtraFields(model.getOrganisaatio());
            commitForms();
            if (!areExtraValid) {
                // TODO virhe jää piiloon
                throw new Validator.InvalidValueException("");
            }
        } catch (Validator.InvalidValueException e) {
            log.warn("invalid value value, debugId: " + e.getDebugId() + ", exception: " + e + ", causes: " + Arrays.asList(e.getCauses()), e);
            topMessageContainer.addErrorMessage(e);
            bottomMessageContainer.addErrorMessage(e);
            return false;
        }
        return true;
    }

    /**
     * Organisaatio save method!
     */
    private void saveOrganisaatio() {
        boolean isNew = (model.getOrganisaatio().getOid() == null);
        try {
            if (!doValidation()) {
                return;
            }

            checkWwwOsoite();
            model.convertToOrganisaatio();
            

            OrganisaatioDTO org;
            generateOids();
            if (isNew) {
                org = organisaatioService.createOrganisaatio(model.getOrganisaatio(), false);
            } else {
                org = organisaatioService.updateOrganisaatio(model.getOrganisaatio(), false);
            }
            if (model.getOrganisaatio().getLakkautusPvm() == null && org.getLakkautusPvm() != null) {
                voimassaoloLoppuu.removeListener(this);
                voimassaoloLoppuu.setValue(org.getLakkautusPvm());
                constructPassivationConfirmationDialog();
                requestRepaint();
            }

            String saveSuccesfulMessage = createSaveSuccesfulMessage();
            topMessageContainer.addConfirmationMessage(saveSuccesfulMessage);
            bottomMessageContainer.addConfirmationMessage(saveSuccesfulMessage);
            serverMessage.setValue(saveSuccesfulMessage);
            nameLabel.setValue(model.getOrganisaatio().getNimi().getTeksti().get(0).getValue());
            organisaatioEdited = false;
            jatkaButton.setEnabled(true);
            bottomJatkaButton.setEnabled(true);
            organisaatioSheet.getTab(kuvailevatForm).setEnabled(true);
            organisaatioSheet.getTab(palvelutOppijalleForm).setEnabled(true);
            model.setOrganisaatio(org);
            setDatesEditableByPermission();
        } catch (Validator.InvalidValueException e) {
            log.warn("invalid value value, debugId: " + e.getDebugId() + ", exception: " + e + ", causes: " + Arrays.asList(e.getCauses()), e);
            if (isNew) {
                model.getOrganisaatio().setOid(null);
            }
            topMessageContainer.addErrorMessage(e);
            bottomMessageContainer.addErrorMessage(e);
        } catch (Exception e) {
            log.error("encountered an exception when saving form: " + e, e);

            if (e instanceof GenericFault) {
                GenericFault gf = ((GenericFault) e);
                if (gf.getLocalizedMessage().contains("LearningInstitutionExistsException") || gf.getLocalizedMessage().contains("organisaatio_oppilaitoskoodi_key") || gf.getFaultInfo().getErrorCode().equals("oppilaitos.exists.with.code")) {
                    String msg = I18N.getMessage("OrganisaatioEditForm.oppilaitos.exists.with.code");
                    topMessageContainer.addErrorMessage(msg);
                    bottomMessageContainer.addErrorMessage(msg);
                } else {
                    String msg = I18N.getMessage(gf.getFaultInfo().getErrorCode());
                    topMessageContainer.addErrorMessage(msg);
                    bottomMessageContainer.addErrorMessage(msg);
                }
                if (gf.getFaultInfo().getErrorCode().contains("javax.persistence.OptimisticLockException")) {
                    OrganisaatioDTO organisaatioDto = organisaatioService.findByOid(model.getOid());
                    OrganisaatioViewButtonEvent ovb = new OrganisaatioViewButtonEvent(organisaatioDto, OrganisaatioViewButtonEvent.FORM_REFRESH);
                    ovb.setFormExceptionName(gf.getFaultInfo().getErrorCode());
                    BlackboardContext.getBlackboard().fire(ovb);
                }
            } else {
                String msg = I18N.getMessage(e.getMessage());
                topMessageContainer.addErrorMessage(msg);
                bottomMessageContainer.addErrorMessage(msg);
            }
            if (isNew) {
                model.getOrganisaatio().setOid(null);
            }
        }
    }

    private String createSaveSuccesfulMessage() {

        SimpleDateFormat sdf = new SimpleDateFormat("d.M.yyyy 'klo' H:mm");
        String dateTime = sdf.format(new Date());

        return I18N.getMessage("c_saveSuccesful", dateTime);
    }
    
    private void generateOids() throws Exception {
        OidGenerator.generateOids(model.getOrganisaatio(), this.oidService);
        generateOpintotoimistoOids();

    }

    private void generateOpintotoimistoOids() throws Exception {
        if (model.getOrganisaatio().getKuvailevatTiedot() != null
                && model.getOrganisaatio().getKuvailevatTiedot().getHakutoimisto() != null) {
            for (YhteystietoDTO curYt : model.getOrganisaatio().getKuvailevatTiedot().getHakutoimisto().getOpintotoimistoYhteystiedot()) {
                OidGenerator.generateOid(curYt, this.oidService);
            }
        }
    }

    /**
     * Performing the commit for the forms used in editing organisaatio data.
     * Validation errors for each form are catched separately and added to the error view.
     * @throws Exception
     */
    private void commitForms() throws Exception {
        List<Validator.InvalidValueException> errors = new ArrayList<Validator.InvalidValueException>();
        try {
            form.commit();
        } catch (Validator.InvalidValueException ex) {
            errors.add(ex);
            errors.addAll(Arrays.asList(ex.getCauses()));
        }
        try {
            kuvailevatForm.commit();
        } catch (Validator.InvalidValueException ex) {
            errors.add(ex);
            errors.addAll(Arrays.asList(ex.getCauses()));
        }

        for (Validator.InvalidValueException curEx : organisaatioKuvailevatTiedot.commitForm()) {
            errors.add(curEx);
            errors.addAll(Arrays.asList(curEx.getCauses()));
        }

        for (Validator.InvalidValueException curEx : palvelutOppijalleForm.commitForm()) {
            this.topMessageContainer.addErrorMessage(curEx);
            this.bottomMessageContainer.addErrorMessage(curEx);
            errors.add(curEx);
            errors.addAll(Arrays.asList(curEx.getCauses()));
        }
        
        //After the errors are added to the error view an empty validation error is thrown to
        //stop the saving of the forms.
        if (!errors.isEmpty()) {
            throw new Validator.InvalidValueException("Validation failed", errors.toArray(new Validator.InvalidValueException[errors.size()]));
        }
    }

    private void checkWwwOsoite() {
        if (model.getWwwOsoite() == null || model.getWwwOsoite().isEmpty()) {
            return;
        }
        if (urlStartsWithScheme(model.getWwwOsoite()) == false) {
            model.setWwwOsoite("http://" + model.getWwwOsoite());
        }
    }

    private boolean urlStartsWithScheme(String givenUrl) {
        if (givenUrl == null || givenUrl.isEmpty()) {
            return false;
        }
        if (givenUrl.startsWith("http://")
                || givenUrl.startsWith("https://")
                || givenUrl.startsWith("ftp://")
                || givenUrl.startsWith("file://")) {
            return true;
        }
        return false;
    }

    private boolean booleanCheckDates() {
        Date fromDate = voimassaoloAlkaa.getValue() != null ? (Date) voimassaoloAlkaa.getValue() : null;
        Date toDate = voimassaoloLoppuu.getValue() != null ? (Date) voimassaoloLoppuu.getValue() : null;
        return !(fromDate != null && toDate != null) || toDate.after(fromDate);
    }

    private FormLayout buildOrganisaationPaaTiedotLayout() {
        organisaationPaaTiedotLayout.removeAllComponents();

        boolean isNew = (model.getOrganisaatio().getOid() == null);
        if (isNew && model.getOrganisaatio() != null && model.getOrganisaatio().getParentOid() != null) {
            OrganisaatioDTO parentDTO = organisaatioService.findByOid(model.getOrganisaatio().getParentOid());
            if (parentDTO != null) {
                showParentsName(parentDTO,T("parentorganization"));
            }
        }

        setMlNimi(new MultiLingualTextField(I18N.getLocale()));
        getMlNimi().setCaption(T("nimi"));
        getMlNimi().addStyleName("requiredField");
        organisaationPaaTiedotLayout.addComponent(getMlNimi());

        createOhjeteksti(T("nimiOhje"));

        virastoTunnus = createTextField("OrganisaatioEditForm.virastoTunnus");
        organisaationPaaTiedotLayout.addComponent(virastoTunnus);
        
        yTunnus = createTextField("OrganisaatioEditForm.ytunnus");
        organisaationPaaTiedotLayout.addComponent(yTunnus);
        
        HorizontalLayout dateLayout = new HorizontalLayout();
        voimassaoloAlkaa = createDateField();
        dateLayout.addComponent(voimassaoloAlkaa);
        Label hyphen = new Label("  -  ");
        dateLayout.addComponent(hyphen);
        voimassaoloLoppuu = createDateField();
        dateLayout.addComponent(voimassaoloLoppuu);
        dateLayout.setCaption(T("dateLayout"));
        dateLayout.addStyleName("requiredField");

        organisaationPaaTiedotLayout.addComponent(dateLayout);

        createOhjeteksti(T("dateLayoutOhje"));

        if (model.getOrganisaatio() != null && model.getOrganisaatio().getYritysmuoto() != null) {
            Label yritysMuotoLabel = new Label(model.getOrganisaatio().getYritysmuoto());
            yritysMuotoLabel.setCaption(T("yritysMuotoLabel"));
            organisaationPaaTiedotLayout.addComponent(yritysMuotoLabel);
        }

        koodistoMaa.setCaption(T("koodistoMaa"));
        organisaationPaaTiedotLayout.addComponent(koodistoMaa);

        createOhjeteksti(T("maaOhje"));

        koodistoKieli.setCaption(T("koodistoKieli"));
        koodistoKieli.setImmediate(true);
        koodistoKieli.addListener(this.changeListener);
        koodistoKieli.setCaptionFormatter(UiUtils.getDefaultCaptionFormatter(I18N.getLocale()));
        organisaationPaaTiedotLayout.addComponent(koodistoKieli);

        // TODO fix the translation key, OrganisaatioForm?
        koodistoKotipaikka.setCaption(I18N.getMessage("OrganisaatioForm.kotipaikka"));
        koodistoKotipaikka.setImmediate(true);
        koodistoKotipaikka.addListener(this.changeListener);
        koodistoKotipaikka.setRequired(false);
        koodistoKotipaikka.addStyleName("requiredField");

        koodistoKotipaikka.setCaptionFormatter(UiUtils.getDefaultCaptionFormatter(I18N.getLocale()));
        organisaationPaaTiedotLayout.addComponent(koodistoKotipaikka);

        buildOsoitteet();

        addNewAddressLink();

        puhelin = createTextField("puhelin.puhelinnumero");
        organisaationPaaTiedotLayout.addComponent(puhelin);

        faksi = createTextField("faksi.puhelinnumero");
        organisaationPaaTiedotLayout.addComponent(faksi);

        email = createTextField("email.email");
        email.setWidth("327px");
        organisaationPaaTiedotLayout.addComponent(email);

        www = createTextField("www.wwwOsoite");
        www.setWidth("327px");
        organisaationPaaTiedotLayout.addComponent(www);

        return organisaationPaaTiedotLayout;
    }

    private void showParentsName(OrganisaatioDTO parentDTO, String labelString) {
        Label parentLabel = new Label();
        parentLabel.addStyleName("parentLabel");

        Label parentLabelBold = new Label();
        parentLabelBold.addStyleName("parentLabelBold");
        labelString += " ";
        parentLabel.setValue(labelString);
        parentLabelBold.setValue(parentDTO.getNimi().getTeksti().get(0).getValue());
        parentHL.addComponent(parentLabel);
        parentHL.addComponent(parentLabelBold);

        parentHL.addStyleName("parentHL");
    }

    private boolean isDomesticAddress() {
        //If model or oid is null then assume that we are creating new organization
        if (this.model != null) {

             if (this.model.getOid() != null) {

                   if (this.model.getPostiosoite() != null && this.model.getPostiosoite().getPostinumero() != null && this.model.getPostiosoite().getPostinumero().trim().length() > 0) {
                       return true;
                   } else
                       return this.model.getRuotsiPostiOsoite() != null && this.model.getRuotsiPostiOsoite().getPostinumero() != null && this.model.getRuotsiPostiOsoite().getPostinumero().trim().length() > 0;
             } else {
                 return true;
             }
        } else {
            return true;
        }
    }

    private void reCreateForeignAddressed(boolean emptyField) {
        koRuotsiLbl.setVisible(false);
        poRuotsiLbl.setVisible(false);
        koSuomiLbl.setVisible(false);
        poSuomiLbl.setVisible(false);
        kayntiOsoiteLbl.setValue(T("osoiteHelpTxtInternational"));
        postiOsoite.reCreateForeignLayout(emptyField);
        kayntiOsoite.reCreateForeignLayout(emptyField);
        ruotsiPostiOsoite.reCreateForeignLayout(emptyField);
        ruotsiKayntiOsoite.reCreateForeignLayout(emptyField);
    }

    private void reCreateDomesticAddresses() {
        koRuotsiLbl.setVisible(true);
        poRuotsiLbl.setVisible(true);
        koSuomiLbl.setVisible(true);
        poSuomiLbl.setVisible(true);
        kayntiOsoiteLbl.setValue(T("osoiteHelpTxt"));
        postiOsoite.reCreateDomesticLayout();
        ruotsiPostiOsoite.reCreateDomesticLayout();
        kayntiOsoite.reCreateDomesticLayout();
        ruotsiKayntiOsoite.reCreateDomesticLayout();
    }

    private HorizontalLayout buildKayntiOsoitteet()  {
        //Kayntiosoitteet horizontal layout
        HorizontalLayout kayntiOsoitteet = new HorizontalLayout();
        VerticalLayout koVl1 = new VerticalLayout();
        koSuomiLbl = new Label(T("suomeksi"));
        HorizontalLayout hlMarginWrapper = new HorizontalLayout();
        kayntiOsoite = new OsoiteField();
        kayntiOsoite.setImmediate(true);
        kayntiOsoite.addListener(this.changeListener);
        hlMarginWrapper.addComponent(kayntiOsoite);
        hlMarginWrapper.setMargin(false, true, false, false);

        koVl1.addComponent(koSuomiLbl);
        koVl1.addComponent(hlMarginWrapper);

        kayntiOsoitteet.addComponent(koVl1);

        VerticalLayout koVl2 = new VerticalLayout();
        koRuotsiLbl = new Label(T("ruotsiksi"));
        ruotsiKayntiOsoite = new OsoiteField();
        ruotsiKayntiOsoite.setImmediate(true);
        ruotsiKayntiOsoite.addListener(this.changeListener);
        ruotsiKayntiOsoite.setArSvensk(true);
        koVl2.addComponent(koRuotsiLbl);
        koVl2.addComponent(ruotsiKayntiOsoite);

        kayntiOsoite.setAlternative(ruotsiKayntiOsoite);

        kayntiOsoitteet.addComponent(koVl2);

        return kayntiOsoitteet;
    }

    private HorizontalLayout buildPostiOsoitteet() {
        HorizontalLayout postiOsoitteet = new HorizontalLayout();
        postiOsoitteet.setCaption(T("postiOsoite"));
        postiOsoitteet.addStyleName("requiredField");

        VerticalLayout poVl1 = new VerticalLayout();
        poSuomiLbl = new Label(T("suomeksi"));
        HorizontalLayout hlPostiMarginWrapper = new HorizontalLayout();
        postiOsoite = new OsoiteField();
        postiOsoite.setImmediate(true);
        postiOsoite.addListener(this.changeListener);
        hlPostiMarginWrapper.addComponent(postiOsoite);
        hlPostiMarginWrapper.setMargin(false,true,false,false);
        poVl1.addComponent(poSuomiLbl);
        poVl1.addComponent(hlPostiMarginWrapper);

        postiOsoitteet.addComponent(poVl1);

        VerticalLayout poVl2 = new VerticalLayout();
        poRuotsiLbl = new Label(T("ruotsiksi"));
        ruotsiPostiOsoite = new OsoiteField();
        ruotsiPostiOsoite.setImmediate(true);
        ruotsiPostiOsoite.addListener(this.changeListener);
        ruotsiPostiOsoite.setArSvensk(true);

        poVl2.addComponent(poRuotsiLbl);
        poVl2.addComponent(ruotsiPostiOsoite);
        
        postiOsoite.setAlternative(ruotsiPostiOsoite);

        postiOsoitteet.addComponent(poVl2);

        return postiOsoitteet;
    }

    private void buildOsoitteet() {

        VerticalLayout optionGroupLayout = new VerticalLayout();
        optionGroupLayout.setCaption(T("kayntiOsoite"));
        optionGroupLayout.addStyleName("requiredField");
        
        osoiteOption = new OptionGroup();
        osoiteOption.addItem(T("suomalainenOsoite"));
        osoiteOption.addItem(T("kansainvalinenOsoite"));
        osoiteOption.setImmediate(true);
        optionGroupLayout.setMargin(true,false,false,false);

        optionGroupLayout.addComponent(osoiteOption);
        organisaationPaaTiedotLayout.addComponent(optionGroupLayout);

        HorizontalLayout kayntiOsoiteLblLayout = new HorizontalLayout();
        kayntiOsoiteLblLayout.setWidth("100%");
        
        kayntiOsoiteLbl = new Label(T("osoiteHelpTxt"));
        kayntiOsoiteLbl.setStyleName(Oph.LABEL_SMALL);
        
        
        kayntiOsoiteLblLayout.addComponent(kayntiOsoiteLbl);
        
        organisaationPaaTiedotLayout.addComponent(kayntiOsoiteLblLayout);

        organisaationPaaTiedotLayout.addComponent(buildKayntiOsoitteet());
        
        VerticalLayout addressButtons = UiUtil.verticalLayout();
        addressButtons.setSizeUndefined();
        addressButtons.addComponent(new Label());
        
        sameAddress = UiUtil.checkbox(addressButtons, T("sameAddress"));

        organisaationPaaTiedotLayout.addComponent(sameAddress);
        
        organisaationPaaTiedotLayout.addComponent(buildPostiOsoitteet());
        sameAddress.setImmediate(true);
        sameAddress.addListener(new Property.ValueChangeListener() {

            private static final long serialVersionUID = 3358321269436950647L;

            @Override
            public void valueChange(ValueChangeEvent event) {
                toggleSameAddress(sameAddress.booleanValue());
            }
        });
    }

    private void toggleSameAddress(boolean selected) {

        if (selected) {
        	postiOsoite.setEnabled(false);
        	postiOsoite.getOsoite().setValue(kayntiOsoite.getOsoite().getValue());
        	postiOsoite.getPostinumero().setValue(kayntiOsoite.getPostinumero().getValue());

            ruotsiPostiOsoite.setEnabled(false);
            ruotsiPostiOsoite.getOsoite().setValue(ruotsiKayntiOsoite.getOsoite().getValue());
            ruotsiPostiOsoite.getPostinumero().setValue(ruotsiKayntiOsoite.getPostinumero().getValue());
        } else {
        	postiOsoite.setEnabled(true);
            ruotsiPostiOsoite.setEnabled(true);
        }
        
    }

    private void createOhjeteksti(String ohje) {
        HorizontalLayout ohjeHl = UiUtil.horizontalLayout();
        ohjeHl.setMargin(false, false, true, false);
        Label ohjeLabel = new Label();
        ohjeLabel.setValue(ohje);
        ohjeLabel.addStyleName(Oph.LABEL_SMALL);
        ohjeHl.addComponent(ohjeLabel);
        organisaationPaaTiedotLayout.addComponent(ohjeHl);
    }

    private DateField createDateField() {
        DateField df = new DateField();
        df.setDateFormat("dd.MM.yyyy");
        df.setImmediate(true);
        df.setResolution(InlineDateField.RESOLUTION_DAY);
        df.addListener(this.changeListener);
        return df;
    }

    private void buildOrganisaatioTyyppiTiedotLayout() {
        this.organisaatioTyyppiTiedotLayout.removeAllComponents();
        createOrganisaatiotyyppi();
        oppilaitosKoodi = this.createTextField("c_oppilaitoskoodi");
        organisaatioTyyppiTiedotLayout.addComponent(oppilaitosKoodi);
        koodistoOppilaitostyyppi.setCaption(I18N.getMessage("OrganisaatioForm.oppilaitostyyppi"));
        koodistoOppilaitostyyppi.setRequiredError(I18N.getMessage("validation.Organisaatio.oppilaitostyyppiNull"));

        organisaatioTyyppiTiedotLayout.addComponent(koodistoOppilaitostyyppi);
        oppilaitosKoodi.setRequiredError(I18N.getMessage("validation.Organisaatio.oppilaitoskoodiNull"));
        oppilaitosKoodi.setEnabled(PortletRole.getInstance().getPermissionService().userCanEditOlkoodi());

        organisaatioTyyppiTiedotLayout.addComponent(koodistoVuosiluokat);

        adjustOrgTyyppiKohtaisetKentat();
    }

    private void addNewAddressLink() {
        organisaationPaaTiedotLayout.addComponent(this.otherAddressesLayout.createNewAddressLink(this.changeListener));
        organisaationPaaTiedotLayout.addComponent(otherAddressesLayout);
    }

    @PostConstruct
    public void constructKoodistoFields() {
        if (koodistoMaa == null) {
            koodistoMaa = createKoodistoComponent(KOODISTO_MAA_URI, "maa", AbstractSelect.Filtering.FILTERINGMODE_CONTAINS);
            koodistoMaa.getField().setImmediate(true);
            koodistoMaa.setFieldValueFormatter(new KoodistoUriFieldFormatter());
            koodistoMaa.setCaptionFormatter(UiUtils.getDefaultCaptionFormatter(I18N.getLocale()));
            koodistoMaa.addListener(new Property.ValueChangeListener() {
                @Override
                public void valueChange(Property.ValueChangeEvent event) {
                    if (event.getProperty().getValue() != null) {
                        organisaatioEdited = true;
                        fireMaaChangedEvent(event.getProperty().getValue()
                                .toString());
                    }
                }
            });

            koodistoKieli = createTwinColKoodisto(KOODISTO_KIELI_URI, "OrganisaatioForm.yrityksenKieli");
            koodistoKieli.setFieldValueFormatter(new KoodistoUriFieldFormatter());
            koodistoKotipaikka = createKoodistoComponent(KOODISTO_KOTIPAIKKA_URI, "kotipaikka", AbstractSelect.Filtering.FILTERINGMODE_STARTSWITH);
            koodistoKotipaikka.setFieldValueFormatter(new KoodistoUriFieldFormatter());
            koodistoOppilaitostyyppi = ComponentBuilder.koodistoCombobox(KOODISTO_OPPILAITOSTYYPPI_URI).build();
            koodistoOppilaitostyyppi.setImmediate(true);
            koodistoOppilaitostyyppi.setFieldValueFormatter(new KoodistoUriAndVersionFieldFormatter());
            koodistoOppilaitostyyppi.setCaptionFormatter(UiUtils.getDefaultCaptionFormatter(I18N.getLocale()));
            koodistoOppilaitostyyppi.addListener(new Property.ValueChangeListener() {
                @Override
                public void valueChange(Property.ValueChangeEvent event) {
                    organisaatioEdited = true;
                    fireOppilaitostyyppiChangedEvent(event.getProperty().getValue().toString());
                }
            });
            koodistoOppilaitostyyppi.setEnabled(PortletRole.getInstance().getPermissionService().userCanEditOppilaitostyyppi());

            koodistoVuosiluokat = createTwinColKoodisto(KOODISTO_VUOSILUOKAT_URI, "organisaatio.vuosiluokat");

            koodistoVuosiluokat.setFieldValueFormatter(new KoodistoUriAndVersionFieldFormatter());


            buildOrganisaationPaaTiedotLayout();
            buildOrganisaatioTyyppiTiedotLayout();
            createOrganisaatioLisatiedotEditor();
            initForm();
            this.otherAddressesLayout.setKoodistoMaa(this.koodistoMaa);
            this.otherAddressesLayout.checkIfOtherAddressesExists();
            this.organisaatioEdited = false;
            if (this.koodistoMaa != null && this.koodistoMaa.getValue() != null) {
                fireMaaChangedEvent(this.koodistoMaa.getValue().toString());
            }
            if (!isDomesticAddress()) {
                reCreateForeignAddressed(false);
            }
            //If selected tab is the second or third,
            //data is validated and if errors exist
            //user is forwarded to the first tab to fix errors
            if (selectedTab != null
                    && (selectedTab == this.kuvailevatForm
                            || selectedTab == this.palvelutOppijalleForm)) {
                try {
                    checkIfPerustiedotValid();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    log.warn("Unable to check if perustiedot is valid : " + ex.getMessage());
                }
            }
        }
    }
    
    /*
     *If perustiedot is not valid user is forwarded to the perustiedot tab
    * to fix the errors
    */
    private void checkIfPerustiedotValid() throws Exception {
        perustiedotInvalid = !doValidation();
        if (perustiedotInvalid) {
            organisaatioSheet.setSelectedTab(0);
        }
    }
    
    /*
     * Showing an error dialog to notify the user that the active tab has been
     * forcibly changed to perustiedot when data requires fixing
     */
    private void showTabChangeDialog() {
        final Window errorPopup = new Window(I18N.getMessage("error"));
        errorPopup.setClosable(false);
        VerticalLayout vl = new VerticalLayout();
        vl.setWidth("380px");
        vl.setSpacing(true);
        vl.setMargin(true);
        VerticalLayout vl1 =new VerticalLayout();
        vl1.setSizeFull();
        vl1.addStyleName("error-container");
        vl1.setSpacing(true);
        Label unexpectedLabel = new Label(T("forcedTabChange"));
        unexpectedLabel.addStyleName("error");
        unexpectedLabel.setWidth("100%");
        vl1.addComponent(unexpectedLabel);
        vl1.setComponentAlignment(unexpectedLabel, Alignment.MIDDLE_CENTER);
        vl.addComponent(vl1);
        Button okButton = UiUtil.button(vl, I18N.getMessage("OK"), new Button.ClickListener() {

            private static final long serialVersionUID = 6028471405922131311L;

            @Override
            public void buttonClick(ClickEvent event) {
                if (errorPopup != null) {
                    getWindow().removeWindow(errorPopup);
                }
            }
        });
       
        
        vl.setComponentAlignment(vl1, Alignment.MIDDLE_CENTER);
        vl.setComponentAlignment(okButton, Alignment.BOTTOM_CENTER);
        
        errorPopup.setContent(vl);
        errorPopup.setModal(true);
        errorPopup.center();
        
        
        getWindow().addWindow(errorPopup);
    }



    private KoodistoComponent createTwinColKoodisto(String koodistoUri, String captionKey) {
        KoodistoComponent koodisto = WidgetFactory.create(koodistoUri);
        koodisto.setCaption(I18N.getMessage(captionKey));
        TwinColSelect koodistoSelect = new TwinColSelect();
        koodistoSelect.setColumns(17);
        koodistoSelect.setRows(12);
        koodisto.setField(koodistoSelect);
        koodisto.setEnabled(true);
        koodisto.setImmediate(true);
        koodisto.setFieldValueFormatter(new KoodistoUriFieldFormatter());
        koodisto.addListener(this.changeListener);
        return koodisto;
    }

    /**
     * Creates and adds lisatiedot editor to main layout.
     */
    private void createOrganisaatioLisatiedotEditor() {
        if (lisatiedotEditor == null) {
            lisatiedotEditor = new YhteystietojenTyyppiEditor(this.form, this);
            lisatiedotEditor.populate(prepareModelWithLisatiedotValues(), this.changeListener);
            organisaatioTyyppiTiedotBottomLayout.addComponent(lisatiedotEditor);
        }
    }

    private TextField createTextField(String messageCode) {
        TextField tField = new TextField();
        tField.setNullRepresentation("");
        tField.setInputPrompt(I18N.getMessage(messageCode));
        tField.setCaption(I18N.getMessage(messageCode));
        tField.setImmediate(true);
        tField.addListener(this.changeListener);
        return tField;
    }

    /**
     * Creates lisatiedot editor model that binds metadata and values.
     *
     * @return
     */
    private YhteystietojenTyyppiEditor.EditorModel prepareModelWithLisatiedotValues() {

        String organizationId = model.getOrganisaatio() != null ? model.getOrganisaatio().getOid() : null;
        List<YhteystietojenTyyppiDTO> groups = organisaatioService.findYhteystietoMetadataForOrganisaatio(getOrganisaatiotyypitListAsString());
        return new YhteystietojenTyyppiEditor.EditorModel(organizationId, groups, model.getOrganisaatio().getYhteystietoArvos());
    }

    private List<String> getOrganisaatiotyypitListAsString() {
        List<String> tyyps = new ArrayList<String>();
        for (String curTyyppi : (Collection<String>) (this.organisaatiotyyppi.getValue())) {
            tyyps.add(curTyyppi);
        }
        if (this.koodistoOppilaitostyyppi.getValue() != null) {
            tyyps.add((String) (this.koodistoOppilaitostyyppi.getValue()));
        }
        return tyyps;
    }

    private void fireMaaChangedEvent(String maaValue) {
        Blackboard blackboard = MainWindow.getBlackboard();
        blackboard.fire(new MaaChangedEvent(maaValue));
    }

    /**
     * Setting the editability of vuosiluokat field based on the selected
     * oppilaitostyyppi
     *
     * @param oppilaitostyyppiVal - the oppilaitostyyppi selected
     */
    private void fireOppilaitostyyppiChangedEvent(String oppilaitostyyppiVal) {
        if (oppilaitostyyppiVal != null
                &&  (oppilaitostyyppiVal.startsWith(T("peruskoulutUri"))
                || oppilaitostyyppiVal.startsWith(T("erityisperuskouluUri"))
                || oppilaitostyyppiVal.startsWith(T("peruskoulutJaLukiotUri")))) {
            koodistoVuosiluokat.setVisible(true);
            koodistoVuosiluokat.setEnabled(true);
        } else {
            koodistoVuosiluokat.setVisible(false);
            koodistoVuosiluokat.setEnabled(false);
        }
        rebuildYhteystietojenTyyppiEditor();
    }

    /**
     * Creates a KoodistoComponent (combobox)
     *
     * @param uri - the uri of the koodisto
     * @param propertyId - the property to be used as datasource for the
     * component.
     * @param filteringMode, see {@link AbstractSelect.Filtering}
     * @return - the KoodistoComponent created.
     */
    private KoodistoComponent createKoodistoComponent(String uri, String propertyId, int filteringMode) {
        KoodistoComponent component = ComponentBuilder.koodistoCombobox(uri)
                .withDataSource(model.getOrganisaatio(), propertyId)
                .build();
        ComboBox combo = (ComboBox)component.getField();
        combo.setFilteringMode(filteringMode);

        return component;
    }

    /**
     * Creates the organisaatiotyyppi optiongroup.
     */
    private void createOrganisaatiotyyppi() {
        try {
            organisaatiotyyppi = new OrganisaatiotyyppiComponent(isParentOrg, model.getOrganisaatio()!=null && model.getOrganisaatio().getOid()!=null);
            organisaatioTyyppiTiedotLayout.addComponent(organisaatiotyyppi);
            organisaatiotyyppi.setImmediate(true);
            organisaatiotyyppi.addListener(new Property.ValueChangeListener() {
                @Override
                public void valueChange(ValueChangeEvent event) {
                    organisaatioEdited = true;
                    rebuildYhteystietojenTyyppiEditor();
                    adjustOrgTyyppiKohtaisetKentat();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            log.warn("Unable to create organisaatiotyyppi component : " + ex.getMessage());
        }
    }

    /**
     * Adjusts the editability of fields that depend on the current selection of
     * organisaatiotyyppi.
     */
    private void adjustOrgTyyppiKohtaisetKentat() {
        adjustOppilaitoskohtaisetKentat();
    }

    /**
     * Re-populates lisatiedot editor with values from user current selection.
     */
    private void rebuildYhteystietojenTyyppiEditor() {
        lisatiedotEditor.populate(prepareModelWithLisatiedotValues(), this.changeListener);
    }

    /**
     * Sets the oppilaitos-specific fields to be disabled or enabled based on
     * whether the type oppilaitos is selected.
     */
	@SuppressWarnings("unchecked")
    private void adjustOppilaitoskohtaisetKentat() {
    	
		boolean isOppilaitos = ((Collection<String>) (this.organisaatiotyyppi.getValue())).contains(OrganisaatioTyyppi.OPPILAITOS.value());
		boolean isKtoimija = ((Collection<String>) (this.organisaatiotyyppi.getValue())).contains(OrganisaatioTyyppi.KOULUTUSTOIMIJA.value());
    	
        oppilaitosKoodi.setVisible(isOppilaitos);
        oppilaitosKoodi.setRequired(isOppilaitos);

        koodistoOppilaitostyyppi.setVisible(isOppilaitos);
        koodistoOppilaitostyyppi.setRequired(isOppilaitos);
		
        koodistoVuosiluokat.setVisible(isOppilaitos && isOppilaitosPeruskoulu());
        
        yTunnus.setVisible(isKtoimija);
        virastoTunnus.setVisible(isKtoimija);
        
    }

    /**
     * Returns true if the current organisaatio is of oppilaitostyyppi
     * Peruskoulut or Peruskouluasteen erityiskoulut. The method is used when
     * adjusting oppilaitostyyppi dependent fields.
     *
     * @return
     */
    private boolean isOppilaitosPeruskoulu() {
        return (this.koodistoOppilaitostyyppi.getValue() != null
                && (((String) this.koodistoOppilaitostyyppi.getValue()).startsWith(T("peruskoulutUri"))
                || ((String) this.koodistoOppilaitostyyppi.getValue()).startsWith(T("erityisperuskouluUri"))
                || ((String) this.koodistoOppilaitostyyppi.getValue()).startsWith(T("peruskoulutJaLukiotUri"))));
    }

    /**
     * @return the mlNimi
     */
    private MultiLingualTextField getMlNimi() {
        return mlNimi;
    }

    /**
     * @param mlNimi the mlNimi to set
     */
    private void setMlNimi(MultiLingualTextField mlNimi) {
        this.mlNimi = mlNimi;
        this.mlNimi.setImmediate(true);
        this.mlNimi.addListener(this.changeListener);
    }

    public KoodistoComponent getKoodistoMaa() {
        return koodistoMaa;
    }


    public void makeUnmodified() {
        this.organisaatioEdited = false;
    }

}

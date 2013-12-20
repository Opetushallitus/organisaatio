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
package fi.vm.sade.organisaatio.ui.organisaatio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.addon.formbinder.FormFieldMatch;
import org.vaadin.addon.formbinder.FormView;

import com.vaadin.data.Validator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.common.LocalizedBusinessException;
import fi.vm.sade.generic.common.validation.ValidationConstants;
import fi.vm.sade.generic.ui.component.SearchableTree;
import fi.vm.sade.generic.ui.validation.ErrorMessage;
import fi.vm.sade.generic.ui.validation.JSR303FieldValidator;
import fi.vm.sade.oid.service.ExceptionMessage;
import fi.vm.sade.oid.service.OIDService;
import fi.vm.sade.oid.service.types.NodeClassCode;
import fi.vm.sade.organisaatio.api.model.GenericFault;
import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi.Teksti;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoElementtiDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoElementtiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.YhteystietojenTyyppiDTO;
import fi.vm.sade.organisaatio.helper.OrganisaatioDisplayHelper;
import fi.vm.sade.organisaatio.ui.MainWindow;
import fi.vm.sade.organisaatio.ui.PortletRole;
import fi.vm.sade.organisaatio.ui.component.CustomLayouts;
import fi.vm.sade.organisaatio.ui.component.DoubleCheckbox;
import fi.vm.sade.organisaatio.ui.component.GenericUiForm;
import fi.vm.sade.organisaatio.ui.component.MuuYhteystietoForm;
import fi.vm.sade.organisaatio.ui.component.MuuYhteystietoList;
import fi.vm.sade.organisaatio.ui.component.SovellettavatTyypitOptions;
import fi.vm.sade.organisaatio.ui.component.YhteystietoElementtiDMS;
import fi.vm.sade.organisaatio.ui.util.UiUtils;
import fi.vm.sade.vaadin.util.UiUtil;

/**
 * A form for specifying the Yhteystietojen tyypit used by different
 * organization types. The specification consists of contact fields to be used
 * and the organizations to which the type is applied. Each field can be
 * obligatory or optionary.
 *
 * @author Antti Salonen
 */
@FormView(matchFieldsBy = FormFieldMatch.ANNOTATION)
@Configurable(preConstruction = false)
public class YhteystietojenTyyppiForm extends GenericUiForm<YhteystietojenTyyppiDTO> {

    private static final Logger LOG = LoggerFactory.getLogger(YhteystietojenTyyppiForm.class);
    // services
    @Autowired(required = true)
    private OrganisaatioService organisaatioService;
    
    @Autowired(required = true)
    private OIDService oidService;
    // other components
    private SearchableTree searchableTree;
    private CustomLayout layout;
    private TextField nimiFi;
    private TextField nimiSv;
    private DoubleCheckbox nimitiedotOptions;
    private DoubleCheckbox nimiketiedotOptions;
    private SovellettavatTyypitOptions sovellettavatOrganisaatiotyypitOptions;
    private YhteystietoElementtiDMS osoiteOptions;
    private YhteystietoElementtiDMS puhelinOptions;
    private YhteystietoElementtiDMS sahkoisetYtOptions;
    private MuuYhteystietoList muutOsoitteet;
    private MuuYhteystietoList muutPuhelimet;
    private MuuYhteystietoList muutSahkoiset;
    private Button buttonCancel;
    private Button buttonRemove;
    private YhteystietojenTyyppiDTO model;
    private Label formOtsikko;
    private Label yttNimi;
    private HorizontalLayout yttNimitiedot;
    private Label kaytossa;
    private Label pakollinen;
    private HorizontalLayout yttOsoitteet;
    private HorizontalLayout yttPuhelinnumerot;
    private HorizontalLayout yttSahkoiset;
    private Label yttOrg;
    private Window dialog;
    private ErrorMessage errorView;
    private MainWindow mainWindow;
    
    public YhteystietojenTyyppiForm(YhteystietojenTyyppiDTO model, SearchableTree searchableTree, MainWindow mainWindow) {
        super(model);
        this.model = model;

        this.searchableTree = searchableTree;
        this.mainWindow = mainWindow;
        // lätkitään fieldit custom layoutiin perustuen niiden locationeihin, joka on sama kuin fieldin @PropertyId
        layout = UiUtils.getCustomLayout(CustomLayouts.LAYOUTS_ORGANISAATION_LISATIEDOT_FORM);
        UiUtils.setFormCustomLayout(form, false, true, layout, null);
        UiUtils.addAnnotatedFieldsToCustomLayout(this, form, layout);

        createLabels();
        createNimiElementit();
        createYhteystietoComponents();
        createCancelButton();
        createRemoveButton();
        buttonSave.setVisible(PortletRole.getInstance().getPermissionService().userCanEditYhteystietojenTyypit());

        // add jsr-303 annotation based validators
        JSR303FieldValidator.addValidatorsBasedOnAnnotations(this);

        layout.setEnabled(PortletRole.getInstance().getPermissionService().userCanEditYhteystietojenTyypit());
        // set root
        setCompositionRoot(form);
    }

    private void createCancelButton() {
        //TODO permissions are not checked properly
            buttonCancel = UiUtils.buttonSmallSecodary(null ,I18N.getMessage("YhteystietojenTyyppiForm.peru"), new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent clickEvent) {
                    MainWindow curApp = (MainWindow) getApplication();
                    curApp.reInitializeLisatietoForm(new YhteystietojenTyyppiDTO());
                }
            });
            form.getFooter().addComponent(buttonCancel);
        buttonCancel.setVisible(PortletRole.getInstance().getPermissionService().userCanEditYhteystietojenTyypit());
    }

    private void createRemoveButton() {
        // TODO permissions are not checked properly
        buttonRemove = UiUtils.buttonSmallSecodary(null,
                I18N.getMessage("YhteystietojenTyyppiForm.poistaTyyppi"),
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent clickEvent) {
                        handleYttRemoval();
                    }
                });

        form.getFooter().addComponent(buttonRemove);
        buttonRemove.setVisible(model != null
                && model.getOid() != null
                && PortletRole.getInstance().getPermissionService()
                        .userCanDeleteYhteystietojenTyyppi());
    }

    private void handleYttRemoval() {
        
            VerticalLayout vl = UiUtil.verticalLayout();
            vl.setSizeUndefined();
            vl.setSpacing(true);
            vl.setMargin(true);
            UiUtil.label(vl, I18N.getMessage("YhteystietojenTyyppiForm.poistoKysely"));
            HorizontalLayout hl = UiUtil.horizontalLayout();
            hl.setSizeFull();
            Button peruutaB = UiUtil.buttonSmallSecodary(hl, I18N.getMessage("c_peruuta"), new Button.ClickListener() {
                
                @Override
                public void buttonClick(ClickEvent event) {
                    closeRemovalDialog();
                }
            });
            Button jatkaB = UiUtil.buttonSmallSecodary(hl, I18N.getMessage("c_jatka"), new Button.ClickListener() {
                
                @Override
                public void buttonClick(ClickEvent event) {
                    closeRemovalDialog();
                    removeYtt();
                }
            });
            hl.setComponentAlignment(peruutaB, Alignment.MIDDLE_LEFT);
            hl.setComponentAlignment(jatkaB, Alignment.MIDDLE_RIGHT);
            vl.addComponent(hl);
            dialog = new Window();
            dialog.setContent(vl);
            dialog.setModal(true);
            dialog.center();
            dialog.setCaption("");
            getWindow().addWindow(dialog);
            

    }
    
    private void removeYtt() {
        try {
            organisaatioService.removeYhteystietojenTyyppiByOid(model.getOid());
            getWindow().showNotification(I18N.getMessage("YhteystietojenTyyppiForm.poistoOnnistui"));
            MainWindow curApp = (MainWindow) getApplication();
            curApp.reInitializeLisatietoForm(new YhteystietojenTyyppiDTO());
            if (searchableTree != null) {
                searchableTree.reload();
            }
        } catch (GenericFault ex) {
            LOG.warn("encountered business exception when saving form: " + ex, ex);
            form.getWindow().showNotification(I18N.getMessage(ex.getMessage()), Window.Notification.TYPE_ERROR_MESSAGE);
        }
    }
    
    private void closeRemovalDialog() {
        if (dialog != null) {
            getWindow().removeWindow(dialog);
        }
    }

    private void createYhteystietoComponents() {
        osoiteOptions = new YhteystietoElementtiDMS(new String[]{
                    I18N.getMessage("YhteystietojenTyyppiForm.kayntiosoite",new Locale("fi")),
                    I18N.getMessage("YhteystietojenTyyppiForm.postiosoite",new Locale("fi")),
                    I18N.getMessage("YhteystietojenTyyppiForm.ulkomaanOsoite",new Locale("fi"))
                },
                new String[]{
                        I18N.getMessage("YhteystietojenTyyppiForm.kayntiosoite",new Locale("sv")),
                        I18N.getMessage("YhteystietojenTyyppiForm.postiosoite",new Locale("sv")),
                        I18N.getMessage("YhteystietojenTyyppiForm.ulkomaanOsoite",new Locale("sv"))
                },
                OrganisaatioDisplayHelper.getLisatietokenttas(model), new YhteystietoElementtiTyyppi[]{
                    YhteystietoElementtiTyyppi.OSOITE,
                    YhteystietoElementtiTyyppi.OSOITE,
                    YhteystietoElementtiTyyppi.OSOITE_ULKOMAA});

        layout.addComponent(osoiteOptions, "osoitetyypit");
        muutOsoitteet = new MuuYhteystietoList(getMuutYhteystiedot(YhteystietoElementtiTyyppi.OSOITE), this);
        layout.addComponent(muutOsoitteet, "muutOsoitteet");

        addNewYhteystietoLink("muuOsoite", "YhteystietojenTyyppiForm.muuOsoite");

        puhelinOptions = new YhteystietoElementtiDMS(new String[]{
                    I18N.getMessage("YhteystietojenTyyppiForm.mobiiliNumero",new Locale("fi")),
                    I18N.getMessage("YhteystietojenTyyppiForm.lankaNumero",new Locale("fi")),
                    I18N.getMessage("YhteystietojenTyyppiForm.faksiNumero",new Locale("fi"))},
                    new String[]{
                            I18N.getMessage("YhteystietojenTyyppiForm.mobiiliNumero",new Locale("sv")),
                            I18N.getMessage("YhteystietojenTyyppiForm.lankaNumero",new Locale("sv")),
                            I18N.getMessage("YhteystietojenTyyppiForm.faksiNumero",new Locale("sv"))},
                OrganisaatioDisplayHelper.getPuhelinnumeros(model), new YhteystietoElementtiTyyppi[]{
                    YhteystietoElementtiTyyppi.PUHELIN,
                    YhteystietoElementtiTyyppi.PUHELIN,
                    YhteystietoElementtiTyyppi.FAKSI,});

        layout.addComponent(puhelinOptions, "puhelinnumerotyypit");
        muutPuhelimet = new MuuYhteystietoList(getMuutYhteystiedot(YhteystietoElementtiTyyppi.PUHELIN), this);
        layout.addComponent(muutPuhelimet, "muutPuhelimet");

        addNewYhteystietoLink("muuPuhelin", "YhteystietojenTyyppiForm.muuPuhelin");

        sahkoisetYtOptions = new YhteystietoElementtiDMS(new String[]{
                    I18N.getMessage("YhteystietojenTyyppiForm.spOsoite",new Locale("fi")),
                    I18N.getMessage("YhteystietojenTyyppiForm.wwwOsoite",new Locale("fi"))},
                new String[]{
                        I18N.getMessage("YhteystietojenTyyppiForm.spOsoite",new Locale("sv")),
                        I18N.getMessage("YhteystietojenTyyppiForm.wwwOsoite",new Locale("sv"))},
                OrganisaatioDisplayHelper.getSahkoinenYhteystietos(model), new YhteystietoElementtiTyyppi[]{
                    YhteystietoElementtiTyyppi.EMAIL,
                    YhteystietoElementtiTyyppi.WWW,});

        layout.addComponent(sahkoisetYtOptions, "sahkoisetYttyypit");
        muutSahkoiset = new MuuYhteystietoList(getMuutYhteystiedot(YhteystietoElementtiTyyppi.WWW), this);
        layout.addComponent(muutSahkoiset, "muutSahkoiset");

        addNewYhteystietoLink("muuSahkoinen", "YhteystietojenTyyppiForm.muuSahkoinen");
    }

    private List<YhteystietoElementtiDTO> getMuutYhteystiedot(YhteystietoElementtiTyyppi tyyppi) {
        List<YhteystietoElementtiDTO> muut = new ArrayList<YhteystietoElementtiDTO>();
        for (YhteystietoElementtiDTO curYtel : model.getAllLisatietokenttas()) {
            if (curYtel != null && curYtel.getTyyppi().value().equals(tyyppi.value())
                    && curYtel.getNimi() != null && isMuuYTElementti(curYtel)) {
                muut.add(curYtel);
            }
        }
        return muut;
    }

    private boolean isMuuYTElementti(YhteystietoElementtiDTO ytel) {
        return ytel.getNimi().startsWith(I18N.getMessage("YhteystietojenTyyppiForm.muuOsPrefix",new Locale("fi")))
                || ytel.getNimi().startsWith(I18N.getMessage("YhteystietojenTyyppiForm.muuPuhPrefix",new Locale("fi")))
                || ytel.getNimi().startsWith(I18N.getMessage("YhteystietojenTyyppiForm.muuSahkPrefix",new Locale("fi"))) ||
                ytel.getNimi().startsWith(I18N.getMessage("YhteystietojenTyyppiForm.muuOsPrefix",new Locale("sv")))
                || ytel.getNimi().startsWith(I18N.getMessage("YhteystietojenTyyppiForm.muuPuhPrefix",new Locale("sv")))
                || ytel.getNimi().startsWith(I18N.getMessage("YhteystietojenTyyppiForm.muuSahkPrefix",new Locale("sv")));
    }

    private void createLabels() {
        errorView = new ErrorMessage();
        layout.addComponent(errorView, "errorView");
        yttNimi = new Label(I18N.getMessage("YhteystietojenTyyppiForm.yttNimi"));
        layout.addComponent(yttNimi, "yttNimi");

        yttNimitiedot = new HorizontalLayout();
        yttNimitiedot.setSpacing(true);
        yttNimitiedot.setWidth("100%");
        kaytossa = new Label(I18N.getMessage("YhteystietojenTyyppiForm.kaytossa"), Label.CONTENT_XHTML);
        yttNimitiedot.addComponent(kaytossa);

        pakollinen = new Label(I18N.getMessage("YhteystietojenTyyppiForm.pakollinen"), Label.CONTENT_XHTML);
        yttNimitiedot.addComponent(pakollinen);

        Label nimitiedotL = new Label(I18N.getMessage("YhteystietojenTyyppiForm.yttNimitiedot"));
        yttNimitiedot.addComponent(nimitiedotL);

        yttNimitiedot.setExpandRatio(kaytossa, 1.0f);
        yttNimitiedot.setExpandRatio(pakollinen, 1.0f);
        yttNimitiedot.setExpandRatio(nimitiedotL, 2.0f);

        layout.addComponent(yttNimitiedot, "yttNimitiedot");


        yttOsoitteet = new HorizontalLayout();
        yttOsoitteet.setSpacing(true);
        yttOsoitteet.setWidth("100%");
        Label osPlaceHolderL = new Label("&nbsp;", Label.CONTENT_XHTML);
        yttOsoitteet.addComponent(osPlaceHolderL);
        Label osPlaceHolderR = new Label("&nbsp;", Label.CONTENT_XHTML);
        yttOsoitteet.addComponent(osPlaceHolderR);
        Label osoitteetL = new Label(I18N.getMessage("YhteystietojenTyyppiForm.yttOsoitteet"));
        yttOsoitteet.addComponent(osoitteetL);

        yttOsoitteet.setExpandRatio(osPlaceHolderL, 1.0f);
        yttOsoitteet.setExpandRatio(osPlaceHolderR, 1.0f);
        yttOsoitteet.setExpandRatio(osoitteetL, 2.0f);
        layout.addComponent(yttOsoitteet, "yttOsoitteet");

        yttPuhelinnumerot = new HorizontalLayout();//
        yttPuhelinnumerot.setSpacing(true);
        yttPuhelinnumerot.setWidth("100%");
        Label puhPlaceHolderL = new Label("&nbsp;", Label.CONTENT_XHTML);
        yttPuhelinnumerot.addComponent(puhPlaceHolderL);
        Label puhPlaceHolderR = new Label("&nbsp;", Label.CONTENT_XHTML);
        yttPuhelinnumerot.addComponent(puhPlaceHolderR);
        Label puhelinnumerotL = new Label(I18N.getMessage("YhteystietojenTyyppiForm.yttPuhelinnumerot"));
        yttPuhelinnumerot.addComponent(puhelinnumerotL);
        
        yttPuhelinnumerot.setExpandRatio(puhPlaceHolderL, 1.0f);
        yttPuhelinnumerot.setExpandRatio(puhPlaceHolderR, 1.0f);
        yttPuhelinnumerot.setExpandRatio(puhelinnumerotL, 2.0f);
        layout.addComponent(yttPuhelinnumerot, "yttPuhelinnumerot");

        yttSahkoiset = new HorizontalLayout();
        yttSahkoiset.setSpacing(true);
        yttSahkoiset.setWidth("100%");
        Label sahPlaceHolderL = new Label("&nbsp;", Label.CONTENT_XHTML);
        yttSahkoiset.addComponent(sahPlaceHolderL);
        Label sahPlaceHolderR = new Label("&nbsp;", Label.CONTENT_XHTML);
        yttSahkoiset.addComponent(sahPlaceHolderR);
        Label sahkoisetL = new Label(I18N.getMessage("YhteystietojenTyyppiForm.yttSahkoiset"));
        yttSahkoiset.addComponent(sahkoisetL);
        
        yttSahkoiset.setExpandRatio(sahPlaceHolderL, 1.0f);
        yttSahkoiset.setExpandRatio(sahPlaceHolderR, 1.0f);
        yttSahkoiset.setExpandRatio(sahkoisetL, 2.0f);
        layout.addComponent(yttSahkoiset, "yttSahkoiset");

        yttOrg = new Label(I18N.getMessage("YhteystietojenTyyppiForm.yttOrg"));
        layout.addComponent(yttOrg, "yttOrg");

        createNimiFields();

        if (this.model != null && this.model.getOid() != null) {
        	formOtsikko = new Label(I18N.getMessage("YhteystietojenTyyppiForm.formOtsikkoMuokkaa"));
        } else {
        	formOtsikko = new Label(I18N.getMessage("YhteystietojenTyyppiForm.formOtsikkoLuo"));
        }
        layout.addComponent(formOtsikko, "formOtsikko");
    }

    private void createNimiFields() {
        GridLayout nimiLayout = new GridLayout(2, 2);
        nimiLayout.setSpacing(true);
        Label fiLabel = new Label(I18N.getMessage("YhteystietojenTyyppiForm.FI"));
        nimiLayout.addComponent(fiLabel);
        nimiFi = new TextField();
        nimiFi.setNullRepresentation("");
        nimiFi.setValue(model.getNimi() != null ? getNimiValue("fi") : null);
        nimiFi.setWidth("280px");
        nimiFi.setMaxLength(ValidationConstants.GENERIC_MAX);
        nimiLayout.addComponent(nimiFi);

        Label svLabel = new Label(I18N.getMessage("YhteystietojenTyyppiForm.SE"));
        nimiLayout.addComponent(svLabel);
        nimiSv = new TextField();
        nimiSv.setNullRepresentation("");
        nimiSv.setValue(model.getNimi() != null ? getNimiValue("sv") : null);
        nimiSv.setWidth("280px");
        nimiSv.setMaxLength(ValidationConstants.GENERIC_MAX);
        nimiLayout.addComponent(nimiSv);
        layout.addComponent(nimiLayout, "nimi");
    }

    private String getNimiValue(String lang) {
        for (Teksti curTeksti : model.getNimi().getTeksti()) {
            if (curTeksti.getKieliKoodi().equals(lang)) {
                return curTeksti.getValue();
            }
        }
        return null;
    }

    private void addNewYhteystietoLink(final String location, String messageCode) {
        Button newAddressBtn = UiUtils.buttonSmallSecodary(null, I18N.getMessage(messageCode), new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                initDialog(location);
            }
        });
        layout.addComponent(newAddressBtn, location);
    }
    
    public void initDialog(String location) {
        if (location.equals("muuOsoite")) {

            dialog = UiUtils.showDialog(this.getApplication(), I18N.getMessage("YhteystietojenTyyppiForm.muuOsoiteAdd"),
                    new MuuYhteystietoForm(new YhteystietoElementtiDTO(),
                    YhteystietoElementtiTyyppi.OSOITE,
                    "YhteystietojenTyyppiForm.muuOsoite", this));


        } else if (location.equals("muuPuhelin")) {

            dialog = UiUtils.showDialog(this.getApplication(), I18N.getMessage("YhteystietojenTyyppiForm.muuPuhelinAdd"),
                    new MuuYhteystietoForm(new YhteystietoElementtiDTO(),
                    YhteystietoElementtiTyyppi.PUHELIN,
                    "YhteystietojenTyyppiForm.muuPuhelin", this));

        } else if (location.equals("muuSahkoinen")) {

            dialog = UiUtils.showDialog(this.getApplication(), I18N.getMessage("YhteystietojenTyyppiForm.muuSahkoinenAdd"),
                    new MuuYhteystietoForm(new YhteystietoElementtiDTO(),
                    YhteystietoElementtiTyyppi.WWW,
                    "YhteystietojenTyyppiForm.muuSahkoinen", this));

        }
        dialog.setWidth("350px");
    }

    public void YTESaved(YhteystietoElementtiDTO ytEl, boolean isAdding) {
        if (dialog != null) {
            this.getWindow().removeWindow(dialog);
        }
        if (isAdding) {
            addYTE(ytEl);
        } else {
            updateYTE(ytEl);
        }
    }

    public void editMuuYtt(YhteystietoElementtiDTO ytel, String otsikko) {
        dialog = UiUtils.showDialog(this.getApplication(), otsikko,
                new MuuYhteystietoForm(ytel,
                ytel.getTyyppi(),
                I18N.getMessage("YhteystietojenTyyppiForm.muokkaaMuuYte"), this));
        dialog.setWidth("250px");
    }

    private void addYTE(YhteystietoElementtiDTO ytEl) {
    	
        //DEBUGSAWAY:LOG.debug("Add the ytel");
        if (ytEl.getTyyppi().value().equals(YhteystietoElementtiTyyppi.OSOITE.value())) {
            this.muutOsoitteet.addYhteystieto(ytEl);
        } else if (ytEl.getTyyppi().value().equals(YhteystietoElementtiTyyppi.PUHELIN.value())) {
            this.muutPuhelimet.addYhteystieto(ytEl);
        } else if (ytEl.getTyyppi().value().equals(YhteystietoElementtiTyyppi.WWW.value())) {
            this.muutSahkoiset.addYhteystieto(ytEl);
        }
        mainWindow.setDataChanged(true);
        mainWindow.addListeners(this);
    }

    private void updateYTE(YhteystietoElementtiDTO ytEl) {
        //DEBUGSAWAY:LOG.debug("updating the ytel");
        if (ytEl.getTyyppi().value().equals(YhteystietoElementtiTyyppi.OSOITE.value())) {
            this.muutOsoitteet.updateYhteystieto(ytEl);
        } else if (ytEl.getTyyppi().value().equals(YhteystietoElementtiTyyppi.PUHELIN.value())) {
            this.muutPuhelimet.updateYhteystieto(ytEl);
        } else if (ytEl.getTyyppi().value().equals(YhteystietoElementtiTyyppi.WWW.value())) {
            this.muutSahkoiset.updateYhteystieto(ytEl);
        }
        mainWindow.setDataChanged(true);
        mainWindow.addListeners(this);        
    }

    protected YhteystietojenTyyppiDTO save(YhteystietojenTyyppiDTO model) throws Exception {
        return null;
    }

    protected YhteystietojenTyyppiDTO save(YhteystietojenTyyppiDTO model, boolean isNew) throws Exception {
        List<YhteystietoElementtiDTO> kenttas = new ArrayList<YhteystietoElementtiDTO>();
        if (getDoubleCheckbox(nimitiedotOptions) != null) {
            kenttas.add(getDoubleCheckbox(nimitiedotOptions));
        }
        if (getDoubleCheckbox(nimiketiedotOptions) != null) {
            kenttas.add(getDoubleCheckbox(nimiketiedotOptions));
        }
        kenttas.addAll(osoiteOptions.saveModel());
        kenttas.addAll(puhelinOptions.saveModel());
        kenttas.addAll(sahkoisetYtOptions.saveModel());
        kenttas.addAll(muutOsoitteet.getModel());
        kenttas.addAll(muutPuhelimet.getModel());
        kenttas.addAll(muutSahkoiset.getModel());

        model.getAllLisatietokenttas().clear();
        model.getAllLisatietokenttas().addAll(kenttas);
        //DEBUGSAWAY:LOG.debug("All lisatietokenttas: " + model.getAllLisatietokenttas().size());
        this.sovellettavatOrganisaatiotyypitOptions.save();

        if (searchableTree != null) {
            searchableTree.reload(); // refresh tree on save - TODO: listener jutut ois parempia
        }
        try {
            model = generateOids(model);
        } catch (ExceptionMessage ex) {
            LOG.error("Error in oid generation: " + ex.getMessage());
        }
        if (!isNew) {
            //DEBUGSAWAY:LOG.debug("\n\nOrganisaationLisatiedtForm, Updating!!!: \n\n");
            organisaatioService.updateYhteystietojenTyyppi(model);

        } else {

            organisaatioService.createYhteystietojenTyyppi(model);

        }
        searchableTree.reload();
        if (buttonRemove != null) {
            buttonRemove.setVisible(true && PortletRole.getInstance().getPermissionService().userCanDeleteYhteystietojenTyyppi());
        } else {
            createRemoveButton();
        }
        this.formOtsikko.setValue(I18N.getMessage("YhteystietojenTyyppiForm.formOtsikkoMuokkaa"));
        return model;
    }

    private YhteystietojenTyyppiDTO generateOids(YhteystietojenTyyppiDTO model) throws ExceptionMessage {
        if (model.getOid() == null) {
            model.setOid(this.oidService.newOid(NodeClassCode.TEKN_5));
        }
        for (YhteystietoElementtiDTO curYel : model.getAllLisatietokenttas()) {
            if (curYel != null && curYel.getOid() == null) {
                curYel.setOid(this.oidService.newOid(NodeClassCode.TEKN_5));
            }
        }
        return model;
    }

    private YhteystietoElementtiDTO getDoubleCheckbox(DoubleCheckbox dcField) {
        if (dcField.isObligatoryClicked()
                || dcField.isUsedClicked()) {
            YhteystietoElementtiDTO dcModel = dcField.getModel();
            if (dcField.getModel().getNimi().equals(I18N.getMessage("YhteystietojenTyyppiForm.nimi", new Locale("fi")))) {
                dcModel.setTyyppi(YhteystietoElementtiTyyppi.NIMI);
            } else {
                dcModel.setTyyppi(YhteystietoElementtiTyyppi.NIMIKE);
            }
            return dcModel;
        }
        return null;
    }

    public void init() {
        //DEBUGSAWAY:LOG.debug("Starting initialization of sovellettavat organisaatiotyypit");
        sovellettavatOrganisaatiotyypitOptions = new SovellettavatTyypitOptions(model);

        layout.addComponent(sovellettavatOrganisaatiotyypitOptions, "sovellettavatOrganisaatiotyypit");
    }

    private void createNimiElementit() {
        YhteystietoElementtiDTO nimiTieto = OrganisaatioDisplayHelper.getNimiTieto(model);
        if (nimiTieto != null) {
            nimitiedotOptions = new DoubleCheckbox(nimiTieto);
        } else {
            nimiTieto = new YhteystietoElementtiDTO();
            nimiTieto.setKaytossa(false);
            nimiTieto.setPakollinen(false);

            nimiTieto.setNimi(I18N.getMessage("YhteystietojenTyyppiForm.nimi",new Locale("fi")));
            nimiTieto.setNimiSv(I18N.getMessage("YhteystietojenTyyppiForm.nimi",new Locale("sv")));

            nimitiedotOptions = new DoubleCheckbox(nimiTieto);
        }

        layout.addComponent(nimitiedotOptions, "nimitiedot");
        YhteystietoElementtiDTO nimikeTieto = OrganisaatioDisplayHelper.getNimike(model);
        if (nimikeTieto != null) {
            nimiketiedotOptions = new DoubleCheckbox(nimikeTieto);
        } else {
            nimikeTieto = new YhteystietoElementtiDTO();
            nimikeTieto.setKaytossa(false);
            nimikeTieto.setPakollinen(false);
            nimikeTieto.setNimi(I18N.getMessage("YhteystietojenTyyppiForm.nimike",new Locale("fi")));
            nimikeTieto.setNimiSv(I18N.getMessage("YhteystietojenTyyppiForm.nimike",new Locale("sv")));
            nimiketiedotOptions = new DoubleCheckbox(nimikeTieto);
        }

        layout.addComponent(nimiketiedotOptions, "nimiketiedot");
    }

    //Getters for Vaadin components, these are used by Selenium tests.
    public YhteystietoElementtiDMS getOsoiteOptions() {
        return osoiteOptions;
    }

    public YhteystietoElementtiDMS getPuhelinOptions() {
        return puhelinOptions;
    }

    public YhteystietoElementtiDMS getSahkoisetYtOptions() {
        return sahkoisetYtOptions;
    }

    public MuuYhteystietoList getMuutOsoitteet() {
        return muutOsoitteet;
    }    
    
    public MuuYhteystietoList getMuutPuhelimet() {
        return muutPuhelimet;
    }  
    
    public MuuYhteystietoList getMuutSahkoiset() {
        return muutSahkoiset;
    }            
    
    public TextField getNimiFi() {
        return nimiFi;
    }
    
    public TextField getNimiSv() {
        return nimiSv;
    }
    
    public SearchableTree getSearchableTree() {
        return searchableTree;
    }

    public void setSearchableTree(SearchableTree searchableTree) {
        this.searchableTree = searchableTree;
    }

    public DoubleCheckbox getNimitiedotOptions() {
        return nimitiedotOptions;
    }

    public SovellettavatTyypitOptions getSovellettavatOrganisaatiotyypitOptions() {
        return sovellettavatOrganisaatiotyypitOptions;
    }

    public DoubleCheckbox getNimiketiedotOptions() {
        return nimiketiedotOptions;
    }

    public Button getButtonCancel() {
        return buttonCancel;
    }

    @Override
    protected void processSave() {
        boolean isNew = model.getOid() == null;
        try {
            errorView.resetErrors();
            boolean isNimiValid = saveNimiFields();
            boolean areFieldNamesUnique = checkFieldNameUniqueness();
            boolean isSomethingSelected = checkSelections();
            form.commit();
            if (!isNimiValid || !areFieldNamesUnique || !isSomethingSelected) {
                throw new Validator.InvalidValueException("");
            }
            save((YhteystietojenTyyppiDTO) getBeanItem().getBean(), isNew);
            form.getWindow().showNotification(I18N.getMessage("c_save_successful"));
            model = this.organisaatioService.readYhteystietojenTyyppi(model.getOid());
            MainWindow curApp = (MainWindow) getApplication();
            curApp.reInitializeLisatietoForm(model);
        } catch (Validator.InvalidValueException e) {
            LOG.warn("invalid value, debugId: " + e.getDebugId() + ", exception: " + e + ", causes: " + Arrays.asList(e.getCauses()), e);
            errorView.addError(e);
            if (isNew) {
                model.setOid(null);
            }
        } catch (LocalizedBusinessException e) {
            LOG.warn("encountered business exception when saving form: " + e, e);
            errorView.addError(I18N.getMessage(e.getKey()));
            if (isNew) {
                model.setOid(null);
            }
        } catch (Throwable e) {
            LOG.error("encountered an exception when saving form: " + e, e);
            errorView.addError(I18N.getMessage(e.getLocalizedMessage()));
            if (isNew) {
                model.setOid(null);
            }
        }
    }
    
    private boolean checkSelections() {
        if (this.nimitiedotOptions.isObligatoryClicked() || this.nimitiedotOptions.isUsedClicked()) {
            return true;
        }
        if (this.nimiketiedotOptions.isObligatoryClicked() || this.nimiketiedotOptions.isUsedClicked()) {
            return true;
        }
        for (DoubleCheckbox curDC : this.osoiteOptions.getSelectOptions()) {
            if (curDC.isObligatoryClicked() || curDC.isUsedClicked()) {
                return true;
            }
        }
        for (DoubleCheckbox curDC : this.puhelinOptions.getSelectOptions()) {
            if (curDC.isObligatoryClicked() || curDC.isUsedClicked()) {
                return true;
            }
        }
        for (DoubleCheckbox curDC : this.sahkoisetYtOptions.getSelectOptions()) {
            if (curDC.isObligatoryClicked() || curDC.isUsedClicked()) {
                return true;
            }
        }
        for (DoubleCheckbox curDC : this.muutOsoitteet.getMuutYhteystiedot()) {
            if (curDC.isObligatoryClicked() || curDC.isUsedClicked()) {
                return true;
            }
        }
        for (DoubleCheckbox curDC : this.muutPuhelimet.getMuutYhteystiedot()) {
            if (curDC.isObligatoryClicked() || curDC.isUsedClicked()) {
                return true;
            }
        }
        for (DoubleCheckbox curDC : this.muutSahkoiset.getMuutYhteystiedot()) {
            if (curDC.isObligatoryClicked() || curDC.isUsedClicked()) {
                return true;
            }
        }
        this.errorView.addError(I18N.getMessage("YhteystietojenTyyppiForm.componentsValidation.notEmpty"));
        return false;
    }

    private boolean checkFieldNameUniqueness() {
        List<String> usedNimis = new ArrayList<String>();
        boolean isUnique = isFieldTypeUnique(muutOsoitteet, usedNimis) 
                            && isFieldTypeUnique(muutPuhelimet, usedNimis) 
                            && isFieldTypeUnique(muutSahkoiset, usedNimis) ;
        if (!isUnique) {
            this.errorView.addError(I18N.getMessage("YhteystietojenTyyppiForm.fieldValidation.uniqueness"));
        }
        return isUnique;        
    }
    
    private boolean isFieldTypeUnique(MuuYhteystietoList fields, List<String> usedNimis) {
        if (fields != null && fields.getModel() != null) {
            for (YhteystietoElementtiDTO curel : fields.getModel()) {
                if (usedNimis.contains(curel.getNimi())) {  
                    return false;
                }
                usedNimis.add(curel.getNimi());
            }
        }
        return true;
    }

    private boolean saveNimiFields() {
        String nimiFiText = (nimiFi.getValue() != null) ? (String) (nimiFi.getValue()) : "";
        String nimiSvText = (nimiSv.getValue() != null) ? (String) (nimiSv.getValue()) : "";
        if (nimiSvText.isEmpty() && nimiFiText.isEmpty()) {
            this.errorView.addError(I18N.getMessage("YhteystietojenTyyppiForm.nimiValidation.notNull"));
            return false;
        } else {
            setNimiValuesToModel(nimiFiText, nimiSvText);
        }
        return true;
    }

    private void setNimiValuesToModel(String fiNimi, String svNimi) {
        MonikielinenTekstiTyyppi nimi = new MonikielinenTekstiTyyppi();
        Teksti nimiTeksti = new Teksti();
        if (!fiNimi.isEmpty()) {
            nimiTeksti.setKieliKoodi("fi");
            nimiTeksti.setValue(fiNimi);
            nimi.getTeksti().add(nimiTeksti);
        }
        if (!svNimi.isEmpty()) {
            nimiTeksti = new Teksti();
            nimiTeksti.setKieliKoodi("sv");
            nimiTeksti.setValue(svNimi);
            nimi.getTeksti().add(nimiTeksti);
        }
        model.setNimi(nimi);
    }
}

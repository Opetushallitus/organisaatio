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
package fi.vm.sade.organisaatio.ui.component;

import com.github.wolfie.blackboard.Blackboard;
import com.vaadin.data.Property;
import com.vaadin.data.util.NestedMethodProperty;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.common.validation.ValidationConstants;
import fi.vm.sade.generic.ui.component.CaptionFormatter;
import fi.vm.sade.generic.ui.component.FieldValueFormatter;
import fi.vm.sade.koodisto.service.types.SearchKoodisCriteriaType;
import fi.vm.sade.koodisto.service.types.common.KieliType;
import fi.vm.sade.koodisto.service.types.common.KoodiMetadataType;
import fi.vm.sade.koodisto.service.types.common.KoodiType;
import fi.vm.sade.koodisto.util.CachingKoodistoClient;
import fi.vm.sade.koodisto.util.KoodiServiceSearchCriteriaBuilder;
import fi.vm.sade.koodisto.util.KoodistoHelper;
import fi.vm.sade.koodisto.widget.KoodistoComponent;
import fi.vm.sade.organisaatio.api.model.types.OsoiteDTO;
import fi.vm.sade.organisaatio.revised.ui.helper.KoodistoUriFieldFormatter;
import fi.vm.sade.organisaatio.ui.MainWindow;
import fi.vm.sade.organisaatio.ui.listener.MaaChangedListener;
import fi.vm.sade.organisaatio.ui.listener.RemoveComponentListener;
import fi.vm.sade.organisaatio.ui.listener.event.MaaChangedEvent;
import fi.vm.sade.organisaatio.ui.util.UiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.addon.customfield.CustomField;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static fi.vm.sade.organisaatio.KoodistoURI.KOODISTO_MAA_URI;
import static fi.vm.sade.organisaatio.KoodistoURI.KOODISTO_POSTINUMERO_URI;
//model.dto.Kieli;

/**
 * Vaadin composite field component for inputting address. Contains fields for -
 * street address - zipcode - city - state - extra row - country
 *
 * OsoiteField listens to organisaatio's country, and State + extrarow + country
 * fields will be visible only if organisaatio's country is other than finland.
 *
 * TODO fix the country == "suommi" string comparions to use for example koodisto uri? / 29.1.2013 mlyly
 *
 * @author Antti Salonen
 * @author tkatva
 */
@Configurable
public class OsoiteField extends CustomField {

    private final static Logger log = LoggerFactory.getLogger(OsoiteField.class);
    private TextField osoiteMuuKaytto;
    private Select osoiteKayttotarkoitus;
    private KoodiType selectedPostinumeroKoodi;

    private TextArea ulkomaanOsoite = new TextArea();

    private VerticalLayout ulkomaanOsoiteLayout;

    private VerticalLayout mainLayout;
    private AbstractLayout osoiteBasicLayout;

    private final KoodistoComponent postinumero = ComponentBuilder.koodistoCombobox(KOODISTO_POSTINUMERO_URI).withInputPrompt("OsoiteField.postinumero")
            .withArvoCaption()
            .withImmediate(true)
            .build();
    protected final KoodistoComponent maa = ComponentBuilder.koodistoCombobox(KOODISTO_MAA_URI).withInputPrompt("OsoiteField.maa").build();

    private final TextField osoite = ComponentBuilder.textField().withInputPrompt("c_katuosoite").withWidth("100%").build();

    private final TextField postitoimipaikka = ComponentBuilder.textField().withInputPrompt("c_postitoimipaikka").build();
    protected final TextField osavaltio = ComponentBuilder.textField().withInputPrompt("c_osavaltio").withWidth("100%").build();
    protected final TextField extraRivi = ComponentBuilder.textField().withInputPrompt("c_extraosoite").withWidth("100%").build();

    private String propertyId;
    private List<RemoveComponentListener> removeListeners;

    private OsoiteDTO osoiteDto;
    private CachingKoodistoClient koodistoRestClient = new CachingKoodistoClient();

    private boolean arSvensk = false;
    
    /**
     * Osoitekomponentti, jonka kanssa tämä on vaihtoehtoisesti pakollinen (joka tämä tai toinen komponentti on pakollinen).
     */
    private OsoiteField alternative;

    public OsoiteField() {
        super();

        osoite.setMaxLength(ValidationConstants.GENERIC_MAX);
        
        // add validators
        mainLayout = new VerticalLayout();
        initUlkomaanOsoiteField();
        setCompositionRoot(mainLayout);
        postitoimipaikka.setEnabled(false);
        addMaaChangedListener();
        initPostinumeroKoodistoComponent();

    }

    @Override
    protected boolean isEmpty() {
    	return isNullOrEmpty(getUlkomaanOsoite().getValue())
			&& (isNullOrEmpty(getUlkomaanOsoite().getValue())
    			|| isNullOrEmpty(postinumero.getValue())
    			|| isNullOrEmpty(postitoimipaikka.getValue()));
    }
    
    private static boolean isNullOrEmpty(Object val) {
    	return val==null || (val instanceof String && ((String) val).isEmpty());
    }
    
    public void setAlternative(OsoiteField alternative) {
		this.alternative = alternative;
		if (alternative!=null && alternative!=this.alternative) {
			alternative.setAlternative(this);
			setRequired(true);
		}
	}
    
    public OsoiteField getAlternative() {
		return alternative;
	}

    private void initUlkomaanOsoiteField() {
      getUlkomaanOsoite().setWidth("100%");
      getUlkomaanOsoite().setInputPrompt(I18N.getMessage("OsoiteField.ulkomaanOsoite"));

    }

    public void enableOrDeEnableOsoiteFields(boolean enableOrDeEnable) {
        this.osoite.setEnabled(enableOrDeEnable);
        this.postinumero.setEnabled(enableOrDeEnable);
        this.postinumero.setEnabled(enableOrDeEnable);
        this.extraRivi.setEnabled(enableOrDeEnable);
        this.postitoimipaikka.setEnabled(enableOrDeEnable);
    }

    @Override
    public Class<?> getType() {
        return OsoiteDTO.class;
    }

    private TextField createTextField(String messageCode) {
        TextField tField = new TextField();
        tField.setNullRepresentation("");
        tField.setInputPrompt(I18N.getMessage(messageCode));
        tField.setCaption(I18N.getMessage(messageCode));
        tField.setImmediate(true);

        return tField;
    }

    @Override
    public Object getValue() {
        Property dataSource = getPropertyDataSource();
        return dataSource == null ? null : dataSource.getValue();
    }

    @Override
    public void setPropertyDataSource(Property newDataSource) {
        super.setPropertyDataSource(newDataSource);
        if (newDataSource.getValue() instanceof OsoiteDTO) {
            osoiteDto = (OsoiteDTO) newDataSource.getValue();
            osoite.setImmediate(true);
            osoite.setPropertyDataSource(new NestedMethodProperty(osoiteDto, "osoite"));
            getUlkomaanOsoite().setPropertyDataSource(new NestedMethodProperty(osoiteDto, "osoite"));
            postinumero.setPropertyDataSource(new NestedMethodProperty(osoiteDto, "postinumero"));
            postitoimipaikka.setImmediate(true);
            postitoimipaikka.setPropertyDataSource(new NestedMethodProperty(osoiteDto, "postitoimipaikka"));
            maa.setPropertyDataSource(new NestedMethodProperty(osoiteDto, "maa"));
            maa.setFieldValueFormatter(new KoodistoUriFieldFormatter());
            extraRivi.setImmediate(true);
            extraRivi.setPropertyDataSource(new NestedMethodProperty(osoiteDto, "extraRivi"));
            extraRivi.setVisible(false);

        }
    }

    public void reCreateDomesticLayout() {
        OsoiteDTO osoite = (OsoiteDTO)getValue();
        if (osoite.getYtjPaivitysPvm() == null) {
            emptyFields();
        }
        mainLayout.removeAllComponents();
        maa.setValue("");
        osoiteBasicLayout = createDomesticLayout();
        mainLayout.addComponent(osoiteBasicLayout);
        requestRepaintAll();

    }

    public void emptyFields() {
        if(osoite != null) {
            osoite.setValue("");
        }
        if (postinumero != null) {
            postinumero.setValue(null);
        }
        if (getUlkomaanOsoite() != null) {
            getUlkomaanOsoite().setValue("");
        }
        if (maa != null ){
            maa.setValue(null);
        }
        if (postitoimipaikka != null) {
            postitoimipaikka.setValue("");
        }
    }

    private VerticalLayout createForeignLayout() {

        if (arSvensk) {
            maa.setCaptionFormatter(new CaptionFormatter() {
                @Override
                public String formatCaption(Object o) {
                    KoodiType koodi =   (KoodiType)o;
                    String retval = "";
                    if (koodi.getMetadata() != null) {

                        for (KoodiMetadataType meta:koodi.getMetadata()) {
                            if (meta.getKieli().equals(KieliType.SV)){
                                retval = meta.getNimi();
                            }
                        }
                    }
                    return retval;
                }
            });
        }
        ulkomaanOsoiteLayout = new VerticalLayout();
        ulkomaanOsoiteLayout.setSizeFull();
        ulkomaanOsoiteLayout.addComponent(getUlkomaanOsoite());

        maa.setSizeFull();
        return ulkomaanOsoiteLayout;
    }

    public void reCreateForeignLayout(boolean emptyField) {
        OsoiteDTO osoite = (OsoiteDTO)getValue();
        if (osoite != null && osoite.getYtjPaivitysPvm() == null && emptyField) {
            emptyFields();
        }
        mainLayout.removeAllComponents();
        mainLayout.setSizeFull();
        getUlkomaanOsoite().setNullRepresentation("");
        if (!arSvensk) {
            mainLayout.addComponent(createForeignLayout());
        }

        requestRepaintAll();

    }

    public void setArSvensk(boolean isSvensk) {
        this.arSvensk = isSvensk;
    }

    private AbstractLayout createDomesticLayout() {

        VerticalLayout vl = new VerticalLayout();
        vl.setWidth("329px");
        vl.addComponent(osoite);
        osoite.setWidth("329px");
        GridLayout obl = new GridLayout(2, 3);
        vl.addComponent(obl);
        obl.addComponent(postinumero, 0, 0);
        obl.addComponent(postitoimipaikka, 1, 0);
        obl.addComponent(extraRivi, 1, 1);

        obl.setColumnExpandRatio(0, 1);
        obl.setColumnExpandRatio(1, 3);
        return vl;
    }

    @PostConstruct
    public void constructLayout() {
        osoiteBasicLayout = createDomesticLayout();
        mainLayout.addComponent(osoiteBasicLayout);
    }

    private void initPostinumeroKoodistoComponent() {

        postinumero.setFieldValueFormatter(new FieldValueFormatter() {
            @Override
            public Object formatFieldValue(Object dto) {
                if (dto instanceof KoodiType) {
                    KoodiType koodi = (KoodiType) dto;
                    return koodi.getKoodiUri();
                } else {
                    return dto;
                }
            }
        });
        postinumero.setPropertyDataSource(new NestedMethodProperty(this, "selectedPostinumeroKoodi"));
        postinumero.setImmediate(true);

        postinumero.addListener(new ValueChangeListener() {
            @Override
            public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {

                if (event.getProperty().getValue() instanceof String) {
                    try {
                        String koodiUri = (String) event.getProperty().getValue();
                        SearchKoodisCriteriaType searchCriteria = KoodiServiceSearchCriteriaBuilder.latestKoodisByUris(koodiUri);

                        List<KoodiType> result = koodistoRestClient.searchKoodis(searchCriteria);
                        if (result.size() != 1) {
                            log.error("Koodi " + koodiUri + " has " + result.size() + " matches when searching!");
                            return;
                        }

                        // Selected postinumero
                        KoodiType postinumeroKoodi = result.get(0);
                        postitoimipaikka.setEnabled(true);
                        // Update postitoimipaikka textual value, order is current lang/FI/SV/EN/x
                        setPostitoimipaikkaNimi(getLocalizedNimiForKoodi(postinumeroKoodi));
                        postitoimipaikka.setEnabled(false);
                    } catch (Exception ex) {
                        log.error("Failed to update postitoimipaikka textual information", ex);
                    }
                } else {
                    postitoimipaikka.setEnabled(true);
                    // Update postitoimipaikka textual value, order is current lang/FI/SV/EN/x
                    setPostitoimipaikkaNimi(null);
                    postitoimipaikka.setEnabled(false);
                }
            }
        });
    }

    /**
     * Get koodi "nimi" from koodi metadata with current locale.
     *
     * Order of tried languages: current, FI, SV, EN, whatever (first) if any
     *
     * @param postinumeroKoodi
     * @return null if metadata not available
     */
    private String getLocalizedNimiForKoodi(KoodiType postinumeroKoodi) {

        if (postinumeroKoodi == null) {
            return null;
        }

        if (postinumeroKoodi.getMetadata() == null || postinumeroKoodi.getMetadata().isEmpty()) {
            return null;
        }

        // Get metadata with current language
        KoodiMetadataType kmdt = KoodistoHelper.getKoodiMetadataForLanguage(postinumeroKoodi, KoodistoHelper.getKieliForLocale(I18N.getLocale()));

        if (arSvensk) {

            kmdt = KoodistoHelper.getKoodiMetadataForLanguage(postinumeroKoodi, KieliType.SV);

        }   else {

        if (kmdt == null) {
            // Try finnish
            kmdt = KoodistoHelper.getKoodiMetadataForLanguage(postinumeroKoodi, KieliType.FI);
        }
        if (kmdt == null) {
            // Try swedish
            kmdt = KoodistoHelper.getKoodiMetadataForLanguage(postinumeroKoodi, KieliType.SV);
        }
        if (kmdt == null) {
            // Try english
            kmdt = KoodistoHelper.getKoodiMetadataForLanguage(postinumeroKoodi, KieliType.EN);
        }
        if (kmdt == null) {
            // Just take whatever we have
            kmdt = postinumeroKoodi.getMetadata().get(0);
        }
        }

        return kmdt != null ? kmdt.getNimi() : null;
    }

    private void setPostitoimipaikkaNimi(String postitoimipaikka) {
        this.postitoimipaikka.setValue(postitoimipaikka);

    }

    protected void addMaaChangedListener() {
        try {
            Blackboard blackboard = MainWindow.getBlackboard();
            blackboard.addListener(new MaaChangedListener() {
                @Override
                public void maaChanged(MaaChangedEvent maaChangedEvent) {
                    setFieldsVisibleBasedOnMaa(maaChangedEvent.getMaa());
                }
            });
        } catch (NullPointerException npe) {
            log.warn("Unable to add MaaChangedListener : " + npe.getMessage());
        }
    }

    public OsoiteField(KoodistoComponent maaField) {
        this();
        setFieldsVisibleBasedOnMaa(maaField.getValue().toString());
    }

    public void initMuuFields(String[] muutTyypit) {

        osoiteKayttotarkoitus = new Select();

        if (muutTyypit != null) {
            for (String muuTyyppi : muutTyypit) {
                osoiteKayttotarkoitus.addItem(muuTyyppi);
            }
        }
        osoiteKayttotarkoitus.setImmediate(true);

        osoiteKayttotarkoitus.addListener(new com.vaadin.data.Property.ValueChangeListener() {
            @Override
            public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
                if (osoiteKayttotarkoitus.getValue() instanceof String) {
                    if (osoiteKayttotarkoitus.getValue().equals("muu")) {
                        osoiteMuuKaytto.setEnabled(true);
                    }
                }
            }
        });

        osoiteMuuKaytto = ComponentBuilder.textField().withInputPrompt("c_muu").withWidth("100%").build();

        osoiteMuuKaytto.setEnabled(false);

        UiUtils.buttonSmallSecodary(osoiteBasicLayout, I18N.getMessage("OsoiteField.poista"),
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        callRemoveListeners();
                    }
                });
    }

    private void callRemoveListeners() {
        if (removeListeners != null) {
            for (RemoveComponentListener listener : removeListeners) {
                listener.removeComponent(this);
            }
        }
    }

    public void addRemoveListener(RemoveComponentListener listener) {
        if (removeListeners == null) {
            removeListeners = new ArrayList<RemoveComponentListener>();
        }
        removeListeners.add(listener);
    }

    public TextField getOsoite() {
        return osoite;
    }

    public KoodistoComponent getPostinumero() {
        return postinumero;
    }

    public TextField getPostitoimipaikka() {
        return postitoimipaikka;
    }

    public TextField getOsavaltio() {
        return osavaltio;
    }

    public TextField getExtraRivi() {
        return extraRivi;
    }

    public KoodistoComponent getMaa() {
        return maa;
    }

    public void setFieldsVisibleBasedOnMaa(Object organisaatioMaa) {
        // TODO: At some point in time there will be valid foreign country
        // address fields but currently no.
        osavaltio.setVisible(false);
        extraRivi.setVisible(false);
        this.maa.setVisible(false);

        requestRepaint();
    }

    /**
     * @return the propertyId
     */
    public String getPropertyId() {
        return propertyId;
    }

    /**
     * @param propertyId the propertyId to set
     */
    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    /**
     * @return the selectedPostinumeroKoodi
     */
    public KoodiType getSelectedPostinumeroKoodi() {
        return selectedPostinumeroKoodi;
    }

    /**
     * @param selectedPostinumeroKoodi the selectedPostinumeroKoodi to set
     */
    public void setSelectedPostinumeroKoodi(KoodiType selectedPostinumeroKoodi) {

        this.selectedPostinumeroKoodi = selectedPostinumeroKoodi;

    }

    public void addListener(ValueChangeListener listener) {
        this.osoite.addListener(listener);
        this.postinumero.addListener(listener);
        this.postitoimipaikka.addListener(listener);
        this.extraRivi.addListener(listener);
    }
    
    @Override
    public void removeListener(ValueChangeListener listener) {
        this.osoite.removeListener(listener);
        this.postinumero.removeListener(listener);
        this.postitoimipaikka.removeListener(listener);
        this.extraRivi.removeListener(listener);
    }

    public TextArea getUlkomaanOsoite() {
        return ulkomaanOsoite;
    }

    public void setUlkomaanOsoite(TextArea ulkomaanOsoite) {
        this.ulkomaanOsoite = ulkomaanOsoite;
    }
}

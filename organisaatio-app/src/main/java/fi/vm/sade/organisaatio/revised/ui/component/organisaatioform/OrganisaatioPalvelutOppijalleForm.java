
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.common.I18NHelper;
import fi.vm.sade.generic.ui.validation.ValidatingViewBoundForm;
import fi.vm.sade.organisaatio.KoodistoURI;
import fi.vm.sade.organisaatio.revised.ui.event.KuvausEvent;
import fi.vm.sade.organisaatio.revised.ui.helper.KoodistoHelper;
import fi.vm.sade.organisaatio.ui.model.LOPTiedotModel;
import fi.vm.sade.organisaatio.ui.model.OrganisaatioKuvailevatTiedotModel;
import fi.vm.sade.organisaatio.ui.util.UiUtils;
import fi.vm.sade.vaadin.util.UiUtil;

/**
 * The palvelut oppijalle tab component.
 * 
 * @author Markus
 *
 */
class OrganisaatioPalvelutOppijalleForm extends VerticalLayout {
    
	private static final long serialVersionUID = 1L;

	/**
     * The tab sheet containing the localized free text descriptionss.
     */
    private OrganisaatioKuvausTabSheet palvelutOppijalleSheet;
    
    /**
     * The model
     */
    private OrganisaatioKuvailevatTiedotModel model;
    
    private I18NHelper i18n = new I18NHelper(this);
    
    /**
     * Map containing a form for each language used.
     */
    private HashMap<String,Form> kuvausForms = new HashMap<String,Form>();
    
    private Property.ValueChangeListener changeListener;
    
    private OrganisaatioEditForm mainForm;

    OrganisaatioPalvelutOppijalleForm(OrganisaatioKuvailevatTiedotModel model, Property.ValueChangeListener listener) {
        super();
        this.changeListener = listener;
        this.model = model;
        setSpacing(true);
        buildLayout();
    }
    
    
    @Override
    public void attach() {
        super.attach();
        if (this.mainForm != null) {
            this.mainForm.makeUnmodified();
        }
    }
    
    /**
     * Commits all the free text desription forms. Catches validation exceptions
     * and passes them to the caller as a list. The caller can e.g. display them
     * on the ui. 
     * @return
     * @throws Exception
     */
    List<Validator.InvalidValueException> commitForm() throws Exception {
        List<Validator.InvalidValueException> errors = new ArrayList<Validator.InvalidValueException>();
        for(Map.Entry<String,Form> curEntry : kuvausForms.entrySet()) {
            try {
                curEntry.getValue().commit();
            } catch (Validator.InvalidValueException ex) {
                errors.add(ex);
            }
        }
        return errors;
    }
    
    /**
     * Building the layout.
     */
    private void buildLayout() {
        addComponent(createOhjePart());
        final KoodistoHelper helper = new KoodistoHelper();
        palvelutOppijalleSheet = new OrganisaatioKuvausTabSheet(KoodistoURI.KOODISTO_KIELI_URI){
            private static final long serialVersionUID = -7916177514458213528L;
             @Override
            public void doAddTab(String uri) {
                 String caption = uri != null ?  helper.tryGetArvoByKoodi(uri) : "";
                 Form kuvauksetForm = createRichTextEditor(uri, caption);
                 
                 this.addTab(uri, kuvauksetForm, caption);
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
        addComponent(palvelutOppijalleSheet);
        
        /*
         * Creating the tabs based on the data in the model
         * A tab for finnish language is created and opened as default. 
         */
        Set<String> tabkeys = new HashSet<String>();
        if (!model.getLopTiedot().keySet().contains(I18N.getMessage("OrganisaatioKuvailevatTiedotFormView.fiUri"))) {
            tabkeys.add(I18N.getMessage("OrganisaatioKuvailevatTiedotFormView.fiUri"));
        }
        
        for (String curKey : model.getLopTiedot().keySet()) {
            tabkeys.add(curKey);
        }
        
        palvelutOppijalleSheet.getKcSelection().setValue(tabkeys);
        palvelutOppijalleSheet.setSelectedTab(1);
    }   
    
    /**
     * Creating the help part for the view.
     * @return
     */
    private HorizontalLayout createOhjePart() {
        HorizontalLayout hl = UiUtil.horizontalLayout();
        hl.setSpacing(false);
        hl.setMargin(false);
        hl.setHeight("50px");
    
        Label ohje = UiUtil.label(hl, T("palvelutOppijalleOhje"));   
        hl.setComponentAlignment(ohje, Alignment.MIDDLE_LEFT);
        return hl;
    }

    
    /**
     * Handling of tab removal. I.e. removes the form from the language-form map
     * and adjust the model data accordingly.
     * @param uri the language uri for which tab is removed.
     */
    private void handleTabRemoval(String uri) {
        this.kuvausForms.remove(uri);
        
        LOPTiedotModel curTiedotModel = this.model.getLopTiedot().get(uri);
        if (this.model.getLopTiedot().get(uri) != null) {
            clearRelevantKuvaukset(curTiedotModel, uri);
        }
    }
    
    /**
     * Clearing of model for a given language. 
     * 
     * @param curTiedotModel
     * @param kieliUri
     */
    private void clearRelevantKuvaukset(LOPTiedotModel curTiedotModel, String kieliUri) {
        curTiedotModel.clearPalvelutOppijalleTiedot();
        if (curTiedotModel.getTiedot().isEmpty()) {
            this.model.getLopTiedot().remove(kieliUri);
        }
    }
    

    private void propagateEvent(KuvausEvent event) {
        fireEvent(event);
    }
    
    /**
     * Creating of the rich text form for the language given as parameter.
     * @param uri the language uri
     * @param kieli the language name.
     * @return
     */
    private Form createRichTextEditor(String uri, String kieli) {
        LOPTiedotModel lopTiedotLang = null;
        //If data for the language exists it is set as model
        if (this.model.getLopTiedot().containsKey(uri)) {
            lopTiedotLang = this.model.getLopTiedot().get(uri);
        //Otherwise an empty model
        } else {
            lopTiedotLang = new LOPTiedotModel();
            this.model.getLopTiedot().put(uri, lopTiedotLang);
        }
       
        PalvelutOppijalleKuvauksetFormView kuvausFormView = new PalvelutOppijalleKuvauksetFormView(lopTiedotLang, kieli, this.changeListener);
        kuvausFormView.addListener(new Listener() {

            private static final long serialVersionUID = 4008613145350843092L;

            @Override
            public void componentEvent(Event event) {
                if (event instanceof KuvausEvent) {
                    propagateEvent((KuvausEvent)event);
                }
            }
            
        });
        //Creation of the actual form.
        BeanItem<LOPTiedotModel> bItem = new BeanItem<LOPTiedotModel>(lopTiedotLang);
        Form kuvauksetForm = new ValidatingViewBoundForm(kuvausFormView);
        kuvauksetForm.setItemDataSource(bItem);
        
        kuvauksetForm.setValidationVisible(false);
        kuvauksetForm.setValidationVisibleOnCommit(false);
        //this.kuvausForms.put(uri, lopForm);
        this.kuvausForms.put(uri,kuvauksetForm);
        return kuvauksetForm;
    }
    
    private String T(String key, Object... args) {
        return i18n.getMessage(key, args);
    }
    
    public void setMainForm(OrganisaatioEditForm mainForm) {
        this.mainForm = mainForm;
    }
    
    
}

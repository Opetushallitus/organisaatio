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

import static fi.vm.sade.organisaatio.KoodistoURI.KOODISTO_OPPILAITOSTYYPPI_URI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Label;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.koodisto.widget.KoodistoComponent;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.model.types.YhteystietojenTyyppiDTO;
import fi.vm.sade.organisaatio.revised.ui.helper.KoodistoUriAndVersionFieldFormatter;
import fi.vm.sade.organisaatio.revised.ui.helper.KoodistoUriFieldFormatter;
import fi.vm.sade.vaadin.util.UiUtil;

/**
 * A component for managing organisaatiotyyppi and oppilaitostyyppi selections in YhteystietojenTyyppiForm.
 * 
 * @author markus
 *
 */
@Configurable(preConstruction = false)
public class SovellettavatTyypitOptions extends VerticalLayout {
    
    private static final Logger LOG = LoggerFactory.getLogger(SovellettavatTyypitOptions.class);
    
    private OptionGroup sovellettavatOrganisaatiotyypitStart;
    private CheckBox valitseKaikkiOppilaitokset;
    private KoodistoComponent koodistoOppilaitostyypit;
    private OptionGroup sovellettavatOrganisaatiotyypitEnd;
    
    private YhteystietojenTyyppiDTO model;
    
    public SovellettavatTyypitOptions(YhteystietojenTyyppiDTO model) {
        this.model = model;
        constructFields();
    }
    
    public OptionGroup getSovellettavatOrganisaatiotyypitStart() {
    	return sovellettavatOrganisaatiotyypitStart;
    }
    public OptionGroup getSovellettavatOrganisaatiotyypitEnd() {
    	return sovellettavatOrganisaatiotyypitEnd;
    }
    public KoodistoComponent getKoodistoOppilaitostyypit() {
    	return koodistoOppilaitostyypit;
    }    
    
    /**
     * Construction of organisaatiotyypit and oppilaitostyypit.
     */
    private void constructFields() {
        //DEBUGSAWAY:LOG.debug("Starting construction of fields");
        //luodaan ensin koulutustoimija ja oppilaitos valinnat
        createOrganisaatiotyyppiStart();
        //oppilaitosvalinnan jälkeen tulevat eri oppilaitostyypit koodistosta
        constructKoodistoFields();
        //oppilaitostyyppivalinnan jälkeen loput organisaatiotyypit
        createOrganisaatiotyyppiEnd();
        valitseKaikkiOppilaitokset.addListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                adjustOppilaitostyypit();
            }

        });
    }
    
    /**
     * koodistoelementtien luominen
     */
    private void constructKoodistoFields() {
        koodistoOppilaitostyypit = createKoodistoComponent(KOODISTO_OPPILAITOSTYYPPI_URI, "YhteystietojenTyyppiForm.FI", "sovellettavatOppilaitostyyppis");
        koodistoOppilaitostyypit.setFieldValueFormatter(new KoodistoUriAndVersionFieldFormatter());
        koodistoOppilaitostyypit.setEnabled(true);
        //DEBUGSAWAY:LOG.debug("Sovellettavat oppilaitostyypit: " + model.getSovellettavatOppilaitostyyppis().size());
        koodistoOppilaitostyypit.setValue(model.getSovellettavatOppilaitostyyppis());     
    }
    
    /**
     * valintojen tallentaminen
     */
    public void save() {
        model.getSovellettavatOrganisaatios().clear();
        model.getSovellettavatOrganisaatios().addAll(getSovellettavatOrganisaatios());
        model.getSovellettavatOppilaitostyyppis().clear();
        model.getSovellettavatOppilaitostyyppis().addAll(getOppilaitostyypit());
        //DEBUGSAWAY:LOG.debug("SovellettavatOppilaitostyyppis size: " + model.getSovellettavatOppilaitostyyppis().size());
    }
    
    private List<String> getOppilaitostyypit() {
        List<String> optyyppis = new ArrayList<String>();
        for (String curStr : (Collection<String>)(koodistoOppilaitostyypit.getValue())) {
            optyyppis.add(curStr);
        }
        return optyyppis;
    }
    
    private KoodistoComponent createKoodistoComponent(String uri, String captionKey, String propertyId) {
        KoodistoComponent component = ComponentBuilder.koodistoOptionGroup(uri)
                .withDebugId(propertyId + "" + System.currentTimeMillis())         
                .build();
        HorizontalLayout hl = new HorizontalLayout();
        Label placeHolder = new Label("&nbsp;&nbsp;&nbsp;", Label.CONTENT_XHTML);
        hl.addComponent(placeHolder);
        hl.addComponent(component);
        this.addComponent(hl);
        return component;
    }
    
    private void createOrganisaatiotyyppiStart() {
        try {
            sovellettavatOrganisaatiotyypitStart = new OptionGroup("");
            VerticalLayout vert = new VerticalLayout();
            vert.setSizeUndefined();
            sovellettavatOrganisaatiotyypitStart.setMultiSelect(true);
            createOrganisatiotyyppiSelectionStart();
            vert.addComponent(sovellettavatOrganisaatiotyypitStart);
            this.valitseKaikkiOppilaitokset = UiUtil.checkbox(vert, I18N.getMessage("YhteystietojenTyyppiForm.kaikkiOppilaitokset"));
            this.valitseKaikkiOppilaitokset.setImmediate(true);
            this.addComponent(vert);
        } catch (Exception ex) {
            LOG.warn("Unable to create organisaatiotyyppi component : " + ex.getMessage());
        }
    }

    private void createOrganisatiotyyppiSelectionStart() {
        
        sovellettavatOrganisaatiotyypitStart.addItem(OrganisaatioTyyppi.KOULUTUSTOIMIJA.value());
        sovellettavatOrganisaatiotyypitStart.setItemCaption(OrganisaatioTyyppi.KOULUTUSTOIMIJA.value(), I18N.getMessage(OrganisaatioTyyppi.KOULUTUSTOIMIJA.name()));
        sovellettavatOrganisaatiotyypitStart.addItem(OrganisaatioTyyppi.OPPILAITOS.value());
        sovellettavatOrganisaatiotyypitStart.setItemCaption(OrganisaatioTyyppi.OPPILAITOS.value(), I18N.getMessage(OrganisaatioTyyppi.OPPILAITOS.name()));
        

        if ((model != null) && (model.getSovellettavatOrganisaatios() != null)) {
            List<String> values = new ArrayList<String>();
            for (OrganisaatioTyyppi curSelected : model.getSovellettavatOrganisaatios()) {
                //DEBUGSAWAY:LOG.debug("Organisaatiotyyppi: " + curSelected);
                if (curSelected.value().equals(OrganisaatioTyyppi.KOULUTUSTOIMIJA.value()) || curSelected.value().equals(OrganisaatioTyyppi.OPPILAITOS.value())) {
                    values.add(curSelected.value());
                }
            }
            sovellettavatOrganisaatiotyypitStart.setValue(values);
        }
        
    }
    
    /**
     * Selecting all oppilaitostyypit.
     */
    private void adjustOppilaitostyypit() {
        if (this.valitseKaikkiOppilaitokset.booleanValue()) {
            selectAllOppilaitokset();
        } else {
           unSelectAllOppilaitokset(); 
        }
    }
    
    private void unSelectAllOppilaitokset() {
        this.koodistoOppilaitostyypit.getField().setValue(new ArrayList<String>());
    }

    private void selectAllOppilaitokset() {
        List<String> values = new ArrayList<String>();
        for (Object curOPTyyppi : this.koodistoOppilaitostyypit.getField().getItemIds()) {
            values.add((String)curOPTyyppi);
        }
        this.koodistoOppilaitostyypit.getField().setValue(values);
    }
    
    private void createOrganisaatiotyyppiEnd() {
        try {
            sovellettavatOrganisaatiotyypitEnd = new OptionGroup("");
            VerticalLayout vert = new VerticalLayout();
            vert.setSizeUndefined();
            sovellettavatOrganisaatiotyypitEnd.setMultiSelect(true);
            createOrganisatiotyyppiSelectionEnd();
            vert.addComponent(sovellettavatOrganisaatiotyypitEnd);
            this.addComponent(vert);
        } catch (Exception ex) {
            LOG.warn("Unable to create organisaatiotyyppi component : " + ex.getMessage());
        }
    }
    
    private void createOrganisatiotyyppiSelectionEnd() {
        //DEBUGSAWAY:LOG.debug("Creating organisaatiotyyppiSelectionEnd ");
        sovellettavatOrganisaatiotyypitEnd.addItem(OrganisaatioTyyppi.OPETUSPISTE.value());
        sovellettavatOrganisaatiotyypitEnd.setItemCaption(OrganisaatioTyyppi.OPETUSPISTE.value(), I18N.getMessage(OrganisaatioTyyppi.OPETUSPISTE.name()));
        sovellettavatOrganisaatiotyypitEnd.addItem(OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE.value());
        sovellettavatOrganisaatiotyypitEnd.setItemCaption(OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE.value(), I18N.getMessage(OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE.name()));
        sovellettavatOrganisaatiotyypitEnd.addItem(OrganisaatioTyyppi.MUU_ORGANISAATIO.value());
        sovellettavatOrganisaatiotyypitEnd.setItemCaption(OrganisaatioTyyppi.MUU_ORGANISAATIO.value(), I18N.getMessage(OrganisaatioTyyppi.MUU_ORGANISAATIO.name()));

        if ((model != null) && (model.getSovellettavatOrganisaatios() != null)) {
            List<String> values = new ArrayList<String>();
            for (OrganisaatioTyyppi curSelected : model.getSovellettavatOrganisaatios()) {
                //DEBUGSAWAY:LOG.debug("Organisaatiotyyppi: " + curSelected);
                if (curSelected.value().equals(OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE.value()) 
                        || curSelected.value().equals(OrganisaatioTyyppi.MUU_ORGANISAATIO.value())
                        || curSelected.value().equals(OrganisaatioTyyppi.OPETUSPISTE.value())) {
                    //DEBUGSAWAY:LOG.debug("Organisaatiotyyppi: " + curSelected);
                    values.add(curSelected.value());
                }
            }
            sovellettavatOrganisaatiotyypitEnd.setValue(values);
        }
    }

    private List<OrganisaatioTyyppi> getSovellettavatOrganisaatios() {
        List<OrganisaatioTyyppi> sovellettavatOrgs = new ArrayList<OrganisaatioTyyppi>();
        for (String curTyyppi : (Collection<String>) (this.sovellettavatOrganisaatiotyypitStart.getValue())) {
            //DEBUGSAWAY:LOG.debug("getSovellettavatOrganisaatios curTyyppi: " + curTyyppi);
            OrganisaatioTyyppi resolvedOrgType = resolveOrganisaatiotyyppi(curTyyppi, OrganisaatioTyyppi.values());
            if (resolvedOrgType != null && !sovellettavatOrgs.contains(resolvedOrgType)) {
                
                sovellettavatOrgs.add(resolvedOrgType);
            }
        }
        for (String curTyyppi : (Collection<String>) (this.sovellettavatOrganisaatiotyypitEnd.getValue())) {
            //DEBUGSAWAY:LOG.debug("getSovellettavatOrganisaatios curTyyppi: " + curTyyppi);
            OrganisaatioTyyppi resolvedOrgType = resolveOrganisaatiotyyppi(curTyyppi, OrganisaatioTyyppi.values());
            if (resolvedOrgType != null && !sovellettavatOrgs.contains(resolvedOrgType)) {
                
                sovellettavatOrgs.add(resolvedOrgType);
            }
        }
        
        //DEBUGSAWAY:LOG.debug("sovellettavatOrgs size: " + sovellettavatOrgs.size());
        return sovellettavatOrgs;
    }

    private OrganisaatioTyyppi resolveOrganisaatiotyyppi(String typeStr, OrganisaatioTyyppi[] allTyypit) {
        for (OrganisaatioTyyppi curTyyppi : allTyypit) {
            if (typeStr.equals(curTyyppi.value())) {
                return curTyyppi;
            }
        }
        return null;
    }

}
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

import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.addon.formbinder.FormFieldMatch;
import org.vaadin.addon.formbinder.FormView;
import org.vaadin.addon.formbinder.PropertyId;

import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.common.I18NHelper;
import fi.vm.sade.generic.ui.component.OphRichTextArea;
import fi.vm.sade.generic.ui.validation.JSR303FieldValidator;
import fi.vm.sade.organisaatio.revised.ui.event.KuvausEvent;
import fi.vm.sade.organisaatio.ui.model.LOPTiedotModel;
import fi.vm.sade.organisaatio.ui.util.UiUtils;
import fi.vm.sade.vaadin.Oph;
import fi.vm.sade.vaadin.util.UiUtil;

/**
 * The the kuvailevat tiedot rich text field form.
 *
 * @author Markus
 */
@FormView(matchFieldsBy = FormFieldMatch.ANNOTATION)
@Configurable(preConstruction = false)
class LOPKuvauksetFormView extends VerticalLayout {
	
	private static final int MAX_LENGTH = 16384;
    private static final int INFO_MAX_LENGTH = 1500;

	/**
	 * LOP yleiskuvaus
	 */
    @PropertyId("yleiskuvaus")
    private OphRichTextArea yleiskuvaus;
    @PropertyId("esteettomyys")
    private OphRichTextArea esteettomyys;
    @PropertyId("oppimisymparisto")
    private OphRichTextArea oppimisymparisto;
    @PropertyId("vuosikello")
    private OphRichTextArea vuosikello;
    @PropertyId("vastuuhenkilot")
    private OphRichTextArea vastuuhenkilot;
    @PropertyId("valintamenettely")
    private OphRichTextArea valintamenettely;
    @PropertyId("aiempiOsaaminen")
    private OphRichTextArea aiempiOsaaminen;
    @PropertyId("kieliopinnot")
    private OphRichTextArea kieliopinnot;
    @PropertyId("tyoharjoittelu")
    private OphRichTextArea tyoharjoittelu;
    @PropertyId("opiskelijaliikkuvuus")
    private OphRichTextArea opiskelijaliikkuvuus;
    @PropertyId("kansainvalisyys")
    private OphRichTextArea kansainvalisyys;

    private LOPTiedotModel model;

    private I18NHelper i18n = new I18NHelper(this);
    private String kieli;
    
    private Property.ValueChangeListener changeListener;

    LOPKuvauksetFormView(LOPTiedotModel model, String kieli, Property.ValueChangeListener listener) {
        super();
        this.changeListener = listener;
        this.model = model;
        this.kieli = kieli;
        createRichTextEditor();
    }

    public LOPTiedotModel getModel() {
        return this.model;
    }

    /**
     * Creation of the form layout containing all the rich text editors used.
     */
    private void createRichTextEditor() {
        setSpacing(true);
        setMargin(true);

        addComponent(createKuvausButtons());

        {
            Label otsikko = UiUtil.label(this, T("yleiskuvaLabel"));
            otsikko.addStyleName(Oph.LABEL_H2);
            Label ohje = UiUtil.label(this, T("yleiskuvaOhje", INFO_MAX_LENGTH));
            ohje.addStyleName(Oph.LABEL_SMALL);
            ohje.setWidth("100%");
            yleiskuvaus = UiUtil.richTextArea(this, null, null, MAX_LENGTH,
                    T("_textTooLong", T("yleiskuvaLabel") + " (" + kieli + ")", MAX_LENGTH));
            yleiskuvaus.setInputPrompt(T("_maksimipituus", MAX_LENGTH));
            yleiskuvaus.setImmediate(false);
            yleiskuvaus.addListener(this.changeListener);

            yleiskuvaus.setWidth("100%");
            VerticalLayout vl = UiUtil.verticalLayout();
            vl.setHeight("50px");
            addComponent(vl);
        }


        {
            Label otsikko = UiUtil.label(this, T("esteettomyysLabel"));
            otsikko.addStyleName(Oph.LABEL_H2);
            Label ohje = UiUtil.label(this, T("esteettomyysOhje", INFO_MAX_LENGTH));
            ohje.addStyleName(Oph.LABEL_SMALL);
            ohje.setWidth("100%");
            esteettomyys = UiUtil.richTextArea(this, null, null, MAX_LENGTH,
                    T("_textTooLong", T("esteettomyysLabel") + " (" + kieli + ")", MAX_LENGTH));
            esteettomyys.setInputPrompt(T("_maksimipituus", MAX_LENGTH));
            esteettomyys.setImmediate(false);
            esteettomyys.addListener(this.changeListener);
            esteettomyys.setWidth("100%");
            VerticalLayout vl = UiUtil.verticalLayout();
            vl.setHeight("50px");
            addComponent(vl);
        }

        {
            Label otsikko = UiUtil.label(this, T("oppimisymparistoLabel"));
            otsikko.addStyleName(Oph.LABEL_H2);
            Label ohje = UiUtil.label(this, T("oppimisymparistoOhje", INFO_MAX_LENGTH));
            ohje.addStyleName(Oph.LABEL_SMALL);
            ohje.setWidth("100%");
            oppimisymparisto = UiUtil.richTextArea(this, null, null, MAX_LENGTH,
                    T("_textTooLong", T("oppimisymparistoLabel") + " (" + kieli + ")", MAX_LENGTH));
            oppimisymparisto.setWidth("100%");
            oppimisymparisto.setInputPrompt(T("_maksimipituus", MAX_LENGTH));
            oppimisymparisto.setImmediate(false);
            oppimisymparisto.addListener(this.changeListener);
            VerticalLayout vl = UiUtil.verticalLayout();
            vl.setHeight("50px");
            addComponent(vl);
        }

        {
            Label otsikko = UiUtil.label(this, T("opetukseVuosikelloLabel"));
            otsikko.addStyleName(Oph.LABEL_H2);
            Label ohje = UiUtil.label(this, T("opetukseVuosikelloOhje"));
            ohje.addStyleName(Oph.LABEL_SMALL);
            ohje.setWidth("100%");
            vuosikello = UiUtil.richTextArea(this, null, null, MAX_LENGTH,
                    T("_textTooLong", T("opetukseVuosikelloLabel") + " (" + kieli + ")", MAX_LENGTH));
            vuosikello.setWidth("100%");
            vuosikello.setInputPrompt(T("_maksimipituus", MAX_LENGTH));
            vuosikello.setImmediate(false);
            vuosikello.addListener(this.changeListener);
            VerticalLayout vl = UiUtil.verticalLayout();
            vl.setHeight("50px");
            addComponent(vl);
        }

        {
            Label otsikko = UiUtil.label(this, T("vastuuhenkilotLabel"));
            otsikko.addStyleName(Oph.LABEL_H2);
            Label ohje = UiUtil.label(this, T("vastuuhenkilotOhje"));
            ohje.addStyleName(Oph.LABEL_SMALL);
            ohje.setWidth("100%");
            vastuuhenkilot = UiUtil.richTextArea(this, null, null, MAX_LENGTH,
                    T("_textTooLong", T("vastuuhenkilotLabel") + " (" + kieli + ")", MAX_LENGTH));
            vastuuhenkilot.setWidth("100%");
            vastuuhenkilot.setInputPrompt(T("_maksimipituus", MAX_LENGTH));
            vastuuhenkilot.setImmediate(false);
            vastuuhenkilot.addListener(this.changeListener);
            VerticalLayout vl = UiUtil.verticalLayout();
            vl.setHeight("50px");
            addComponent(vl);
        }

        {
            Label otsikko = UiUtil.label(this, T("valintamenettelyLabel"));
            otsikko.addStyleName(Oph.LABEL_H2);
            Label ohje = UiUtil.label(this, T("valintamenettelyOhje"));
            ohje.addStyleName(Oph.LABEL_SMALL);
            ohje.setWidth("100%");
            valintamenettely = UiUtil.richTextArea(this, null, null, MAX_LENGTH,
                    T("_textTooLong", T("valintamenettelyLabel") + " (" + kieli + ")", MAX_LENGTH));
            valintamenettely.setWidth("100%");
            valintamenettely.setInputPrompt(T("_maksimipituus", MAX_LENGTH));
            valintamenettely.setImmediate(false);
            valintamenettely.addListener(this.changeListener);
            VerticalLayout vl = UiUtil.verticalLayout();
            vl.setHeight("50px");
            addComponent(vl);
        }

        {
            Label otsikko = UiUtil.label(this, T("aiemminHankittuLabel"));
            otsikko.addStyleName(Oph.LABEL_H2);
            Label ohje = UiUtil.label(this, T("aiemminHankittuOhje"));
            ohje.addStyleName(Oph.LABEL_SMALL);
            ohje.setWidth("100%");
            aiempiOsaaminen = UiUtil.richTextArea(this, null, null, MAX_LENGTH,
                    T("_textTooLong", T("aiemminHankittuLabel") + " (" + kieli + ")", MAX_LENGTH));
            aiempiOsaaminen.setWidth("100%");
            aiempiOsaaminen.setInputPrompt(T("_maksimipituus", MAX_LENGTH));
            aiempiOsaaminen.setImmediate(false);
            aiempiOsaaminen.addListener(this.changeListener);
            VerticalLayout vl = UiUtil.verticalLayout();
            vl.setHeight("50px");
            addComponent(vl);
        }

        {
            Label otsikko = UiUtil.label(this, T("kieliopinnotLabel"));
            otsikko.addStyleName(Oph.LABEL_H2);
            Label ohje = UiUtil.label(this, T("kieliopinnotOhje"));
            ohje.addStyleName(Oph.LABEL_SMALL);
            ohje.setWidth("100%");
            kieliopinnot = UiUtil.richTextArea(this, null, null, MAX_LENGTH,
                    T("_textTooLong", T("kieliopinnotLabel") + " (" + kieli + ")", MAX_LENGTH));
            kieliopinnot.setWidth("100%");
            kieliopinnot.setInputPrompt(T("_maksimipituus", MAX_LENGTH));
            kieliopinnot.setImmediate(false);
            kieliopinnot.addListener(this.changeListener);
            VerticalLayout vl = UiUtil.verticalLayout();
            vl.setHeight("50px");
            addComponent(vl);
        }

        {
            Label otsikko = UiUtil.label(this, T("tyoharjoitteluLabel"));
            otsikko.addStyleName(Oph.LABEL_H2);
            Label ohje = UiUtil.label(this, T("tyoharjoitteluOhje"));
            ohje.addStyleName(Oph.LABEL_SMALL);
            ohje.setWidth("100%");
            tyoharjoittelu = UiUtil.richTextArea(this, null, null, MAX_LENGTH,
                    T("_textTooLong", T("tyoharjoitteluLabel") + " (" + kieli + ")", MAX_LENGTH));
            tyoharjoittelu.setWidth("100%");
            tyoharjoittelu.setInputPrompt(T("_maksimipituus", MAX_LENGTH));
            tyoharjoittelu.setImmediate(false);
            tyoharjoittelu.addListener(this.changeListener);
            VerticalLayout vl = UiUtil.verticalLayout();
            vl.setHeight("50px");
            addComponent(vl);
        }

        {
            Label otsikko = UiUtil.label(this, T("opiskelijaliikkuvuusLabel"));
            otsikko.addStyleName(Oph.LABEL_H2);
            Label ohje = UiUtil.label(this, T("opiskelijaliikkuvuusOhje"));
            ohje.addStyleName(Oph.LABEL_SMALL);
            ohje.setWidth("100%");
            opiskelijaliikkuvuus = UiUtil.richTextArea(this, null, null, MAX_LENGTH,
                    T("_textTooLong", T("opiskelijaliikkuvuusLabel") + " (" + kieli + ")", MAX_LENGTH));
            opiskelijaliikkuvuus.setWidth("100%");
            opiskelijaliikkuvuus.setInputPrompt(T("_maksimipituus", MAX_LENGTH));
            opiskelijaliikkuvuus.setImmediate(false);
            opiskelijaliikkuvuus.addListener(this.changeListener);
            VerticalLayout vl = UiUtil.verticalLayout();
            vl.setHeight("50px");
            addComponent(vl);
        }

        {
            Label otsikko = UiUtil.label(this, T("kansainvalisetOhjelmatLabel"));
            otsikko.addStyleName(Oph.LABEL_H2);
            Label ohje = UiUtil.label(this, T("kansainvalisetOhjelmatOhje"));
            ohje.addStyleName(Oph.LABEL_SMALL);
            ohje.setWidth("100%");
            kansainvalisyys = UiUtil.richTextArea(this, null, null, MAX_LENGTH,
                    T("_textTooLong", T("kansainvalisetOhjelmatLabel") + " (" + kieli + ")", MAX_LENGTH));
            kansainvalisyys.setWidth("100%");
            kansainvalisyys.setInputPrompt(T("_maksimipituus", MAX_LENGTH));
            kansainvalisyys.setImmediate(false);
            kansainvalisyys.addListener(this.changeListener);
            VerticalLayout vl = UiUtil.verticalLayout();
            vl.setHeight("50px");
            addComponent(vl);
        }

        JSR303FieldValidator.addValidatorsBasedOnAnnotations(this);

    }

    private HorizontalLayout createKuvausButtons() {
        HorizontalLayout hl = UiUtil.horizontalLayout();
        hl.setSpacing(false);
        hl.setMargin(false);
        hl.setHeight("50px");
        HorizontalLayout hl1 = UiUtil.horizontalLayout();
        hl1.setSizeUndefined();
        UiUtil.button(hl1, T("Tallenna"), new Button.ClickListener() {

            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {
                startSave();
            }
        });

        hl.addComponent(hl1);
        hl.setComponentAlignment(hl1, Alignment.MIDDLE_LEFT);
        return hl;
    }

    private void startSave() {
        fireEvent(new KuvausEvent(this, KuvausEvent.SAVE));
    }


    private String T(String key, Object... args) {
        return i18n.getMessage(key, args);
    }
}

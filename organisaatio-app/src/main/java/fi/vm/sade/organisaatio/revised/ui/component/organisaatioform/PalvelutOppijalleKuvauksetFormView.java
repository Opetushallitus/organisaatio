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
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import fi.vm.sade.generic.common.I18NHelper;
import fi.vm.sade.generic.ui.component.OphRichTextArea;
import fi.vm.sade.organisaatio.revised.ui.event.KuvausEvent;
import fi.vm.sade.organisaatio.ui.model.LOPTiedotModel;
import fi.vm.sade.vaadin.Oph;
import fi.vm.sade.vaadin.util.UiUtil;

/**
 * Form for editing palvelut oppijalle descriptions in one language.
 * 
 * @author Markus
 */
@FormView(matchFieldsBy = FormFieldMatch.ANNOTATION)
@Configurable(preConstruction = false)
class PalvelutOppijalleKuvauksetFormView extends VerticalLayout {

	private static final int MAX_LENGTH = 16384;
	private static final int INFO_MAX_LENGTH = 1500;

	private static final long serialVersionUID = 1L;

	@PropertyId("kustannukset")
	private OphRichTextArea kustannukset;
	@PropertyId("rahoitus")
	private OphRichTextArea rahoitus;
	@PropertyId("opiskelijaruokailu")
	private OphRichTextArea opiskelijaruokailu;
	@PropertyId("terveydenhuolto")
	private OphRichTextArea terveydenhuolto;
	@PropertyId("vakuutukset")
	private OphRichTextArea vakuutukset;
	@PropertyId("opiskelijaliikunta")
	private OphRichTextArea opiskelijaliikunta;
	@PropertyId("vapaaAika")
	private OphRichTextArea vapaaAika;
	@PropertyId("opiskelijaJarjestot")
	private OphRichTextArea opiskelijaJarjestot;
	@PropertyId("tietoaAsumisesta")
	private OphRichTextArea tietoaAsumisesta;

	private final LOPTiedotModel model;

	private final String kieli;

	private final I18NHelper i18n = new I18NHelper(this);

	private final Property.ValueChangeListener changeListener;

	public PalvelutOppijalleKuvauksetFormView(LOPTiedotModel model,
			String kieli, Property.ValueChangeListener listener) {
		setSpacing(true);
		setMargin(true);
		this.changeListener = listener;
		this.kieli = kieli;
		createRichTextEditor();
		this.model = model;
	}

	/**
	 * Creation of the form.
	 */
	private void createRichTextEditor() {

		addComponent(createKuvausButtons());

		{
			Label otsikko = UiUtil.label(this, T("kustannuksetLabel"));
			otsikko.addStyleName(Oph.LABEL_H2);
			Label ohje = UiUtil.label(this,
					T("kustannuksetOhje", INFO_MAX_LENGTH));
			ohje.addStyleName(Oph.LABEL_SMALL);
			ohje.setWidth("100%");
			kustannukset = UiUtil.richTextArea(
					this,
					null,
					null,
					MAX_LENGTH,
					T("_textTooLong", T("kustannuksetLabel") + " (" + kieli
							+ ")", MAX_LENGTH));
			kustannukset.setInputPrompt(T("_maksimipituus", MAX_LENGTH));
			kustannukset.setImmediate(false);
			kustannukset.addListener(this.changeListener);
			kustannukset.setWidth("100%");
			VerticalLayout vl = UiUtil.verticalLayout();
			vl.setHeight("50px");
			addComponent(vl);
		}

		{
			Label otsikko = UiUtil.label(this, T("tietoAsumisestaLabel"));
			otsikko.addStyleName(Oph.LABEL_H2);
			Label ohje = UiUtil.label(this,
					T("tietoaAsumisestaOhje", INFO_MAX_LENGTH));
			ohje.addStyleName(Oph.LABEL_SMALL);
			ohje.setWidth("100%");
			tietoaAsumisesta = UiUtil.richTextArea(
					this,
					null,
					null,
					MAX_LENGTH,
					T("_textTooLong", T("tietoAsumisestaLabel") + " (" + kieli
							+ ")", MAX_LENGTH));
			tietoaAsumisesta.setInputPrompt(T("_maksimipituus", MAX_LENGTH));
			tietoaAsumisesta.setImmediate(false);
			tietoaAsumisesta.addListener(this.changeListener);
			tietoaAsumisesta.setWidth("100%");
			VerticalLayout vl = UiUtil.verticalLayout();
			vl.setHeight("50px");
			addComponent(vl);
		}

		{
			Label otsikko = UiUtil.label(this, T("rahoitusLabel"));
			otsikko.addStyleName(Oph.LABEL_H2);
			Label ohje = UiUtil.label(this, T("rahoitusOhje"));
			ohje.addStyleName(Oph.LABEL_SMALL);
			ohje.setWidth("100%");
			rahoitus = UiUtil.richTextArea(
					this,
					null,
					null,
					MAX_LENGTH,
					T("_textTooLong", T("rahoitusLabel") + " (" + kieli + ")",
							MAX_LENGTH));
			rahoitus.setInputPrompt(T("_maksimipituus", MAX_LENGTH));
			rahoitus.setImmediate(false);
			rahoitus.addListener(this.changeListener);
			rahoitus.setWidth("100%");
			VerticalLayout vl = UiUtil.verticalLayout();
			vl.setHeight("50px");
			addComponent(vl);
		}

		{
			Label otsikko = UiUtil.label(this, T("ruokailuLabel"));
			otsikko.addStyleName(Oph.LABEL_H2);
			Label ohje = UiUtil.label(this, T("ruokailuOhje", INFO_MAX_LENGTH));
			ohje.addStyleName(Oph.LABEL_SMALL);
			ohje.setWidth("100%");
			opiskelijaruokailu = UiUtil.richTextArea(
					this,
					null,
					null,
					MAX_LENGTH,
					T("_textTooLong", T("ruokailuLabel") + " (" + kieli + ")",
							MAX_LENGTH));
			opiskelijaruokailu.setInputPrompt(T("_maksimipituus", MAX_LENGTH));
			opiskelijaruokailu.setImmediate(false);
			opiskelijaruokailu.addListener(this.changeListener);
			opiskelijaruokailu.setWidth("100%");
			VerticalLayout vl = UiUtil.verticalLayout();
			vl.setHeight("50px");
			addComponent(vl);
		}

		{
			Label otsikko = UiUtil.label(this, T("terveydenhuoltoLabel"));
			otsikko.addStyleName(Oph.LABEL_H2);
			Label ohje = UiUtil.label(this,
					T("terveydenhuoltoOhje", INFO_MAX_LENGTH));
			ohje.addStyleName(Oph.LABEL_SMALL);
			ohje.setWidth("100%");
			terveydenhuolto = UiUtil.richTextArea(
					this,
					null,
					null,
					MAX_LENGTH,
					T("_textTooLong", T("terveydenhuoltoLabel") + " (" + kieli
							+ ")", MAX_LENGTH));
			terveydenhuolto.setInputPrompt(T("_maksimipituus", MAX_LENGTH));
			terveydenhuolto.setImmediate(false);
			terveydenhuolto.addListener(this.changeListener);
			terveydenhuolto.setWidth("100%");
			VerticalLayout vl = UiUtil.verticalLayout();
			vl.setHeight("50px");
			addComponent(vl);
		}

		{
			Label otsikko = UiUtil.label(this, T("vakuutuksetLabel"));
			otsikko.addStyleName(Oph.LABEL_H2);
			Label ohje = UiUtil.label(this, T("vakuutuksetOhje"));
			ohje.addStyleName(Oph.LABEL_SMALL);
			ohje.setWidth("100%");
			vakuutukset = UiUtil.richTextArea(
					this,
					null,
					null,
					MAX_LENGTH,
					T("_textTooLong", T("vakuutuksetLabel") + " (" + kieli
							+ ")", MAX_LENGTH));
			vakuutukset.setInputPrompt(T("_maksimipituus", MAX_LENGTH));
			vakuutukset.setImmediate(false);
			vakuutukset.addListener(this.changeListener);
			vakuutukset.setWidth("100%");
			VerticalLayout vl = UiUtil.verticalLayout();
			vl.setHeight("50px");
			addComponent(vl);
		}

		{
			Label otsikko = UiUtil.label(this, T("opiskelijaliikuntaLabel"));
			otsikko.addStyleName(Oph.LABEL_H2);
			Label ohje = UiUtil.label(this, T("opiskelijaliikuntaOhje"));
			ohje.addStyleName(Oph.LABEL_SMALL);
			ohje.setWidth("100%");
			opiskelijaliikunta = UiUtil.richTextArea(
					this,
					null,
					null,
					MAX_LENGTH,
					T("_textTooLong", T("opiskelijaliikuntaLabel") + " ("
							+ kieli + ")", MAX_LENGTH));
			opiskelijaliikunta.setInputPrompt(T("_maksimipituus", MAX_LENGTH));
			opiskelijaliikunta.setImmediate(false);
			opiskelijaliikunta.addListener(this.changeListener);
			opiskelijaliikunta.setWidth("100%");
			VerticalLayout vl = UiUtil.verticalLayout();
			vl.setHeight("50px");
			addComponent(vl);
		}

		{
			Label otsikko = UiUtil.label(this, T("vapaaAikaLabel"));
			otsikko.addStyleName(Oph.LABEL_H2);
			Label ohje = UiUtil.label(this, T("vapaaAikaOhje"));
			ohje.addStyleName(Oph.LABEL_SMALL);
			ohje.setWidth("100%");
			vapaaAika = UiUtil.richTextArea(
					this,
					null,
					null,
					MAX_LENGTH,
					T("_textTooLong", T("vapaaAikaLabel") + " (" + kieli + ")",
							MAX_LENGTH));
			vapaaAika.setInputPrompt(T("_maksimipituus", MAX_LENGTH));
			vapaaAika.setImmediate(false);
			vapaaAika.addListener(this.changeListener);
			vapaaAika.setWidth("100%");
			VerticalLayout vl = UiUtil.verticalLayout();
			vl.setHeight("50px");
			addComponent(vl);
		}

		{
			Label otsikko = UiUtil.label(this, T("opiskelijajarjestotLabel"));
			otsikko.addStyleName(Oph.LABEL_H2);
			Label ohje = UiUtil.label(this, T("opiskelijajarjestotOhje"));
			ohje.addStyleName(Oph.LABEL_SMALL);
			ohje.setWidth("100%");
			opiskelijaJarjestot = UiUtil.richTextArea(
					this,
					null,
					null,
					MAX_LENGTH,
					T("_textTooLong", T("opiskelijajarjestotLabel") + " ("
							+ kieli + ")", MAX_LENGTH));
			opiskelijaJarjestot.setInputPrompt(T("_maksimipituus", MAX_LENGTH));
			opiskelijaJarjestot.setImmediate(false);
			opiskelijaJarjestot.addListener(this.changeListener);
			opiskelijaJarjestot.setWidth("100%");
			VerticalLayout vl = UiUtil.verticalLayout();
			vl.setHeight("50px");
			addComponent(vl);
		}

	}

	/**
	 * Creating the save and preview buttons.
	 * 
	 * @return
	 */
	private HorizontalLayout createKuvausButtons() {
		HorizontalLayout hl = UiUtil.horizontalLayout();
		hl.setSpacing(false);
		hl.setMargin(false);
		hl.setHeight("50px");
		HorizontalLayout hl1 = UiUtil.horizontalLayout();
		hl1.setSizeUndefined();
		UiUtil.button(hl1, T("Tallenna"), new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				startSave();
			}
		});

		hl.addComponent(hl1);

		hl.setComponentAlignment(hl1, Alignment.MIDDLE_LEFT);
		return hl;
	}

	/**
	 * Save button click is fired as event to a listening parent container.
	 */
	private void startSave() {
		fireEvent(new KuvausEvent(this, KuvausEvent.SAVE));
	}

	public LOPTiedotModel getModel() {
		return model;
	}

	private String T(String key, Object... args) {
		return i18n.getMessage(key, args);
	}

}

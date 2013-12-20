/*
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
package fi.vm.sade.organisaatio.revised.ui.component.search;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.common.I18NHelper;
import fi.vm.sade.organisaatio.helper.OrganisaatioDisplayHelper;
import fi.vm.sade.organisaatio.ui.PortletRole;
import fi.vm.sade.organisaatio.ui.UserContext;
import fi.vm.sade.vaadin.util.UiUtil;

class SearchRestrictionComponent extends VerticalLayout {

    private final Logger logger = LoggerFactory.getLogger(SearchRestrictionComponent.class);
    private I18NHelper i18n = new I18NHelper(getClass());
    private static final long serialVersionUID = 1L;
    private Label organisaatioNimi;
    private Label kaikkiValittuNimi;
    private Button poistaValintaB;
    private Button asetaOletusB;
    private Label caption;

    SearchRestrictionComponent() {
        super();
        this.setMargin(false, false, false, true);
        this.setSizeUndefined();
    }

    
    @Override
    public void attach() {

        final UserContext context = PortletRole.getInstance().getUserContext();

        logger.info("attaching(), oph-oid:" + context.getOphOid() + " my oids: " + context.getUserOrganisaatios() + " is oph user:" + context.isOPHUser() + " org:" + context.getUserOrganisaatios()); 

        if (context != null) {
            if (context.getUserOrganisaatios()!=null && context.isShowRestrictionComponent() && poistaValintaB == null) {
                buildLayout();
            }
        }
    }

    private void setValittu(boolean val) {
        PortletRole.getInstance().getUserContext().setUseRestriction(val);
        poistaValintaB.setVisible(val);
        asetaOletusB.setVisible(!val);
        kaikkiValittuNimi.setVisible(!val);
        organisaatioNimi.setVisible(val);
    }

    private void buildLayout() {

        final HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        caption = UiUtil.label(hl,  i18n.getMessage("valittuOrganisaatio"));
        organisaatioNimi = UiUtil.label(hl, PortletRole.getInstance().getUserContext().getOrgTitle());
        organisaatioNimi.setSizeUndefined();
        kaikkiValittuNimi = UiUtil.label(hl, i18n.getMessage("kaikkiValittu"));
        organisaatioNimi.setSizeUndefined();
        poistaValintaB = UiUtil.buttonLink(hl, i18n.getMessage("poistaOrganisaatioValinta"));
        poistaValintaB.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                setValittu(false);
            }
        });

        asetaOletusB = UiUtil.buttonLink(hl, i18n.getMessage("palautaOletusOrganisaatioValinta"));
        asetaOletusB.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                setValittu(true);
            }
        });
        hl.setComponentAlignment(caption, Alignment.MIDDLE_LEFT);
        hl.setComponentAlignment(organisaatioNimi, Alignment.MIDDLE_CENTER);
        hl.setComponentAlignment(kaikkiValittuNimi, Alignment.MIDDLE_CENTER);
        hl.setComponentAlignment(asetaOletusB, Alignment.TOP_RIGHT);
        hl.setComponentAlignment(poistaValintaB, Alignment.TOP_RIGHT);
        addComponent(hl);
        setValittu(true);

    }
}
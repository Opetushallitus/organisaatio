package fi.vm.sade.organisaatio.revised.ui.component.mainview;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.ui.blackboard.BlackboardContext;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi.Teksti;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.auth.OrganisaatioContext;
import fi.vm.sade.organisaatio.auth.OrganisaatioPermissionServiceImpl;
import fi.vm.sade.organisaatio.revised.ui.event.OrganisaatioViewButtonEvent;
import fi.vm.sade.organisaatio.revised.ui.helper.KoodistoHelper;
import fi.vm.sade.organisaatio.ui.PortletRole;
import fi.vm.sade.organisaatio.ui.util.UiUtils;
import fi.vm.sade.vaadin.Oph;

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
/**
 * @author Timo Santasalo / Teknokala Ky
 */
public abstract class AbstractOrganisaatioMainView extends CustomComponent implements OrganisaatioMainView {

	private static final long serialVersionUID = 1L;

    private VerticalLayout buttonLayout;
    private HorizontalLayout buttonHorizontalLayout;
    
    //Buttons
    private Button buttonPaivitaYtj;
    private Button buttonTakaisin;
    private Label nimiLabel = new Label();
    
    Window confirmationDialogWindow;
    
    protected abstract OrganisaatioViewPresenter getPresenter();
    
    public void togglePaivitaYtjButtonVisibility(boolean visible) {
		final OrganisaatioPermissionServiceImpl permissionService = PortletRole.getInstance().getPermissionService();

        if (buttonPaivitaYtj != null && permissionService.userCanUpdateYTJ()) {
            buttonPaivitaYtj.setVisible(visible);
        }
    }
    

    private String getLocalizedName(OrganisaatioDTO od) {
    	for (Teksti mt : od.getNimi().getTeksti()) {
    		String cv = new KoodistoHelper().tryGetArvoByKoodi(mt.getKieliKoodi());
    		if (I18N.getLocale().getLanguage().equals(cv)) {
    			return mt.getValue();
    		}
    	}
    	return null;
    }


	protected void refreshButtons(OrganisaatioDTO od) {
    	if (buttonLayout==null) {
    		return;
    	}
    	
        //Permissions
    	OrganisaatioContext ctx = OrganisaatioContext.get(od);
        buttonPaivitaYtj.setVisible(PortletRole.getInstance().getPermissionService().userCanUpdateYTJ());
        nimiLabel.setValue(getLocalizedName(od));
    }
    
    protected VerticalLayout buildButtonLayout() {
        // common part: create layout
        buttonLayout = new VerticalLayout();
        buttonLayout.setImmediate(false);
        buttonLayout.setMargin(false);
        
        nimiLabel.addStyleName(Oph.LABEL_H1);
        nimiLabel.addStyleName(Oph.SPACING_BOTTOM_30);

        // buttonHorizontalLayout
        buttonHorizontalLayout = buildButtonHorizontalLayout();
        buttonLayout.addComponent(buttonHorizontalLayout);
        buttonLayout.addComponent(nimiLabel);

        buttonLayout.addStyleName(Oph.SPACING_TOP_20);
        buttonLayout.addStyleName(Oph.SPACING_RIGHT_20);
        buttonLayout.addStyleName(Oph.SPACING_LEFT_20);

        return buttonLayout;
    }

    private HorizontalLayout buildButtonHorizontalLayout() {
        // common part: create layout
        buttonHorizontalLayout = new HorizontalLayout();
        buttonHorizontalLayout.setImmediate(false);
        buttonHorizontalLayout.setSpacing(true);
        buttonHorizontalLayout.addStyleName(Oph.SPACING_BOTTOM_30);


        // buttonTakaisin
        buttonTakaisin = UiUtils.button(buttonHorizontalLayout, "", new Button.ClickListener() {
            private static final long serialVersionUID = 5019806363620874205L;

            @Override
            public void buttonClick(ClickEvent event) {
                BlackboardContext.getBlackboard().fire(new OrganisaatioViewButtonEvent(getPresenter().getSelectedOrganisaatio(), OrganisaatioViewButtonEvent.TAKAISIN));
            }
        });
        buttonTakaisin.setStyleName(Oph.BUTTON_BACK);

        buttonPaivitaYtj = UiUtils.buttonSmallPrimary(buttonHorizontalLayout, I18N.getMessage("OrganisaatioMainView.paivitaYtjBtn"), new Button.ClickListener() {
            private static final long serialVersionUID = 5019806363620874205L;

            @Override
            public void buttonClick(ClickEvent event) {
            	getPresenter().refreshOrganization();
            }
        });



        return buttonHorizontalLayout;
    }
    
    public void removeDialog() {
        getWindow().removeWindow(confirmationDialogWindow);
        BlackboardContext.getBlackboard().fire(new OrganisaatioViewButtonEvent(getPresenter().getSelectedOrganisaatio(), OrganisaatioViewButtonEvent.TAKAISIN));
    }

}

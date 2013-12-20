package fi.vm.sade.organisaatio.ui.component;

import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.organisaatio.ui.listener.event.ConfirmationEvent;
import fi.vm.sade.organisaatio.ui.listener.ConfirmationListener;
import fi.vm.sade.organisaatio.ui.util.UiUtils;
import java.util.ArrayList;
import java.util.List;
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
 *
 * @author Tuomas Katva
 */
public class ConfirmationDialog extends CustomComponent {

    private VerticalLayout rootLayout;
    private Panel textPlaceHolder;
    private HorizontalLayout buttonLayout;
    private Label dialogText;
    private Button okButton;
    private Button cancelButton;
    private List<ConfirmationListener> listeners;

    public ConfirmationDialog(String labelText) {
        initDialog(labelText);
    }

    private void initDialog(String labelText) {

        rootLayout = new VerticalLayout();

        //Create the confirmation dialog text "line"
        textPlaceHolder = new Panel();
        dialogText = new Label(labelText);

        textPlaceHolder.addComponent(dialogText);
        rootLayout.addComponent(textPlaceHolder);

        //Create the "line" containing the buttons
        buttonLayout = new HorizontalLayout();
        buttonLayout.setSizeFull();
        okButton = UiUtils.buttonSmallSecodary(null, I18N.getMessage("confirmationDialog.okButton"), new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                fireConfirmationEvent(true);
            }
        });

        cancelButton = UiUtils.buttonSmallSecodary(null, I18N.getMessage("confirmationDialog.cancelButton"), new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                fireConfirmationEvent(false);
            }
        });

        buttonLayout.addComponent(cancelButton);
        buttonLayout.addComponent(okButton);
        buttonLayout.setComponentAlignment(cancelButton,Alignment.MIDDLE_LEFT);
        buttonLayout.setComponentAlignment(okButton,Alignment.MIDDLE_RIGHT);
        buttonLayout.setMargin(true,true,true,true);

        rootLayout.addComponent(buttonLayout);
        setCompositionRoot(rootLayout);
    }

    private void fireConfirmationEvent(boolean result) {
//        Blackboard blackboard = BaseApplication.getBlackboard();
//        blackboard.fire(new ConfirmationEvent(result));
        fireListeners(result);
    }

    
    private void fireListeners(boolean result) {
        ConfirmationEvent event = new ConfirmationEvent(result);
        for (ConfirmationListener listener:listeners) {
            listener.handleConfirmation(event);
        }
    }
    
    public void addListener(ConfirmationListener listener) {
        getListeners().add(listener);
    }

    /**
     * @return the listeners
     */
    public List<ConfirmationListener> getListeners() {
        if (listeners == null) {
            listeners = new ArrayList<ConfirmationListener>();
        }
        return listeners;
    }
    
}

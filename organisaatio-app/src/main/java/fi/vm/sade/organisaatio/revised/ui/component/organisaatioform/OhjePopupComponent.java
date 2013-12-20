package fi.vm.sade.organisaatio.revised.ui.component.organisaatioform;


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
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.PopupView;
import fi.vm.sade.organisaatio.ui.util.UiUtils;

/**
 *
 * @author Tuomas Katva
 */
public class OhjePopupComponent extends HorizontalLayout {

    private OhjePopup popup;
    private PopupView popupView;
    private Button showPopupBtn;

    public OhjePopupComponent(String message) {
        init(message);
    }

    public OhjePopupComponent(String message, String popupWidth, String popupHeight) {
        init(message);
        popup.setWidthAndHeight(popupWidth, popupHeight);
    }

    private void init(String message) {
        setSpacing(true);
        setWidth(100, UNITS_PERCENTAGE);

        popup = new OhjePopup(message);
        popupView = new PopupView(popup);
        popupView.setHideOnMouseOut(false);
        addComponent(popupView);

        showPopupBtn = UiUtils.buttonSmallInfo(this, new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                if (popupView != null) {
                    popupView.setPopupVisible(true);
                }
            }
        });

        setComponentAlignment(showPopupBtn, Alignment.TOP_RIGHT);
    }
}

package fi.vm.sade.organisaatio.ui.component;/*
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

import com.github.wolfie.blackboard.Blackboard;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.organisaatio.ui.MainWindow;
import fi.vm.sade.organisaatio.ui.listener.YtjSelectListener;
import fi.vm.sade.organisaatio.ui.listener.event.YtjSelectedEventImpl;
import fi.vm.sade.organisaatio.ui.util.UiUtils;
import fi.vm.sade.rajapinnat.ytj.api.YTJDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Tuomas Katva
 * Date: 23.4.2013
 */
public class YtjOrganisaatioRow extends HorizontalLayout {

    private Label organisaatioName;

    private Label yTunnus;

    private Button valitseBtn;

    private YTJDTO dto;

    private String oldOid;

    private List<YtjSelectListener> listeners;

    public YtjOrganisaatioRow(YTJDTO dto) {
        this.dto = dto;

        createLabels();

    }

    private String t(String key) {
        return I18N.getMessage(key);
    }

    private void createLabels() {
        if (dto.getNimi() != null && dto.getNimi().trim().length() > 0) {
            setOrganisaatioName(new Label(dto.getNimi()));
        } else {
            setOrganisaatioName(new Label(dto.getSvNimi()));
        }
        setyTunnus(new Label(dto.getYtunnus()));

        setValitseBtn(UiUtils.buttonLink(null,t("YtjOrganisaatioRow.valitseBtnLabel"),new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                 fireYtjEvent(false,dto);
            }
        }));
    }

    private void fireYtjEvent(boolean cancel, YTJDTO selectedOrg) {

        if (listeners != null) {
            for (YtjSelectListener listener:listeners) {
            if (this.oldOid != null) {
             listener.organizationSelected(new YtjSelectedEventImpl(selectedOrg,cancel,oldOid));
            } else {
             listener.organizationSelected(new YtjSelectedEventImpl(selectedOrg, cancel));
            }
            }
        }



    }

    public void addListener(YtjSelectListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<YtjSelectListener>();
        }
        listeners.add(listener);
    }

    public void addListeners(List<YtjSelectListener> listeners) {
        if (this.listeners == null) {
            this.listeners = new ArrayList<YtjSelectListener>();
        }
        this.listeners.addAll(listeners);
    }

    public Label getOrganisaatioName() {
        return organisaatioName;
    }

    public void setOrganisaatioName(Label organisaatioName) {
        this.organisaatioName = organisaatioName;
    }

    public Label getyTunnus() {
        return yTunnus;
    }

    public void setyTunnus(Label yTunnus) {
        this.yTunnus = yTunnus;
    }

    public Button getValitseBtn() {
        return valitseBtn;
    }

    public void setValitseBtn(Button valitseBtn) {
        this.valitseBtn = valitseBtn;
    }

    public String getOldOid() {
        return oldOid;
    }

    public void setOldOid(String oldOid) {
        this.oldOid = oldOid;
    }
}

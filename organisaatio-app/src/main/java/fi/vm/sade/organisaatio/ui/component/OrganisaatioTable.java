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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.util.BeanContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.organisaatio.ui.listener.YtjSelectListener;
import fi.vm.sade.organisaatio.ui.listener.event.YtjSelectedEventImpl;
import fi.vm.sade.organisaatio.ui.util.UiUtils;
import fi.vm.sade.rajapinnat.ytj.api.YTJDTO;
import fi.vm.sade.rajapinnat.ytj.api.YTJKieli;
import fi.vm.sade.rajapinnat.ytj.api.YTJService;
import fi.vm.sade.rajapinnat.ytj.api.exception.YtjConnectionException;

/**
 *
 * @author Tuomas Katva
 */
@Configurable(preConstruction = false)
public class OrganisaatioTable extends CustomComponent {

    @Autowired(required = true)
    private YTJService ytjService;
    private YtjSearch ytjSeachComponent;
    private final int componentWidthPixel = 700;
    private final int componentHeightPixel = 500;
    private final float lesserRatio = 0.4f;
    private final float biggerRatio = 0.6f;
    private final String tableHdr = "c_yritysValintaHdr";
    private Table table = null;
    private YTJDTO selectedOrg = null;
    private final int pageLenght = 10;
    private Panel ohjePanel = null;
    private GridLayout buttonLayout;
    private Button jatkaValitsemattaBtn;
    private Button haeYtjBtn;
    private Button peruutaBtn;
    private Label ohjeLbl;
    private GridLayout rootLayout = new GridLayout(1, 3);
    private final Logger log = LoggerFactory.getLogger(getClass());
    private String oldOrgOid;
    private List<YtjSelectListener> listeners;

    public OrganisaatioTable(List<YTJDTO> ytjResultsParam, String initialYtunnus) {

        initializeLayout(ytjResultsParam);
        if (initialYtunnus!=null) {
        	ytjSeachComponent.getOrganisaatioYtunnus().setValue(initialYtunnus);
        }
//        addYtjSearchListener();

    }

    public OrganisaatioTable() {
    }

    private void initializeLayout(List<YTJDTO> ytjResultsParam) {
        rootLayout.setMargin(true);
        rootLayout.setSpacing(true);
        rootLayout.setWidth(componentWidthPixel, Sizeable.UNITS_PIXELS);
        rootLayout.setHeight(componentHeightPixel, Sizeable.UNITS_PIXELS);


        ytjSeachComponent = new YtjSearch();
        ytjSeachComponent.setSizeFull();


        rootLayout.addComponent(UiUtils.newPopupCssLayout(ytjSeachComponent), 0, 0);

        if (ytjResultsParam.size() > 0) {

            createTable(ytjResultsParam, tableHdr);

        } else {
            showMessage(I18N.getMessage("OrganisaatioTable.ytjOhjeTeksti"));
        }

        rootLayout.setRowExpandRatio(0, lesserRatio);
        rootLayout.setRowExpandRatio(1, biggerRatio);

        UiUtils.processDebugIds(this, "orgtable_");

        createButtons();

        setCompositionRoot(rootLayout);
    }

    private void showMessage(String msg) {

        if (ohjePanel != null) {
            rootLayout.removeComponent(ohjePanel);

        }
        if (table != null) {
            rootLayout.removeComponent(table);
            rootLayout.removeComponent(0, 1);
            table = null;
        }
        ohjePanel = new Panel();
        ohjeLbl = new Label(msg);
        ohjePanel.addComponent(ohjeLbl);
        ohjePanel.setSizeFull();

        rootLayout.addComponent(ohjePanel, 0, 1);


    }

    private void createButtons() {
        buttonLayout = new GridLayout(2, 1);
        peruutaBtn = UiUtils.buttonSmallPrimary(null, I18N.getMessage("OrganisaatioTable.peruutaBtn"));
        buttonLayout.addComponent(peruutaBtn, 0, 0);


        HorizontalLayout jatkaBtnLayout = new HorizontalLayout();

        jatkaValitsemattaBtn = UiUtils.buttonSmallPrimary(null, I18N.getMessage("OrganisaatioTable.jatkaValitsemattaBtn"));
        jatkaBtnLayout.addComponent(jatkaValitsemattaBtn);

        haeYtjBtn = UiUtils.buttonSmallPrimary(null, I18N.getMessage("OrganisaatioTable.haeYtjBtn"));

        jatkaBtnLayout.addComponent(haeYtjBtn);

        buttonLayout.addComponent(jatkaBtnLayout, 1, 0);

        buttonLayout.setColumnExpandRatio(0, 3);
        buttonLayout.setColumnExpandRatio(1, 1);

        rootLayout.addComponent(buttonLayout, 0, 2);

        createButtonListeners();
    }

    private void createButtonListeners() {
        haeYtjBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                String ytunnus = ytjSeachComponent.getOrganisaatioYtunnus().getValue().toString();
                String nimi = ytjSeachComponent.getOrganisaatioNimi().getValue().toString();
                List<YTJDTO> returnValues = new ArrayList<YTJDTO>();
                if (ytunnus != null && ytunnus.length() > 1) {
                    try {
                        YTJDTO selectedOrg = ytjService.findByYTunnus(ytunnus.trim(), YTJKieli.FI);

                        returnValues.add(selectedOrg);
                        createTable(returnValues, tableHdr);
                    } catch (YtjConnectionException ex) {
                        ex.printStackTrace();
                        log.error("YtjConnectionException : " + ex.toString());
                        showMessage(I18N.getMessage("OrganisaatioTable.virheViesti"));
                    } catch (Exception exp) {
                        exp.printStackTrace();
                        log.error("Unknown exception connecting to YTJ : " + exp.toString());
                    }

                } else if (nimi != null && nimi.length() > 0) {
                    try {
                        returnValues = ytjService.findByYNimi(nimi.trim(), true, YTJKieli.FI);
                        createTable(returnValues, tableHdr);
                    } catch (YtjConnectionException ex) {
                        ex.printStackTrace();
                        log.warn("YtjConnectionException : " + ex.toString());
                        showMessage(I18N.getMessage("OrganisaatioTable.virheViesti"));
                    } catch (Exception exp) {
                        exp.printStackTrace();
                        log.error("Unknown exception connecting to YTJ : " + exp.toString());
                        showMessage(I18N.getMessage("OrganisaatioTable.virheViesti"));
                    }
                }

            }
        });

        peruutaBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                selectedOrg = null;
                fireYtjEvent(true);
            }
        });

        jatkaValitsemattaBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                selectedOrg = null;
                fireYtjEvent(false);
            }
        });


    }

    private void addTable(Table tableParam) {
        //Check if existing results are displayed if so, then remove the old table and add new

        if (table != null) {
            rootLayout.removeComponent(table);
            table.removeAllItems();

            table = null;
        }
        //Check if ohje is displayed, if it is remove it
        if (ohjePanel != null) {
            rootLayout.removeComponent(ohjePanel);
        }
        table = tableParam;
        table.setSizeFull();
        table.requestRepaint();
        rootLayout.addComponent(UiUtils.newPopupCssLayout(table), 0, 1);

    }

    private void createTable(List<YTJDTO> ytjResultsParam, String captionKey) {

        if (ohjePanel != null) {
            rootLayout.removeComponent(ohjePanel);
            rootLayout.removeComponent(0, 1);
            ohjePanel = null;
        }
        if (table != null) {
            table.removeAllItems();
        } else {
            table = new Table(t(captionKey));
            rootLayout.addComponent(UiUtils.newPopupCssLayout(table), 0, 1);
        }


        table.setContainerDataSource(createRowContainer(ytjResultsParam));



        table.setSelectable(false);


        table.setColumnHeader("yTunnus", t("c_ytunnus"));
        table.setColumnHeader("organisaatioName",t("c_nimi"));
        table.setColumnHeader("valitseBtn","");
        table.setColumnExpandRatio("organisaatioName",1.5f);
        table.setVisibleColumns(new String[]{"organisaatioName","yTunnus","valitseBtn"});
        table.setPageLength(pageLenght);
        table.setImmediate(true);
        table.setMultiSelect(false);



        table.setSizeFull();
        table.requestRepaint();



    }

    private String t(String key) {
        return I18N.getMessage(key);
    }

    private BeanContainer<String,YtjOrganisaatioRow> createRowContainer(List<YTJDTO> ytjdtos) {
        BeanContainer<String,YtjOrganisaatioRow> rows = new BeanContainer<String, YtjOrganisaatioRow>(YtjOrganisaatioRow.class);
        for (YTJDTO ytjdto:ytjdtos) {
           YtjOrganisaatioRow row = new YtjOrganisaatioRow(ytjdto);
            if (this.oldOrgOid != null) {
                row.setOldOid(oldOrgOid);
            }
            if (this.listeners != null) {
                row.addListeners(this.listeners);
            }
            rows.addItem(ytjdto.getYtunnus(),row);
        }
        return rows;
    }

    public void addListener(YtjSelectListener listener) {
        if (this.listeners == null) {
            this.listeners = new ArrayList<YtjSelectListener>();
        }
        this.listeners.add(listener);
    }

    public void addListeners(List<YtjSelectListener> listeners) {
        if (this.listeners == null) {
            this.listeners = new ArrayList<YtjSelectListener>();
        }
       this.listeners.addAll(listeners);
    }

    private void fireYtjEvent(boolean cancel) {

        if(this.listeners != null) {
            for (YtjSelectListener listener:listeners) {
                if (this.oldOrgOid != null) {
                    listener.organizationSelected(new YtjSelectedEventImpl(selectedOrg, cancel,oldOrgOid));
                } else {
                    listener.organizationSelected(new YtjSelectedEventImpl(selectedOrg, cancel));
                }
            }
        }

       /* Blackboard blackboard = MainWindow.getBlackboard();
        blackboard.fire(new YtjSelectedEventImpl(selectedOrg, cancel));*/
    }

    /**
     * @return the selectedOrg
     */
    public YTJDTO getSelectedOrg() {
        return selectedOrg;
    }

    /**
     * @param selectedOrg the selectedOrg to set
     */
    public void setSelectedOrg(YTJDTO selectedOrg) {
        this.selectedOrg = selectedOrg;
    }

    public String getOldOrgOid() {
        return oldOrgOid;
    }

    public void setOldOrgOid(String oldOrgOid) {
        this.oldOrgOid = oldOrgOid;
    }
}

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

import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;

import fi.vm.sade.organisaatio.api.model.types.YhteystietoElementtiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoElementtiDTO;
import fi.vm.sade.organisaatio.ui.organisaatio.YhteystietojenTyyppiForm;

import fi.vm.sade.generic.common.I18N;
import com.vaadin.data.util.NestedMethodProperty;
import fi.vm.sade.organisaatio.ui.util.UiUtils;
import fi.vm.sade.vaadin.util.UiUtil;

/**
 * Form for creating muu yhteystieto in YhteystietojenTyyppiForm.
 *
 * @author markus
 *
 */
public class MuuYhteystietoForm extends VerticalLayout {

    private Label errorLabel;
    private Label kenttaOtsikko;
    private TextField kenttaKuvausFi;
    private TextField kenttaKuvausSv;
    private CheckBox kaytossa;
    private CheckBox pakollinen;
    private YhteystietoElementtiTyyppi kenttaTyyppi;
    private YhteystietoElementtiDTO model;
    private Button save;
    private Button cancel;
    private YhteystietojenTyyppiForm yttForm;
    private boolean isAdding = false;

    public MuuYhteystietoForm(YhteystietoElementtiDTO model, YhteystietoElementtiTyyppi tyyppi, String labelCode, YhteystietojenTyyppiForm yttForm) {

        this.yttForm = yttForm;
        this.model = model;
        this.kenttaTyyppi = tyyppi;
        initComponents(labelCode);
    }

    private void initComponents(String labelCode) {
        kenttaOtsikko = new Label(I18N.getMessage(labelCode));
        addComponent(this.kenttaOtsikko);     
        errorLabel = UiUtil.label(this, I18N.getMessage("YhteystietojenTyyppiForm.nimiPakollinen"));//new Label(I18N.getMessage("YhteystietojenTyyppiForm.nimiPakollinen"));
        errorLabel.setWidth("150px");
        errorLabel.setVisible(false);
        kenttaKuvausFi = new TextField();
        kenttaKuvausFi.setRequired(true);
        kenttaKuvausFi.setNullRepresentation("");
        kenttaKuvausFi.setCaption(I18N.getMessage("YhteystietojenTyyppiForm.MuuYhteystietoFI"));
        kenttaKuvausFi.setPropertyDataSource(new NestedMethodProperty(model, "nimi"));

        addComponent(kenttaKuvausFi);
        kenttaKuvausSv = new TextField();
        kenttaKuvausSv.setNullRepresentation("");
        kenttaKuvausSv.setCaption(I18N.getMessage("YhteystietojenTyyppiForm.MuuYhteystietoSV"));
        kenttaKuvausSv.setPropertyDataSource(new NestedMethodProperty(model, "nimiSv"));
        addComponent(kenttaKuvausSv);

        kaytossa = new CheckBox();
        kaytossa.setCaption(I18N.getMessage("YhteystietojenTyyppiForm.kaytossaFull"));
        kaytossa.setPropertyDataSource(new NestedMethodProperty(model, "kaytossa"));
        addComponent(kaytossa);
        pakollinen = new CheckBox();
        pakollinen.setCaption(I18N.getMessage("YhteystietojenTyyppiForm.pakollinenFull"));
        pakollinen.setPropertyDataSource(new NestedMethodProperty(model, "pakollinen"));
        addComponent(pakollinen);
        errorLabel = UiUtil.label(this, I18N.getMessage("YhteystietojenTyyppiForm.nimiPakollinen"));//new Label(I18N.getMessage("YhteystietojenTyyppiForm.nimiPakollinen"));
        errorLabel.setWidth("150px");
        errorLabel.setVisible(false);
        initButtons();

        //If the model does not have a name we are creating a new yhteystietoelementti
        if (model.getNimi() == null) {
            isAdding = true;
        }
    }

    private void initButtons() {
        save = UiUtils.buttonSmallSecodary(this, I18N.getMessage("c_tallenna"), new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                //LOG.info("Save utton is clicked");
                saveYhteystietoElementti();
            }
        });
        cancel = UiUtils.buttonSmallSecodary(null, I18N.getMessage("c_peruuta"));
    }

    /**
     * Saving the form.
     */
    private void saveYhteystietoElementti() {
        if (this.kenttaKuvausFi.getValue() == null) {
            this.errorLabel.setVisible(true);
            return;
        } else {
            this.errorLabel.setVisible(false);
        }
        model.setNimi(getNameString((this.kenttaKuvausFi.getValue() != null) ? (String)(this.kenttaKuvausFi.getValue()) : ""));
        model.setNimiSv(getNameString((this.kenttaKuvausSv.getValue() != null) ? (String)(this.kenttaKuvausSv.getValue()) : ""));
        if (model.isPakollinen()) {
            model.setKaytossa(true);
        }
        model.setTyyppi(kenttaTyyppi);
        yttForm.YTESaved(model, isAdding);
    }

    private String getNameString(String nimi) {
        if (this.kenttaTyyppi.value().equals(YhteystietoElementtiTyyppi.OSOITE.value())) {
            return (!nimi.contains(I18N.getMessage("YhteystietojenTyyppiForm.muuOsPrefix")))
                    ? I18N.getMessage("YhteystietojenTyyppiForm.muuOsPrefix") + " " + nimi
                    : nimi;
        } else if (this.kenttaTyyppi.value().equals(YhteystietoElementtiTyyppi.PUHELIN.value())) {
            return (!nimi.contains(I18N.getMessage("YhteystietojenTyyppiForm.muuPuhPrefix")))
                    ? I18N.getMessage("YhteystietojenTyyppiForm.muuPuhPrefix") + " " + nimi
                    : nimi;
        } else {
            return (!nimi.contains(I18N.getMessage("YhteystietojenTyyppiForm.muuSahkPrefix")))
                    ? I18N.getMessage("YhteystietojenTyyppiForm.muuSahkPrefix") + " " + nimi
                    : nimi;
        }
    }

    public Label getKenttaOtsikko() {
        return kenttaOtsikko;
    }

    public TextField getKenttaKuvausFi() {
        return kenttaKuvausFi;
    }

    public TextField getKenttaKuvausSv() {
        return kenttaKuvausSv;
    }

    public boolean isAdding() {
        return isAdding;
    }
}

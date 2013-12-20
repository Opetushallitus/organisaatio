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

import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.VerticalLayout;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoElementtiDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoElementtiTyyppi;
import fi.vm.sade.organisaatio.ui.organisaatio.YhteystietojenTyyppiForm;

/**
 * Component for displaying muu yhteystieto defnitions in YhteystietojenTyyppiForm.
 * 
 * @author markus
 *
 */
public class MuuYhteystietoList extends VerticalLayout {
    
    private static final Logger LOG = LoggerFactory.getLogger(MuuYhteystietoList.class);
    
    private List<DoubleCheckbox> muutYhteystiedot;
    private List<YhteystietoElementtiDTO> model;
    private YhteystietojenTyyppiForm yttForm;
    
    public List<YhteystietoElementtiDTO> getModel() {
        return model;
    }

    public MuuYhteystietoList(List<YhteystietoElementtiDTO> model, YhteystietojenTyyppiForm yttForm) {
        muutYhteystiedot = new ArrayList<DoubleCheckbox>();
        this.model = model;
        this.yttForm = yttForm;
        init();
    }
    
    void removeYhteystietoElementti(DoubleCheckbox removable) {
        for (YhteystietoElementtiDTO curYtel : model) {
            if (curYtel.getNimi().equals(removable.getChoiceValue())) {
                curYtel.setPakollinen(false);
                curYtel.setKaytossa(false);
            }
        }
        removeComponent(removable);
    }
    
    void editYhteystietoElementti(DoubleCheckbox editable, YhteystietoElementtiTyyppi tyyppi) {
        String otsikko = getTyyppiOtsikko(tyyppi);
        for (YhteystietoElementtiDTO curYtel : model) {
            if (curYtel.getNimi().equals(editable.getModel().getNimi())) {
                this.yttForm.editMuuYtt(curYtel, otsikko);    
            }
        }
    }
    
    private String getTyyppiOtsikko(YhteystietoElementtiTyyppi tyyppi) {
        if (tyyppi.value().equals(YhteystietoElementtiTyyppi.FAKSI.value()) || tyyppi.value().equals(YhteystietoElementtiTyyppi.PUHELIN.value())) {
            return I18N.getMessage("YhteystietojenTyyppiForm.muuPuhelin");
        }
        if (tyyppi.value().equals(YhteystietoElementtiTyyppi.OSOITE.value()) || tyyppi.value().equals(YhteystietoElementtiTyyppi.OSOITE_ULKOMAA.value())) {
            return I18N.getMessage("YhteystietojenTyyppiForm.muuOsoite");
        }
        return I18N.getMessage("YhteystietojenTyyppiForm.muuSahkoinen");
    }
    
    public void addYhteystieto(YhteystietoElementtiDTO newYtel) {
        model.add(newYtel);
        DoubleCheckbox newDC = new DoubleCheckbox(newYtel);
        newDC.addEditButton(this);
        newDC.addRemoveButton(this);
        addComponent(newDC);
        muutYhteystiedot.add(newDC);
    }
    
    public void updateYhteystieto(YhteystietoElementtiDTO ytel) {
        //DEBUGSAWAY:LOG.debug("updating yhteystieto: " + ytel.getOid());
        for (DoubleCheckbox curDC : muutYhteystiedot) {
            YhteystietoElementtiDTO curModel = curDC.getModel();
            if (curModel.getOid() != null && curModel.getOid().equals(ytel.getOid())) {
                //DEBUGSAWAY:LOG.debug("updating yhteystieto, oid matches");
                curDC.bind(ytel);
            }
        }
    }
    
    public List<DoubleCheckbox> getMuutYhteystiedot() {
        return muutYhteystiedot;
    }
    
    private void init() {
        for (YhteystietoElementtiDTO curYtel : model) {
            DoubleCheckbox newDC = new DoubleCheckbox(curYtel);
            newDC.addEditButton(this);
            newDC.addRemoveButton(this);
            addComponent(newDC);
            muutYhteystiedot.add(newDC);
        }
    }

}

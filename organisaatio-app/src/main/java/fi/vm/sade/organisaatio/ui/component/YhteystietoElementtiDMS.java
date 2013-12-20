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

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoElementtiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoElementtiDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete implementation of DoubleMultiSelect to be used for osoites, puhelinnumeros, and sahkoinenYhteystietos.
 *
 * @author Markus Holi
 */
public class YhteystietoElementtiDMS extends DoubleMultiSelect<YhteystietoElementtiDTO> {

    private YhteystietoElementtiTyyppi[] tyypit;

    public YhteystietoElementtiDMS(String[] optionValues, List<YhteystietoElementtiDTO> m, YhteystietoElementtiTyyppi tyypit[]) {
        super(optionValues, m);
        this.tyypit = tyypit;
    }

    public YhteystietoElementtiDMS(String[] optionValues, String[] optionValuesSv, List<YhteystietoElementtiDTO> m, YhteystietoElementtiTyyppi tyypit[]) {
        super(optionValues, optionValuesSv, m);
        this.tyypit = tyypit;
    }

    @Override
    protected DoubleCheckbox createDCheckbox(String curOptionval) {
        if (model != null) {
            for (YhteystietoElementtiDTO curDto : model) {
                if (curDto.getNimi().equals((curOptionval))) {
                    return new DoubleCheckbox(curDto);
                }
            }
        }
        YhteystietoElementtiDTO ytel = new YhteystietoElementtiDTO();

        ytel.setNimi(curOptionval);
        ytel.setKaytossa(false);
        ytel.setPakollinen(false);
        
        return new DoubleCheckbox(ytel);
    }

    @Override
    public List<YhteystietoElementtiDTO> saveModel() {
        for (int i = 0; i < selectOptions.size(); i++) {
            DoubleCheckbox curDC = selectOptions.get(i);
            YhteystietoElementtiDTO curModel = curDC.getModel();
            YhteystietoElementtiDTO existingElem = getElementWithName(curModel);
            if ((curModel.getOid() == null) && (existingElem == null) && curModel.isKaytossa()) {
                curModel.setTyyppi(getTyyppi(i));
                model.add(curModel);
            } else if (existingElem != null) {
            	existingElem.setKaytossa(curModel.isKaytossa());
            	existingElem.setPakollinen(curModel.isPakollinen());
            	existingElem.setNimiSv(curModel.getNimiSv());
            	existingElem.setTyyppi(curModel.getTyyppi());
            }
        }
        
        
        return model;
    }
    
    public List<DoubleCheckbox> getSelectOptions() {
        return this.selectOptions;
    }
    
    private YhteystietoElementtiDTO getElementWithName(YhteystietoElementtiDTO elem) {
    	for (YhteystietoElementtiDTO curEl : model) {
    		if(curEl.getNimi().equals(elem.getNimi()) 
    				&& curEl.getOid() != null
    				&& !curEl.getOid().equals(elem.getOid())) {
    			return curEl;
    		}
    	}
    	return null;
    }

    private YhteystietoElementtiTyyppi getTyyppi(int tyyppiIndex) {
        return tyypit[tyyppiIndex];
    }
}

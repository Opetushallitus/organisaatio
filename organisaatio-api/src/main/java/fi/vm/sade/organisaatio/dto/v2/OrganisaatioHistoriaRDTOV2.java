/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
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
 */

package fi.vm.sade.organisaatio.dto.v2;

import java.util.ArrayList;
import java.util.List;


public class OrganisaatioHistoriaRDTOV2 {
    private List<OrganisaatioSuhdeDTOV2> childSuhteet = new ArrayList<OrganisaatioSuhdeDTOV2>();
    private List<OrganisaatioSuhdeDTOV2> parentSuhteet = new ArrayList<OrganisaatioSuhdeDTOV2>();

    /**
     * @return the childSuhteet
     */
    public List<OrganisaatioSuhdeDTOV2> getChildSuhteet() {
        return childSuhteet;
    }

    /**
     * @param childSuhteet the childSuhteet to set
     */
    public void setChildSuhteet(List<OrganisaatioSuhdeDTOV2> childSuhteet) {
        this.childSuhteet = childSuhteet;
    }

    /**
     * @return the parentSuhteet
     */
    public List<OrganisaatioSuhdeDTOV2> getParentSuhteet() {
        return parentSuhteet;
    }

    /**
     * @param parentSuhteet the parentSuhteet to set
     */
    public void setParentSuhteet(List<OrganisaatioSuhdeDTOV2> parentSuhteet) {
        this.parentSuhteet = parentSuhteet;
    }

}

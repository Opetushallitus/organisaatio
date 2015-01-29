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

import java.util.Date;


public class OrganisaatioLiitosDTOV2 {
    private OrganisaatioCoreInfoDTOV2 organisaatio;
    private OrganisaatioCoreInfoDTOV2 kohde;

    private Date alkuPvm;

    public Date getAlkuPvm() {
        return alkuPvm;
    }

    public void setAlkuPvm(Date alkuPvm) {
        this.alkuPvm = alkuPvm;
    }

    /**
     * @return the organisaatio
     */
    public OrganisaatioCoreInfoDTOV2 getOrganisaatio() {
        return organisaatio;
    }

    /**
     * @param organisaatio the organisaatio to set
     */
    public void setOrganisaatio(OrganisaatioCoreInfoDTOV2 organisaatio) {
        this.organisaatio = organisaatio;
    }

    /**
     * @return the kohde
     */
    public OrganisaatioCoreInfoDTOV2 getKohde() {
        return kohde;
    }

    /**
     * @param kohde the kohde to set
     */
    public void setKohde(OrganisaatioCoreInfoDTOV2 kohde) {
        this.kohde = kohde;
    }

}

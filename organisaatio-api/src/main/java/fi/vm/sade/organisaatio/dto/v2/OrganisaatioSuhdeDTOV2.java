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
import java.util.Map;


public class OrganisaatioSuhdeDTOV2 {
    private OrganisaatioCoreInfoDTOV2 child;
    private OrganisaatioCoreInfoDTOV2 parent;

    private Date alkuPvm;
    private Date loppuPvm;
    private String suhdeTyyppi;

    public Date getAlkuPvm() {
        return alkuPvm;
    }

    public void setAlkuPvm(Date alkuPvm) {
        this.alkuPvm = alkuPvm;
    }

    /**
     * @return the loppuPvm
     */
    public Date getLoppuPvm() {
        return loppuPvm;
    }

    /**
     * @param loppuPvm the loppuPvm to set
     */
    public void setLoppuPvm(Date loppuPvm) {
        this.loppuPvm = loppuPvm;
    }

    /**
     * @return the suhdeTyyppi
     */
    public String getSuhdeTyyppi() {
        return suhdeTyyppi;
    }

    /**
     * @param suhdeTyyppi the suhdeTyyppi to set
     */
    public void setSuhdeTyyppi(String suhdeTyyppi) {
        this.suhdeTyyppi = suhdeTyyppi;
    }

    /**
     * @return the child
     */
    public OrganisaatioCoreInfoDTOV2 getChild() {
        return child;
    }

    /**
     * @param child the child to set
     */
    public void setChild(OrganisaatioCoreInfoDTOV2 child) {
        this.child = child;
    }

    /**
     * @return the parent
     */
    public OrganisaatioCoreInfoDTOV2 getParent() {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(OrganisaatioCoreInfoDTOV2 parent) {
        this.parent = parent;
    }
}

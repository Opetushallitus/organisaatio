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


public class OrganisaatioHistoriaRDTOV2 {
    private String childOid;
    private String parentOid;
    private Date alkuPvm;
    private Date loppuPvm;
    private String suhdeTyyppi;

    public Date getAlkuPvm() {
        return alkuPvm;
    }

    public void setAlkuPvm(Date alkuPvm) {
        this.alkuPvm = alkuPvm;
    }

    public String getChildOid() {
        return childOid;
    }

    public void setChildOid(String childOid) {
        this.childOid = childOid;
    }

    /**
     * @return the parentOid
     */
    public String getParentOid() {
        return parentOid;
    }

    /**
     * @param parentOid the parentOid to set
     */
    public void setParentOid(String parentOid) {
        this.parentOid = parentOid;
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
}

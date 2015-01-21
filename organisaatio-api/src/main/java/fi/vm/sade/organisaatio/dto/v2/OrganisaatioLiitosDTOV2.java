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


public class OrganisaatioLiitosDTOV2 {
    private String organisaatioOid;
    private String kohdeOid;

    private Map<String, String> organisaatioNimi;
    private Map<String, String> kohdeNimi;

    private Date alkuPvm;

    public Date getAlkuPvm() {
        return alkuPvm;
    }

    public void setAlkuPvm(Date alkuPvm) {
        this.alkuPvm = alkuPvm;
    }

    /**
     * @return the organisaatioOid
     */
    public String getOrganisaatioOid() {
        return organisaatioOid;
    }

    /**
     * @param organisaatioOid the organisaatioOid to set
     */
    public void setOrganisaatioOid(String organisaatioOid) {
        this.organisaatioOid = organisaatioOid;
    }

    /**
     * @return the kohdeOid
     */
    public String getKohdeOid() {
        return kohdeOid;
    }

    /**
     * @param kohdeOid the kohdeOid to set
     */
    public void setKohdeOid(String kohdeOid) {
        this.kohdeOid = kohdeOid;
    }

    /**
     * @return the organisaatioNimi
     */
    public Map<String, String> getOrganisaatioNimi() {
        return organisaatioNimi;
    }

    /**
     * @param organisaatioNimi the organisaatioNimi to set
     */
    public void setOrganisaatioNimi(Map<String, String> organisaatioNimi) {
        this.organisaatioNimi = organisaatioNimi;
    }

    /**
     * @return the kohdeNimi
     */
    public Map<String, String> getKohdeNimi() {
        return kohdeNimi;
    }

    /**
     * @param kohdeNimi the kohdeNimi to set
     */
    public void setKohdeNimi(Map<String, String> kohdeNimi) {
        this.kohdeNimi = kohdeNimi;
    }

}

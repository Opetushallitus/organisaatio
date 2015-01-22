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

package fi.vm.sade.organisaatio.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import fi.vm.sade.generic.model.BaseEntity;

/**
 * This entity is used to manage merge relationships between Organisaatio's.
 * It is used to keep track of historical "as it was" situations.
 *
 * @author simok
 */
@Entity
@Table(name = "organisaatioliitos")
@org.hibernate.annotations.Table(appliesTo = "organisaatioliitos", comment = "Sisältää organisaatioiden liitokset.")
public class OrganisaatioLiitos extends BaseEntity {

    private static long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Organisaatio organisaatio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Organisaatio kohde;

    @Temporal(TemporalType.DATE)
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
    public Organisaatio getOrganisaatio() {
        return organisaatio;
    }

    /**
     * @param organisaatio the organisaatio to set
     */
    public void setOrganisaatio(Organisaatio organisaatio) {
        this.organisaatio = organisaatio;
    }

    /**
     * @return the kohde
     */
    public Organisaatio getKohde() {
        return kohde;
    }

    /**
     * @param kohde the kohde to set
     */
    public void setKohde(Organisaatio kohde) {
        this.kohde = kohde;
    }

}

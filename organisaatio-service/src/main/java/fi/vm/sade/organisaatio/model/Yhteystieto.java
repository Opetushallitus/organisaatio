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

import fi.vm.sade.security.xssfilter.FilterXss;
import org.hibernate.annotations.Table;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

/**
 * @author Antti Salonen
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(appliesTo = "Yhteystieto", comment = "Sisältää organisaation yhteystiedot. Kaikki yhteystietotyypit tallennetaan tähän samaan tauluun.")
public class Yhteystieto extends OrganisaatioBaseEntity {

    @ManyToOne(optional = true)
    private Organisaatio organisaatio;

    @NotNull
    protected String yhteystietoOid;

    @FilterXss
    private String kieli;
    
    public String getYhteystietoOid() {
        return yhteystietoOid;
    }

    public void setYhteystietoOid(String yhteystietoOid) {
        this.yhteystietoOid = yhteystietoOid;
    }

    public Organisaatio getOrganisaatio() {
        return organisaatio;
    }

    public void setOrganisaatio(Organisaatio organisaatio) {
        this.organisaatio = organisaatio;
    }

    /**
     * @return kieli
     */
    public String getKieli() {
        return kieli;
    }

    /**
     * @param kieli yhteystiedotn kieli
     */
    public void setKieli(String kieli) {
        this.kieli = kieli;
    }
}

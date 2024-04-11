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

import org.hibernate.annotations.Comment;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name="yhteystieto")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Comment("Sisältää organisaation yhteystiedot. Kaikki yhteystietotyypit tallennetaan tähän samaan tauluun.")
public class Yhteystieto extends OrganisaatioBaseEntity {

    @ManyToOne(optional = true)
    private Organisaatio organisaatio;

    @NotNull
    protected String yhteystietoOid;

    private String kieli;  // TODO XSS filtteri

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

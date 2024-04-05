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

import jakarta.persistence.*;
import java.util.Date;


/**
 * This entity is used to manage relationships between Organisaatio's.
 * It is used to keep track of historical "as it was" situations.
 *
 * @author mlyly
 */
@Entity
@jakarta.persistence.Table(name = "organisaatiosuhde")
@org.hibernate.annotations.Table(appliesTo = "organisaatiosuhde", comment = "Sisältää organisaatioiden väliset suhteet. Suhteen tyyppejä ovat LIITOS ja HISTORIA.")
public class OrganisaatioSuhde extends BaseEntity {

	private static final long serialVersionUID = 1L;

    /**
     * Relation types.
     * Possible extension point for different types of relations (belongs, relates, ...)
     */
    public enum OrganisaatioSuhdeTyyppi {
        /**
         * This is used when Organisation is "moved" so that the old relation is stored for later history browsing.
         */
         HISTORIA,
        /**
         * When old Organisation ceases to exist, old ones should be attached to it with this type of information.
         */
        LIITOS
    };

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name="suhdetyyppi")
    private OrganisaatioSuhdeTyyppi suhdeTyyppi = OrganisaatioSuhdeTyyppi.HISTORIA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Organisaatio parent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Organisaatio child;

    @Temporal(TemporalType.DATE)
    @Column(name="alkupvm")
    private Date alkuPvm;

    @Temporal(TemporalType.DATE)
    @Column(name="loppupvm")
    private Date loppuPvm;
    @Column(name="opetuspisteenjarjnro")
    private String opetuspisteenJarjNro;


    public String getOpetuspisteenJarjNro() {
        return opetuspisteenJarjNro;
    }

    public void setOpetuspisteenJarjNro(String opetuspisteenJarjNro) {
        this.opetuspisteenJarjNro = opetuspisteenJarjNro;
    }

    public Organisaatio getParent() {
        return parent;
    }

    public void setParent(Organisaatio parent) {
        this.parent = parent;
    }

    public Organisaatio getChild() {
        return child;
    }

    public void setChild(Organisaatio child) {
        this.child = child;
    }

    public Date getAlkuPvm() {
        return alkuPvm;
    }

    public void setAlkuPvm(Date alkuPvm) {
        this.alkuPvm = alkuPvm;
    }

    public Date getLoppuPvm() {
        return loppuPvm;
    }

    public void setLoppuPvm(Date loppuPvm) {
        this.loppuPvm = loppuPvm;
    }

    public OrganisaatioSuhdeTyyppi getSuhdeTyyppi() {
        return suhdeTyyppi;
    }

    public void setSuhdeTyyppi(OrganisaatioSuhdeTyyppi suhdeTyyppi) {
        this.suhdeTyyppi = suhdeTyyppi;
    }

}

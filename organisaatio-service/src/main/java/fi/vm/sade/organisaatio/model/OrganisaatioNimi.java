/*
* Copyright (c) 2014 The Finnish Board of Education - Opetushallitus
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

package fi.vm.sade.organisaatio.model;


import jakarta.persistence.*;
import java.util.Date;

/**
 * Entity luokka organisaation nimelle ja nimihistorialle.
 *
 * TODO xssfiltteri
 */
@Entity
@Table(name = "organisaatio_nimi")
@org.hibernate.annotations.Table(appliesTo = "organisaatio_nimi", comment = "Sisältää organisaation nimen ja nimihistorian.")
@NamedQuery(
        name = "OrganisaatioNimiDAO.findNimet",
        query = "SELECT n FROM OrganisaatioNimi n WHERE n.organisaatio.oid = :organisaatioOid"
)
@NamedQuery(
        name = "OrganisaatioNimiDAO.findNimi",
        query = "SELECT n FROM OrganisaatioNimi n WHERE n.organisaatio.oid = :organisaatioOid"
)
public class OrganisaatioNimi extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    private Organisaatio organisaatio;

    @Temporal(jakarta.persistence.TemporalType.DATE)
    @Column(name="alkupvm")
    private Date alkuPvm;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "nimi_mkt")
    private MonikielinenTeksti nimi;

    @Column(length = 255)
    private String paivittaja;

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
     * @return the alkuPvm
     */
    public Date getAlkuPvm() {
        return alkuPvm;
    }

    /**
     * @param alkuPvm the alkuPvm to set
     */
    public void setAlkuPvm(Date alkuPvm) {
        this.alkuPvm = alkuPvm;
    }

    /**
     * @return the nimi
     */
    public MonikielinenTeksti getNimi() {
        return nimi;
    }

    /**
     * @param nimi the nimi to set
     */
    public void setNimi(MonikielinenTeksti nimi) {
        this.nimi = nimi;
    }

    /**
     * @return the paivittaja
     */
    public String getPaivittaja() {
        return paivittaja;
    }

    /**
     * @param paivittaja the paivittaja to set
     */
    public void setPaivittaja(String paivittaja) {
        this.paivittaja = paivittaja;
    }
}

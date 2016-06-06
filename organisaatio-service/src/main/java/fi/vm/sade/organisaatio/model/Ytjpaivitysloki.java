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

import fi.vm.sade.generic.model.BaseEntity;
import fi.vm.sade.security.xssfilter.XssFilterListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "ytjpaivitysloki")
@org.hibernate.annotations.Table(appliesTo = "ytjpaivitysloki", comment = "Sisältää YTJ-massapäivityksen statuksen ja listan virheistä.")
@EntityListeners(XssFilterListener.class)
public class YtjPaivitysLoki extends BaseEntity {

    @Temporal(TemporalType.TIMESTAMP)
    private Date paivitysaika;

    @Column
    private int paivitetytLkm;

    @Column
    private String paivitysTila;

    @OneToMany(mappedBy = "ytjPaivitysLoki", cascade = CascadeType.ALL, orphanRemoval=true)
    private List<YtjVirhe> ytjvirheet = new ArrayList<YtjVirhe>();

    public Date getPaivitysaika() {
        return paivitysaika;
    }

    public void setPaivitysaika(Date paivitysaika) {
        this.paivitysaika = paivitysaika;
    }

    public int getPaivitetytLkm() {
        return paivitetytLkm;
    }

    public void setPaivitetytLkm(int paivitetytLkm) {
        this.paivitetytLkm = paivitetytLkm;
    }

    public String getPaivitysTila() {
        return paivitysTila;
    }

    public void setPaivitysTila(String paivitysTila) {
        this.paivitysTila = paivitysTila;
    }

    public List<YtjVirhe> getYtjvirheet() {
        return ytjvirheet;
    }

    public void setYtjvirheet(List<YtjVirhe> ytjvirheet) {
        this.ytjvirheet = ytjvirheet;
    }
}

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import fi.vm.sade.generic.model.BaseEntity;
import fi.vm.sade.security.xssfilter.XssFilterListener;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "ytjpaivitysloki")
@org.hibernate.annotations.Table(appliesTo = "ytjpaivitysloki", comment = "Sisältää YTJ-massapäivityksen statuksen ja listan virheistä.")
@EntityListeners(XssFilterListener.class)
public class YtjPaivitysLoki extends BaseEntity {

    public enum YTJPaivitysStatus {
        ONNISTUNUT,
        ONNISTUNUT_VIRHEITA,
        EPAONNISTUNUT
    }

    @Temporal(TemporalType.TIMESTAMP)
    private Date paivitysaika;

    @Column(name = "paivitetyt_lkm")
    private long paivitetytLkm;

    @Enumerated(EnumType.STRING)
    @Column(name = "paivitys_tila")
    private YTJPaivitysStatus paivitysTila;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "ytjPaivitysLoki", cascade = CascadeType.ALL, orphanRemoval=true)
    private List<YtjVirhe> ytjVirheet = new ArrayList<YtjVirhe>();

    public Date getPaivitysaika() {
        return paivitysaika;
    }

    public void setPaivitysaika(Date paivitysaika) {
        this.paivitysaika = paivitysaika;
    }

    public long getPaivitetytLkm() {
        return paivitetytLkm;
    }

    public void setPaivitetytLkm(long paivitetytLkm) {
        this.paivitetytLkm = paivitetytLkm;
    }

    public YTJPaivitysStatus getPaivitysTila() {
        return paivitysTila;
    }

    public void setPaivitysTila(YTJPaivitysStatus paivitysTila) {
        this.paivitysTila = paivitysTila;
    }

    public List<YtjVirhe> getYtjVirheet() {
        return ytjVirheet;
    }

    public void setYtjVirheet(List<YtjVirhe> ytjVirheet) {
        this.ytjVirheet = ytjVirheet;
    }
}

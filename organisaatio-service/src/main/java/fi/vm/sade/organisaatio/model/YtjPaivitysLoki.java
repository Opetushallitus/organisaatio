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

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import jakarta.persistence.*;
import java.util.*;

// TODO XSS filtteri
@Entity
@Table(name = "ytjpaivitysloki")
@org.hibernate.annotations.Table(appliesTo = "ytjpaivitysloki", comment = "Sisältää YTJ-massapäivityksen statuksen ja listan virheistä.")
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

    @Column(name = "paivitys_tila_selite")
    private String paivitysTilaSelite;

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

    public String getPaivitysTilaSelite() {
        return paivitysTilaSelite;
    }

    public void setPaivitysTilaSelite(String paivitysTilaSelite) {
        this.paivitysTilaSelite = paivitysTilaSelite;
    }

    public List<YtjVirhe> getYtjVirheet() {
        return ytjVirheet;
    }

    public void setYtjVirheet(List<YtjVirhe> ytjVirheet) {
        this.ytjVirheet = ytjVirheet;
    }

    public static Map<String, List<YtjVirhe>> getYtjVirheetMapByOid(List<YtjVirhe> ytjVirheet) {
        Map<String, List<YtjVirhe>> virheMap = new HashMap<>();
        for(final YtjVirhe ytjVirhe : ytjVirheet) {
            if(virheMap.containsKey(ytjVirhe.getOid())) {
                virheMap.get(ytjVirhe.getOid()).add(ytjVirhe);
            }
            else {
                virheMap.put(ytjVirhe.getOid(), new ArrayList<YtjVirhe>(){{add(ytjVirhe);}});
            }
        }
        return virheMap;
    }
}

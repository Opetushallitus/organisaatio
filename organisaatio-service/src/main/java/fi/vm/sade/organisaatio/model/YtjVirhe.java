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

import org.hibernate.annotations.Comment;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

// TODO XSS filtteri
@Entity
@Table(name = "ytjvirhe")
@Comment("Sisältää YTJ-massapäivityksessä tulleen virheen oidin, virheellisen kentän ja kuvauksen.")
public class YtjVirhe extends BaseEntity {

    public enum YTJVirheKohde {
        KOODISTO,
        TALLENNUS,
        VALIDOINTI,
        TUNTEMATON,
        ALKUPVM,
        NIMI,
        KIELI,
        OSOITE,
        SAHKOPOSTI,
        PUHELIN,
        WWW,
        LOPPUPVM
    }

    @JsonIgnore
    @ManyToOne
    private YtjPaivitysLoki ytjPaivitysLoki;

    @NotNull
    private String oid;

    @Column(length = 255)
    private String orgNimi;

    @Enumerated(EnumType.STRING)
    @Column(length = 255)
    private YTJVirheKohde virhekohde;

    @Column(length = 255)
    private String virheviesti;

    public YtjPaivitysLoki getYtjPaivitysLoki() {
        return ytjPaivitysLoki;
    }

    public void setYtjPaivitysLoki(YtjPaivitysLoki ytjPaivitysLoki) {
        this.ytjPaivitysLoki = ytjPaivitysLoki;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getOrgNimi() {
        return orgNimi;
    }

    public void setOrgNimi(String orgNimi) {
        this.orgNimi = orgNimi;
    }

    public YTJVirheKohde getVirhekohde() {
        return virhekohde;
    }

    public void setVirhekohde(YTJVirheKohde virhekohde) {
        this.virhekohde = virhekohde;
    }

    public String getVirheviesti() {
        return virheviesti;
    }

    public void setVirheviesti(String virheviesti) {
        this.virheviesti = virheviesti;
    }
}

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

@Entity
@Table(name = "ytjvirhe")
@org.hibernate.annotations.Table(appliesTo = "ytjvirhe", comment = "Sisältää YTJ-massapäivityksessä tulleen virheen oidin, virheellisen kentän ja kuvauksen.")
@EntityListeners(XssFilterListener.class)
public class YtjVirhe extends BaseEntity {

    @ManyToOne
    private YtjPaivitysLoki ytjPaivitysLoki;

    @NotNull
    private String oid;

    @Column(length = 255)
    private String orgNimi;

    @Column(length = 255)
    private String virhekentta;

    @Column(length = 255)
    private String virheviesti;

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

    public String getVirhekentta() {
        return virhekentta;
    }

    public void setVirhekentta(String virhekentta) {
        this.virhekentta = virhekentta;
    }

    public String getVirheviesti() {
        return virheviesti;
    }

    public void setVirheviesti(String virheviesti) {
        this.virheviesti = virheviesti;
    }
}

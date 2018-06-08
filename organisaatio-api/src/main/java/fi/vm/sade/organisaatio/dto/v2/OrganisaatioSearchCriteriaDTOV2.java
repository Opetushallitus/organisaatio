/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
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

package fi.vm.sade.organisaatio.dto.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Organisaation hakuehdot")
public class OrganisaatioSearchCriteriaDTOV2 {

    private boolean aktiiviset;
    private boolean suunnitellut;
    private boolean lakkautetut;

    private Set<String> kunta = new HashSet<String>();
    private String organisaatiotyyppi;
    private Set<String> oppilaitostyyppi = new HashSet<String>();
    private Set<String> kieli = new HashSet<String>();

    private List<String> oidRestrictionList = new ArrayList<String>();

    private String searchStr;
    private String oid;

    private boolean skipParents;

    /**
     * Default no-arg constructor
     *
     */
    public OrganisaatioSearchCriteriaDTOV2() {
        super();
    }

    @ApiModelProperty(value = "Otetaanko aktiiviset organisaatiot mukaan hakutuloksiin", required = true)
    public boolean getAktiiviset() {
        return aktiiviset;
    }

    public void setAktiiviset(boolean aktiiviset) {
        this.aktiiviset = aktiiviset;
    }

    @ApiModelProperty(value = "Otetaanko suunnitellut organisaatiot mukaan hakutuloksiin", required = true)
    public boolean getSuunnitellut() {
        return suunnitellut;
    }

    public void setSuunnitellut(boolean value) {
        this.suunnitellut = value;
    }

    @ApiModelProperty(value = "Otetaanko lakkautetut organisaatiot mukaan hakutuloksiin", required = true)
    public boolean getLakkautetut() {
        return lakkautetut;
    }

    public void setLakkautetut(boolean lakkautetut) {
        this.lakkautetut = lakkautetut;
    }

    @ApiModelProperty(value = "Haettavan organisaation kunta tai lista kunnista", required = true)
    public Set<String> getKunta() {
        return kunta;
    }

    public void setKunta(Set<String> value) {
        if (value != null) {
            this.kunta.addAll(value);
        }
    }

    @ApiModelProperty(value = "Haettavan organisaation tyyppi", required = true)
    public String getOrganisaatioTyyppi() {
        return organisaatiotyyppi;
    }

    public void setOrganisaatioTyyppi(String value) {
        this.organisaatiotyyppi = value;
    }

    @ApiModelProperty(value = "Haettavan oppilaitoksen tyyppi tai lista tyypeistä", required = true)
    public Set<String> getOppilaitosTyyppi() {
        return oppilaitostyyppi;
    }

    public void setOppilaitosTyyppi(Set<String> oppilaitostyyppi) {
        if (oppilaitostyyppi != null) {
            this.oppilaitostyyppi.addAll(oppilaitostyyppi);
        }
    }
    
    @ApiModelProperty(value = "Haettavan organisaation kieli tai lista kielistä", required = true)
    public Set<String> getKieli() {
        return kieli;
    }

    public void setKieli(Set<String> value) {
        if (value != null) {
            this.kieli.addAll(value);
        }
    }

    @ApiModelProperty(value = "Lista sallituista organisaatioiden oid:stä", required = true)
    public List<String> getOidRestrictionList() {
        return this.oidRestrictionList;
    }

    public void setOidRestrictionList(List<String> oidRestrictionList) {
        this.oidRestrictionList.addAll(oidRestrictionList);
    }

    @ApiModelProperty(value = "Hakutermit", required = true)
    public String getSearchStr() {
        return searchStr;
    }

    public void setSearchStr(String searchStr) {
        this.searchStr = searchStr;
    }

    @ApiModelProperty(value = "Jätetäänkö yläorganisaatiot pois hakutuloksista", required = true)
    public boolean getSkipParents() {
        return skipParents;
    }

    public void setSkipParents(boolean skipParents) {
        this.skipParents = skipParents;
    }

    @ApiModelProperty(value = "Haku oid:lla. Hakutermi jätetään huomioimatta jos oid on annettu.")
    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

}

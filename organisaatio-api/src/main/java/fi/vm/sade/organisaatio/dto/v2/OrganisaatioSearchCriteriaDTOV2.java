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

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Organisaation hakuehdot")
public class OrganisaatioSearchCriteriaDTOV2 {

    private boolean aktiiviset;
    private boolean suunnitellut;
    private boolean lakkautetut;

    private String kunta;
    private String organisaatiotyyppi;
    private String oppilaitostyyppi;
    private String kieli;

    private List<String> oidResctrictionList = new ArrayList<String>();

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

    @ApiModelProperty(value = "Haettavan organisaation kunta", required = true)
    public String getKunta() {
        return kunta;
    }

    public void setKunta(String value) {
        this.kunta = value;
    }

    @ApiModelProperty(value = "Haettavan organisaation tyyppi", required = true)
    public String getOrganisaatioTyyppi() {
        return organisaatiotyyppi;
    }

    public void setOrganisaatioTyyppi(String value) {
        this.organisaatiotyyppi = value;
    }

    @ApiModelProperty(value = "Haettavan oppilaitoksen tyyppi", required = true)
    public String getOppilaitosTyyppi() {
        return oppilaitostyyppi;
    }

    public void setOppilaitosTyyppi(String oppilaitostyyppi) {
        this.oppilaitostyyppi = oppilaitostyyppi;
    }
    
    @ApiModelProperty(value = "Haettavan organisaation kieli", required = true)
    public String getKieli() {
        return kieli;
    }

    public void setKieli(String value) {
        this.kieli = value;
    }

    @ApiModelProperty(value = "Lista sallituista organisaatioiden oid:stä", required = true)
    public List<String> getOidRestrictionList() {
        return this.oidResctrictionList;
    }

    public void setOidRestrictionList(List<String> oidRestrictionList) {
        this.oidResctrictionList.addAll(oidResctrictionList);
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

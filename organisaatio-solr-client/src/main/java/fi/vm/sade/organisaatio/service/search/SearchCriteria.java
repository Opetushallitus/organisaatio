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

package fi.vm.sade.organisaatio.service.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchCriteria {

    private boolean aktiiviset;
    private boolean suunnitellut;
    private boolean lakkautetut;

    private List<String> kunta = new ArrayList<String>();
    private String organisaatiotyyppi;
    private List<String> oppilaitostyyppi = new ArrayList<String>();
    private List<String> kieli = new ArrayList<String>();

    private List<String> oidRestrictionList = new ArrayList<String>();

    private String searchStr;

    private String oid;

    private boolean skipParents;

    /**
     * Default no-arg constructor
     *
     */
    public SearchCriteria() {
        super();
    }

    public boolean getAktiiviset() {
        return aktiiviset;
    }

    public void setAktiiviset(boolean aktiiviset) {
        this.aktiiviset = aktiiviset;
    }

    public boolean getSuunnitellut() {
        return suunnitellut;
    }

    public void setSuunnitellut(boolean value) {
        this.suunnitellut = value;
    }

    public boolean getLakkautetut() {
        return lakkautetut;
    }

    public void setLakkautetut(boolean lakkautetut) {
        this.lakkautetut = lakkautetut;
    }

    public List<String> getKunta() {
        return kunta;
    }

    public void setKunta(List<String> value) {
        this.kunta.clear();
        this.kunta.addAll(value);
    }

    public String getOrganisaatioTyyppi() {
        return organisaatiotyyppi;
    }

    public void setOrganisaatioTyyppi(String value) {
        this.organisaatiotyyppi = value;
    }
    
    public List<String> getKieli() {
        return kieli;
    }

    public void setKieli(List<String> value) {
        this.kieli.clear();
        this.kieli.addAll(value);
    }

    public List<String> getOppilaitosTyyppi() {
        return oppilaitostyyppi;
    }

    public void setOppilaitosTyyppi(List<String> oppilaitostyyppi) {
        this.oppilaitostyyppi.clear();
        this.oppilaitostyyppi.addAll(oppilaitostyyppi);
    }

    public List<String> getOidRestrictionList() {
        return this.oidRestrictionList;
    }

    public void setOidRestrictionList(List<String> oidRestrictionList) {
        this.oidRestrictionList.clear();
        this.oidRestrictionList.addAll(oidRestrictionList);
    }

    public String getSearchStr() {
        return searchStr;
    }

    public void setSearchStr(String searchStr) {
        this.searchStr = searchStr;
    }

    public boolean getSkipParents() {
        return skipParents;
    }

    public void setSkipParents(boolean skipParents) {
        this.skipParents = skipParents;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

}

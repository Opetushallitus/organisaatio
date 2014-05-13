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
    
    private String kunta;
    private String organisaatiotyyppi;
    private String oppilaitostyyppi;

    private List<String> oidRestrictionList = new ArrayList<String>();

    private String searchStr;

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

    public String getKunta() {
        return kunta;
    }

    public void setKunta(String value) {
        this.kunta = value;
    }

    public String getOrganisaatioTyyppi() {
        return organisaatiotyyppi;
    }

    public void setOrganisaatioTyyppi(String value) {
        this.organisaatiotyyppi = value;
    }

    public String getOppilaitosTyyppi() {
        return oppilaitostyyppi;
    }

    public void setOppilaitosTyyppi(String oppilaitostyyppi) {
        this.oppilaitostyyppi = oppilaitostyyppi;
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
}

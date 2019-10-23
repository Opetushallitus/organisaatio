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
import java.util.Collection;
import java.util.List;

public class SearchCriteria {

    private boolean aktiiviset;
    private boolean suunnitellut;
    private boolean lakkautetut;
    private Boolean poistettu;

    private List<String> yritysmuoto = new ArrayList<>();
    private List<String> kunta = new ArrayList<String>();
    private List<String> organisaatiotyyppi = new ArrayList<String>();
    private List<String> oppilaitostyyppi = new ArrayList<String>();
    private List<String> kieli = new ArrayList<String>();

    private Collection<String> oidRestrictionList = new ArrayList<String>();

    private String searchStr;

    private Collection<String> oid = new ArrayList<>();
    private Collection<String> parentOidPaths = new ArrayList<String>();

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

    public Boolean getPoistettu() {
        return poistettu;
    }

    public void setPoistettu(Boolean poistettu) {
        this.poistettu = poistettu;
    }

    public List<String> getYritysmuoto() {
        return yritysmuoto;
    }

    public void setYritysmuoto(List<String> yritysmuoto) {
        this.yritysmuoto = yritysmuoto;
    }

    public List<String> getKunta() {
        return kunta;
    }

    public void setKunta(List<String> value) {
        this.kunta.clear();
        if (value != null) {
            this.kunta.addAll(value);
        }
    }

    public List<String> getOrganisaatioTyyppi() {
        return organisaatiotyyppi;
    }

    public void setOrganisaatioTyyppi(List<String> organisaatiotyyppi) {
        this.organisaatiotyyppi.clear();
        if (organisaatiotyyppi != null) {
            this.organisaatiotyyppi.addAll(organisaatiotyyppi);
        }
    }

    public List<String> getKieli() {
        return kieli;
    }

    public void setKieli(List<String> value) {
        this.kieli.clear();
        if (value != null) {
            this.kieli.addAll(value);
        }
    }

    public List<String> getOppilaitosTyyppi() {
        return oppilaitostyyppi;
    }

    public void setOppilaitosTyyppi(List<String> oppilaitostyyppi) {
        this.oppilaitostyyppi.clear();
        if (oppilaitostyyppi != null) {
            this.oppilaitostyyppi.addAll(oppilaitostyyppi);
        }
    }

    public Collection<String> getOidRestrictionList() {
        return this.oidRestrictionList;
    }

    public void setOidRestrictionList(Collection<String> oidRestrictionList) {
        this.oidRestrictionList.clear();
        this.oidRestrictionList.addAll(oidRestrictionList);
    }

    public String getSearchStr() {
        return searchStr;
    }

    public void setSearchStr(String searchStr) {
        this.searchStr = searchStr;
    }

    public Collection<String> getOid() {
        return oid;
    }

    public void setOid(Collection oid) {
        this.oid.clear();
        if (oid != null) {
            this.oid.addAll(oid);
        }
    }

    public Collection<String> getParentOidPaths() {
        return parentOidPaths;
    }

    public void setParentOidPaths(Collection<String> parentOidPaths) {
        this.parentOidPaths.clear();
        if (parentOidPaths != null) {
            this.parentOidPaths.addAll(parentOidPaths);
        }
    }

}

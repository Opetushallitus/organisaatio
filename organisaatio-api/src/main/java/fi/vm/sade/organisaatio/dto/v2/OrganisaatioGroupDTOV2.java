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

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author simok
 */
public class OrganisaatioGroupDTOV2 extends OrganisaatioCoreInfoDTOV2 {
    private int version;
    private String parentOid;
    private String parentOidPath;
    private Date alkuPvm;
    private Date lakkautusPvm;
    private Map<String, String> kuvaus;
    private List<String> ryhmatyypit;
    private List<String> kayttoryhmat;

    /**
     * @return the version
     */
    public int getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * @return the alkuPvm
     */
    public Date getAlkuPvm() {
        return alkuPvm;
    }

    /**
     * @param alkuPvm the alkuPvm to set
     */
    public void setAlkuPvm(Date alkuPvm) {
        this.alkuPvm = alkuPvm;
    }

    /**
     * @return the lakkautusPvm
     */
    public Date getLakkautusPvm() {
        return lakkautusPvm;
    }

    /**
     * @param lakkautusPvm the lakkautusPvm to set
     */
    public void setLakkautusPvm(Date lakkautusPvm) {
        this.lakkautusPvm = lakkautusPvm;
    }

    /**
     * @return the kuvaus
     */
    public Map<String, String> getKuvaus() {
        return kuvaus;
    }

    /**
     * @param kuvaus the kuvaus to set
     */
    public void setKuvaus(Map<String, String> kuvaus) {
        this.kuvaus = kuvaus;
    }

    /**
     * @return the ryhmatyypit
     */
    public List<String> getRyhmatyypit() {
        return ryhmatyypit;
    }

    /**
     * @param ryhmatyypit the ryhmatyypit to set
     */
    public void setRyhmatyypit(List<String> ryhmatyypit) {
        this.ryhmatyypit = ryhmatyypit;
    }

    /**
     * @return the kayttoryhmat
     */
    public List<String> getKayttoryhmat() {
        return kayttoryhmat;
    }

    /**
     * @param kayttoryhmat the kayttoryhmat to set
     */
    public void setKayttoryhmat(List<String> kayttoryhmat) {
        this.kayttoryhmat = kayttoryhmat;
    }

    /**
     * @return the parentOid
     */
    public String getParentOid() {
        return parentOid;
    }

    /**
     * @param parentOid the parentOid to set
     */
    public void setParentOid(String parentOid) {
        this.parentOid = parentOid;
    }

    /**
     * @return the parentOidPath
     */
    public String getParentOidPath() {
        return parentOidPath;
    }

    /**
     * @param parentOidPath the parentOidPath to set
     */
    public void setParentOidPath(String parentOidPath) {
        this.parentOidPath = parentOidPath;
    }

}

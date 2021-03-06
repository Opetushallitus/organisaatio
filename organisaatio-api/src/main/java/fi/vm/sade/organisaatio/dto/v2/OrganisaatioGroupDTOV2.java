package fi.vm.sade.organisaatio.dto.v2;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.Map;
import java.util.Set;

@ApiModel(value = "Ryhmän tiedot", parent=OrganisaatioCoreInfoDTOV2.class)
public class OrganisaatioGroupDTOV2 extends OrganisaatioCoreInfoDTOV2 {
    private int version;
    private String parentOid;
    private String parentOidPath;
    private Date alkuPvm;
    private Date lakkautusPvm;
    private Map<String, String> kuvaus;
    private Set<String> ryhmatyypit;
    private Set<String> kayttoryhmat;

    /**
     * @return the version
     */
    @ApiModelProperty(value = "Versio", required = true)
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
    @ApiModelProperty(value = "Ryhmän voimassaolon alkupäivämäärä", required = true)
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
    @ApiModelProperty(value = "Ryhmän lakkautuspäivämäärä", required = true)
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
    @ApiModelProperty(value = "Ryhmän kuvaus", required = true)
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
    @ApiModelProperty(value = "Ryhmätyypit", required = true)
    public Set<String> getRyhmatyypit() {
        return ryhmatyypit;
    }

    /**
     * @param ryhmatyypit the ryhmatyypit to set
     */
    public void setRyhmatyypit(Set<String> ryhmatyypit) {
        this.ryhmatyypit = ryhmatyypit;
    }

    /**
     * @return the kayttoryhmat
     */
    @ApiModelProperty(value = "Ryhmän käyttöryhmät", required = true)
    public Set<String> getKayttoryhmat() {
        return kayttoryhmat;
    }

    /**
     * @param kayttoryhmat the kayttoryhmat to set
     */
    public void setKayttoryhmat(Set<String> kayttoryhmat) {
        this.kayttoryhmat = kayttoryhmat;
    }

    /**
     * @return the parentOid
     */
    @ApiModelProperty(value = "Parent oid", required = true)
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
    @ApiModelProperty(value = "Parent oid polkuna", required = true)
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

package fi.vm.sade.organisaatio.dto.v2;


import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;
import java.util.Map;
import java.util.Set;

@Schema(description = "Ryhmän tiedot")
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
    @Schema(description = "Versio", required = true)
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
    @Schema(description = "Ryhmän voimassaolon alkupäivämäärä", required = true)
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
    @Schema(description = "Ryhmän lakkautuspäivämäärä", required = true)
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
    @Schema(description = "Ryhmän kuvaus", required = true)
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
    @Schema(description = "Ryhmätyypit", required = true)
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
    @Schema(description = "Ryhmän käyttöryhmät", required = true)
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
    @Schema(description = "Parent oid", required = true)
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
    @Schema(description = "Parent oid polkuna", required = true)
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

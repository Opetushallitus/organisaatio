package fi.vm.sade.organisaatio.api.search;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashSet;
import java.util.Set;

@ApiModel(value = "Organisaation hakuehdot")
public class OrganisaatioSearchCriteria {

    private boolean vainLakkautetut;

    private boolean vainAktiiviset;

    private Set<String> oppilaitostyyppi = new HashSet<String>();

    private String organisaatiotyyppi;

    private boolean suunnitellut;

    private Set<String> kunta = new HashSet<String>();

    private List<String> oidResctrictionList = new ArrayList<String>();

    private String searchStr;

    private String oid;

    private boolean skipParents;

    /**
     * Default no-arg constructor
     *
     */
    public OrganisaatioSearchCriteria() {
        super();
    }

    public void setOidRestrictionList(List<String> oidRestrictionList) {
        this.oidResctrictionList.addAll(oidResctrictionList);
    }

    /**
     * Gets the value of the oppilaitosTyyppi property.
     *
     * @return possible object is {@link String }
     *
     */
    @ApiModelProperty(value = "Haettavan oppilaitoksen tyyppi tai lista tyypeistä", required = true)
    public Set<String> getOppilaitosTyyppi() {
        return oppilaitostyyppi;
    }

    /**
     * Sets the value of the oppilaitosTyyppi property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setOppilaitosTyyppi(Set<String> value) {
        if (value != null) {
            this.oppilaitostyyppi.addAll(value);
        }
    }

    /**
     * Gets the value of the organisaatioTyyppi property.
     *
     * @return possible object is {@link String }
     *
     */
    @ApiModelProperty(value = "Haettavan organisaation tyyppi", required = true)
    public String getOrganisaatioTyyppi() {
        return organisaatiotyyppi;
    }

    /**
     * Sets the value of the organisaatioTyyppi property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setOrganisaatioTyyppi(String value) {
        this.organisaatiotyyppi = value;
    }

    /**
     * Gets the value of the suunnitellut property.
     *
     */
    @ApiModelProperty(value = "Otetaanko suunnitellut organisaatiot mukaan hakutuloksiin", required = true)
    public boolean getSuunnitellut() {
        return suunnitellut;
    }

    /**
     * Sets the value of the suunnitellut property.
     *
     */
    public void setSuunnitellut(boolean value) {
        this.suunnitellut = value;
    }

    @ApiModelProperty(value = "Jätetäänkö yläorganisaatiot pois hakutuloksista", required = true)
    public boolean getSkipParents() {
        return skipParents;
    }

    /**
     * If true does not return parents, default = false
     *
     * @param skipParents
     */
    public void setSkipParents(boolean skipParents) {
        this.skipParents = skipParents;
    }

    /**
     * Gets the value of the kunta property.
     *
     * @return possible object is {@link String }
     *
     */
    @ApiModelProperty(value = "Haettavan organisaation kunta tai lista kunnista", required = true)
    public Set<String> getKunta() {
        return kunta;
    }

    /**
     * Sets the value of the kunta property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setKunta(Set<String> value) {
        if (value != null) {
            this.kunta.addAll(value);
        }
    }

    @ApiModelProperty(value = "Lista sallituista organisaatioiden oid:stä", required = true)
    public List<String> getOidRestrictionList() {
        return this.oidResctrictionList;
    }

    @ApiModelProperty(value = "Hakutermit", required = true)
    public String getSearchStr() {
        return searchStr;
    }

    public void setSearchStr(String searchStr) {
        this.searchStr = searchStr;
    }

    /**
     * Gets the value of the vainLakkautetut property.
     */
    @ApiModelProperty(value = "Otetaanko vain lakkautetut organisaatiot mukaan hakutuloksiin", required = true)
    public boolean getVainLakkautetut() {
        return vainLakkautetut;
    }

    /**
     * Sets the value of the vainLakkautetut property.
     *
     */
    public void setVainLakkautetut(boolean value) {
        this.vainLakkautetut = value;
    }

    /**
     * Gets the value of the vainAktiiviset property.
     */
    @ApiModelProperty(value = "Otetaanko vain aktiiviset organisaatiot mukaan hakutuloksiin", required = true)
    public boolean getVainAktiiviset() {
        return vainAktiiviset;
    }

    /**
     * Sets the value of the vainLakkautetut property.
     *
     */
    public void setVainAktiiviset(boolean vainAktiiviset) {
        this.vainAktiiviset = vainAktiiviset;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

}

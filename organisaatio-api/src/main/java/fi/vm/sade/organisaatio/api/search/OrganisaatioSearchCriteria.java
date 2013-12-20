package fi.vm.sade.organisaatio.api.search;

import java.util.ArrayList;
import java.util.List;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Organisaation hakuehdot")
public class OrganisaatioSearchCriteria {

    private boolean lakkautetut;

    private String oppilaitostyyppi;

    private String organisaatiotyyppi;

    private boolean suunnitellut;

    private String kunta;

    private List<String> oidResctrictionList = new ArrayList<String>();

    private String searchStr;

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
     * Gets the value of the lakkautetut property.
     */
    @ApiModelProperty(value = "Otetaanko lakkautetut organisaatiot mukaan hakutuloksiin", required = true)
    public boolean getLakkautetut() {
        return lakkautetut;
    }

    /**
     * Sets the value of the lakkautetut property.
     * 
     */
    public void setLakkautetut(boolean value) {
        this.lakkautetut = value;
    }

    /**
     * Gets the value of the oppilaitosTyyppi property.
     * 
     * @return possible object is {@link String }
     * 
     */
    @ApiModelProperty(value = "Haettavan oppilaitoksen tyyppi", required = true)
    public String getOppilaitosTyyppi() {
        return oppilaitostyyppi;
    }

    /**
     * Sets the value of the oppilaitosTyyppi property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setOppilaitosTyyppi(String value) {
        this.oppilaitostyyppi = value;
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
    @ApiModelProperty(value = "Haettavan organisaation kunta", required = true)
    public String getKunta() {
        return kunta;
    }

    /**
     * Sets the value of the kunta property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setKunta(String value) {
        this.kunta = value;
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

}

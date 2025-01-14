package fi.vm.sade.organisaatio.api.search;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.HashSet;
import java.util.Set;

@Schema(description = "Organisaation hakuehdot")
public class OrganisaatioSearchCriteria {

    private boolean vainLakkautetut;

    private boolean vainAktiiviset;

    private Set<String> oppilaitostyyppi = new HashSet<>();

    private String organisaatiotyyppi;

    private boolean suunnitellut;

    private Set<String> kunta = new HashSet<>();

    private Set<String> oidRestrictionList = new HashSet<>();

    private String searchStr;

    private String oid;

    private boolean skipParents;

    /**
     * Default no-arg constructor
     */
    public OrganisaatioSearchCriteria() {
        super();
    }

    public void setOidRestrictionList(Set<String> oidRestrictionList) {
        this.oidRestrictionList.addAll(oidRestrictionList);
    }

    /**
     * Gets the description of the oppilaitosTyyppi property.
     *
     * @return possible object is {@link String }
     */
    @Schema(description = "Haettavan oppilaitoksen tyyppi tai lista tyypeistä", required = true)
    public Set<String> getOppilaitostyyppi() {
        return oppilaitostyyppi;
    }

    /**
     * Sets the description of the oppilaitosTyyppi property.
     *
     * @param value allowed object is {@link String }
     */
    public void setOppilaitostyyppi(Set<String> value) {
        if (value != null) {
            this.oppilaitostyyppi.addAll(value);
        }
    }

    /**
     * Gets the value of the organisaatioTyyppi property.
     *
     * @return possible object is {@link String }
     */
    @Schema(description = "Haettavan organisaation tyyppi", required = true)
    public String getOrganisaatiotyyppi() {
        return organisaatiotyyppi;
    }

    /**
     * Sets the value of the organisaatioTyyppi property.
     *
     * @param value allowed object is {@link String }
     */
    public void setOrganisaatiotyyppi(String value) {
        this.organisaatiotyyppi = value;
    }

    /**
     * Gets the value of the suunnitellut property.
     */
    @Schema(description = "Otetaanko suunnitellut organisaatiot mukaan hakutuloksiin", required = true)
    public boolean getSuunnitellut() {
        return suunnitellut;
    }

    /**
     * Sets the value of the suunnitellut property.
     */
    public void setSuunnitellut(boolean value) {
        this.suunnitellut = value;
    }

    @Schema(description = "Jätetäänkö yläorganisaatiot pois hakutuloksista", required = true)
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
     */
    @Schema(description = "Haettavan organisaation kunta tai lista kunnista", required = true)
    public Set<String> getKunta() {
        return kunta;
    }

    /**
     * Sets the value of the kunta property.
     *
     * @param value allowed object is {@link String }
     */
    public void setKunta(Set<String> value) {
        if (value != null) {
            this.kunta.addAll(value);
        }
    }

    @Schema(description = "Lista sallituista organisaatioiden oid:stä", required = true)
    public Set<String> getOidRestrictionList() {
        return this.oidRestrictionList;
    }

    @Schema(description = "Hakutermit", required = true)
    public String getSearchStr() {
        return searchStr;
    }

    public void setSearchStr(String searchStr) {
        this.searchStr = searchStr;
    }

    /**
     * Gets the value of the vainLakkautetut property.
     */
    @Schema(description = "Otetaanko vain lakkautetut organisaatiot mukaan hakutuloksiin", required = true)
    public boolean getVainLakkautetut() {
        return vainLakkautetut;
    }

    /**
     * Sets the value of the vainLakkautetut property.
     */
    public void setVainLakkautetut(boolean value) {
        this.vainLakkautetut = value;
    }

    /**
     * Gets the value of the vainAktiiviset property.
     */
    @Schema(description = "Otetaanko vain aktiiviset organisaatiot mukaan hakutuloksiin", required = true)
    public boolean getVainAktiiviset() {
        return vainAktiiviset;
    }

    /**
     * Sets the value of the vainLakkautetut property.
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

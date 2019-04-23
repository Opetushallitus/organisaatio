package fi.vm.sade.organisaatio.dto.v4;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashSet;
import java.util.Set;

@ApiModel(value = "Organisaation hakuehdot v4")
public class OrganisaatioSearchCriteriaDTOV4 {

    private boolean aktiiviset;
    private boolean suunnitellut;
    private boolean lakkautetut;

    private Set<String> kunta = new HashSet<>();
    private String organisaatiotyyppi;
    private Set<String> oppilaitostyyppi = new HashSet<>();
    private Set<String> kieli = new HashSet<>();

    private Set<String> oidRestrictionList = new HashSet<>();

    private String searchStr;
    private String oid;

    private boolean skipParents;

    /**
     * Default no-arg constructor
     *
     */
    public OrganisaatioSearchCriteriaDTOV4() {
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

    @ApiModelProperty(value = "Haettavan organisaation kunta tai lista kunnista", required = true)
    public Set<String> getKunta() {
        return kunta;
    }

    public void setKunta(Set<String> value) {
        this.kunta = value;
    }

    @ApiModelProperty(value = "Haettavan organisaation tyyppi", required = true)
    public String getOrganisaatioTyyppi() {
        return organisaatiotyyppi;
    }

    public void setOrganisaatioTyyppi(String value) {
        this.organisaatiotyyppi = value;
    }

    @ApiModelProperty(value = "Haettavan oppilaitoksen tyyppi tai lista tyypeistä", required = true)
    public Set<String> getOppilaitosTyyppi() {
        return oppilaitostyyppi;
    }

    public void setOppilaitosTyyppi(Set<String> oppilaitostyyppi) {
        this.oppilaitostyyppi = oppilaitostyyppi;
    }
    
    @ApiModelProperty(value = "Haettavan organisaation kieli tai lista kielistä", required = true)
    public Set<String> getKieli() {
        return kieli;
    }

    public void setKieli(Set<String> value) {
        this.kieli = value;
    }

    @ApiModelProperty(value = "Lista sallituista organisaatioiden oid:stä", required = true)
    public Set<String> getOidRestrictionList() {
        if (this.oidRestrictionList == null) {
            this.oidRestrictionList = new HashSet<>();
        }
        return this.oidRestrictionList;
    }

    public void setOidRestrictionList(Set<String> oidRestrictionList) {
        this.oidRestrictionList = oidRestrictionList;
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

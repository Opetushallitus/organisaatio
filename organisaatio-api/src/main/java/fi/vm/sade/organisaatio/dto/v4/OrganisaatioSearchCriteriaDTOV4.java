package fi.vm.sade.organisaatio.dto.v4;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.HashSet;
import java.util.Set;

@Schema(description = "Organisaation hakuehdot v4")
public class OrganisaatioSearchCriteriaDTOV4 {

    private boolean aktiiviset;
    private boolean suunnitellut;
    private boolean lakkautetut;

    private Set<String> yritysmuoto = new HashSet<>();
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

    @Schema(description = "Otetaanko aktiiviset organisaatiot mukaan hakutuloksiin", required = true)
    public boolean getAktiiviset() {
        return aktiiviset;
    }

    public void setAktiiviset(boolean aktiiviset) {
        this.aktiiviset = aktiiviset;
    }

    @Schema(description = "Otetaanko suunnitellut organisaatiot mukaan hakutuloksiin", required = true)
    public boolean getSuunnitellut() {
        return suunnitellut;
    }

    public void setSuunnitellut(boolean value) {
        this.suunnitellut = value;
    }

    @Schema(description = "Otetaanko lakkautetut organisaatiot mukaan hakutuloksiin", required = true)
    public boolean getLakkautetut() {
        return lakkautetut;
    }

    public void setLakkautetut(boolean lakkautetut) {
        this.lakkautetut = lakkautetut;
    }

    @Schema(description = "Haettavan organisaation yritysmuoto tai lista yritysmuodoista", required = true)
    public Set<String> getYritysmuoto() {
        return yritysmuoto;
    }

    public void setYritysmuoto(Set<String> yritysmuoto) {
        this.yritysmuoto = yritysmuoto;
    }

    @Schema(description = "Haettavan organisaation kunta tai lista kunnista", required = true)
    public Set<String> getKunta() {
        return kunta;
    }

    public void setKunta(Set<String> value) {
        this.kunta = value;
    }

    @Schema(description = "Haettavan organisaation tyyppi", required = true)
    public String getOrganisaatiotyyppi() {
        return organisaatiotyyppi;
    }

    public void setOrganisaatiotyyppi(String value) {
        this.organisaatiotyyppi = value;
    }

    @Schema(description = "Haettavan oppilaitoksen tyyppi tai lista tyypeistä", required = true)
    public Set<String> getOppilaitostyyppi() {
        return oppilaitostyyppi;
    }

    public void setOppilaitostyyppi(Set<String> oppilaitostyyppi) {
        this.oppilaitostyyppi = oppilaitostyyppi;
    }
    
    @Schema(description = "Haettavan organisaation kieli tai lista kielistä", required = true)
    public Set<String> getKieli() {
        return kieli;
    }

    public void setKieli(Set<String> value) {
        this.kieli = value;
    }

    @Schema(description = "Lista sallituista organisaatioiden oid:stä", required = true)
    public Set<String> getOidRestrictionList() {
        if (this.oidRestrictionList == null) {
            this.oidRestrictionList = new HashSet<>();
        }
        return this.oidRestrictionList;
    }

    public void setOidRestrictionList(Set<String> oidRestrictionList) {
        this.oidRestrictionList = oidRestrictionList;
    }

    @Schema(description = "Hakutermit", required = true)
    public String getSearchStr() {
        return searchStr;
    }

    public void setSearchStr(String searchStr) {
        this.searchStr = searchStr;
    }

    @Schema(description = "Jätetäänkö yläorganisaatiot pois hakutuloksista", required = true)
    public boolean getSkipParents() {
        return skipParents;
    }

    public void setSkipParents(boolean skipParents) {
        this.skipParents = skipParents;
    }

    @Schema(description = "Haku oid:lla. Hakutermi jätetään huomioimatta jos oid on annettu.")
    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

}

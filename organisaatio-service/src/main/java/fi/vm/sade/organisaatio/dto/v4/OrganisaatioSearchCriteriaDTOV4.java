package fi.vm.sade.organisaatio.dto.v4;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.HashSet;
import java.util.Set;

@Schema(description = "Organisaation hakuehdot v4")
public class OrganisaatioSearchCriteriaDTOV4 {
    @Schema(defaultValue = "true")
    @Parameter(description = "Otetaanko aktiiviset organisaatiot mukaan hakutuloksiin", required = true)
    private boolean aktiiviset;
    @Schema(defaultValue = "false")
    @Parameter(description = "Otetaanko suunnitellut organisaatiot mukaan hakutuloksiin", required = true)
    private boolean suunnitellut;
    @Schema(defaultValue = "false")
    @Parameter(description = "Otetaanko lakkautetut organisaatiot mukaan hakutuloksiin", required = true)
    private boolean lakkautetut;
    @Parameter(description = "Haettavan organisaation yritysmuoto tai lista yritysmuodoista")
    private Set<String> yritysmuoto = new HashSet<>();
    @Parameter(description = "Haettavan organisaation kunta tai lista kunnista")
    private Set<String> kunta = new HashSet<>();
    @Parameter(description = "Haettavan organisaation tyyppi")
    private String organisaatiotyyppi;
    @Parameter(description = "Haettavan oppilaitoksen tyyppi tai lista tyypeistä")
    private Set<String> oppilaitostyyppi = new HashSet<>();
    @Parameter(description = "Haettavan organisaation kieli tai lista kielistä")
    private Set<String> kieli = new HashSet<>();
    @Parameter(description = "Lista sallituista organisaatioiden oid:stä")
    private Set<String> oidRestrictionList = new HashSet<>();
    @Parameter(description = "Hakutermit")
    private String searchStr;
    @Parameter(description = "Haku oid:lla. Hakutermi jätetään huomioimatta jos oid on annettu.")
    private String oid;
    @Parameter(description = "Jätetäänkö yläorganisaatiot pois hakutuloksista")
    private boolean skipParents;

    /**
     * Default no-arg constructor
     */
    public OrganisaatioSearchCriteriaDTOV4() {
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

    public Set<String> getYritysmuoto() {
        return yritysmuoto;
    }

    public void setYritysmuoto(Set<String> yritysmuoto) {
        this.yritysmuoto = yritysmuoto;
    }

    public Set<String> getKunta() {
        return kunta;
    }

    public void setKunta(Set<String> value) {
        this.kunta = value;
    }

    public String getOrganisaatiotyyppi() {
        return organisaatiotyyppi;
    }

    public void setOrganisaatiotyyppi(String value) {
        this.organisaatiotyyppi = value;
    }

    public Set<String> getOppilaitostyyppi() {
        return oppilaitostyyppi;
    }

    public void setOppilaitostyyppi(Set<String> oppilaitostyyppi) {
        this.oppilaitostyyppi = oppilaitostyyppi;
    }

    public Set<String> getKieli() {
        return kieli;
    }

    public void setKieli(Set<String> value) {
        this.kieli = value;
    }

    public Set<String> getOidRestrictionList() {
        if (this.oidRestrictionList == null) {
            this.oidRestrictionList = new HashSet<>();
        }
        return this.oidRestrictionList;
    }

    public void setOidRestrictionList(Set<String> oidRestrictionList) {
        this.oidRestrictionList = oidRestrictionList;
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

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

}

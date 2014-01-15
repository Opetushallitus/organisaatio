package fi.vm.sade.organisaatio.api.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OrganisaatioSearchCriteria  {

    private boolean vainLakkautetut;
    private boolean vainAktiiviset;
    private String oppilaitostyyppi;
    private String organisaatiotyyppi;
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
        System.out.println("oid r list called");
        this.oidResctrictionList.addAll(oidResctrictionList);
    }

    /**
     * Gets the value of the oppilaitosTyyppi property.
     * 
     * @return possible object is {@link String }
     * 
     */
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


    public boolean getSkipParents(){
        return skipParents;
    }
    
    /**
     * If true does not return parents, default = false
     * @param skipParents
     */
    public void setSkipParents(boolean skipParents){
        this.skipParents = skipParents;
    }

    /**
     * Gets the value of the kunta property.
     * 
     * @return possible object is {@link String }
     * 
     */
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

    public List<String> getOidRestrictionList() {
        return this.oidResctrictionList;
    }

    public String getSearchStr() {
        return searchStr;
    }

    public void setSearchStr(String searchStr) {
        System.out.println("searchSrt:" + searchStr);
        this.searchStr = searchStr;
    }


    public boolean isVainLakkautetut() {
        return vainLakkautetut;
    }


    public void setVainLakkautetut(boolean vainLakkautetut) {
        this.vainLakkautetut = vainLakkautetut;
    }


    public boolean isVainAktiiviset() {
        return vainAktiiviset;
    }


    public void setVainAktiiviset(boolean vainAktiiviset) {
        this.vainAktiiviset = vainAktiiviset;
    }

    
    
}

package fi.vm.sade.organisaatio.dto.v2;

import java.util.HashSet;
import java.util.Set;

public class YhteystiedotSearchCriteriaDTOV2 {
    // Palautettavien organisaatioiden kielirajaus. 
    // Lista kielivalikoima-koodiston koodiUreja. 
    // Esim. ["kielivalikoima_en", "kielivalikoima_sv"]
    private Set<String> kieliList = new HashSet<>();

    // Palautettavien organisaatioiden kuntarajaus. 
    // Lista kunta-koodiston koodiUreja. 
    // Esim. ["kunta_905", "kunta_401"]
    private Set<String> kuntaList = new HashSet<>();
        
    // Palautettavien organisaatioiden oppilaitostyyppirajaus. 
    // Lista oppilaitostyyppi-koodiston koodiUreja. / List<String>	
    // Esim. ["oppilaitostyyppi_19", "oppilaitostyyppi_91"]
    private Set<String> oppilaitostyyppiList = new HashSet<>();

    // Palautettavien organisaatioiden y-tunnusrajaus.
    // Lista y-tunnuksia.
    // Esim. ["0147510-4", "0203797-4"]
    private Set<String> ytunnusList = new HashSet<>();
    
    // Palautettavien organisaatioiden vuosiluokat.
    // Lista vuosiluokat-koodiston koodiUreja.
    // Esim. ["vuosiluokat_1","vuosiluokat_2"]
    private Set<String> vuosiluokkaList = new HashSet<>();

    // Palautettavien organisaatioiden oidit.
    // Lista oid:ja.
    // Esim. ["1.2.246.562.10.195703655110","1.2.246.562.10.22439399159"]
    private Set<String> oidList = new HashSet<>();

    // Hakutuloksen määrän rajoite.
    private int limit;
    
    /**
     * Default no-arg constructor
     * 
     */
    public YhteystiedotSearchCriteriaDTOV2() {
        super();
    }
    
    /**
     * @return the kieliList
     */
    public Set<String> getKieliList() {
        return kieliList;
    }

    /**
     * @param kieliList the kieliList to set
     */
    public void setKieliList(Set<String> kieliList) {
        this.kieliList = kieliList;
    }

    /**
     * @return the kuntaList
     */
    public Set<String> getKuntaList() {
        return kuntaList;
    }

    /**
     * @param kuntaList the kuntaList to set
     */
    public void setKuntaList(Set<String> kuntaList) {
        this.kuntaList = kuntaList;
    }

    /**
     * @return the oppilaitostyyppiList
     */
    public Set<String> getOppilaitostyyppiList() {
        return oppilaitostyyppiList;
    }

    /**
     * @param oppilaitostyyppiList the oppilaitostyyppiList to set
     */
    public void setOppilaitostyyppiList(Set<String> oppilaitostyyppiList) {
        this.oppilaitostyyppiList = oppilaitostyyppiList;
    }

    /**
     * @return the ytunnusList
     */
    public Set<String> getYtunnusList() {
        return ytunnusList;
    }

    /**
     * @param ytunnusList the ytunnusList to set
     */
    public void setYtunnusList(Set<String> ytunnusList) {
        this.ytunnusList = ytunnusList;
    }

    /**
     * @return the vuosiluokkaList
     */
    public Set<String> getVuosiluokkaList() {
        return vuosiluokkaList;
    }

    /**
     * @param vuosiluokkaList the vuosiluokkaList to set
     */
    public void setVuosiluokkaList(Set<String> vuosiluokkaList) {
        this.vuosiluokkaList = vuosiluokkaList;
    }

    /**
     * @return the limit
     */
    public int getLimit() {
        return limit;
    }

    /**
     * @param limit the limit to set
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     * @return the oidList
     */
    public Set<String> getOidList() {
        return oidList;
    }

    /**
     * @param oidList the oidList to set
     */
    public void setOidList(Set<String> oidList) {
        this.oidList = oidList;
    }
    
}

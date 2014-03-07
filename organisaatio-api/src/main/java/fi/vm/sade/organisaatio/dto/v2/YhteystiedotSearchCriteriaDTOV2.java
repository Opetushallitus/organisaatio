/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 */

package fi.vm.sade.organisaatio.dto.v2;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author simok
 */
public class YhteystiedotSearchCriteriaDTOV2 {
    // Palautettavien organisaatioiden kielirajaus. 
    // Lista kielivalikoima-koodiston koodiUreja. 
    // Esim. ["kielivalikoima_en", "kielivalikoima_sv"]
    private List<String> kieliList = new ArrayList<String>();

    // Palautettavien organisaatioiden kuntarajaus. 
    // Lista kunta-koodiston koodiUreja. 
    // Esim. ["kunta_905", "kunta_401"]
    private List<String> kuntaList = new ArrayList<String>();
        
    // Palautettavien organisaatioiden oppilaitostyyppirajaus. 
    // Lista oppilaitostyyppi-koodiston koodiUreja. / List<String>	
    // Esim. ["oppilaitostyyppi_19", "oppilaitostyyppi_91"]
    private List<String> oppilaitostyyppiList = new ArrayList<String>();

    // Palautettavien organisaatioiden y-tunnusrajaus.
    // Lista y-tunnuksia.
    // Esim. ["0147510-4", "0203797-4"]
    private List<String> ytunnusList = new ArrayList<String>();
    
    // Palautettavien organisaatioiden vuosiluokat.
    // Lista vuosiluokat-koodiston koodiUreja.
    // Esim. ["vuosiluokat_1","vuosiluokat_2"]
    private List<String> vuosiluokkaList = new ArrayList<String>();

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
    public List<String> getKieliList() {
        return kieliList;
    }

    /**
     * @param kieliList the kieliList to set
     */
    public void setKieliList(List<String> kieliList) {
        this.kieliList = kieliList;
    }

    /**
     * @return the kuntaList
     */
    public List<String> getKuntaList() {
        return kuntaList;
    }

    /**
     * @param kuntaList the kuntaList to set
     */
    public void setKuntaList(List<String> kuntaList) {
        this.kuntaList = kuntaList;
    }

    /**
     * @return the oppilaitostyyppiList
     */
    public List<String> getOppilaitostyyppiList() {
        return oppilaitostyyppiList;
    }

    /**
     * @param oppilaitostyyppiList the oppilaitostyyppiList to set
     */
    public void setOppilaitostyyppiList(List<String> oppilaitostyyppiList) {
        this.oppilaitostyyppiList = oppilaitostyyppiList;
    }

    /**
     * @return the ytunnusList
     */
    public List<String> getYtunnusList() {
        return ytunnusList;
    }

    /**
     * @param ytunnusList the ytunnusList to set
     */
    public void setYtunnusList(List<String> ytunnusList) {
        this.ytunnusList = ytunnusList;
    }

    /**
     * @return the vuosiluokkaList
     */
    public List<String> getVuosiluokkaList() {
        return vuosiluokkaList;
    }

    /**
     * @param vuosiluokkaList the vuosiluokkaList to set
     */
    public void setVuosiluokkaList(List<String> vuosiluokkaList) {
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
    
}

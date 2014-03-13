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

import java.util.List;
import java.util.Map;

/**
 *
 * @author simok
 */
public class OrganisaatioYhteystiedotDTOV2 {
    
    private String oid;
    
    private Map<String, String> nimi;
    
    private List<String> tyypit;

    private List<String> kielet; 
    
    private String kotipaikka;
    
    // Organisaatiotunniste saadaan vaikka näin --> CONCAT(oppilaitoskoodi, ytunnus, toimipistekoodi) as organisaatio_nro,
    private String oppilaitosKoodi;
    private String ytunnus;
    private String toimipisteKoodi;

    private List<OsoiteDTOV2> postiosoite;

    private List<OsoiteDTOV2> kayntiosoite;

    
    /**
     * @return the oid
     */
    public String getOid() {
        return oid;
    }

    /**
     * @param oid the oid to set
     */
    public void setOid(String oid) {
        this.oid = oid;
    }

    /**
     * @return the nimi
     */
    public Map<String, String> getNimi() {
        return nimi;
    }

    /**
     * @param nimi the nimi to set
     */
    public void setNimi(Map<String, String> nimi) {
        this.nimi = nimi;
    }

    /**
     * @return the tyypit
     */
    public List<String> getTyypit() {
        return tyypit;
    }

    /**
     * @param tyypit the tyypit to set
     */
    public void setTyypit(List<String> tyypit) {
        this.tyypit = tyypit;
    }

    /**
     * @return the kotipaikka
     */
    public String getKotipaikka() {
        return kotipaikka;
    }

    /**
     * @param kotipaikka the kotipaikka to set
     */
    public void setKotipaikka(String kotipaikka) {
        this.kotipaikka = kotipaikka;
    }

    /**
     * @return the toimipistekoodi
     */
    public String getToimipisteKoodi() {
        return toimipisteKoodi;
    }

    /**
     * @param toimipisteKoodi the toimipistekoodi to set
     */
    public void setToimipisteKoodi(String toimipisteKoodi) {
        this.toimipisteKoodi = toimipisteKoodi;
    }

    /**
     * @return the kielet
     */
    public List<String> getKielet() {
        return kielet;
    }

    /**
     * @param kielet the kielet to set
     */
    public void setKielet(List<String> kielet) {
        this.kielet = kielet;
    }

    /**
     * @return the ytunnus
     */
    public String getYtunnus() {
        return ytunnus;
    }

    /**
     * @param ytunnus the ytunnus to set
     */
    public void setYtunnus(String ytunnus) {
        this.ytunnus = ytunnus;
    }

    /**
     * @return the oppilaitosKoodi
     */
    public String getOppilaitosKoodi() {
        return oppilaitosKoodi;
    }

    /**
     * @param oppilaitosKoodi the oppilaitosKoodi to set
     */
    public void setOppilaitosKoodi(String oppilaitosKoodi) {
        this.oppilaitosKoodi = oppilaitosKoodi;
    }

    /**
     * @return the postiosoite
     */
    public List<OsoiteDTOV2> getPostiosoite() {
        return postiosoite;
    }

    /**
     * @param postiosoite the postiosoite to set
     */
    public void setPostiosoite(List<OsoiteDTOV2> postiosoite) {
        this.postiosoite = postiosoite;
    }

    /**
     * @return the kayntiosoite
     */
    public List<OsoiteDTOV2> getKayntiosoite() {
        return kayntiosoite;
    }

    /**
     * @param kayntiosoite the kayntiosoite to set
     */
    public void setKayntiosoite(List<OsoiteDTOV2> kayntiosoite) {
        this.kayntiosoite = kayntiosoite;
    }
}

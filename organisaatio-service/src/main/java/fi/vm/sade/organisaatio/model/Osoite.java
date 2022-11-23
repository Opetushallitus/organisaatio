/*
 *
 * Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
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
 * European Union Public Licence for more details.
 */

package fi.vm.sade.organisaatio.model;


import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;

import static fi.vm.sade.generic.common.validation.ValidationConstants.GENERIC_MAX;
import static fi.vm.sade.generic.common.validation.ValidationConstants.GENERIC_MIN;

/**
 * @author Antti
 */
@Entity  // TODO XSS filtteri
public class Osoite extends Yhteystieto {

	private static final long serialVersionUID = 1L;

    public static final String TYYPPI_POSTIOSOITE = "posti";
    public static final String TYYPPI_KAYNTIOSOITE = "kaynti";
    public static final String TYYPPI_ULKOMAINEN_KAYNTIOSOITE = "ulkomainen_kaynti";
    public static final String TYYPPI_ULKOMAINEN_POSTIOSOITE = "ulkomainen_posti";
    public static final String TYYPPI_MUU = "muu";
    
    @NotNull
    @Pattern(regexp = TYYPPI_POSTIOSOITE + "|" + TYYPPI_KAYNTIOSOITE + "|" + 
            TYYPPI_ULKOMAINEN_KAYNTIOSOITE  + "|" + TYYPPI_ULKOMAINEN_POSTIOSOITE  + "|" + TYYPPI_MUU)
    private String osoiteTyyppi;
    
    @NotNull
    @Size(min = GENERIC_MIN, max = GENERIC_MAX)
    private String osoite; // TODO XSS filtteri
    
    //@NotNull
    //Postinumero can be null because of the foreign addresses
    //Postinumero is stored as koodistouri so no validation can be done
    //@Pattern(regexp = ZIPCODE_PATTERN, message = "{validation.invalid.zipcode}")
    private String postinumero; // TODO XSS filtteri

    //Postitoimipaikka can be null because of the foreign addresses
    //@NotNull
    //@Size(min = GENERIC_MIN, max = GENERIC_MAX)
    private String postitoimipaikka; // TODO XSS filtteri
    private String osavaltio;  // TODO XSS filtteri
    private String extraRivi; // TODO XSS filtteri
    private String maa; // TODO XSS filtteri

    //Should these be strings
    private Double lat;
    private Double lng;

    private String coordinateType;
    
    @Temporal(TemporalType.DATE)
    private Date ytjPaivitysPvm;

    public Osoite() {
    }

    public Osoite(String osoiteTyyppi, String osoite, String postinumero, String postitoimipaikka, String oid) {
        this.yhteystietoOid = (oid != null) ? oid : "" + System.currentTimeMillis() + Math.random();
        this.osoiteTyyppi = osoiteTyyppi;
        this.osoite = osoite;
        this.postinumero = postinumero;
        this.postitoimipaikka = postitoimipaikka;
    }
 
    public String getOsoiteTyyppi() {
        return osoiteTyyppi;
    }

    public void setOsoiteTyyppi(String osoiteTyyppi) {
        this.osoiteTyyppi = osoiteTyyppi;
    }

    public String getOsoite() {
        return osoite;
    }

    public void setOsoite(String osoite) {
        this.osoite = osoite;
    }

    public String getPostinumero() {
        return postinumero;
    }

    public void setPostinumero(String postinumero) {
        this.postinumero = postinumero;
    }

    public String getPostitoimipaikka() {
        return postitoimipaikka;
    }

    public void setPostitoimipaikka(String postitoimipaikka) {
        this.postitoimipaikka = postitoimipaikka;
    }

    @Override
    public String toString() {
        return "Osoite[tyyppi=" + osoiteTyyppi + "]";
    }

    /**
     * @return the lat
     */
    public Double getLat() {
        return lat;
    }

    /**
     * @param lat the lat to set
     */
    public void setLat(Double lat) {
        this.lat = lat;
    }

    /**
     * @return the lng
     */
    public Double getLng() {
        return lng;
    }

    /**
     * @param lng the lng to set
     */
    public void setLng(Double lng) {
        this.lng = lng;
    }

    /**
     * @return the coordinateType
     */
    public String getCoordinateType() {
        return coordinateType;
    }

    /**
     * @param coordinateType the coordinateType to set
     */
    public void setCoordinateType(String coordinateType) {
        this.coordinateType = coordinateType;
    }

    public String getOsavaltio() {
        return osavaltio;
    }

    public void setOsavaltio(String osavaltio) {
        this.osavaltio = osavaltio;
    }

    public String getExtraRivi() {
        return extraRivi;
    }

    public void setExtraRivi(String extraRivi) {
        this.extraRivi = extraRivi;
    }

    public String getMaa() {
        return maa;
    }

    public void setMaa(String maa) {
        this.maa = maa;
    }

    /**
     * @return the ytjPaivitysPvm
     */
    public Date getYtjPaivitysPvm() {
        return ytjPaivitysPvm;
    }

    /**
     * @param ytjPaivitysPvm the ytjPaivitysPvm to set
     */
    public void setYtjPaivitysPvm(Date ytjPaivitysPvm) {
        this.ytjPaivitysPvm = ytjPaivitysPvm;
    }
}

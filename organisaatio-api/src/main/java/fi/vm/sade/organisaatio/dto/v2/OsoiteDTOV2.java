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

/**
 *
 * @author simok
 */
public class OsoiteDTOV2 {
    private String kieli;
    private String osoiteTyyppi;
    private String osoite;
    private String postinumero;

    /**
     * @return the kieli
     */
    public String getKieli() {
        return kieli;
    }

    /**
     * @param kieli the kieli to set
     */
    public void setKieli(String kieli) {
        this.kieli = kieli;
    }

    /**
     * @return the osoiteTyyppi
     */
    public String getOsoiteTyyppi() {
        return osoiteTyyppi;
    }

    /**
     * @param osoiteTyyppi the osoiteTyyppi to set
     */
    public void setOsoiteTyyppi(String osoiteTyyppi) {
        this.osoiteTyyppi = osoiteTyyppi;
    }

    /**
     * @return the osoite
     */
    public String getOsoite() {
        return osoite;
    }

    /**
     * @param osoite the osoite to set
     */
    public void setOsoite(String osoite) {
        this.osoite = osoite;
    }

    /**
     * @return the postinumero
     */
    public String getPostinumero() {
        return postinumero;
    }

    /**
     * @param postinumero the postinumero to set
     */
    public void setPostinumero(String postinumero) {
        this.postinumero = postinumero;
    }
}

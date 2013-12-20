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

import static fi.vm.sade.generic.common.validation.ValidationConstants.WWW_PATTERN;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import fi.vm.sade.security.xssfilter.FilterXss;
import fi.vm.sade.security.xssfilter.XssFilterListener;

/**
 * @author Antti Salonen
 */
@Entity
@EntityListeners(XssFilterListener.class)
public class Www extends Yhteystieto {

	private static final long serialVersionUID = 1L;

    @NotNull
    @Pattern(regexp = "[-a-zA-Z0-9+&@#/%ÄäÖö?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%ÄäÖö=~_|]", message = "{validation.invalid.www}")
    @FilterXss
    private String wwwOsoite;
    
    public Www() {
        this.yhteystietoOid = "" + System.currentTimeMillis() + Math.random();
    }
    
    public Www(String oid) {
        this.yhteystietoOid = (oid != null) ? oid : "" + System.currentTimeMillis() + Math.random();
    }

    public String getWwwOsoite() {
        return wwwOsoite;
    }

    public void setWwwOsoite(String wwwOsoite) {
        this.wwwOsoite = wwwOsoite;
    }
}

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

import static fi.vm.sade.generic.common.validation.ValidationConstants.EMAIL_PATTERN;

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
public class Email extends Yhteystieto {

	private static final long serialVersionUID = 1L;
	
	@NotNull
    @Pattern(regexp = EMAIL_PATTERN, message = "{validation.invalid.email}")
    @FilterXss
    private String email;

    public Email() {
        this.yhteystietoOid = "" + System.currentTimeMillis() + Math.random();
    }

    public Email(String email, String oid) {
        this.yhteystietoOid = (oid != null) ? oid : "" + System.currentTimeMillis() + "" + Math.random();
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

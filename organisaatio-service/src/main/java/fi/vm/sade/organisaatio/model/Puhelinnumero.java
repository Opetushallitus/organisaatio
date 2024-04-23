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

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * @author Antti
 */
@Entity
public class Puhelinnumero extends Yhteystieto {

	private static final long serialVersionUID = 1L;

    public static final String TYYPPI_PUHELIN = "puhelin";
    public static final String VALIDATION_REGEXP = "^(\\+|-| |\\(|\\)|[0-9]){3,100}$";

    @NotNull
    @Pattern(regexp = VALIDATION_REGEXP, message = "{validation.invalid.phone}")
    private String puhelinnumero; // TODO XSS filtteri

    @NotNull
    @Pattern(regexp = TYYPPI_PUHELIN)
    private String tyyppi;

    public Puhelinnumero() {
    }

    public Puhelinnumero(String puhelinnumero, String tyyppi, String oid) {
        this.yhteystietoOid = (oid != null) ? oid : "" + System.currentTimeMillis() + Math.random();
        this.puhelinnumero = puhelinnumero;
        this.tyyppi = tyyppi;
    }

    public String getPuhelinnumero() {
        return puhelinnumero;
    }

    public void setPuhelinnumero(String puhelinnumero) {
        this.puhelinnumero = puhelinnumero;
    }

    public String getTyyppi() {
        return tyyppi;
    }

    public void setTyyppi(String tyyppi) {
        this.tyyppi = tyyppi;
    }
}

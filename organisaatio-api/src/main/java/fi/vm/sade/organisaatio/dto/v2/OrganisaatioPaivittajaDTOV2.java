/*
 * Copyright (c) 2014 The Finnish Board of Education - Opetushallitus
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

import java.util.Date;

/**
 *
 * @author jpel
 */
public class OrganisaatioPaivittajaDTOV2 {
    private Date paivitysPvm;
    private String paivittaja;

    /**
     * @return the paivittaja
     */
    public String getPaivittaja() {
        return paivittaja;
    }

    /**
     * @param paivittaja the paivittaja to set
     */
    public void setPaivittaja(String paivittaja) {
        this.paivittaja = paivittaja;
    }

    /**
     * @return the paivitysPvm
     */
    public Date getPaivitysPvm() {
        return paivitysPvm;
    }

    /**
     * @param paivitysPvm the paivitysPvm to set
     */
    public void setPaivitysPvm(Date paivitysPvm) {
        this.paivitysPvm = paivitysPvm;
    }
}

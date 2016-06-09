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

package fi.vm.sade.organisaatio.model.dto;

public class YTJErrorsDto {
    public boolean organisaatioValid;
    public boolean nimiValid;
    public boolean nimiSvValid;
    public boolean nimiHistory;
    public boolean osoiteValid;
    public boolean wwwValid;
    public boolean puhelinnumeroValid;
    public boolean ytunnusPvmValid;

    public YTJErrorsDto() {
        organisaatioValid = true;
        nimiValid = true;
        nimiSvValid = true;
        nimiHistory = true;
        osoiteValid = true;
        wwwValid = true;
        puhelinnumeroValid = true;
        ytunnusPvmValid = true;
    }
}

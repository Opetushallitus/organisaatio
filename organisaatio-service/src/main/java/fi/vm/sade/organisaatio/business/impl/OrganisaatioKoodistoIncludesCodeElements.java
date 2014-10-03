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

package fi.vm.sade.organisaatio.business.impl;

    /**
     * OrganisaatioKoodistoIncludesCodeElements-luokka on osa Koodi:a jonka 
     * gson serialisoi/unserialisoi REST-kutsujen JSON:sta.
     */
public class OrganisaatioKoodistoIncludesCodeElements {
    private String codeElementUri;
    private String codeElementVersion;
    private String codeElementValue;
    private boolean passive;
    
    public String getCodeElementUri() {
        return codeElementUri;
    }

    public void setCodeElementUri(String codeElementUri) {
        this.codeElementUri = codeElementUri;
    }

    public String getCodeElementVersion() {
        return codeElementVersion;
    }

    public void setCodeElementVersion(String codeElementVersion) {
        this.codeElementVersion = codeElementVersion;
    }

    public String getCodeElementValue() {
        return codeElementValue;
    }

    public void setCodeElementValue(String codeElementValue) {
        this.codeElementValue = codeElementValue;
    }

    public boolean isPassive() {
        return passive;
    }

    public void setPassive(boolean passive) {
        this.passive = passive;
    }
    
}

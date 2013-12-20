
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
package fi.vm.sade.organisaatio.ui.model;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioKuvaTyyppi;

/**
 * 
 * @author Markus
 *
 */
public class OrganisaatioKuvaModel {
    
    private String mimeType;
    private String fileName;
    private byte[] kuva;
    
    private OrganisaatioKuvaTyyppi model;
    
    public OrganisaatioKuvaModel() {
       
    }
    
    public OrganisaatioKuvaModel(OrganisaatioKuvaTyyppi model) {
        this.model = (model != null) ? model : new OrganisaatioKuvaTyyppi();
        mimeType = this.model.getMimeType();
        fileName = this.model.getFileName();
        kuva = this.model.getKuva();
    }
    
    public OrganisaatioKuvaTyyppi convertToDto() {
        model.setMimeType(mimeType);
        model.setFileName(fileName);
        model.setKuva(kuva);
        return model;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public byte[] getKuva() {
        return kuva;
    }
    public void setKuva(byte[] kuva) {
        this.kuva = kuva;
    }
}

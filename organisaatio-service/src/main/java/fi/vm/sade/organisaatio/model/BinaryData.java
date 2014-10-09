/*
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

import javax.persistence.*;

import fi.vm.sade.generic.model.BaseEntity;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;

/**
 * Store images and other binary data.
 *
 * @author mlyly
 */
@Entity
public class BinaryData extends BaseEntity {

    /**
     * Translatable name.
     */
    @ManyToOne(cascade= CascadeType.ALL)
    private MonikielinenTeksti name = new MonikielinenTeksti();
    private String filename;
    private String mimeType;

    @Lob
    @Basic(fetch=FetchType.LAZY)
    private byte[] data;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public MonikielinenTeksti getName() {
        return name;
    }

    public void setName(MonikielinenTeksti name) {
        this.name = name;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}

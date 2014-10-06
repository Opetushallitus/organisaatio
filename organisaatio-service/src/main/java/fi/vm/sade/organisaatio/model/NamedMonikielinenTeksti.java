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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import fi.vm.sade.generic.model.BaseEntity;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;

/**
 * This class represents a "keyed" translatable text with name and value - both translatable.
 *
 * Used for LOP's multitude of data... "generalInformationAboutXXX" x 100
 *
 * @author mlyly
 */
@Entity
public class NamedMonikielinenTeksti extends BaseEntity {

    /**
     * Descriptive "key" - for example "generalInformationAboutStudies"
     */
    private String key;

    /**
     * Translatable name
     */
    @ManyToOne(cascade= CascadeType.ALL)
    private MonikielinenTeksti name;

    /**
     * Translatable value
     */
    @ManyToOne(cascade= CascadeType.ALL)
    private MonikielinenTeksti value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public MonikielinenTeksti getName() {
        return name;
    }

    public void setName(MonikielinenTeksti name) {
        this.name = name;
    }

    public MonikielinenTeksti getValue() {
        return value;
    }

    public void setValue(MonikielinenTeksti value) {
        this.value = value;
    }

    /**
     * Using key to index in the hash set.
     */
    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }
}

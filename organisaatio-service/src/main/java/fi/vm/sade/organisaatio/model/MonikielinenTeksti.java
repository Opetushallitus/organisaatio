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

import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import fi.vm.sade.security.xssfilter.XssFilter;
import com.google.common.base.Objects;
import fi.vm.sade.generic.model.BaseEntity;

/**
 * Generic translatable text.
 *
 * @author jraanamo
 * @author mlyly
 */
@Entity
public class MonikielinenTeksti extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@ElementCollection(fetch= FetchType.EAGER)
    @MapKeyColumn(name="key")
    @Column(name="value", length=16384)
    @CollectionTable(joinColumns=@JoinColumn(name="id"))
    private Map<String, String> values = new HashMap<String, String>();


    @PrePersist
    @PreUpdate
    public void filterXss() {
    	for (Map.Entry<String, String> e : values.entrySet()) {
    		e.setValue(XssFilter.filter(e.getValue()));
    	}
    }

    public Map<String, String> getValues() {
        return values;
    }

    public void addString(String key, String value) {
        if (value == null) {
            getValues().remove(key);
        } else {
            getValues().put(key, value);
        }
    }

    public String getString(String key) {
        return getValues().get(key);
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof MonikielinenTeksti) {
            return Objects.equal(this.values, ((MonikielinenTeksti)o).values);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.values);
    }
}

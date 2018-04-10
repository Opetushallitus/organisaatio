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

import com.google.common.base.Objects;
import fi.vm.sade.generic.model.BaseEntity;
import fi.vm.sade.security.xssfilter.XssFilter;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

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
    @BatchSize(size = 1000)
    private Map<String, String> values = new HashMap<>();


    @PrePersist
    @PreUpdate
    public void filterXss() {
    	for (Map.Entry<String, String> e : values.entrySet()) {
    		e.setValue(XssFilter.filter(e.getValue()));
            // Allow ampersand characters
            e.setValue(e.getValue().replace("&amp;", "&"));
    	}
    }

    public Map<String, String> getValues() {
        return values;
    }

    public void setValues(Map<String, String> values) {
        this.values = values;
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

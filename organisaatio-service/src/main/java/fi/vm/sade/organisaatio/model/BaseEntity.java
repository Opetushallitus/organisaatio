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

import java.io.Serializable;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.domain.Persistable;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Base class for entities in the system.
 *
 * Support for versioning with optimistic locking.
 *
 * @author tommiha
 */
@Data
@MappedSuperclass
public class BaseEntity implements Persistable<Long>, Serializable {

    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "increment")
    @GenericGenerator(name = "increment", strategy = "increment" )
    private Long id;

    @Version
    @Column(nullable = false)
    private Long version;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof BaseEntity && id != null && id.equals(((BaseEntity) o).getId());
    }

    @Override
    public int hashCode() {
        return id == null ? super.hashCode() : id.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());
        sb.append("[id=");
        sb.append(getId());
        sb.append(", version=");
        sb.append(getVersion());
        sb.append("]");

        return sb.toString();
    }

    @Override
    public boolean isNew() {
        return null == this.getId();
    }
}

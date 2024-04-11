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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @author tommiha
 */
@MappedSuperclass
@XmlRootElement(name = "BaseEntity")
@XmlAccessorType(XmlAccessType.FIELD)
public class OrganisaatioBaseEntity extends BaseEntity {
    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioBaseEntity.class);

    @PostUpdate
    public void onPostUpdate() {
        try {
            LOG.debug("@PostUpdate - onPostUpdate(): {}", this);
        } catch (Throwable ex) {
            LOG.error("onPostUpdate() - logging failed.", ex);
        }
    }

    @PostPersist
    public void onPostPersist() {
        try {
            LOG.debug("@PostPersist - onPostPersist(): {}", this);
        } catch (Throwable ex) {
            LOG.error("onPostPersist() - logging failed.", ex);
        }
    }

    @PostRemove
    public void onPostRemove() {
        try {
            LOG.debug("@PostRemove - onPostRemove(): {}", this);
        } catch (Throwable ex) {
            LOG.error("onPostRemove() - logging failed.", ex);
        }
    }

}

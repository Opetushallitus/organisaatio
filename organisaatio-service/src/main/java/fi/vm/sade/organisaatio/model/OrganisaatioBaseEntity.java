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

import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import fi.vm.sade.generic.model.BaseEntity;
//import fi.vm.sade.log.client.LoggerHelper;
//import fi.vm.sade.log.model.SimpleBeanSerializer;
//import fi.vm.sade.log.model.Tapahtuma;
import java.util.Map;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author tommiha
 */
@MappedSuperclass
@XmlRootElement(name = "BaseEntity")
@XmlAccessorType(XmlAccessType.FIELD)
public class OrganisaatioBaseEntity extends BaseEntity {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioBaseEntity.class);

    /**
     * This method is used to find the correct DTO class for given entity.
     *
     * For example: fi.vm.sade.organisaatio.model.Organisaatio has converter fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO.
     *
     * @return
     */
    public Class getDTOClass() {
        try {
            return Class.forName(this.getClass().getName().replace("model", "api.model.types") + "DTO");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private String getTekija() {
        if (SecurityContextHolder.getContext() != null
                && SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().getPrincipal() != null) {
            return SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        } else {
            return null;
        }
    }
    private transient Map<String, String> _onLoadValues;

//    @PostLoad
//    public void onPostLoad() {
//        try {
//            LOG.debug("@PostLoad - onPostLoad(): {}", this);
//
//            if (LoggerHelper.isRecording()) {
//                // Don't need the tapahtuma, but lets initialize internal state here
//                createUpdateTapahtuma();
//            }
//        } catch (Throwable ex) {
//            LOG.error("onPostLoad() - logging failed.", ex);
//        }
//    }

    @PostUpdate
    public void onPostUpdate() {
        try {
            LOG.debug("@PostUpdate - onPostUpdate(): {}", this);

//            if (LoggerHelper.isRecording()) {
//                Tapahtuma t = createUpdateTapahtuma();
//                LoggerHelper.record(t);
//            }
        } catch (Throwable ex) {
            LOG.error("onPostUpdate() - logging failed.", ex);
        }
    }

    @PostPersist
    public void onPostPersist() {
        try {
            LOG.debug("@PostPersist - onPostPersist(): {}", this);

//            if (LoggerHelper.isRecording()) {
//                Tapahtuma t = createUpdateTapahtuma();
//                t.setType("CREATE");
//                LoggerHelper.record(t);
//            }
        } catch (Throwable ex) {
            LOG.error("onPostPersist() - logging failed.", ex);
        }
    }

    @PostRemove
    public void onPostRemove() {
        try {
            LOG.debug("@PostRemove - onPostRemove(): {}", this);
//            if (LoggerHelper.isRecording()) {
//                Tapahtuma t = createUpdateTapahtuma();
//                t.setType("DELETE");
//                t.addValue("DELETED", "true");
//                LoggerHelper.record(t);
//            }
        } catch (Throwable ex) {
            LOG.error("onPostRemove() - logging failed.", ex);
        }
    }

    /**
     * Helper to create Tapahtuma entry AND update the internal state of the object for next call so that diff can be acquired.
     *
     * @return
     */
//    private Tapahtuma createUpdateTapahtuma() {
//        Map<String, String> values = SimpleBeanSerializer.getBeanAsMap(this);
//        Tapahtuma t = Tapahtuma.createUPDATEMaps("tarjonta-service", getTekija(), this.getClass().getSimpleName(), "" + getId(), _onLoadValues, values);
//
////         Update current state (for possible future loggings)
//        _onLoadValues = values;
//
//        return t;
//    }
}

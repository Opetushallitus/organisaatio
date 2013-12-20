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
package fi.vm.sade.organisaatio.revised.ui.event;

import java.io.Serializable;

import com.github.wolfie.blackboard.Event;
import com.github.wolfie.blackboard.Listener;
import com.github.wolfie.blackboard.annotation.ListenerMethod;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;

/**
 * Events fired by Peruuta and Jatka buttons in organisaatio edit form.
 * 
 * @author markus
 *
 */
public class OrganisaatioFormButtonEvent implements Event {

    public static final String PERUUTA = "peruuta";
    public static final String JATKA = "jatka";
    
    private String eventType;
    private OrganisaatioDTO organisaatio;
    
    public OrganisaatioFormButtonEvent(OrganisaatioDTO organisaatio, String eventType) {
        this.eventType = eventType;
        this.organisaatio = organisaatio;
    }
    
    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public OrganisaatioDTO getOrganisaatio() {
        return organisaatio;
    }

    public void setOrganisaatio(OrganisaatioDTO organisaatio) {
        this.organisaatio = organisaatio;
    }
    
    public interface OrganisaatioFormButtonEventListener extends Listener, Serializable {

        @ListenerMethod
        void onOrganisaatioFormButtonEvent(OrganisaatioFormButtonEvent event);

    }
}

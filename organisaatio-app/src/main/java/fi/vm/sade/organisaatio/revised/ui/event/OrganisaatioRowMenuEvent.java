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
import java.util.ArrayList;
import java.util.List;

import com.github.wolfie.blackboard.Event;
import com.github.wolfie.blackboard.Listener;
import com.github.wolfie.blackboard.annotation.ListenerMethod;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioPerustietoType;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;

/**
 * Events fired by menu of SearchResultRow in OrganisaatioListView.
 * @author markus
 *
 */
public class OrganisaatioRowMenuEvent implements Event {
    
    private OrganisaatioDTO organisaatio;
    private String eventType;
    private List<OrganisaatioPerustieto> organisaatios = new ArrayList<OrganisaatioPerustieto>();

    public OrganisaatioRowMenuEvent(OrganisaatioDTO organisaatio, String eventType, List<OrganisaatioPerustieto> organisaatios) {
        this.organisaatio = organisaatio;
        this.eventType = eventType;
        this.organisaatios = organisaatios;
    }

    public OrganisaatioDTO getOrganisaatio() {
        return organisaatio;
    }

    public void setOrganisaatio(OrganisaatioDTO organisaatio) {
        this.organisaatio = organisaatio;
    }
    
    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public List<OrganisaatioPerustieto> getOrganisaatios() {
        return organisaatios;
    }

    public void setOrganisaatios(List<OrganisaatioPerustieto> organisaatios) {
        this.organisaatios = organisaatios;
    }

    public interface OrganisaatioRowMenuEventListener extends Listener, Serializable {

        @ListenerMethod
        void onOrganisaatioRowMenuEvent(OrganisaatioRowMenuEvent event);

    }

}

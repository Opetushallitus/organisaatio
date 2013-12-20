package fi.vm.sade.organisaatio.revised.ui.event;

import java.io.Serializable;

import com.github.wolfie.blackboard.Event;
import com.github.wolfie.blackboard.Listener;
import com.github.wolfie.blackboard.annotation.ListenerMethod;

import fi.vm.sade.organisaatio.api.search.OrganisaatioSearchCriteria;

public class OrganisaatioSearchStartEvent implements Event {

    private OrganisaatioSearchCriteria searchCriteria;
    
    public OrganisaatioSearchCriteria getSearchCriteria() {
        return searchCriteria;
    }

    public void setSearchCriteria(OrganisaatioSearchCriteria searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

    public OrganisaatioSearchStartEvent(OrganisaatioSearchCriteria searchCriteria) {
        this.searchCriteria = searchCriteria;
    }
    
    public interface OrganisaatioSearchStartEventListener extends Listener, Serializable {

        @ListenerMethod
        void onOrganisaatioSearchStart(OrganisaatioSearchStartEvent event);

    }
    
    
}

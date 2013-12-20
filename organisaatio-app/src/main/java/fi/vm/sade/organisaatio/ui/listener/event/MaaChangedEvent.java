/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.vm.sade.organisaatio.ui.listener.event;

import com.github.wolfie.blackboard.Event;

/**
 *
 * @author Tuomas Katva
 */
public class MaaChangedEvent implements Event {
    
    private String selectedMaa;
    
    public MaaChangedEvent(String maaParam) {
        selectedMaa = maaParam;
    }
    
    public String getMaa() {
        return selectedMaa;
    }
}

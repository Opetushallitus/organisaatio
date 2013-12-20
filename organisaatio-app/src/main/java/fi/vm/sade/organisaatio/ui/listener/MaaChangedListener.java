/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.vm.sade.organisaatio.ui.listener;


import com.github.wolfie.blackboard.annotation.ListenerMethod;
import fi.vm.sade.organisaatio.ui.listener.event.MaaChangedEvent;
/**
 *
 * @author Tuomas
 */
public interface MaaChangedListener extends com.github.wolfie.blackboard.Listener {
 
    @ListenerMethod
    void maaChanged(MaaChangedEvent maaChangedEvent);
}

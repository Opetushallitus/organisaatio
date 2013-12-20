/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.vm.sade.organisaatio.ui.listener.event;

import fi.vm.sade.rajapinnat.ytj.api.YTJDTO;

/**
 *
 * @author Tuomas Katva
 */
public class YtjSelectedEventImpl {
     
    private final YTJDTO ytj;
    private final boolean cancelled;
    private String oldOrgOid;
    
    public YtjSelectedEventImpl(final YTJDTO ytjParam, boolean cancel) {
        ytj = ytjParam;
        cancelled = cancel;
    }

    public YtjSelectedEventImpl(final YTJDTO ytjParam, boolean cancel, String oldOid) {
        ytj = ytjParam;
        cancelled = cancel;
        this.oldOrgOid = oldOid;
    }
    
    public YTJDTO getYtjDto() {
        return ytj;
    }
    
    public boolean isCancelled() {
        return cancelled;
    }

    public String getOldOrgOid() {
        return oldOrgOid;
    }
}

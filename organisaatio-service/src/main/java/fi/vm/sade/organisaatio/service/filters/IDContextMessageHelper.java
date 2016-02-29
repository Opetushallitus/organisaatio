package fi.vm.sade.organisaatio.service.filters;

import org.apache.cxf.phase.PhaseInterceptorChain;

// Static class to provide ID chain in current message context or constant "caller-id" (clientsubsystemcode).
public final class IDContextMessageHelper {
    // Provides the "caller-id" (clientSubSystemCode) to be provided on header when sending messages.
    private static final String localClientSubSystemCode = "organisaatio.organisaatio-service.backend";

    // Private constructor to prevent instantiating this class.
    private IDContextMessageHelper() { }

    static public String getClientSubSystemCode() {
        return localClientSubSystemCode;
    }

    static public String getIDChain() {
        // Get the ID from cxf message exchange
        if(PhaseInterceptorChain.getCurrentMessage() != null) {
            return (String)PhaseInterceptorChain.getCurrentMessage().getExchange().get("ID");
        }
        // Default ID
        return null;
    }

    // Save the received callerid to cxf message exchange.
    // NOTE: Expects other service to return the ID.
    static public void setReceivedIDChain(String callerid) {
        // Use default callerid
        if(callerid == null || callerid.isEmpty()) {
            PhaseInterceptorChain.getCurrentMessage().getExchange().put("ID", getIDChain());
        }
        // Put new or replace the old callerid.
        else {
            PhaseInterceptorChain.getCurrentMessage().getExchange().put("ID", callerid);
        }
    }
}

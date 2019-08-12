package fi.vm.sade.organisaatio.service.filters;

import org.apache.cxf.phase.PhaseInterceptorChain;

// Static class to provide ID chain in current message context or constant "caller-id".
public final class IDContextMessageHelper {
    public final static String CSRF_HEADER_NAME = "CSRF";

    // Provides the "caller-id" to be provided on header when sending messages.
    private static final String CALLER_ID = "1.2.246.562.10.00000000001.organisaatio-service";

    // Private constructor to prevent instantiating this class.
    private IDContextMessageHelper() { }

    static public String getCallerId() {
        return CALLER_ID;
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

    static public String getCsrfHeader() {
        if (PhaseInterceptorChain.getCurrentMessage() == null ||
                PhaseInterceptorChain.getCurrentMessage().getExchange().get(CSRF_HEADER_NAME) == null) {
            return null;
        }

        return (String) PhaseInterceptorChain.getCurrentMessage().getExchange().get(CSRF_HEADER_NAME);
    }

    static public void setCsrfHeader(String csrfHeader) {
        if (csrfHeader != null && !csrfHeader.isEmpty()) {
            PhaseInterceptorChain.getCurrentMessage().getExchange().put(CSRF_HEADER_NAME, csrfHeader);
        }
    }
}

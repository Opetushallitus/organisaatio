package fi.vm.sade.organisaatio.service.filters;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;


// Saves the ID chain from incoming REST messages for outputfilter. Loadbalancer adds this ID to messages automatically.
public class InputFilter implements RequestHandler {
    private static final Logger LOG = LoggerFactory.getLogger(InputFilter.class);
    static int runner = 0;

    @Override
    public Response handleRequest(Message inMessage, ClassResourceInfo classResourceInfo) {
        // Try to find IDCain from message header.
        String IDChain = null;
        HttpServletRequest request = null;
        SecurityContextHolderAwareRequestWrapper wrapper = (SecurityContextHolderAwareRequestWrapper)inMessage.get("HTTP.REQUEST");
        if(wrapper != null) {
            request = (HttpServletRequest)wrapper.getRequest();
        }
        if(request != null) {
            IDChain = request.getHeader("ID");
        }
        // Save the ID chain to cxf exchange.
        if(IDChain != null) {
            inMessage.getExchange().put("ID", IDChain);
        }
        // Continue the process.
        return null;
    }
}

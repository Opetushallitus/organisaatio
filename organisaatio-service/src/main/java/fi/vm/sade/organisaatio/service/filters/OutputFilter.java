package fi.vm.sade.organisaatio.service.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.apache.cxf.phase.PhaseInterceptorChain;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Pass on the received ID Chain intact. Load balancer will update this ID chain automatically. Add the same "Caller-Id"
// (clientSubSystemCode) to every sent REST message.
@Component
@Provider
public class OutputFilter implements ContainerResponseFilter {
    private static final Logger LOG = LoggerFactory.getLogger(OutputFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        String IDChain = (String)PhaseInterceptorChain.getCurrentMessage().getExchange().get("ID");
        String csrfCookie = (String) PhaseInterceptorChain.getCurrentMessage().getExchange().get("CSRF");

        // Add callerid and ID chain headers to the output message.
        if(responseContext != null && responseContext.getHeaders() != null) {
            MultivaluedMap<String, Object> responseHeaders = responseContext.getHeaders();
            responseHeaders.add("ID", IDChain);
            responseHeaders.add("clientSubSystemCode", IDContextMessageHelper.getClientSubSystemCode());
            responseHeaders.add("CSRF", csrfCookie);
        }
    }
}

package fi.vm.sade.organisaatio.service.filters;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.ext.Provider;
import org.apache.cxf.phase.PhaseInterceptorChain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static fi.vm.sade.organisaatio.service.filters.IDContextMessageHelper.CSRF_HEADER_NAME;

@Component
@Provider
@PreMatching
// Saves the ID chain from incoming REST messages for outputfilter. Loadbalancer adds this ID to messages automatically.
public class InputFilter implements ContainerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(InputFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Try to find IDCain from message header.
        String IDChain = requestContext.getHeaderString("ID");
        // Save the ID chain to cxf exchange.
        if(IDChain != null) {
            PhaseInterceptorChain.getCurrentMessage().getExchange().put("ID", IDChain);
        }

        Cookie csrfCookie = requestContext.getCookies().get(CSRF_HEADER_NAME);
        if (csrfCookie != null) {
            PhaseInterceptorChain.getCurrentMessage().getExchange().put(CSRF_HEADER_NAME, csrfCookie.getValue());
        }
    }
}

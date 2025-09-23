package fi.vm.sade;

import jakarta.servlet.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.io.IOException;

@Slf4j
public class RequestIdFilter implements Filter {
    public static final String REQUEST_ID_ATTRIBUTE = RequestIdFilter.class.getName() + ".requestId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            var requestId = generateRequestId();
            MDC.put(REQUEST_ID_ATTRIBUTE, requestId);
            request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
            chain.doFilter(request, response);
        } finally {
            MDC.remove(REQUEST_ID_ATTRIBUTE);
        }
    }

    public static String generateRequestId() {
        return java.util.UUID.randomUUID().toString();
    }
}

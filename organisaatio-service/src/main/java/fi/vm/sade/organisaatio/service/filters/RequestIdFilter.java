package fi.vm.sade.organisaatio.service.filters;


import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RequestIdFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            MDC.put("requestId", generateRequestId());
            chain.doFilter(request, response);
        } finally {
            MDC.remove("requestId");
        }
    }

    public static String generateRequestId() {
        return java.util.UUID.randomUUID().toString();
    }
}

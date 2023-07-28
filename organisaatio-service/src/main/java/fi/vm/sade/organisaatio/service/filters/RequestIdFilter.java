package fi.vm.sade.organisaatio.service.filters;


import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RequestIdFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            MDC.put("requestId", getRequestIdFromRequest(request).orElseGet(RequestIdFilter::generateRequestId));
            chain.doFilter(request, response);
        } finally {
            MDC.remove("requestId");
        }
    }

    Optional<String> getRequestIdFromRequest(ServletRequest request) {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            Optional<String> value = Optional.ofNullable(httpRequest.getHeader("X-Request-Id"));
            value.ifPresent(v -> log.info("Found requestId from request: {}", v));
            return value;
        }
        return Optional.empty();
    }

    public static String generateRequestId() {
        return java.util.UUID.randomUUID().toString();
    }
}

package fi.vm.sade.organisaatio.service.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CacheFilter implements Filter {
    private static final boolean IS_PUBLIC = true;
    private static final int MAX_AGE = 60 * 60 * 24;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        ((HttpServletResponse) servletResponse).setHeader("cache-control", getCacheHeaderValue());
        filterChain.doFilter(servletRequest, servletResponse);
    }

    String getCacheHeaderValue() {
        return String.format("%s, max-age=%s", IS_PUBLIC ? "public" : "private", MAX_AGE);
    }
}

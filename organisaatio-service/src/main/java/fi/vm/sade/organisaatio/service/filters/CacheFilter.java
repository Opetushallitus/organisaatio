package fi.vm.sade.organisaatio.service.filters;

import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CacheFilter implements Filter {
    private static final boolean IS_PUBLIC = true;
    private static final int MAX_AGE = 60 * 60 * 24;


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        ((HttpServletResponse) servletResponse).setHeader("cache-control",
                String.format("%s, max-age=%s", IS_PUBLIC ? "public" : "private", MAX_AGE));
        filterChain.doFilter(servletRequest, servletResponse);
    }
}

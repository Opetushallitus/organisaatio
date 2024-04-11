package fi.vm.sade.organisaatio.service.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

public class CsrfHeaderFilter implements Filter {
    private static final String CSRF_HEADER_NAME = "CSRF";
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String csrfHeader = httpServletRequest.getHeader(CSRF_HEADER_NAME);
        Optional<String> crsfCookie = getCsrfCookie(httpServletRequest.getCookies());
        if (csrfHeader == null && crsfCookie.isPresent()) {
            CsrfServletRequestWrapper csrfServletRequestWrapper = new CsrfServletRequestWrapper(httpServletRequest);
            csrfServletRequestWrapper.addHeader(CSRF_HEADER_NAME, crsfCookie.get());
            filterChain.doFilter(csrfServletRequestWrapper, response);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private Optional<String> getCsrfCookie(Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(CSRF_HEADER_NAME)) {
                    return Optional.ofNullable(cookie.getValue());
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void destroy() {

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }
}

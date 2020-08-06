package fi.vm.sade.organisaatio.service.filters;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

import static fi.vm.sade.organisaatio.service.filters.IDContextMessageHelper.CSRF_HEADER_NAME;

public class CsrfHeaderFilter implements Filter {

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

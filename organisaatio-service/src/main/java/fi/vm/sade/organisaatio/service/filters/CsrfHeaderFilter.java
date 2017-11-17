package fi.vm.sade.organisaatio.service.filters;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;

import static fi.vm.sade.organisaatio.service.filters.IDContextMessageHelper.CSRF_HEADER_NAME;

public class CsrfHeaderFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        CsrfServletRequestWrapper csrfServletRequestWrapper = new CsrfServletRequestWrapper(httpServletRequest);

        Enumeration<String> csrfHeader = httpServletRequest.getHeaders(CSRF_HEADER_NAME);
        if (csrfHeader.hasMoreElements() || httpServletRequest.getCookies() == null) {
            filterChain.doFilter(request, response);
            return;
        }

        for (Cookie cookie : httpServletRequest.getCookies()) {
            if (cookie.getName().equals(CSRF_HEADER_NAME)) {
                csrfServletRequestWrapper.addHeader(CSRF_HEADER_NAME, cookie.getValue());
                filterChain.doFilter(csrfServletRequestWrapper, response);
                return;
            }
        }

        filterChain.doFilter(request, response);
   }

    @Override
    public void destroy() {

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }
}

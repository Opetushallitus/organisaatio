package fi.vm.sade.organisaatio.service.filters;

import fi.vm.sade.javautils.kayttooikeusclient.OphUserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Optional;

@Component
@Slf4j
public class RequestCallerFilter extends GenericFilterBean {
    public static final String CALLER_HENKILO_OID_ATTRIBUTE = RequestCallerFilter.class.getName() + ".callerHenkiloOid";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            getUserDetails(servletRequest).ifPresent(userDetails -> {
                var oid = userDetails.getUsername();
                MDC.put(CALLER_HENKILO_OID_ATTRIBUTE, oid);
                servletRequest.setAttribute(CALLER_HENKILO_OID_ATTRIBUTE, oid);
            });
            getJwtToken(servletRequest).ifPresent(jwt -> {
                var oid = jwt.getName();
                MDC.put(CALLER_HENKILO_OID_ATTRIBUTE, oid);
                servletRequest.setAttribute(CALLER_HENKILO_OID_ATTRIBUTE, oid);
            });
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            MDC.remove(CALLER_HENKILO_OID_ATTRIBUTE);
        }
    }

    private Optional<OphUserDetailsServiceImpl.UserDetailsImpl> getUserDetails(ServletRequest servletRequest) {
        if (servletRequest instanceof HttpServletRequest request) {
            var principal = request.getUserPrincipal();
            if (principal instanceof CasAuthenticationToken token) {
                var userDetails = token.getUserDetails();
                if (userDetails instanceof OphUserDetailsServiceImpl.UserDetailsImpl casUserDetails) {
                    return Optional.of(casUserDetails);
                }
            }
        }
        return Optional.empty();
    }

    private Optional<JwtAuthenticationToken> getJwtToken(ServletRequest servletRequest) {
        if (servletRequest instanceof HttpServletRequest request) {
            var principal = request.getUserPrincipal();
            if (principal instanceof JwtAuthenticationToken token) {
                return Optional.of(token);
            }
        }
        return Optional.empty();
    }
}
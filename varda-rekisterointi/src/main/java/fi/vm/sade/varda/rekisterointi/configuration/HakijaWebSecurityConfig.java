package fi.vm.sade.varda.rekisterointi.configuration;

import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.util.Constants;
import fi.vm.sade.varda.rekisterointi.util.ServletUtils;

import org.apereo.cas.client.validation.Cas30ServiceTicketValidator;
import org.apereo.cas.client.validation.TicketValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.*;

import static fi.vm.sade.varda.rekisterointi.util.ServletUtils.findSessionAttribute;
import static java.util.Collections.singletonList;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class HakijaWebSecurityConfig {
    private static final String HAKIJA_ROLE = "APP_VARDAREKISTEROINTI_HAKIJA";
    private static final String HAKIJA_PATH_CLOB = "/hakija/**";

    private final OphProperties ophProperties;

    public HakijaWebSecurityConfig(OphProperties ophProperties) {
        this.ophProperties = ophProperties;
    }

    @Bean
    @Order(2)
    SecurityFilterChain hakijSecurityFilterChain(HttpSecurity http, SecurityContextRepository securityContextRepository) throws Exception {
        return http
                .headers(headers -> headers.disable())
                .csrf(csrf -> csrf.disable())
                .securityMatcher(HAKIJA_PATH_CLOB)
                .authorizeHttpRequests(authz -> authz.anyRequest().hasRole(HAKIJA_ROLE))
                .addFilterAt(authenticationFilter(securityContextRepository), CasAuthenticationFilter.class)
                .addFilterBefore(new SaveLoginRedirectFilter(), CasAuthenticationFilter.class)
                .addFilterAfter(new ValtuudetRedirectFilter(), CasAuthenticationFilter.class)
                .securityContext(securityContext -> securityContext
                        .requireExplicitSave(true)
                        .securityContextRepository(new HttpSessionSecurityContextRepository()))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint()))
                .build();
    }

    private TicketValidator ticketValidator() {
        return new Cas30ServiceTicketValidator(ophProperties.url("varda-rekisterointi.cas.oppija.url"));
    }

    private ServiceProperties serviceProperties() {
        ServiceProperties properties = new ServiceProperties();
        properties.setService(ophProperties.url("varda-rekisterointi.hakija.login") + "/j_spring_cas_security_check");
        properties.setSendRenew(false);
        properties.setAuthenticateAllArtifacts(true);
        return properties;
    }

    private AuthenticationEntryPoint authenticationEntryPoint() {
        CasAuthenticationEntryPoint authenticationEntryPoint = new CasAuthenticationEntryPoint();
        authenticationEntryPoint.setLoginUrl(ophProperties.url("varda-rekisterointi.cas.oppija.url"));
        authenticationEntryPoint.setServiceProperties(serviceProperties());
        return authenticationEntryPoint;
    }

    private CasAuthenticationFilter authenticationFilter(SecurityContextRepository securityContextRepository) throws Exception {
        CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
        casAuthenticationFilter.setAuthenticationManager(new ProviderManager(authenticationProvider()));
        casAuthenticationFilter.setServiceProperties(serviceProperties());
        casAuthenticationFilter.setFilterProcessesUrl("/j_spring_cas_security_check");
        casAuthenticationFilter.setAuthenticationSuccessHandler(
            new SimpleUrlAuthenticationSuccessHandler(ophProperties.url("varda-rekisterointi.hakija.valtuudet.redirect")));
        casAuthenticationFilter.setSecurityContextRepository(securityContextRepository);
        return casAuthenticationFilter;
    }

    private AuthenticationProvider authenticationProvider() {
        CasAuthenticationProvider authenticationProvider = new CasAuthenticationProvider();
        authenticationProvider.setAuthenticationUserDetailsService(new CasUserDetailsService());
        authenticationProvider.setServiceProperties(serviceProperties());
        authenticationProvider.setTicketValidator(ticketValidator());
        authenticationProvider.setKey("varda-rekisterointi");
        return authenticationProvider;
    }

    private static class SaveLoginRedirectFilter extends GenericFilterBean {
        @Override
        public void doFilter(
                ServletRequest request,
                ServletResponse response,
                FilterChain chain) throws IOException, ServletException {
            var httpRequest = (HttpServletRequest) request;
            var url = httpRequest.getRequestURL().toString();
            if (httpRequest.getQueryString() != null && httpRequest.getQueryString().contains("login=true")) {
                ServletUtils.setSessionAttribute(httpRequest, Constants.SESSION_ATTRIBUTE_NAME_ORIGINAL_REQUEST, url);
            }
            chain.doFilter(request, response);
        }
    }

    private static class ValtuudetRedirectFilter extends GenericFilterBean {
        @Override
        public void doFilter(
                ServletRequest request,
                ServletResponse response,
                FilterChain chain) throws IOException, ServletException {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            boolean hasBusinessId = findSessionAttribute(httpRequest, Constants.SESSION_ATTRIBUTE_NAME_BUSINESS_ID, String.class)
                .isPresent();
            boolean hasOrgName = findSessionAttribute(httpRequest, Constants.SESSION_ATTRIBUTE_NAME_ORGANISATION_NAME,
                String.class).isPresent();
            if (!httpRequest.getRequestURI().contains("/valtuudet/") && (!hasBusinessId || !hasOrgName)) {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                String encodedRedirectURL = httpResponse.encodeRedirectURL(
                    httpRequest.getContextPath() + "/hakija/valtuudet/redirect");
                httpResponse.setStatus(HttpStatus.TEMPORARY_REDIRECT.value());
                httpResponse.setHeader("Location", encodedRedirectURL);
            }
            chain.doFilter(request, response);
        }
    }

    private class CasUserDetailsService implements AuthenticationUserDetailsService<CasAssertionAuthenticationToken> {
        @Override
        public UserDetails loadUserDetails(CasAssertionAuthenticationToken token) throws UsernameNotFoundException {
            String[] principal = ((String) token.getPrincipal()).split(",");
            List<SimpleGrantedAuthority> authorities = singletonList(
                new SimpleGrantedAuthority(String.format("ROLE_%s", HAKIJA_ROLE)));
            return new User(principal[1], "", true, true, true, true, authorities);
        }
    }
}

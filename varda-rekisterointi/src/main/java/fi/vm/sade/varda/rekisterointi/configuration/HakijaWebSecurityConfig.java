package fi.vm.sade.varda.rekisterointi.configuration;

import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.NameContainer;

import org.apereo.cas.client.validation.Cas20ServiceTicketValidator;
import org.apereo.cas.client.validation.TicketValidationException;
import org.apereo.cas.client.validation.TicketValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.preauth.*;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.*;

import static fi.vm.sade.varda.rekisterointi.configuration.LocaleConfiguration.DEFAULT_LOCALE;
import static fi.vm.sade.varda.rekisterointi.configuration.LocaleConfiguration.SESSION_ATTRIBUTE_NAME_LOCALE;
import static fi.vm.sade.varda.rekisterointi.util.ServletUtils.findSessionAttribute;
import static java.util.Collections.singletonList;

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
    SecurityFilterChain hakijSecurityFilterChain(HttpSecurity http) throws Exception {
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setMatchingRequestParameterName(null);
        return http
                .headers(headers -> headers.disable())
                .csrf(csrf -> csrf.disable())
                .securityMatcher(HAKIJA_PATH_CLOB)
                .authorizeHttpRequests(authz -> authz.anyRequest().hasRole(HAKIJA_ROLE))
                .addFilterBefore(hakijaAuthenticationProcessingFilter(), BasicAuthenticationFilter.class)
                .securityContext(securityContext -> securityContext
                        .requireExplicitSave(true)
                        .securityContextRepository(new HttpSessionSecurityContextRepository()))
                .requestCache(cache -> cache.requestCache(requestCache))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(hakijaAuthenticationEntryPoint()))
                .build();
    }

    TicketValidator casOppijaticketValidator() {
        return new Cas20ServiceTicketValidator(ophProperties.url("varda-rekisterointi.cas.oppija.url"));
    }

    Filter hakijaAuthenticationProcessingFilter() throws Exception {
        HakijaAuthenticationFilter filter = new HakijaAuthenticationFilter("/hakija/login", casOppijaticketValidator(), ophProperties);
        filter.setAuthenticationManager(new ProviderManager(hakijaAuthenticationProvider()));
        String authenticationSuccessUrl = ophProperties.url("varda-rekisterointi.hakija.valtuudet.redirect");
        filter.setAuthenticationSuccessHandler(new SimpleUrlAuthenticationSuccessHandler(authenticationSuccessUrl));
        return filter;
    }

    AuthenticationEntryPoint hakijaAuthenticationEntryPoint() {
        String loginCallbackUrl = ophProperties.url("varda-rekisterointi.hakija.login");
        String defaultLoginUrl = ophProperties.url("varda-rekisterointi.cas.oppija.login", loginCallbackUrl);
        return new AuthenticationEntryPointImpl(defaultLoginUrl, ophProperties, loginCallbackUrl);
    }

    AuthenticationProvider hakijaAuthenticationProvider() {
        PreAuthenticatedAuthenticationProvider authenticationProvider = new PreAuthenticatedAuthenticationProvider();
        authenticationProvider.setPreAuthenticatedUserDetailsService(new PreAuthenticatedGrantedAuthoritiesUserDetailsService());
        return authenticationProvider;
    }

    private static class AuthenticationEntryPointImpl extends LoginUrlAuthenticationEntryPoint {
        private final OphProperties properties;
        private final String loginCallbackUrl;

        public AuthenticationEntryPointImpl(String loginFormUrl, OphProperties properties, String loginCallbackUrl) {
            super(loginFormUrl);
            this.properties = properties;
            this.loginCallbackUrl = loginCallbackUrl;
        }

        @Override
        protected String determineUrlToUseForThisRequest(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) {
            Locale locale = findSessionAttribute(request, SESSION_ATTRIBUTE_NAME_LOCALE, Locale.class)
                    .orElse(DEFAULT_LOCALE);
            String language = locale.getLanguage();
            return properties.url("varda-rekisterointi.cas.oppija.login", loginCallbackUrl, language);
        }
    }

    private static class HakijaAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

        private final TicketValidator oppijaticketValidator;
        private final OphProperties properties;


        public HakijaAuthenticationFilter(String defaultFilterProcessesUrl, TicketValidator oppijaticketValidator, OphProperties properties) {
            super(defaultFilterProcessesUrl);
            this.properties = properties;
            this.oppijaticketValidator = oppijaticketValidator;
        }

        @Override
        public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
            try {
                return getAuthenticationManager().authenticate(createAuthRequest(request, validateTicket(resolveTicket(request))));
            } catch (TicketValidationException e) {
                throw new AuthenticationCredentialsNotFoundException("Unable to authenticate because required param doesn't exist");
            }
        }
        private PreAuthenticatedAuthenticationToken createAuthRequest(HttpServletRequest request, Map<String, Object> casPrincipalAttributes) {
            String nationalIdentificationNumber = Optional.ofNullable((String) casPrincipalAttributes.get("nationalIdentificationNumber"))
                    .orElseThrow(() -> new PreAuthenticatedCredentialsNotFoundException("Unable to authenticate because required param doesn't exist"));
            String surname = Optional.ofNullable((String) casPrincipalAttributes.get("sn"))
                    .orElse("");
            String firstName = Optional.ofNullable((String) casPrincipalAttributes.get("firstName"))
                    .orElse("");

            PreAuthenticatedAuthenticationToken authRequest = new PreAuthenticatedAuthenticationToken(nationalIdentificationNumber, "N/A");
            List<? extends GrantedAuthority> authorities = singletonList(new SimpleGrantedAuthority(String.format("ROLE_%s", HAKIJA_ROLE)));
            authRequest.setDetails(new CasOppijaAuthenticationDetails(request, authorities, firstName, surname));
            return authRequest;
        }

        private String resolveTicket(HttpServletRequest request) {
            return Optional.ofNullable(request.getParameter("ticket"))
                    .orElseThrow(() -> new PreAuthenticatedCredentialsNotFoundException("Unable to authenticate because required param doesn't exist"));
        }
        private Map<String, Object> validateTicket(String ticket) throws TicketValidationException {
            return oppijaticketValidator.validate(ticket, properties.url("varda-rekisterointi.hakija.login")).getPrincipal().getAttributes();
        }

    }

    private static class CasOppijaAuthenticationDetails extends PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails implements NameContainer {

        private final String firstName;
        private final String surname;

        public CasOppijaAuthenticationDetails(HttpServletRequest request, Collection<? extends GrantedAuthority> authorities, String firstName, String surname) {
            super(request, authorities);
            this.firstName = firstName;
            this.surname = surname;
        }

        @Override
        public String getFirstName() {
            return firstName;
        }

        @Override
        public String getSurname() {
            return surname;
        }

    }

}

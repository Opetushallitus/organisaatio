package fi.vm.sade.varda.rekisterointi.configuration;

import fi.vm.sade.java_utils.security.OpintopolkuCasAuthenticationFilter;
import fi.vm.sade.javautils.kayttooikeusclient.OphUserDetailsServiceImpl;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.NameContainer;
import fi.vm.sade.varda.rekisterointi.util.Constants;
import org.jasig.cas.client.validation.Cas20ProxyTicketValidator;
import org.jasig.cas.client.validation.TicketValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.preauth.*;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static fi.vm.sade.varda.rekisterointi.configuration.LocaleConfiguration.DEFAULT_LOCALE;
import static fi.vm.sade.varda.rekisterointi.configuration.LocaleConfiguration.SESSION_ATTRIBUTE_NAME_LOCALE;
import static fi.vm.sade.varda.rekisterointi.util.ServletUtils.findSessionAttribute;
import static java.util.Collections.singletonList;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String HAKIJA_ROLE = "APP_VARDAREKISTEROINTI_HAKIJA";
    private static final String VIRKAILIJA_ROLE = "APP_YKSITYISTEN_REKISTEROITYMINEN_CRUD";
    private static final String HAKIJA_PATH_CLOB = "/hakija/**";
    private static final String VIRKAILIJA_PATH_CLOB = "/virkailija/**";

    private final OphProperties ophProperties;

    public WebSecurityConfig(OphProperties ophProperties) {
        this.ophProperties = ophProperties;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.headers().disable().csrf().disable();
        http.authorizeRequests()
                .antMatchers(HAKIJA_PATH_CLOB).hasRole(HAKIJA_ROLE)
                .antMatchers(VIRKAILIJA_PATH_CLOB).hasRole(VIRKAILIJA_ROLE)
                .and()
                .addFilterBefore(hakijaAuthenticationProcessingFilter(), BasicAuthenticationFilter.class)
                .addFilterAfter(virkailijaAuthenticationProcessingFilter(), ShibbolethAuthenticationFilter.class)
                .exceptionHandling()
                .defaultAuthenticationEntryPointFor(
                        hakijaAuthenticationEntryPoint(),
                        new AntPathRequestMatcher(HAKIJA_PATH_CLOB))
                .defaultAuthenticationEntryPointFor(
                        virkailijaAuthenticationEntryPoint(),
                        new AntPathRequestMatcher(VIRKAILIJA_PATH_CLOB)
                );
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(hakijaAuthenticationProvider());
        auth.authenticationProvider(virkailijaAuthenticationProvider());
    }

    @Bean
    public ServiceProperties serviceProperties() {
        ServiceProperties serviceProperties = new ServiceProperties();
        serviceProperties.setService(ophProperties.getProperty("varda-rekisterointi.virkailija") + "/j_spring_cas_security_check");
        serviceProperties.setSendRenew(false);
        serviceProperties.setAuthenticateAllArtifacts(true);
        return serviceProperties;
    }

    @Bean
    public Filter hakijaAuthenticationProcessingFilter() throws Exception {
        ShibbolethAuthenticationFilter filter = new ShibbolethAuthenticationFilter("/hakija/login");
        filter.setAuthenticationManager(authenticationManager());
        String authenticationSuccessUrl = ophProperties.url("varda-rekisterointi.hakija.valtuudet.redirect");
        filter.setAuthenticationSuccessHandler(new SimpleUrlAuthenticationSuccessHandler(authenticationSuccessUrl));
        return filter;
    }

    @Bean
    public Filter virkailijaAuthenticationProcessingFilter() throws Exception {
        OpintopolkuCasAuthenticationFilter casAuthenticationFilter = new OpintopolkuCasAuthenticationFilter(serviceProperties());
        casAuthenticationFilter.setAuthenticationManager(authenticationManager());
        casAuthenticationFilter.setFilterProcessesUrl("/virkailija/j_spring_cas_security_check");
        return casAuthenticationFilter;
    }

    @Bean
    public AuthenticationEntryPoint hakijaAuthenticationEntryPoint() {
        String loginCallbackUrl = ophProperties.url("varda-rekisterointi.hakija.login");
        String defaultLoginUrl = ophProperties.url("shibbolethVirkailija.login", "FI", loginCallbackUrl);
        return new AuthenticationEntryPointImpl(defaultLoginUrl, ophProperties, loginCallbackUrl);
    }

    @Bean
    public AuthenticationEntryPoint virkailijaAuthenticationEntryPoint() {
        CasAuthenticationEntryPoint casAuthenticationEntryPoint = new CasAuthenticationEntryPoint();
        casAuthenticationEntryPoint.setLoginUrl(ophProperties.url("cas.login"));
        casAuthenticationEntryPoint.setServiceProperties(serviceProperties());
        return casAuthenticationEntryPoint;
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
            return properties.url("shibbolethVirkailija.login", language.toUpperCase(), loginCallbackUrl);
        }
    }

    @Bean
    public AuthenticationProvider hakijaAuthenticationProvider() {
        PreAuthenticatedAuthenticationProvider authenticationProvider = new PreAuthenticatedAuthenticationProvider();
        authenticationProvider.setPreAuthenticatedUserDetailsService(new PreAuthenticatedGrantedAuthoritiesUserDetailsService());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationProvider virkailijaAuthenticationProvider() {
        CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
        String host = ophProperties.url("kayttooikeus-service.host");
        casAuthenticationProvider.setUserDetailsService(new OphUserDetailsServiceImpl(host, Constants.CALLER_ID));
        casAuthenticationProvider.setServiceProperties(serviceProperties());
        casAuthenticationProvider.setTicketValidator(ticketValidator());
        casAuthenticationProvider.setKey("varda-rekisterointi");
        return casAuthenticationProvider;
    }

    @Bean
    public TicketValidator ticketValidator() {
        Cas20ProxyTicketValidator ticketValidator = new Cas20ProxyTicketValidator(ophProperties.url("cas.base"));
        ticketValidator.setAcceptAnyProxy(true);
        return ticketValidator;
    }

    private static class ShibbolethAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

        public ShibbolethAuthenticationFilter(String defaultFilterProcessesUrl) {
            super(defaultFilterProcessesUrl);
        }

        @Override
        public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException {
            String nationalIdentificationNumber = Optional.ofNullable(request.getHeader("nationalidentificationnumber"))
                    .or(() -> Optional.ofNullable(request.getParameter("hetu"))) // for easier development
                    .orElseThrow(() -> new PreAuthenticatedCredentialsNotFoundException("Unable to authenticate because required header doesn't exist"));

            String firstName = Optional.ofNullable(request.getHeader("firstname"))
                    .map(str -> new String(str.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8))
                    .orElse("");
            String surname = Optional.ofNullable(request.getHeader("sn"))
                    .map(str -> new String(str.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8))
                    .orElse("");

            PreAuthenticatedAuthenticationToken authRequest = new PreAuthenticatedAuthenticationToken(nationalIdentificationNumber, "N/A");
            List<? extends GrantedAuthority> authorities = singletonList(new SimpleGrantedAuthority(String.format("ROLE_%s", HAKIJA_ROLE)));
            authRequest.setDetails(new ShibbolethWebAuthenticationDetails(request, authorities, firstName, surname));
            return getAuthenticationManager().authenticate(authRequest);
        }

    }

    private static class ShibbolethWebAuthenticationDetails extends PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails implements NameContainer {

        private final String firstName;
        private final String surname;

        public ShibbolethWebAuthenticationDetails(HttpServletRequest request, Collection<? extends GrantedAuthority> authorities, String firstName, String surname) {
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

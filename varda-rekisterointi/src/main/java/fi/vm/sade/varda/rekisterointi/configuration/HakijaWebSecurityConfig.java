package fi.vm.sade.varda.rekisterointi.configuration;

import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.NameContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
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
@Order(2)
@EnableWebSecurity
public class HakijaWebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String HAKIJA_ROLE = "APP_VARDAREKISTEROINTI_HAKIJA";
    private static final String HAKIJA_PATH_CLOB = "/hakija/**";

    private final OphProperties ophProperties;

    public HakijaWebSecurityConfig(OphProperties ophProperties) {
        this.ophProperties = ophProperties;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.headers().disable().csrf().disable();
        http.antMatcher(HAKIJA_PATH_CLOB).authorizeRequests()
                .anyRequest().hasRole(HAKIJA_ROLE)
                .and()
                .addFilterBefore(hakijaAuthenticationProcessingFilter(), BasicAuthenticationFilter.class)
                .exceptionHandling()
                .authenticationEntryPoint(hakijaAuthenticationEntryPoint());
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(hakijaAuthenticationProvider());
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
    public AuthenticationEntryPoint hakijaAuthenticationEntryPoint() {
        String loginCallbackUrl = ophProperties.url("varda-rekisterointi.hakija.login");
        String defaultLoginUrl = ophProperties.url("shibbolethVirkailija.login", "FI", loginCallbackUrl);
        return new AuthenticationEntryPointImpl(defaultLoginUrl, ophProperties, loginCallbackUrl);
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

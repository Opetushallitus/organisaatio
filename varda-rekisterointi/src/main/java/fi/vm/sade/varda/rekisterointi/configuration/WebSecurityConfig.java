package fi.vm.sade.varda.rekisterointi.configuration;

import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.NameContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.preauth.*;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Optional;

import static java.util.Collections.emptyList;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final OphProperties properties;

    public WebSecurityConfig(OphProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/actuator/info", "/actuator/health").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(authenticationProcessingFilter(), BasicAuthenticationFilter.class)
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint());
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    @Bean
    public Filter authenticationProcessingFilter() throws Exception {
        ShibbolethAuthenticationFilter filter = new ShibbolethAuthenticationFilter("/initsession");
        filter.setAuthenticationManager(authenticationManager());
        String authenticationSuccessUrl = properties.url("varda-rekisterointi.valtuudet.redirect");
        filter.setAuthenticationSuccessHandler(new SimpleUrlAuthenticationSuccessHandler(authenticationSuccessUrl));
        return filter;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        String loginCallbackUrl = properties.url("varda-rekisterointi.login");
        String loginUrl = properties.url("shibbolethVirkailija.login", loginCallbackUrl);
        return new LoginUrlAuthenticationEntryPoint(loginUrl);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        PreAuthenticatedAuthenticationProvider authenticationProvider = new PreAuthenticatedAuthenticationProvider();
        authenticationProvider.setPreAuthenticatedUserDetailsService(new PreAuthenticatedGrantedAuthoritiesUserDetailsService());
        return authenticationProvider;
    }

    private static class ShibbolethAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

        public ShibbolethAuthenticationFilter(String defaultFilterProcessesUrl) {
            super(defaultFilterProcessesUrl);
        }

        @Override
        public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
            String nationalIdentificationNumber = request.getHeader("nationalidentificationnumber");
            if (nationalIdentificationNumber == null) {
                throw new PreAuthenticatedCredentialsNotFoundException("Unable to authenticate because required header doesn't exist");
            }

            Charset iso8859 = Charset.forName("ISO-8859-1");
            Charset utf8 = Charset.forName("UTF-8");
            String givenName = Optional.ofNullable(request.getHeader("firstname"))
                    .map(str -> new String(str.getBytes(iso8859), utf8))
                    .orElse("");
            String surname = Optional.ofNullable(request.getHeader("sn"))
                    .map(str -> new String(str.getBytes(iso8859), utf8))
                    .orElse("");

            PreAuthenticatedAuthenticationToken authRequest = new PreAuthenticatedAuthenticationToken(nationalIdentificationNumber, "N/A");
            authRequest.setDetails(new ShibbolethWebAuthenticationDetails(request, emptyList(), givenName, surname));
            return getAuthenticationManager().authenticate(authRequest);
        }

    }

    private static class ShibbolethWebAuthenticationDetails extends PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails implements NameContainer {

        private final String givenName;
        private final String surname;

        public ShibbolethWebAuthenticationDetails(HttpServletRequest request, Collection<? extends GrantedAuthority> authorities, String givenName, String surname) {
            super(request, authorities);
            this.givenName = givenName;
            this.surname = surname;
        }

        @Override
        public String getGivenName() {
            return givenName;
        }

        @Override
        public String getSurname() {
            return surname;
        }

    }

}

package fi.vm.sade.varda.rekisterointi.configuration;

import fi.vm.sade.java_utils.security.OpintopolkuCasAuthenticationFilter;
import fi.vm.sade.javautils.kayttooikeusclient.OphUserDetailsServiceImpl;
import fi.vm.sade.properties.OphProperties;

import org.apereo.cas.client.session.SingleSignOutFilter;
import org.apereo.cas.client.validation.Cas20ProxyTicketValidator;
import org.apereo.cas.client.validation.TicketValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

import jakarta.servlet.Filter;

import static fi.vm.sade.varda.rekisterointi.util.Constants.VIRKAILIJA_UI_ROLES;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = false, prePostEnabled = true, securedEnabled = true)
public class VirkailijaWebSecurityConfiguration {
    private static final String VIRKAILIJA_PATH_CLOB = "/virkailija/**";

    private final OphProperties ophProperties;

    VirkailijaWebSecurityConfiguration(OphProperties ophProperties) {
        this.ophProperties = ophProperties;
    }

    @Profile("!dev")
    @Bean
    @Order(1)
    SecurityFilterChain virkailiSecurityFilterChain(HttpSecurity http) throws Exception {
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setPortResolver(request -> request.getServerPort()); // override default PortResolverImpl
        requestCache.setMatchingRequestParameterName(null);
        return http
            .headers(headers -> headers.disable())
            .csrf(csrf -> csrf.disable())
            .securityMatcher(VIRKAILIJA_PATH_CLOB)
            .authorizeHttpRequests(authz -> authz.anyRequest().hasAnyRole(VIRKAILIJA_UI_ROLES))
            .addFilterBefore(virkailijaAuthenticationProcessingFilter(), BasicAuthenticationFilter.class)
            .addFilterBefore(singleSignOutFilter(), CasAuthenticationFilter.class)
            .securityContext(securityContext -> securityContext
                    .requireExplicitSave(true)
                    .securityContextRepository(new HttpSessionSecurityContextRepository()))
            .requestCache(cache -> cache.requestCache(requestCache))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(virkailijaAuthenticationEntryPoint()))
            .build();
    }

    ServiceProperties serviceProperties() {
        ServiceProperties serviceProperties = new ServiceProperties();
        serviceProperties.setService(ophProperties.url("varda-rekisterointi.virkailija") + "/j_spring_cas_security_check");
        serviceProperties.setSendRenew(false);
        serviceProperties.setAuthenticateAllArtifacts(true);
        return serviceProperties;
    }

    Filter virkailijaAuthenticationProcessingFilter() throws Exception {
        OpintopolkuCasAuthenticationFilter casAuthenticationFilter = new OpintopolkuCasAuthenticationFilter(serviceProperties());
        casAuthenticationFilter.setAuthenticationManager(new ProviderManager(virkailijaAuthenticationProvider()));
        casAuthenticationFilter.setFilterProcessesUrl("/virkailija/j_spring_cas_security_check");
        return casAuthenticationFilter;
    }

    AuthenticationEntryPoint virkailijaAuthenticationEntryPoint() {
        CasAuthenticationEntryPoint casAuthenticationEntryPoint = new CasAuthenticationEntryPoint();
        casAuthenticationEntryPoint.setLoginUrl(ophProperties.url("cas.login"));
        casAuthenticationEntryPoint.setServiceProperties(serviceProperties());
        return casAuthenticationEntryPoint;
    }

    AuthenticationProvider virkailijaAuthenticationProvider() {
        CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
        casAuthenticationProvider.setAuthenticationUserDetailsService(new OphUserDetailsServiceImpl());
        casAuthenticationProvider.setServiceProperties(serviceProperties());
        casAuthenticationProvider.setTicketValidator(ticketValidator());
        casAuthenticationProvider.setKey("varda-rekisterointi");
        return casAuthenticationProvider;
    }

    TicketValidator ticketValidator() {
        Cas20ProxyTicketValidator ticketValidator = new Cas20ProxyTicketValidator(ophProperties.url("cas.base"));
        ticketValidator.setAcceptAnyProxy(true);
        return ticketValidator;
    }

    SingleSignOutFilter singleSignOutFilter() {
        SingleSignOutFilter singleSignOutFilter = new SingleSignOutFilter();
        singleSignOutFilter.setIgnoreInitConfiguration(true);
        return singleSignOutFilter;
    }
}

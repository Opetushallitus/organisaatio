package fi.vm.sade.organisaatio.config;

import fi.vm.sade.java_utils.security.OpintopolkuCasAuthenticationFilter;
import fi.vm.sade.organisaatio.config.properties.CasProperties;
import fi.vm.sade.properties.OphProperties;
import lombok.RequiredArgsConstructor;

import org.apereo.cas.client.session.SingleSignOutFilter;
import org.apereo.cas.client.validation.Cas30ProxyTicketValidator;
import org.apereo.cas.client.validation.TicketValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

@Profile("!dev")
@Configuration
@EnableMethodSecurity(jsr250Enabled = false, prePostEnabled = true, securedEnabled = true)
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfiguration {
    private final CasProperties casProperties;
    private final OphProperties ophProperties;

    @Bean
    ServiceProperties serviceProperties() {
        ServiceProperties serviceProperties = new ServiceProperties();
        serviceProperties.setService(casProperties.getService() + "/j_spring_cas_security_check");
        serviceProperties.setSendRenew(casProperties.getSendRenew());
        serviceProperties.setAuthenticateAllArtifacts(true);
        return serviceProperties;
    }

    //
    // CAS authentication provider (authentication manager)
    //

    @Bean
    CasAuthenticationProvider casAuthenticationProvider(ServiceProperties serviceProperties, TicketValidator ticketValidator, UserDetailsService userDetailsService) {
        CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
        casAuthenticationProvider.setUserDetailsService(userDetailsService);
        casAuthenticationProvider.setServiceProperties(serviceProperties);
        casAuthenticationProvider.setTicketValidator(ticketValidator);
        casAuthenticationProvider.setKey(casProperties.getKey());
        return casAuthenticationProvider;
    }

    @Bean
    TicketValidator ticketValidator() {
        Cas30ProxyTicketValidator ticketValidator = new Cas30ProxyTicketValidator(this.ophProperties.url("cas.base"));
        ticketValidator.setAcceptAnyProxy(true);
        return ticketValidator;
    }

    @Bean
    HttpSessionSecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    //
    // CAS filter
    //
    @Bean
    CasAuthenticationFilter casAuthenticationFilter(
            AuthenticationConfiguration authenticationConfiguration,
            ServiceProperties serviceProperties,
            SecurityContextRepository securityContextRepository) throws Exception {
        OpintopolkuCasAuthenticationFilter casAuthenticationFilter = new OpintopolkuCasAuthenticationFilter(serviceProperties);
        casAuthenticationFilter.setAuthenticationManager(authenticationConfiguration.getAuthenticationManager());
        casAuthenticationFilter.setServiceProperties(serviceProperties);
        casAuthenticationFilter.setFilterProcessesUrl("/j_spring_cas_security_check");
        casAuthenticationFilter.setSecurityContextRepository(securityContextRepository);
        return casAuthenticationFilter;
    }

    //
    // CAS single logout filter
    // requestSingleLogoutFilter is not configured because our users always sign out through CAS logout (using virkailija-raamit
    // logout button) when CAS calls this filter if user has ticket to this service.
    //
    @Bean
    SingleSignOutFilter singleSignOutFilter() {
        SingleSignOutFilter singleSignOutFilter = new SingleSignOutFilter();
        singleSignOutFilter.setIgnoreInitConfiguration(true);
        return singleSignOutFilter;
    }

    //
    // CAS entry point
    //
    @Bean
    CasAuthenticationEntryPoint casAuthenticationEntryPoint() {
        CasAuthenticationEntryPoint casAuthenticationEntryPoint = new CasAuthenticationEntryPoint();
        casAuthenticationEntryPoint.setLoginUrl(ophProperties.url("cas.login"));
        casAuthenticationEntryPoint.setServiceProperties(serviceProperties());
        return casAuthenticationEntryPoint;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, CasAuthenticationFilter casAuthenticationFilter,
            AuthenticationEntryPoint authenticationEntryPoint, SecurityContextRepository securityContextRepository) throws Exception {
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setMatchingRequestParameterName(null);
        http
            .headers(headers -> headers.disable())
            .csrf(csrf -> csrf.disable())
            .securityMatcher("/**")
            .authorizeHttpRequests(authz -> authz
                    .requestMatchers("/buildversion.txt").permitAll()
                    .requestMatchers("/actuator/health").permitAll()
                    .requestMatchers("/swagger-ui.html").permitAll()
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/swagger-resources/**").permitAll()
                    .requestMatchers("/v3/api-docs/**").permitAll()
                    .requestMatchers("/").permitAll()
                    .requestMatchers("/api/**").permitAll()
                    .requestMatchers("/rest/**").permitAll()
                    .requestMatchers("/mock/**").permitAll()
                    .requestMatchers("/static/**").permitAll()
                    .anyRequest().authenticated())
            .addFilterAt(casAuthenticationFilter, CasAuthenticationFilter.class)
            .addFilterBefore(singleSignOutFilter(), CasAuthenticationFilter.class)
            .securityContext(securityContext -> securityContext
                .requireExplicitSave(true)
                .securityContextRepository(securityContextRepository))
            .requestCache(cache -> cache.requestCache(requestCache))
            .exceptionHandling(handling -> handling.authenticationEntryPoint(authenticationEntryPoint));
        return http.build();
    }
}

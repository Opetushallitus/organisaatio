package fi.vm.sade.organisaatio.config;

import fi.vm.sade.java_utils.security.OpintopolkuCasAuthenticationFilter;
import fi.vm.sade.javautils.kayttooikeusclient.OphUserDetailsServiceImpl;
import fi.vm.sade.organisaatio.config.properties.CasProperties;
import fi.vm.sade.properties.OphProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apereo.cas.client.session.SingleSignOutFilter;
import org.apereo.cas.client.validation.Cas30ProxyTicketValidator;
import org.apereo.cas.client.validation.TicketValidator;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.util.matcher.RequestMatcher;

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
    CasAuthenticationProvider casAuthenticationProvider(ServiceProperties serviceProperties, TicketValidator ticketValidator) {
        CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
        casAuthenticationProvider.setAuthenticationUserDetailsService(new OphUserDetailsServiceImpl());
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
    @Order(1)
    SecurityFilterChain oauth2FilterChain(HttpSecurity http) throws Exception {
        return http
                .headers(headers -> headers.disable())
                .csrf(csrf -> csrf.disable())
                .securityMatcher(new RequestMatcher() {
                    @Override
                    public boolean matches(HttpServletRequest request) {
                        return isOauth2Request(request);
                    }
                })
                .authorizeHttpRequests(authz -> authz.anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(oauth2JwtConverter())))
                .build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain casSecurityFilterChain(
            HttpSecurity http,
            CasAuthenticationFilter casAuthenticationFilter,
            AuthenticationEntryPoint authenticationEntryPoint,
            SecurityContextRepository securityContextRepository
    ) throws Exception {
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setMatchingRequestParameterName(null);
        http
            .headers(headers -> headers.disable())
            .csrf(csrf -> csrf.disable())
            .securityMatcher(new RequestMatcher() {
                @Override
                public boolean matches(HttpServletRequest request) {
                    return !isOauth2Request(request);
                }
            })
            .authorizeHttpRequests(authz -> authz
                    .requestMatchers("/actuator/health").permitAll()
                    .requestMatchers("/swagger-ui.html").permitAll()
                    .requestMatchers("/swagger-ui/").permitAll()
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/swagger-resources/**").permitAll()
                    .requestMatchers("/api-docs/**").permitAll()
                    .requestMatchers("/").permitAll()
                    .requestMatchers("/api/**").permitAll()
                    .requestMatchers("/rest/**").permitAll()
                    .requestMatchers("/mock/**").permitAll()
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

    private Converter<Jwt, AbstractAuthenticationToken> oauth2JwtConverter() {
        return new Converter<Jwt, AbstractAuthenticationToken>() {
            JwtGrantedAuthoritiesConverter delegate = new JwtGrantedAuthoritiesConverter();

            @Override
            public AbstractAuthenticationToken convert(Jwt source) {
                var authorityList = extractRoles(source);
                var delegateAuthorities = delegate.convert(source);
                if (delegateAuthorities != null) {
                    authorityList.addAll(delegateAuthorities);
                }
                return new JwtAuthenticationToken(source, authorityList);
            }

            private List<GrantedAuthority> extractRoles(Jwt jwt) {
                Map<String, List<String>> roleClaim = jwt.getClaims().get("roles") != null
                        ? (Map<String, List<String>>) jwt.getClaims().get("roles")
                        : Map.of();
                var roles = roleClaim.keySet()
                        .stream()
                        .map((oid) -> {
                            var orgRoles = roleClaim.get(oid);
                            return orgRoles.stream().map((role) -> List.of(
                                    "ROLE_APP_" + role,
                                    "ROLE_APP_" + role + "_" + oid
                            )).toList();
                        })
                        .flatMap(List::stream)
                        .flatMap(List::stream)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.<GrantedAuthority>toList());
                return roles;
            }
        };
    }

    private boolean isOauth2Request(HttpServletRequest request) {
        return request.getHeader("Authorization") != null && request.getHeader("Authorization").startsWith("Bearer ");
    }
}

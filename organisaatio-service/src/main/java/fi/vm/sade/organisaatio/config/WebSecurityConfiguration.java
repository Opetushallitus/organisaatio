package fi.vm.sade.organisaatio.config;

import fi.vm.sade.java_utils.security.OpintopolkuCasAuthenticationFilter;
import fi.vm.sade.organisaatio.config.properties.CasProperties;
import fi.vm.sade.properties.OphProperties;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.validation.Cas20ProxyTicketValidator;
import org.jasig.cas.client.validation.TicketValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;

@Profile("!dev")
@Configuration
@EnableGlobalMethodSecurity(jsr250Enabled = false, prePostEnabled = true, securedEnabled = true)
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
    private final CasProperties casProperties;
    private final OphProperties ophProperties;
    private final UserDetailsService userDetailsService;

    @Autowired
    public WebSecurityConfiguration(CasProperties casProperties, OphProperties ophProperties, UserDetailsService userDetailsService) {
        this.casProperties = casProperties;
        this.ophProperties = ophProperties;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public ServiceProperties serviceProperties() {
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
    public CasAuthenticationProvider casAuthenticationProvider() {
        CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
        casAuthenticationProvider.setUserDetailsService(this.userDetailsService);
        casAuthenticationProvider.setServiceProperties(serviceProperties());
        casAuthenticationProvider.setTicketValidator(ticketValidator());
        casAuthenticationProvider.setKey(casProperties.getKey());
        return casAuthenticationProvider;
    }

    @Bean
    public TicketValidator ticketValidator() {
        Cas20ProxyTicketValidator ticketValidator = new Cas20ProxyTicketValidator(this.ophProperties.url("cas.base"));
        ticketValidator.setAcceptAnyProxy(true);
        return ticketValidator;
    }

    //
    // CAS filter
    //
    @Bean
    public CasAuthenticationFilter casAuthenticationFilter() throws Exception {
        OpintopolkuCasAuthenticationFilter casAuthenticationFilter = new OpintopolkuCasAuthenticationFilter(serviceProperties());
        casAuthenticationFilter.setAuthenticationManager(authenticationManager());
        casAuthenticationFilter.setFilterProcessesUrl("/j_spring_cas_security_check");
        return casAuthenticationFilter;
    }

    //
    // CAS single logout filter
    // requestSingleLogoutFilter is not configured because our users always sign out through CAS logout (using virkailija-raamit
    // logout button) when CAS calls this filter if user has ticket to this service.
    //
    @Bean
    public SingleSignOutFilter singleSignOutFilter() {
        SingleSignOutFilter singleSignOutFilter = new SingleSignOutFilter();
        singleSignOutFilter.setIgnoreInitConfiguration(true);
        singleSignOutFilter.setCasServerUrlPrefix(this.ophProperties.url("cas.base"));
        return singleSignOutFilter;
    }

    //
    // CAS entry point
    //
    @Bean
    public CasAuthenticationEntryPoint casAuthenticationEntryPoint() {
        CasAuthenticationEntryPoint casAuthenticationEntryPoint = new CasAuthenticationEntryPoint();
        casAuthenticationEntryPoint.setLoginUrl(ophProperties.url("cas.login"));
        casAuthenticationEntryPoint.setServiceProperties(serviceProperties());
        return casAuthenticationEntryPoint;
    }

    @Override
    @SuppressWarnings("java:S4502")
    protected void configure(HttpSecurity http) throws Exception {
        http
                .headers().disable()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/buildversion.txt").permitAll()
                .antMatchers("/actuator/health").permitAll()
                .antMatchers("/swagger-ui/**").permitAll()
                .antMatchers("/api-docs/**").permitAll()
                .antMatchers("/").permitAll()
                .antMatchers("/api/**").permitAll()
                .antMatchers("/rest/**").permitAll()
                .antMatchers("/mock/**").permitAll()
                .antMatchers("/static/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilter(casAuthenticationFilter())
                .exceptionHandling()
                .authenticationEntryPoint(casAuthenticationEntryPoint())
                .and()
                .addFilterBefore(singleSignOutFilter(), CasAuthenticationFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .authenticationProvider(casAuthenticationProvider());
    }
}

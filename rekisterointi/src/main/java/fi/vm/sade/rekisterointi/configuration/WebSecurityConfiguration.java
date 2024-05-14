package fi.vm.sade.rekisterointi.configuration;

import fi.vm.sade.properties.OphProperties;

import org.apache.http.HttpStatus;
import org.apereo.cas.client.validation.Cas30ServiceTicketValidator;
import org.apereo.cas.client.validation.TicketValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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

import java.io.IOException;
import java.util.List;

import static java.util.Collections.singletonList;
import static fi.vm.sade.rekisterointi.util.Constants.SESSION_ATTRIBUTE_NAME_ORIGINAL_REQUEST;
import static fi.vm.sade.rekisterointi.util.Constants.SESSION_ATTRIBUTE_NAME_BUSINESS_ID;
import static fi.vm.sade.rekisterointi.util.Constants.SESSION_ATTRIBUTE_NAME_ORGANISATION_NAME;
import static fi.vm.sade.rekisterointi.util.ServletUtils.findSessionAttribute;
import static fi.vm.sade.rekisterointi.util.ServletUtils.setSessionAttribute;

@Profile("!dev")
@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {
  private static final String HAKIJA_ROLE = "APP_REKISTEROINTI_HAKIJA";
  private final OphProperties ophProperties;

  public WebSecurityConfiguration(OphProperties ophProperties) {
    this.ophProperties = ophProperties;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, CasAuthenticationFilter casAuthenticationFilter,
      AuthenticationEntryPoint authenticationEntryPoint, SecurityContextRepository securityContextRepository)
      throws Exception {
    http.headers().disable().csrf().disable()
        .securityMatcher("/hakija/**")
        .authorizeHttpRequests(authz -> authz
            .requestMatchers("/hakija/**").hasRole(HAKIJA_ROLE)
            .anyRequest().authenticated()
            .and()
            .addFilterAt(casAuthenticationFilter, CasAuthenticationFilter.class)
            .addFilterBefore(new SaveLoginRedirectFilter(), CasAuthenticationFilter.class)
            .addFilterAfter(new ValtuudetRedirectFilter(), CasAuthenticationFilter.class))
        .securityContext(securityContext -> securityContext
            .requireExplicitSave(true)
            .securityContextRepository(securityContextRepository))
        .exceptionHandling()
        .authenticationEntryPoint(authenticationEntryPoint);
    return http.build();
  }

  @Bean
  public TicketValidator casOppijaticketValidator() {
    return new Cas30ServiceTicketValidator(ophProperties.url("cas-oppija.url"));
  }

  @Bean
  public HttpSessionSecurityContextRepository securityContextRepository() {
    return new HttpSessionSecurityContextRepository();
  }

  @Bean
  public ServiceProperties casServiceProperties() {
    ServiceProperties properties = new ServiceProperties();
    properties.setService(ophProperties.url("rekisterointi.hakija.login") + "/j_spring_cas_security_check");
    properties.setSendRenew(false);
    properties.setAuthenticateAllArtifacts(true);
    return properties;
  }

  @Bean
  public CasAuthenticationFilter casAuthenticationFilter(
      AuthenticationConfiguration authenticationConfiguration,
      ServiceProperties serviceProperties,
      SecurityContextRepository securityContextRepository) throws Exception {
    CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
    casAuthenticationFilter.setAuthenticationManager(authenticationConfiguration.getAuthenticationManager());
    casAuthenticationFilter.setServiceProperties(serviceProperties);
    casAuthenticationFilter.setFilterProcessesUrl("/j_spring_cas_security_check");
    casAuthenticationFilter.setAuthenticationSuccessHandler(
        new SimpleUrlAuthenticationSuccessHandler(ophProperties.url("rekisterointi.hakija.valtuudet.redirect")));
    casAuthenticationFilter.setSecurityContextRepository(securityContextRepository);
    return casAuthenticationFilter;
  }

  @Bean
  public AuthenticationEntryPoint authenticationEntryPoint(ServiceProperties serviceProperties) {
    CasAuthenticationEntryPoint authenticationEntryPoint = new CasAuthenticationEntryPoint();
    authenticationEntryPoint.setLoginUrl(ophProperties.url("cas-oppija.login"));
    authenticationEntryPoint.setServiceProperties(serviceProperties);
    return authenticationEntryPoint;
  }

  @Bean
  public AuthenticationProvider authenticationProvider(
      AuthenticationUserDetailsService<CasAssertionAuthenticationToken> userDetailsService,
      ServiceProperties serviceProperties, TicketValidator ticketValidator) {
    CasAuthenticationProvider authenticationProvider = new CasAuthenticationProvider();
    authenticationProvider.setAuthenticationUserDetailsService(userDetailsService);
    authenticationProvider.setServiceProperties(serviceProperties);
    authenticationProvider.setTicketValidator(ticketValidator);
    authenticationProvider.setKey("rekisterointi");
    return authenticationProvider;
  }

  @Bean
  public CasUserDetailsService casUserDetailsService() {
    return new CasUserDetailsService();
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
        setSessionAttribute(httpRequest, SESSION_ATTRIBUTE_NAME_ORIGINAL_REQUEST, url);
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
      boolean hasBusinessId = findSessionAttribute(httpRequest, SESSION_ATTRIBUTE_NAME_BUSINESS_ID, String.class)
          .isPresent();
      boolean hasOrgName = findSessionAttribute(httpRequest, SESSION_ATTRIBUTE_NAME_ORGANISATION_NAME,
          String.class).isPresent();
      if (!httpRequest.getRequestURI().contains("/valtuudet/") && (!hasBusinessId || !hasOrgName)) {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String encodedRedirectURL = httpResponse.encodeRedirectURL(
            httpRequest.getContextPath() + "/hakija/valtuudet/redirect");
        httpResponse.setStatus(HttpStatus.SC_TEMPORARY_REDIRECT);
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

package fi.vm.sade.rekisterointi.configuration;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.GenericFilterBean;

import static fi.vm.sade.rekisterointi.util.Constants.SESSION_ATTRIBUTE_NAME_BUSINESS_ID;
import static fi.vm.sade.rekisterointi.util.Constants.SESSION_ATTRIBUTE_NAME_ORGANISATION_NAME;
import static fi.vm.sade.rekisterointi.util.ServletUtils.setSessionAttribute;

@Profile("dev")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = false, prePostEnabled = false, securedEnabled = true)
public class DevWebSecurityConfiguration {

  UserDetailsService userDetailsService;
  PasswordEncoder passwordEncoder;

  @Autowired
  public DevWebSecurityConfiguration(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
    this.userDetailsService = userDetailsService;
    this.passwordEncoder = passwordEncoder;
  }

  @Bean
  public DaoAuthenticationProvider authProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(this.passwordEncoder);
    return authProvider;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http.csrf().disable().authorizeHttpRequests()
        .requestMatchers("/jotpa").permitAll()
        .requestMatchers("/jotpa/**").permitAll()
        .requestMatchers("/api/**").permitAll()
        .requestMatchers("/actuator/health").permitAll()
        .anyRequest().authenticated()
        .and()
        .authenticationProvider(authProvider())
        .addFilterAfter(new DoLoginFilter(), BasicAuthenticationFilter.class)
        .httpBasic()
        .and().build();
  }

  public static class DoLoginFilter extends GenericFilterBean {
    @Override
    public void doFilter(
        ServletRequest request,
        ServletResponse response,
        FilterChain chain) throws IOException, ServletException {
      var httpRequest = (HttpServletRequest) request;
      setSessionAttribute(httpRequest, SESSION_ATTRIBUTE_NAME_BUSINESS_ID, "0772017-4");
      setSessionAttribute(httpRequest, SESSION_ATTRIBUTE_NAME_ORGANISATION_NAME, "Meyer Turku Oy");
      chain.doFilter(request, response);
    }
  }
}

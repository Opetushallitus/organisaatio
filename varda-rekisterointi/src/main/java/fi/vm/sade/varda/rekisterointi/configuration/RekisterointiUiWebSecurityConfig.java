package fi.vm.sade.varda.rekisterointi.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@Order(3)
@EnableWebSecurity
public class RekisterointiUiWebSecurityConfig extends WebSecurityConfigurerAdapter {
  private static final String ENDPOINT = "/api/rekisterointi";
  private static final String ROLE = "REKISTEROINTI_UI";

  @Value("${varda-rekisterointi.rekisterointi-ui.username}")
  private String username;

  @Value("${varda-rekisterointi.rekisterointi-ui.password}")
  private String password;

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.inMemoryAuthentication()
        .withUser(username).password("{noop}" + password)
        .authorities(ROLE);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.headers().disable().csrf().disable()
        .antMatcher(ENDPOINT).authorizeRequests()
        .anyRequest().authenticated()
        .and()
        .httpBasic();
  }
}

package fi.vm.sade.varda.rekisterointi.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import static fi.vm.sade.varda.rekisterointi.util.Constants.JOTPA_ROLE;
import static fi.vm.sade.varda.rekisterointi.util.Constants.PAAKAYTTAJA_AUTHORITY;
import static fi.vm.sade.varda.rekisterointi.util.Constants.VARDA_ROLE;
import static fi.vm.sade.varda.rekisterointi.util.Constants.VIRKAILIJA_ROLE;
import static fi.vm.sade.varda.rekisterointi.util.Constants.VIRKAILIJA_UI_ROLES;

@Profile("dev")
@Configuration
@EnableWebSecurity
public class DevVirkailijaWebSecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().authorizeRequests()
                .antMatchers("/virkailija/**").permitAll()
                .anyRequest().authenticated()
                .and().exceptionHandling()
                .and().httpBasic();
        ;
    }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.inMemoryAuthentication()
        .withUser("devaaja").password("{noop}devaaja")
        .authorities(PAAKAYTTAJA_AUTHORITY);
  }
}
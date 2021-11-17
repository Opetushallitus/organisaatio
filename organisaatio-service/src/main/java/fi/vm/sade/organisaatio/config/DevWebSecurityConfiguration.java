package fi.vm.sade.organisaatio.config;


import fi.vm.sade.organisaatio.config.properties.CasProperties;
import fi.vm.sade.properties.OphProperties;
import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.ComponentScan;
        import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
        import org.springframework.security.config.annotation.web.builders.WebSecurity;
        import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
        import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
        import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;

@Profile("dev")
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(jsr250Enabled = false, prePostEnabled = false, securedEnabled = true)
public class DevWebSecurityConfiguration extends WebSecurityConfigurerAdapter {

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

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().authorizeRequests()
                .antMatchers("/buildversion.txt").permitAll()
                .antMatchers("/actuator/health").permitAll()
                .antMatchers("/organisaatio/swagger-ui/**").permitAll()
                .antMatchers("/organisaatio-service/**").permitAll()
                .antMatchers("/organisaatio/api-docs/**").permitAll()
                .antMatchers("/organisaatio-service/rest/**").permitAll()
                .antMatchers("/organisaatio/api/**").permitAll()
                .anyRequest().authenticated().and().exceptionHandling().accessDeniedHandler(accessDeniedHandler())
                .and().httpBasic();
        ;
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authProvider());
    }
}
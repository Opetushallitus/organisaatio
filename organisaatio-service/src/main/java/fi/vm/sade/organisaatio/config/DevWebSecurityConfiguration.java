package fi.vm.sade.organisaatio.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;

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
                .antMatchers("/swagger-ui/**").permitAll()
                .antMatchers("/api-docs/**").permitAll()
                .antMatchers("/").permitAll()
                .antMatchers("/api/**").permitAll()
                .antMatchers("/rest/**").permitAll()
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
package fi.vm.sade.rekisterointi.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Profile("dev")
@Configuration
public class DevUserDetailsServiceConfiguration {
  private static final String DEVAAJA = "dev";
  private static final SimpleGrantedAuthority[] OPH_AUTHORITIES = new SimpleGrantedAuthority[] {
      new SimpleGrantedAuthority(String.format("%s_1.2.246.562.10.00000000001", "ROLE_APP_REKISTEROINTI_HAKIJA")),
      new SimpleGrantedAuthority("ROLE_APP_REKISTEROINTI_HAKIJA")
  };

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public UserDetailsService userDetailsService() {

    return new DevUserDetailsService(passwordEncoder());
  }

  static class DevUserDetailsService implements UserDetailsService {
    PasswordEncoder passwordEncoder;

    private DevUserDetailsService(PasswordEncoder passwordEncoder) {
      this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
      return User.builder()
          .authorities(List.of(OPH_AUTHORITIES))
          .password(this.passwordEncoder.encode(DEVAAJA))
          .username(DEVAAJA)
          .build();
    }
  }
}

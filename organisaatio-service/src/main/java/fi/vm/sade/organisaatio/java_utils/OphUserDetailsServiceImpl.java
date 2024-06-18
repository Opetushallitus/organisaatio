package fi.vm.sade.organisaatio.java_utils;

import com.google.gson.Gson;
import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpRequest;
import fi.vm.sade.javautils.http.auth.Authenticator;
import fi.vm.sade.properties.OphProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.Optional;

/**
 * {@link UserDetailsService}-toteutus joka hakee käyttäjän roolit käyttöoikeuspalvelusta.
 */
public class OphUserDetailsServiceImpl implements UserDetailsService {

    static final String USERDETAILS_URL_KEY = "kayttooikeus-service.userDetails.byUsername";
    private final Gson gson = new Gson();
    private final OphHttpClient httpClient;
    private final OphProperties properties;

    /**
     * Rakentaja.
     *
     * @param urlVirkailija käyttöoikeuspalvelun sisäinen osoite ("scheme://host" ilman "/kayttooikeus-service/...")
     * @param callerId      kutsuvan palvelun tunniste, esim. "1.2.246.562.10.00000000001.oppijanumerorekisteri"
     *                      (ks. https://confluence.csc.fi/pages/viewpage.action?pageId=50858064 )
     */
    public OphUserDetailsServiceImpl(String urlVirkailija, String callerId, Authenticator authenticator) {
        this(
                new OphHttpClient.Builder(callerId).authenticator(authenticator).build(),
                new OphProperties("/kayttooikeusclient-oph.properties")
                        .addOverride("url-virkailija", urlVirkailija)
        );
    }

    OphUserDetailsServiceImpl(OphHttpClient httpClient, OphProperties properties) {
        this.httpClient = httpClient;
        this.properties = properties;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String url = properties.url(USERDETAILS_URL_KEY, username);
        OphHttpRequest request = OphHttpRequest.Builder.get(url).build();
        Optional<UserDetails> userDetails = httpClient.<UserDetails>execute(request)
                .expectedStatus(200)
                .mapWith(json -> gson.fromJson(json, UserDetailsImpl.class));
        return userDetails.orElseThrow(() -> new UsernameNotFoundException(
                String.format("Käyttäjää ei löytynyt käyttäjätunnuksella '%s'", username)));
    }

    private static final class UserDetailsImpl implements UserDetails {

        private Collection<GrantedAuthorityImpl> authorities;
        private String password;
        private String username;
        private boolean accountNonExpired;
        private boolean accountNonLocked;
        private boolean credentialsNonExpired;
        private boolean enabled;

        @Override
        public Collection<GrantedAuthorityImpl> getAuthorities() {
            return authorities;
        }

        public void setAuthorities(Collection<GrantedAuthorityImpl> authorities) {
            this.authorities = authorities;
        }

        @Override
        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        @Override
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        @Override
        public boolean isAccountNonExpired() {
            return accountNonExpired;
        }

        public void setAccountNonExpired(boolean accountNonExpired) {
            this.accountNonExpired = accountNonExpired;
        }

        @Override
        public boolean isAccountNonLocked() {
            return accountNonLocked;
        }

        public void setAccountNonLocked(boolean accountNonLocked) {
            this.accountNonLocked = accountNonLocked;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return credentialsNonExpired;
        }

        public void setCredentialsNonExpired(boolean credentialsNonExpired) {
            this.credentialsNonExpired = credentialsNonExpired;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

    }

    private static final class GrantedAuthorityImpl implements GrantedAuthority {

        private String authority;

        @Override
        public String getAuthority() {
            return authority;
        }

        public void setAuthority(String authority) {
            this.authority = authority;
        }

    }

}

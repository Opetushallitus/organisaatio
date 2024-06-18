package fi.vm.sade.organisaatio.java_utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class KayttookeusServiceUserDetailsService implements UserDetailsService {
    private final ObjectMapper mapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private final HttpClient httpClient = HttpClient.newBuilder()
            .cookieHandler(new CookieManager())
            .connectTimeout(Duration.ofSeconds(60))
            .build();

    private final String baseUrl;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            log.info("Resolving user details for user '{}'", username);
            var details = getUserDetailsFromKayttooikeusService(username)
                    .orElseThrow(() -> new UsernameNotFoundException(String.format("Käyttäjää ei löytynyt käyttäjätunnuksella '%s'", username)));
            log.info("Resolved: {}", details);
            return UserDetailsImpl.fromUserDetailsResponse(details);
        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error while fetching user details", e);
            throw e;
        }
    }

    @SneakyThrows
    private Optional<UserDetailsResponse> getUserDetailsFromKayttooikeusService(String username) {
        var uri = URI.create(baseUrl + "/userDetails/" + username);
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .header("Accept", "application/json")
                .header("Caller-Id", "1.2.246.562.10.00000000001.organisaatio-service")
                .header("CSRF", "CSRF")
                .header("Cookie", "CSRF=CSRF")
                .build();
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("Received response: {} {}", response.statusCode(), response.body());
        return switch (response.statusCode()) {
            case 200 -> Optional.of(mapper.readValue(response.body(), UserDetailsResponse.class));
            case 404 -> Optional.empty();
            default -> {
                var msg = String.format("Unexpected response %d %s", response.statusCode(), response.body());
                throw new RuntimeException(msg);
            }
        };
    }

    private record UserDetailsResponse(List<UserDetailsResponseAuthority> authorities, String username) {}
    private record UserDetailsResponseAuthority(String authority) {}

    @RequiredArgsConstructor
    private static final class UserDetailsImpl implements UserDetails {
        public static UserDetailsImpl fromUserDetailsResponse(UserDetailsResponse details) {
            var authorities = details.authorities.stream().map(UserDetailsResponseAuthority::authority).toList();
            return new UserDetailsImpl(details.username, authorities);
        }

        private final String username;
        private final List<String> authorities;
        @Override public String getUsername() { return username; }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities.stream().map(SimpleGrantedAuthority::new).toList();
        }

        @Override public String getPassword() { return null; }
        @Override public boolean isAccountNonExpired() { return true; }
        @Override public boolean isAccountNonLocked() { return true; }
        @Override public boolean isCredentialsNonExpired() { return true; }
        @Override public boolean isEnabled() { return true; }
    }
}

package fi.vm.sade.varda.rekisterointi.util;

import fi.vm.sade.varda.rekisterointi.NameContainer;
import fi.vm.sade.varda.rekisterointi.RequestContext;
import org.springframework.security.core.Authentication;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.vm.sade.varda.rekisterointi.util.ServletUtils.*;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;

public class RequestContextImpl implements RequestContext {

    private final Optional<String> username;
    private final String ip;
    private final Optional<String> session;
    private final Optional<String> userAgent;

    public RequestContextImpl(String ip) {
        this(Optional.empty(), ip, Optional.empty(), Optional.empty());
    }

    public RequestContextImpl(String username, String ip) {
        this(Optional.of(username), ip, Optional.empty(), Optional.empty());
    }

    protected RequestContextImpl(Optional<String> username, String ip, Optional<String> session, Optional<String> userAgent) {
        this.username = requireNonNull(username, "username");
        this.ip = requireNonNull(ip, "ip");
        this.session = requireNonNull(session, "session");
        this.userAgent = requireNonNull(userAgent, "userAgent");
    }

    public static RequestContextImpl of(HttpServletRequest request) {
        return new RequestContextImpl(resolveUsername(request), resolveIp(request),
                ServletUtils.resolveSession(request), resolveUserAgent(request));
    }

    public static RequestContextImpl of(HttpServletRequest request, Authentication authentication) {
        return new RequestContextImpl(resolveUsername(request), resolveIp(request),
                resolveSession(request, authentication), resolveUserAgent(request));
    }

    private static Optional<String> resolveSession(HttpServletRequest request, Authentication authentication) {
        String session = Stream.of(ServletUtils.resolveSession(request), resolveUsername(request), resolveName(authentication))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(not(String::isEmpty))
                .collect(Collectors.joining(","));
        return Optional.ofNullable(session);
    }

    private static Optional<String> resolveName(Authentication authentication) {
        if (authentication.getDetails() instanceof NameContainer) {
            NameContainer nameContainer = (NameContainer) authentication.getDetails();
            String name = Stream.of(nameContainer.getFirstName(), nameContainer.getSurname())
                    .filter(Objects::nonNull)
                    .collect(joining(" "));
            return Optional.of(name);
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getUsername() {
        return username;
    }

    @Override
    public String getIp() {
        return ip;
    }

    @Override
    public Optional<String> getSession() {
        return session;
    }

    @Override
    public Optional<String> getUserAgent() {
        return userAgent;
    }

}

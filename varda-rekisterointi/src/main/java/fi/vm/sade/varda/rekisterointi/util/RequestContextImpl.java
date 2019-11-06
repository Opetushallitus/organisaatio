package fi.vm.sade.varda.rekisterointi.util;

import fi.vm.sade.varda.rekisterointi.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static fi.vm.sade.varda.rekisterointi.util.ServletUtils.*;
import static java.util.Objects.requireNonNull;

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
                resolveSession(request), resolveUserAgent(request));
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

package fi.vm.sade.varda.rekisterointi.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Optional;

public final class ServletUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServletUtils.class);

    private ServletUtils() {
    }

    public static <T> Optional<T> findSessionAttribute(HttpServletRequest request, String name, Class<T> type) {
        return Optional.ofNullable(request.getSession(false))
                .flatMap(session -> Optional.ofNullable(session.getAttribute(name)))
                .filter(type::isInstance)
                .map(type::cast);
    }

    public static void removeSessionAttribute(HttpServletRequest request, String name) {
      HttpSession session = request.getSession();
      session.removeAttribute(name);
    }

    public static <T> T setSessionAttribute(HttpServletRequest request, String name, T value) {
        HttpSession session = request.getSession();
        session.setAttribute(name, value);
        return value;
    }

    public static Optional<String> resolveUsername(HttpServletRequest request) {
        return Optional.ofNullable(request.getUserPrincipal()).map(Principal::getName);
    }

    public static String resolveIp(HttpServletRequest request) {
        return resolveIp(
                request.getHeader("X-Real-IP"),
                request.getHeader("X-Forwarded-For"),
                request.getRemoteAddr(),
                request.getRequestURI());
    }

    static String resolveIp(String realIp, String forwardedFor, String remoteAddr, String requestUri) {
        if (isNotEmpty(realIp)) {
            return realIp;
        }
        if (isNotEmpty(forwardedFor)) {
            if (forwardedFor.contains(",")) {
                LOGGER.error(
                        "Could not find X-Real-IP header, but X-Forwarded-For contains multiple values: {}, this can cause problems",
                        forwardedFor);
            }
            return forwardedFor;
        }
        LOGGER.warn(
                "X-Real-IP or X-Forwarded-For was not set. Are we not running behind a load balancer? Request URI is '{}'",
                requestUri);
        return remoteAddr;
    }

    public static Optional<String> resolveSession(HttpServletRequest request) {
        return Optional.ofNullable(request.getSession(false)).map(HttpSession::getId);
    }

    public static Optional<String> resolveUserAgent(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("User-Agent"));
    }

    private static boolean isNotEmpty(String value) {
        return value != null && !value.isEmpty();
    }

}

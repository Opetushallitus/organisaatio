package fi.vm.sade.varda.rekisterointi.util;

import fi.vm.sade.javautils.http.HttpServletRequestUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Optional;

public final class ServletUtils {

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
        return HttpServletRequestUtils.getRemoteAddress(request);
    }

    public static Optional<String> resolveSession(HttpServletRequest request) {
        return Optional.ofNullable(request.getSession(false)).map(HttpSession::getId);
    }

    public static Optional<String> resolveUserAgent(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("User-Agent"));
    }

}

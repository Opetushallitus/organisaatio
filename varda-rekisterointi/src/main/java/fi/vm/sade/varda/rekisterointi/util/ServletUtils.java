package fi.vm.sade.varda.rekisterointi.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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

    public static <T> T setSessionAttribute(HttpServletRequest request, String name, T value) {
        HttpSession session = request.getSession();
        session.setAttribute(name, value);
        return value;
    }

}

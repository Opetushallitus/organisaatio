package fi.vm.sade.organisaatio.service.util;

import java.util.function.Predicate;

public final class PredicateUtil {

    private PredicateUtil() {
    }

    public static <T> Predicate<T> not(Predicate<T> predicate) {
        return predicate.negate();
    }

}

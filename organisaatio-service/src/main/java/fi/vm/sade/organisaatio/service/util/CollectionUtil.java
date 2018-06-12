package fi.vm.sade.organisaatio.service.util;

import java.util.Collection;
import java.util.Optional;

public final class CollectionUtil {

    private CollectionUtil() {
    }

    public static <E, T extends Collection<E>> Optional<T> ofNullableAndNotEmpty(T nullableCollection) {
        return Optional.ofNullable(nullableCollection).filter(collection -> !collection.isEmpty());
    }

}

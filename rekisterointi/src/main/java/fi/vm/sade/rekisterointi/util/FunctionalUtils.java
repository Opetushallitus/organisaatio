package fi.vm.sade.rekisterointi.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Supplier;

public class FunctionalUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionalUtils.class);

    public static <T> Supplier<Optional<T>> exceptionToEmptySupplier(Supplier<Optional<T>> supplier) {
        return () -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                LOGGER.error("Supplier threw an exception, returning empty", e);
                return Optional.empty();
            }
        };
    }

}

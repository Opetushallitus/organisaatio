package fi.vm.sade.varda.rekisterointi.util;

import fi.vm.sade.varda.rekisterointi.model.OrganisaatioV4Dto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Supplier;

public class FunctionalUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionalUtils.class);

    public static Supplier<Optional<OrganisaatioV4Dto>> exceptionToEmptySupplier(Supplier<Optional<OrganisaatioV4Dto>> supplier) {
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

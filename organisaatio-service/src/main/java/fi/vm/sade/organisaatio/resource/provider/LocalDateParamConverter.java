package fi.vm.sade.organisaatio.resource.provider;

import javax.ws.rs.ext.ParamConverter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class LocalDateParamConverter implements ParamConverter<LocalDate> {

    @Override
    public LocalDate fromString(String value) {
        return Optional.ofNullable(value)
                .map(str -> {
                    try {
                        return LocalDate.parse(str);
                    } catch (DateTimeParseException e) {
                        throw new IllegalArgumentException(e);
                    }
                })
                .orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public String toString(LocalDate value) {
        return Optional.ofNullable(value)
                .map(LocalDate::toString)
                .orElseThrow(IllegalArgumentException::new);
    }

}

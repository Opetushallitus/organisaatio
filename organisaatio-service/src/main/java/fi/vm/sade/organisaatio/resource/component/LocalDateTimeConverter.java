package fi.vm.sade.organisaatio.resource.component;

import fi.vm.sade.organisaatio.resource.exception.ConversionException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class LocalDateTimeConverter implements Converter<String, LocalDateTime> {

    private static final BiFunction<String, DateTimeFormatter, LocalDateTime> dateConverter = (input, formatter) -> LocalDate.parse(input, formatter).atStartOfDay();
    private static final BiFunction<String, DateTimeFormatter, LocalDateTime> timestampConverter = (input, formatter) -> LocalDateTime.parse(input, formatter);

    @Override
    public LocalDateTime convert(final String input) {
        return Stream.of(SupportedFormats.values())
                .map(converter -> converter.convert(input))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() ->
                        new ConversionException(String.format("unable to parse (%s) supported formats are %s",
                                input, String.join(", ", Stream.of(SupportedFormats.values()).map(converter -> converter.pattern)
                                        .collect(Collectors.toList())
                                ))));
    }

    private enum SupportedFormats {
        DATE_ONLY("yyyy-MM-dd", dateConverter),
        TIMESTAMP_T("yyyy-MM-dd'T'HH:mm", timestampConverter),
        TIMESTAMP_SPACE("yyyy-MM-dd HH:mm", timestampConverter);

        private final BiFunction<String, DateTimeFormatter, LocalDateTime> conversionFunction;
        private final DateTimeFormatter formatter;
        public String pattern;

        SupportedFormats(String pattern, BiFunction<String, DateTimeFormatter, LocalDateTime> conversionFunction) {
            this.pattern = pattern;
            this.conversionFunction = conversionFunction;
            this.formatter = DateTimeFormatter.ofPattern(pattern);
        }

        public LocalDateTime convert(String input) {
            try {
                return conversionFunction.apply(input, formatter);
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
    }
}

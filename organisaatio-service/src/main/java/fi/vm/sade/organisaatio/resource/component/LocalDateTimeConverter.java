package fi.vm.sade.organisaatio.resource.component;

import fi.vm.sade.organisaatio.resource.exception.ConversionException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class LocalDateTimeConverter implements Converter<String, LocalDateTime> {
    private static final String SUPPORTED_DATE_ONLY_FORMAT = "yyyy-MM-dd";
    private static final List<String> SUPPORTED_DATE_TIME_FORMATS = List.of("yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd HH:mm");
    private static final List<DateTimeFormatter> DATE_TIME_FORMATTERS = SUPPORTED_DATE_TIME_FORMATS
            .stream()
            .map(DateTimeFormatter::ofPattern)
            .collect(Collectors.toList());

    @Override
    public LocalDateTime convert(String s) {
        if (s.length() == SUPPORTED_DATE_ONLY_FORMAT.length()) {
            try {
                return LocalDate.parse(s, DateTimeFormatter.ISO_DATE).atStartOfDay();
            } catch (DateTimeParseException ex) {
                // deliberate empty block, single exception thrown in end
            }
        } else {
            for (DateTimeFormatter dateTimeFormatter : DATE_TIME_FORMATTERS) {
                try {
                    return LocalDateTime.parse(s, dateTimeFormatter);
                } catch (DateTimeParseException ex) {
                    // deliberate empty block so that all parsers run
                }
            }
        }
        throw new ConversionException(String.format("unable to parse (%s) supported formats are %s",
                s, String.join(", ", Stream.concat(Stream.of(SUPPORTED_DATE_ONLY_FORMAT), SUPPORTED_DATE_TIME_FORMATS.stream())
                        .collect(Collectors.toList())))) {
        };
    }
}

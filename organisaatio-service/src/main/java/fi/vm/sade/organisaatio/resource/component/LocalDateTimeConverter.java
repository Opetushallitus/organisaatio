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

@Component
public class LocalDateTimeConverter implements Converter<String, LocalDateTime> {

    private static final List<String> SUPPORTED_FORMATS = Arrays.asList("yyyy-MM-dd", "yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd HH:mm");
    private static final List<DateTimeFormatter> DATE_TIME_FORMATTERS = SUPPORTED_FORMATS
            .stream()
            .filter(a -> a.length() > 10)
            .map(DateTimeFormatter::ofPattern)
            .collect(Collectors.toList());

    @Override
    public LocalDateTime convert(String s) {
        if (s.length() == 10) {
            try {
                return LocalDate.parse(s, DateTimeFormatter.ISO_DATE).atStartOfDay();
            } catch (DateTimeParseException ex) {
                // deliberate empty block so that all parsers run
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
                s, String.join(", ", SUPPORTED_FORMATS))) {
        };
    }
}

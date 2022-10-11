package fi.vm.sade.organisaatio.resource.component;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;


import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LocalDateConverter implements Converter<String, LocalDate> {

    private static final List<String> SUPPORTED_FORMATS = Arrays.asList("yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss");
    private static final List<DateTimeFormatter> DATE_TIME_FORMATTERS = SUPPORTED_FORMATS
            .stream()
            .map(DateTimeFormatter::ofPattern)
            .collect(Collectors.toList());

    @Override
    public LocalDate convert(String s) {

        for (DateTimeFormatter dateTimeFormatter : DATE_TIME_FORMATTERS) {
            try {
                return LocalDate.parse(s, dateTimeFormatter);
            } catch (DateTimeParseException ex) {
                // deliberate empty block so that all parsers run
            }
        }

        throw new RuntimeException(String.format("unable to parse (%s) supported formats are %s",
                s, String.join(", ", SUPPORTED_FORMATS)));
    }
}

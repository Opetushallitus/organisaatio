package fi.vm.sade.organisaatio.resource.component;

import fi.vm.sade.organisaatio.resource.exception.ConversionException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalDateTimeConverterTest {

    private final LocalDateTimeConverter res = new LocalDateTimeConverter();

    @ParameterizedTest(name = "{index}: date input {0} has year {1} and hour {2}")
    @CsvSource({
            "1970-01-01 12:12,1970,12",
            "1970-01-01,1970,0",
            "1970-01-01T12:12,1970,12"})
    void convert1(String input, int year, int hour) {
        LocalDateTime dateTime = res.convert(input);
        assertEquals(year, dateTime.getYear());
        assertEquals(hour, dateTime.getHour());
    }

    @ParameterizedTest(name = "{index}: date input {0} throws exception")
    @CsvSource({
            "1970-01-33 12:12,1970,12",
            "1970-01-33,1970,0",
            "1970-01-33:12,1970,12"})
    void convert1(String input) {
        assertThrows(ConversionException.class, () -> res.convert(input));
    }

}
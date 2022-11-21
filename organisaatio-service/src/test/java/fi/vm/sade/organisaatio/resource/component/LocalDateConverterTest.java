package fi.vm.sade.organisaatio.resource.component;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LocalDateConverterTest {

    private final LocalDateConverter res = new LocalDateConverter();

    @Test
    void convert1() {
        LocalDate dateTime = res.convert("2022-11-21");
        assertNotNull(dateTime);
        assertEquals(2022, dateTime.getYear());
        assertEquals(Month.NOVEMBER, dateTime.getMonth());
        assertEquals(21, dateTime.getDayOfMonth());
    }
}
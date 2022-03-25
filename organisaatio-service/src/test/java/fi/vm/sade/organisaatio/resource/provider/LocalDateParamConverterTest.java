package fi.vm.sade.organisaatio.resource.provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class LocalDateParamConverterTest {

    private LocalDateParamConverter converter;

    @BeforeEach
    public void setup() {
        converter = new LocalDateParamConverter();
    }

    @Test
    public void fromStringShouldReturnCorrectly() {
        assertThat(converter.fromString("2018-05-25")).isEqualTo("2018-05-25");
    }

    @Test
    public void fromStringShouldThrowIllegalArgumentExceptionWithInvalid() {
        assertThat(catchThrowable(() -> converter.fromString("olematon"))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void fromStringShouldThrowIllegalArgumentExceptionWithNull() {
        assertThat(catchThrowable(() -> converter.fromString(null))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void toStringShouldReturnCorrectly() {
        assertThat(converter.toString(LocalDate.of(2018, 5, 25))).isEqualTo("2018-05-25");
    }

    @Test
    public void toStringShouldThrowIllegalArgumentExceptionWithNull() {
        assertThat(catchThrowable(() -> converter.toString(null))).isInstanceOf(IllegalArgumentException.class);
    }

}

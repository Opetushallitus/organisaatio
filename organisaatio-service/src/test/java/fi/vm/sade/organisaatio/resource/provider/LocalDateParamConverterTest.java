package fi.vm.sade.organisaatio.resource.provider;

import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import org.junit.Before;
import org.junit.Test;

public class LocalDateParamConverterTest {

    private LocalDateParamConverter converter;

    @Before
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

package fi.vm.sade.organisaatio.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

public class ObjectMapperConfigurationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapperConfiguration().objectMapper(new JsonJavaSqlDateSerializer());
    }

    @Test
    public void javaSqlTimestampDeserializer() throws IOException {
        String json = "1535523954000";

        Timestamp timestamp = objectMapper.readValue(json, Timestamp.class);

        assertThat(timestamp).hasSameTimeAs("2018-08-29T05:25:54-01:00");
    }

    @Test
    public void javaSqlTimestampSerializer() throws IOException {
        OffsetDateTime dateTime = OffsetDateTime.of(2018, 8, 29, 05, 25, 54, 0, ZoneOffset.ofHours(-1));
        Timestamp timestamp = new Timestamp(dateTime.toInstant().toEpochMilli());

        String json = objectMapper.writeValueAsString(timestamp);

        assertThat(json).isEqualTo("1535523954000");
    }

}

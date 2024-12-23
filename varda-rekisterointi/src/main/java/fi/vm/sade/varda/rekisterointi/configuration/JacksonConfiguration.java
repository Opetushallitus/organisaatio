package fi.vm.sade.varda.rekisterointi.configuration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Configuration
public class JacksonConfiguration {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder
                .deserializerByType(LocalDate.class, new LocalDateDeserializer());
    }

    private static class LocalDateDeserializer extends JsonDeserializer<LocalDate> {

        @Override
        public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.hasToken(JsonToken.VALUE_NUMBER_INT)) {
                // organisaatiopalvelun hakurajapinta palauttaa organisaation alkuPvm-kentän epochina
                return Instant.ofEpochMilli(p.getLongValue()).atZone(ZoneId.of("Europe/Helsinki")).toLocalDate();
            }
            return com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer.INSTANCE.deserialize(p, ctxt);
        }
    }

}

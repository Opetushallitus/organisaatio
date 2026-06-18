package fi.vm.sade.varda.rekisterointi.configuration;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.module.SimpleModule;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer.INSTANCE;

@Configuration
public class JacksonConfiguration {
    @Bean
    JsonMapperBuilderCustomizer customizer() {
        return builder -> builder
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .addModule(new SimpleModule()
                        .addDeserializer(LocalDate.class, new EpochMillisLocalDateDeserializer()));
    }

    private static class EpochMillisLocalDateDeserializer extends ValueDeserializer<LocalDate> {

        @Override
        public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            if (p.hasToken(JsonToken.VALUE_NUMBER_INT)) {
                // organisaatiopalvelun hakurajapinta palauttaa organisaation alkuPvm-kentän epochina
                return Instant.ofEpochMilli(p.getLongValue()).atZone(ZoneId.of("Europe/Helsinki")).toLocalDate();
            }
            return INSTANCE.deserialize(p, ctxt);
        }
    }

}

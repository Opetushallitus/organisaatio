package fi.vm.sade.organisaatio.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

@Configuration
public class ObjectMapperConfiguration {
    @Bean
    public ObjectMapper objectMapper(JsonJavaSqlDateSerializer jsonJavaSqlDateSerializer) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Module module = new SimpleModule()
                .addSerializer(Timestamp.class, jsonJavaSqlDateSerializer);

        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .setDateFormat(dateFormat)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .registerModule(module)
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule());
    }
}

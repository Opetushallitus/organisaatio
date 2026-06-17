package fi.vm.sade.organisaatio.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.module.SimpleModule;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@Configuration
public class ObjectMapperConfiguration {
    @Bean
    JsonMapperBuilderCustomizer customizer(JsonJavaSqlDateSerializer jsonJavaSqlDateSerializer) {
        return builder -> builder
            .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS)
            .enable(MapperFeature.DEFAULT_VIEW_INCLUSION)
            .enable(MapperFeature.USE_GETTERS_AS_SETTERS)
            .disable(MapperFeature.DETECT_PARAMETER_NAMES)
            .disable(MapperFeature.FIX_FIELD_NAME_UPPER_CASE_PREFIX)
            .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .accessorNaming(new Jackson2AccessorNamingStrategy.Provider())
            .changeDefaultPropertyInclusion(value ->
                JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL))
            .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd"))
            .addModule(new SimpleModule().addSerializer(Timestamp.class, jsonJavaSqlDateSerializer));
    }
}

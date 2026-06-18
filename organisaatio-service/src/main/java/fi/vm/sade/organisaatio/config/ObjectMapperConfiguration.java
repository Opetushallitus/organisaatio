package fi.vm.sade.organisaatio.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.ytj.ArrayOfYTieto;
import fi.ytj.Tiedot;
import fi.ytj.YTunnusDTO;
import fi.ytj.YritysHakuDTO;
import fi.ytj.YritysTunnusHistoriaDTO;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.module.SimpleModule;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

@Configuration
public class ObjectMapperConfiguration {
    @Bean
    JsonMapperBuilderCustomizer customizer(JsonJavaSqlDateSerializer jsonJavaSqlDateSerializer) {
        return builder -> builder
            .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(MapperFeature.DEFAULT_VIEW_INCLUSION)
            .addMixIn(YTunnusDTO.class, YTunnusMixin.class)
            .addMixIn(Tiedot.class, YTunnusMixin.class)
            .addMixIn(YritysHakuDTO.class, YTunnusMixin.class)
            .addMixIn(YritysTunnusHistoriaDTO.class, YritysTunnusHistoriaMixin.class)
            .addMixIn(ArrayOfYTieto.class, ArrayOfYTietoMixin.class)
            .changeDefaultPropertyInclusion(value ->
                JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL))
            .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd"))
            .addModule(new SimpleModule().addSerializer(Timestamp.class, jsonJavaSqlDateSerializer));
    }

    abstract static class YTunnusMixin {
        @JsonProperty("ytunnus")
        abstract String getYTunnus();

        @JsonProperty("ytunnus")
        abstract void setYTunnus(String ytunnus);
    }

    abstract static class YritysTunnusHistoriaMixin {
        @JsonProperty("ytunnusVanha")
        abstract String getYTunnusVanha();

        @JsonProperty("ytunnusVanha")
        abstract void setYTunnusVanha(String ytunnusVanha);

        @JsonProperty("ytunnusUusi")
        abstract String getYTunnusUusi();

        @JsonProperty("ytunnusUusi")
        abstract void setYTunnusUusi(String ytunnusUusi);
    }

    abstract static class ArrayOfYTietoMixin {
        @JsonProperty("ytieto")
        abstract List<?> getYTieto();
    }
}
